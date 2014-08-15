/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.jobentry.GenericJobEntryMetaAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.jobentry.IJobEntryAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.jobentry.IJobEntryAnalyzerProvider;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * The JobAnalyzer class is responsible for gathering job metadata, creating links
 * to form relationships between the job and its child collaborators (ie, entries, dbMetas), and
 * calling the analyzers responsible for providing the metadata for the child collaborators.
 */
public class JobAnalyzer extends BaseDocumentAnalyzer {

  /**
   * A set of types supported by this analyzer
   */
  private static final Set<String> defaultSupportedTypes = new HashSet<String>() {
    {
      add( "kjb" );
    }
  };

  /**
   * A reference to the job entry analyzer provider
   */
  private IJobEntryAnalyzerProvider jobEntryAnalyzerProvider;

  private Logger log = LoggerFactory.getLogger( JobAnalyzer.class );

  @Override
  public synchronized IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, IMetaverseDocument document )
    throws MetaverseAnalyzerException {

    validateState( document );

    Object repoObject = document.getContent();

    JobMeta job = null;
    if ( repoObject instanceof String ) {

      // hydrate the job
      try {
        String content = (String) repoObject;
        ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
        job = new JobMeta( xmlStream, null, null );
      } catch ( KettleXMLException e ) {
        throw new MetaverseAnalyzerException( e );
      }

    } else if ( repoObject instanceof JobMeta ) {
      job = (JobMeta) repoObject;
    }

    // construct a dummy job based on our JobMeta so we get out VariableSpace set properly
    job.setFilename( document.getStringID() );
    Job j = new Job( null, job );
    j.setInternalKettleVariables( job );

    IMetaverseComponentDescriptor documentDescriptor =
        getChildComponentDescriptor( descriptor, document.getStringID(), DictionaryConst.NODE_TYPE_JOB );

    // Create a metaverse node and start filling in details
    IMetaverseNode node = metaverseObjectFactory.createNodeObject(
        documentDescriptor.getStringID(),
        job.getName(),
        DictionaryConst.NODE_TYPE_JOB );


    // pull out the standard fields
    String description = job.getDescription();
    if ( description != null ) {
      node.setProperty( DictionaryConst.PROPERTY_DESCRIPTION, description );
    }

    String extendedDescription = job.getExtendedDescription();
    if ( extendedDescription != null ) {
      node.setProperty( "extendedDescription", extendedDescription );
    }

    Date createdDate = job.getCreatedDate();
    if ( createdDate != null ) {
      node.setProperty( DictionaryConst.PROPERTY_CREATED, Long.toString( createdDate.getTime() ) );
    }

    String createdUser = job.getCreatedUser();
    if ( createdUser != null ) {
      node.setProperty( DictionaryConst.PROPERTY_CREATED_BY, createdUser );
    }

    Date lastModifiedDate = job.getModifiedDate();
    if ( lastModifiedDate != null ) {
      node.setProperty( DictionaryConst.PROPERTY_LAST_MODIFIED, Long.toString( lastModifiedDate.getTime() ) );
    }

    String lastModifiedUser = job.getModifiedUser();
    if ( lastModifiedUser != null ) {
      node.setProperty( DictionaryConst.PROPERTY_LAST_MODIFIED_BY, lastModifiedUser );
    }

    String status = Messages.getString( "INFO.JobOrTrans.Status_" + Integer.toString( job.getJobstatus() ) );
    if ( status != null && !status.startsWith( "!" ) ) {
      node.setProperty( DictionaryConst.PROPERTY_STATUS, status );
    }

    node.setProperty( "path", document.getProperty( "path" ) );


    // handle the entries
    for ( int i = 0; i < job.nrJobEntries(); i++ ) {
      JobEntryCopy entry = job.getJobEntry( i );
      try {
        entry.getEntry().setParentJob( new Job( null, job ) );

        if ( entry != null ) {
          IMetaverseNode jobEntryNode = null;
          JobEntryInterface jobEntryInterface = entry.getEntry();
          IMetaverseComponentDescriptor entryDescriptor =
              getChildComponentDescriptor( documentDescriptor, entry.getName(), DictionaryConst.NODE_TYPE_JOB_ENTRY );
          Set<IJobEntryAnalyzer> jobEntryAnalyzers = getJobEntryAnalyzers( jobEntryInterface );
          if ( jobEntryAnalyzers != null && !jobEntryAnalyzers.isEmpty() ) {
            for ( IJobEntryAnalyzer jobEntryAnalyzer : jobEntryAnalyzers ) {
              jobEntryAnalyzer.setMetaverseBuilder( metaverseBuilder );
              jobEntryNode = jobEntryAnalyzer.analyze( entryDescriptor, entry.getEntry() );
            }
          } else {
            IJobEntryAnalyzer<JobEntryInterface> defaultJobEntryAnalyzer = new GenericJobEntryMetaAnalyzer();
            defaultJobEntryAnalyzer.setMetaverseBuilder( metaverseBuilder );
            jobEntryNode = defaultJobEntryAnalyzer.analyze( entryDescriptor, jobEntryInterface );
          }
          if ( jobEntryNode != null ) {
            metaverseBuilder.addLink( node, DictionaryConst.LINK_CONTAINS, jobEntryNode );
          }
        }
      } catch ( MetaverseAnalyzerException mae ) {
        //Don't throw an exception, just log and carry on
        log.error( "Error processing " + entry.getName(), mae );
      } catch ( Exception e ) {
        //Don't throw an exception, just log and carry on
        log.error( "Error processing " + entry.getName(), e );
      }
    }

    metaverseBuilder.addNode( node );
    addParentLink( documentDescriptor, node );
    return node;
  }

  /**
   * Returns a set of strings corresponding to which types of content are supported by this analyzer
   *
   * @return the supported types (as a set of Strings)
   * @see org.pentaho.platform.api.metaverse.IDocumentAnalyzer#getSupportedTypes()
   */
  @Override
  public Set<String> getSupportedTypes() {
    return defaultSupportedTypes;
  }

  protected Set<IJobEntryAnalyzer> getJobEntryAnalyzers( final JobEntryInterface jobEntryInterface ) {

    Set<IJobEntryAnalyzer> jobEntryAnalyzers = new HashSet<IJobEntryAnalyzer>();

    // Attempt to discover a BaseStepMeta from the given StepMeta
    jobEntryAnalyzerProvider = getJobEntryAnalyzerProvider();
    if ( jobEntryAnalyzerProvider != null ) {
      if ( jobEntryInterface == null ) {
        jobEntryAnalyzers.addAll( jobEntryAnalyzerProvider.getAnalyzers() );
      } else {
        Set<Class<?>> analyzerClassSet = new HashSet<Class<?>>( 1 );
        analyzerClassSet.add( jobEntryInterface.getClass() );
        jobEntryAnalyzers.addAll( jobEntryAnalyzerProvider.getAnalyzers( analyzerClassSet ) );
      }
    } else {
      jobEntryAnalyzers.add( new GenericJobEntryMetaAnalyzer() );
    }

    return jobEntryAnalyzers;
  }

  protected void setJobEntryAnalyzerProvider( IJobEntryAnalyzerProvider jobEntryAnalyzerProvider ) {
    this.jobEntryAnalyzerProvider = jobEntryAnalyzerProvider;
  }

  /**
   * Retrieves the step analyzer provider. This is used to find step-specific analyzers
   *
   * @return the IKettleStepAnalyzer provider instance that provides step-specific analyzers
   */
  protected IJobEntryAnalyzerProvider getJobEntryAnalyzerProvider() {
    if ( jobEntryAnalyzerProvider != null ) {
      return jobEntryAnalyzerProvider;
    }
    jobEntryAnalyzerProvider = (IJobEntryAnalyzerProvider) PentahoSystem.get( IJobEntryAnalyzerProvider.class );
    return jobEntryAnalyzerProvider;
  }
}
