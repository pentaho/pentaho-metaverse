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
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JobAnalyzer extends AbstractAnalyzer<IMetaverseDocument> implements IDocumentAnalyzer {

  private static final Set<String> defaultSupportedTypes = new HashSet<String>() {
    {
      add( DictionaryConst.NODE_TYPE_JOB );
    }
  };

  @Override
  public IMetaverseNode analyze( IMetaverseDocument document ) throws MetaverseAnalyzerException {

    if ( document == null ) {
      throw new MetaverseAnalyzerException( "Document is null!" );
    }

    Object repoObject = document.getContent();

    if ( repoObject == null ) {
      throw new MetaverseAnalyzerException( "Document has no content!" );
    }

    if ( metaverseObjectFactory == null ) {
      throw new MetaverseAnalyzerException( "MetaverseObjectFactory is null!" );
    }

    if ( metaverseBuilder == null ) {
      throw new MetaverseAnalyzerException( "MetaverseBuilder is null!" );
    }

    JobMeta job = null;
    if ( repoObject instanceof String ) {

      // hydrate the job
      try {
        String content = (String) repoObject;
        ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
        job = new JobMeta( xmlStream, null,  null );
      } catch ( KettleXMLException e ) {
        throw new MetaverseAnalyzerException( e );
      }

    } else if ( repoObject instanceof JobMeta ) {
      job = (JobMeta) repoObject;
    }

    // Create a metaverse node and start filling in details
    // TODO get unique ID and set it on the node
    IMetaverseNode node = metaverseObjectFactory.createNodeObject( "TODO" );

    node.setType(  DictionaryConst.NODE_TYPE_JOB);
    node.setName( job.getName() );

    // pull out the standard fields
    String description = job.getDescription();
    node.setProperty( "description", description );

    Date createdDate = job.getCreatedDate();
    node.setProperty( "createdDate", createdDate );

    Date lastModifiedDate = job.getModifiedDate();
    node.setProperty( "lastModifiedDate", lastModifiedDate );

    // handle the entries
    for ( int i = 0; i < job.nrJobEntries(); i++ ) {
      JobEntryCopy entry = job.getJobEntry( i );

      if ( entry != null ) {
        IAnalyzer<JobEntryCopy> analyzer = getJobEntryAnalyzer( entry );
        IMetaverseNode entryNode = analyzer.analyze( entry );
        metaverseBuilder.addLink( node, DictionaryConst.LINK_CONTAINS, entryNode );
      }

    }

    metaverseBuilder.addNode( node );
    return node;
  }

  protected IAnalyzer<JobEntryCopy> getJobEntryAnalyzer( JobEntryCopy jobEntry ) {

    // TODO Look for implementing analyzers for this step.
    //
    // Choices might include:
    //
    // - Class.forName(<step name + StepAnalyzer>)
    // - Annotation
    // - PentahoSystem.get()
    //
    // If none can be found, a default handler should be returned.

    IAnalyzer<JobEntryCopy> entryAnalyzer = new JobEntryAnalyzer();
    entryAnalyzer.setMetaverseObjectFactory( metaverseObjectFactory );
    entryAnalyzer.setMetaverseBuilder( metaverseBuilder );

    return entryAnalyzer;
  }


  /**
   * Returns a set of strings corresponding to which types of content are supported by this analyzer
   *
   * @return the supported types (as a set of Strings)
   *
   * @see org.pentaho.platform.api.metaverse.IDocumentAnalyzer#getSupportedTypes()
   */
  @Override
  public Set<String> getSupportedTypes() {
    return defaultSupportedTypes;
  }

}
