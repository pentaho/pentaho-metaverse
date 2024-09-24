/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.jobentry.transjob;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.TransformationAnalyzer;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IClonableJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.JobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzerProvider;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.api.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides an analyzer for the "Execute Transformation" job entry
 */
public class TransJobEntryAnalyzer extends JobEntryAnalyzer<JobEntryTrans> {

  private Logger log = LoggerFactory.getLogger( TransJobEntryAnalyzer.class );

  @Override
  public Set<Class<? extends JobEntryInterface>> getSupportedEntries() {
    Set<Class<? extends JobEntryInterface>> supportedEntries = new HashSet<Class<? extends JobEntryInterface>>();
    supportedEntries.add( JobEntryTrans.class );
    return supportedEntries;
  }

  @Override
  protected void customAnalyze( JobEntryTrans entry, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    TransMeta subTransMeta = null;
    JobMeta parentJobMeta = entry.getParentJob().getJobMeta();
    // For some reason the JobMeta's variables have been reset by now, so re-activate them
    parentJobMeta.activateParameters();

    Repository repo = parentJobMeta.getRepository();
    String transPath = null;
    MetaverseAnalyzerException exception = null;
    switch ( entry.getSpecificationMethod() ) {
      case FILENAME:
        try {
          transPath = parentJobMeta.environmentSubstitute( entry.getFilename() );
          String normalized = KettleAnalyzerUtil.normalizeFilePath( transPath );

          subTransMeta = getSubTransMeta( normalized );
          entry.copyVariablesFrom( subTransMeta );
          transPath = normalized;

        } catch ( Exception e ) {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubTransNotFoundInParentJob", transPath,
            parentJobMeta.toString() ), e );
        }
        break;
      case REPOSITORY_BY_NAME:
        if ( repo != null ) {
          String dir = parentJobMeta.environmentSubstitute( entry.getDirectory() );
          String file = parentJobMeta.environmentSubstitute( entry.getTransname() );
          try {
            RepositoryDirectoryInterface rdi = repo.findDirectory( dir );
            subTransMeta = repo.loadTransformation( file, rdi, null, true, null );
            transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubTransNotFoundInParentJob", file,
              parentJobMeta.toString() ), e );
          }
        } else {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForJobSubTrans",
            parentJobMeta.toString() ) );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subTransMeta = repo.loadTransformation( entry.getTransObjectId(), null );
            transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubTransNotFoundInParentJob",
              ( entry.getTransObjectId() == null ? "N/A" : entry.getTransObjectId().toString() ),
              parentJobMeta.toString() ), e );
          }
        } else {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForJobSubTrans",
            parentJobMeta.toString() ) );
        }
        break;
    }
    rootNode.setProperty( DictionaryConst.PROPERTY_PATH, transPath );

    if ( exception != null ) {
      throw exception;
    }

    subTransMeta.copyVariablesFrom( parentJobMeta );
    subTransMeta.setFilename( transPath );

    IComponentDescriptor ds =
      new MetaverseComponentDescriptor( subTransMeta.getName(), DictionaryConst.NODE_TYPE_TRANS,
        descriptor.getNamespace().getParentNamespace() );

    IMetaverseNode transformationNode = createNodeFromDescriptor( ds );
    transformationNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
    transformationNode.setProperty( DictionaryConst.PROPERTY_PATH, transPath );
    transformationNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, transformationNode );

    // pull in the sub-job lineage only if the consolidateSubGraphs flag is set to true
    if ( MetaverseConfig.consolidateSubGraphs() ) {
      final IDocument subTransDocument = KettleAnalyzerUtil.buildDocument( getMetaverseBuilder(), subTransMeta,
        transPath, getDocumentDescriptor().getNamespace() );
      if ( subTransDocument != null ) {
        final IComponentDescriptor subtransDocumentDescriptor = new MetaverseComponentDescriptor(
          subTransDocument.getStringID(), DictionaryConst.NODE_TYPE_TRANS, getDocumentDescriptor().getNamespace(),
          getDescriptor().getContext() );

        // analyze the sub-transformation
        final TransformationAnalyzer transformationAnalyzer = new TransformationAnalyzer();
        transformationAnalyzer.setStepAnalyzerProvider( PentahoSystem.get( IStepAnalyzerProvider.class ) );
        transformationAnalyzer.setMetaverseBuilder( getMetaverseBuilder() );
        transformationAnalyzer.analyze( subtransDocumentDescriptor, subTransMeta, transformationNode, transPath );
      }
    }
  }

  protected TransMeta getSubTransMeta( String filePath ) throws FileNotFoundException, KettleXMLException,
    KettleMissingPluginsException {
    FileInputStream fis = new FileInputStream( filePath );
    return new TransMeta( fis, null, true, null, null );
  }

  @Override
  protected IClonableJobEntryAnalyzer newInstance() {
    return new TransJobEntryAnalyzer();
  }
}
