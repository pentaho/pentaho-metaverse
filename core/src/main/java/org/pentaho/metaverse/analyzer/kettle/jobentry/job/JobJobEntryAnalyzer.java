/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.jobentry.job;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IClonableJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.JobEntryAnalyzer;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.api.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides an analyzer for the "Execute Job" job entry
 */
public class JobJobEntryAnalyzer extends JobEntryAnalyzer<JobEntryJob> {

  private Logger log = LoggerFactory.getLogger( JobJobEntryAnalyzer.class );

  @Override
  public Set<Class<? extends JobEntryInterface>> getSupportedEntries() {
    Set<Class<? extends JobEntryInterface>> supportedEntries = new HashSet<>();
    supportedEntries.add( JobEntryJob.class );
    return supportedEntries;
  }

  @Override
  protected void customAnalyze( JobEntryJob entry, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {
    JobMeta subJobMeta = null;
    JobMeta parentJobMeta = entry.getParentJob().getJobMeta();
    // For some reason the JobMeta's variables have been reset by now, so re-activate them
    parentJobMeta.activateParameters();

    Repository repo = parentJobMeta.getRepository();
    String jobPath = null;
    MetaverseAnalyzerException exception = null;
    switch ( entry.getSpecificationMethod() ) {
      case FILENAME:
        try {
          jobPath = parentJobMeta.environmentSubstitute( entry.getFilename() );
          String normalized = KettleAnalyzerUtil.normalizeFilePath( parentJobMeta.getBowl(), jobPath );

          subJobMeta = getSubJobMeta( normalized );
          jobPath = normalized;

        } catch ( Exception e ) {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubJobNotFoundInParentJob", jobPath,
            parentJobMeta.toString() ), e );
        }
        break;
      case REPOSITORY_BY_NAME:
        if ( repo != null ) {
          String dir = parentJobMeta.environmentSubstitute( entry.getDirectory() );
          String file = parentJobMeta.environmentSubstitute( entry.getJobName() );
          try {
            RepositoryDirectoryInterface rdi = repo.findDirectory( dir );
            subJobMeta = repo.loadJob( file, rdi, null, null );
            String filename = subJobMeta.getFilename() == null ? subJobMeta.toString() : subJobMeta.getFilename();
            jobPath = filename + "." + subJobMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubJobNotFoundInParentJob", file,
              parentJobMeta.toString() ), e );
          }
        } else {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForJobSubJob",
            parentJobMeta.toString() ) );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subJobMeta = repo.loadJob( entry.getJobObjectId(), null );
            String filename = subJobMeta.getFilename() == null ? subJobMeta.toString() : subJobMeta.getFilename();
            jobPath = filename + "." + subJobMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.SubJobsNotFoundInParentJob",
              ( entry.getJobObjectId() == null ? "N/A" : entry.getJobObjectId().toString() ),
              parentJobMeta.toString() ), e );
          }
        } else {
          exception = new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForJobSubJob",
            parentJobMeta.toString() ) );
        }
        break;
    }
    rootNode.setProperty( DictionaryConst.PROPERTY_PATH, jobPath );

    if ( exception != null ) {
      throw exception;
    }

    subJobMeta.copyVariablesFrom( parentJobMeta );
    subJobMeta.setFilename( jobPath );

    IComponentDescriptor ds =
      new MetaverseComponentDescriptor( subJobMeta.getName(), DictionaryConst.NODE_TYPE_JOB,
        descriptor.getNamespace().getParentNamespace() );

    IMetaverseNode jobNode = createNodeFromDescriptor( ds );
    jobNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
    jobNode.setProperty( DictionaryConst.PROPERTY_PATH, jobPath );
    jobNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, jobNode );

    // pull in the sub-job lineage only if the consolidateSubGraphs flag is set to true
    if ( MetaverseConfig.consolidateSubGraphs() ) {
      final IDocument subTransDocument = KettleAnalyzerUtil.buildDocument( getMetaverseBuilder(), subJobMeta,
        jobPath, getDocumentDescriptor().getNamespace() );
      if ( subTransDocument != null ) {
        final IComponentDescriptor subtransDocumentDescriptor = new MetaverseComponentDescriptor(
          subTransDocument.getStringID(), DictionaryConst.NODE_TYPE_TRANS, getDocumentDescriptor().getNamespace(),
          getDescriptor().getContext() );

        // analyze the sub-job
        getDocumentAnalyzer().analyze( subtransDocumentDescriptor, subJobMeta, jobNode, jobPath );
      }
    }
  }

  protected JobMeta getSubJobMeta( String filePath ) throws FileNotFoundException, KettleXMLException,
    KettleMissingPluginsException {
    FileInputStream fis = new FileInputStream( filePath );
    return new JobMeta( fis, null, null );
  }

  @Override
  public IClonableJobEntryAnalyzer newInstance() {
    return new JobJobEntryAnalyzer();
  }
}
