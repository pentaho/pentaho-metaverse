/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package com.pentaho.metaverse.analyzer.kettle.jobentry.transjob;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.JobEntryAnalyzer;

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
    // String entryFilename = entry.getFilename();
    TransMeta subTransMeta = null;
    String name = entry.getName();
    JobMeta parentJobMeta = entry.getParentJob().getJobMeta();
    Repository repo = parentJobMeta.getRepository();
    String transPath = null;
    switch ( entry.getSpecificationMethod() ) {
      case FILENAME:
        transPath = parentJobMeta.environmentSubstitute( entry.getFilename() );
        try {
          String normalized = KettleAnalyzerUtil.normalizeFilePath( transPath );

          subTransMeta = getSubTransMeta( normalized );
          transPath = normalized;

        } catch ( Exception e ) {
          log.error( e.getMessage(), e );
          throw new MetaverseAnalyzerException( "Sub transformation can not be found - " + transPath, e );
        }
        break;
      case REPOSITORY_BY_NAME:
        if ( repo != null ) {
          String dir = parentJobMeta.environmentSubstitute( entry.getDirectoryPath() );
          String file = parentJobMeta.environmentSubstitute( entry.getFilename() );
          try {
            RepositoryDirectoryInterface rdi = repo.findDirectory( dir );
            subTransMeta = repo.loadTransformation( file, rdi, null, true, null );
            transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            log.error( e.getMessage(), e );
            throw new MetaverseAnalyzerException( "Sub transformation can not be found in repository - " + file, e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the transformation" );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subTransMeta = repo.loadTransformation( entry.getTransObjectId(), null );
            transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            log.error( e.getMessage(), e );
            throw new MetaverseAnalyzerException( "Sub transformation can not be found by reference - "
                + entry.getTransObjectId(), e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the transformation" );
        }
        break;
    }

    IComponentDescriptor ds =
        new MetaverseComponentDescriptor( name, DictionaryConst.NODE_TYPE_TRANS, descriptor.getNamespace()
            .getParentNamespace() );

    IMetaverseNode transformationNode = createNodeFromDescriptor( ds );
    transformationNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
    transformationNode.setProperty( DictionaryConst.PROPERTY_PATH, transPath );
    transformationNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, transformationNode );

  }

  protected TransMeta getSubTransMeta( String filePath ) throws FileNotFoundException, KettleXMLException,
    KettleMissingPluginsException {
    FileInputStream fis = new FileInputStream( filePath );
    return new TransMeta( fis, null, true, null, null );
  }
}
