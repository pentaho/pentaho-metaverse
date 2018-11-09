/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
    switch ( entry.getSpecificationMethod() ) {
      case FILENAME:
        try {
          jobPath = parentJobMeta.environmentSubstitute( entry.getFilename() );
          String normalized = KettleAnalyzerUtil.normalizeFilePath( jobPath );

          subJobMeta = getSubJobMeta( normalized );
          jobPath = normalized;

        } catch ( Exception e ) {
          throw new MetaverseAnalyzerException( "Sub job can not be found - " + jobPath, e );
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
            throw new MetaverseAnalyzerException( "Sub job can not be found in repository - " + file, e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the job" );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subJobMeta = repo.loadJob( entry.getJobObjectId(), null );
            String filename = subJobMeta.getFilename() == null ? subJobMeta.toString() : subJobMeta.getFilename();
            jobPath = filename + "." + subJobMeta.getDefaultExtension();
          } catch ( KettleException e ) {
            throw new MetaverseAnalyzerException( "Sub job can not be found by reference - "
              + entry.getJobObjectId(), e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the job" );
        }
        break;
    }
    subJobMeta.copyVariablesFrom( parentJobMeta );

    IComponentDescriptor ds =
      new MetaverseComponentDescriptor( subJobMeta.getName(), DictionaryConst.NODE_TYPE_JOB,
        descriptor.getNamespace().getParentNamespace() );

    IMetaverseNode jobNode = createNodeFromDescriptor( ds );
    jobNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, ds.getNamespaceId() );
    jobNode.setProperty( DictionaryConst.PROPERTY_PATH, jobPath );
    jobNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, jobNode );

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
