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

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.pentaho.metaverse.api.GraphConst;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.platform.api.metaverse.IAnalyzer;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

public class TransformationAnalyzer extends AbstractAnalyzer<IMetaverseDocument> implements IDocumentAnalyzer {

  private static final long serialVersionUID = 3147152759123052372L;

  private static final Set<String> defaultSupportedTypes = new HashSet<String>() {
    /**
     * Default serial ID for
     */
    private static final long serialVersionUID = -7433589337075366681L;

    {
      add( "ktr" );
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

    TransMeta transMeta = null;
    if ( repoObject instanceof String ) {
      // hydrate the transformation
      try {
        String content = (String) repoObject;
        ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
        transMeta = new TransMeta( xmlStream, null, false, null, null );
      } catch ( KettleXMLException e ) {
        throw new MetaverseAnalyzerException( e );
      } catch ( KettleMissingPluginsException e ) {
        throw new MetaverseAnalyzerException( e );
      }
    } else if ( repoObject instanceof TransMeta ) {
      transMeta = (TransMeta) repoObject;
    }

    // Create a metaverse node and start filling in details
    // TODO get unique ID and set it on the node
    IMetaverseNode node = metaverseObjectFactory.createNodeObject( "TODO" );
    node.setName( transMeta.getName() );
    node.setType( "transformation" );

    // pull out the standard fields
    String description = transMeta.getDescription();
    node.setProperty( "description", description );

    Date createdDate = transMeta.getCreatedDate();
    node.setProperty( "createdDate", createdDate );

    Date lastModifiedDate = transMeta.getModifiedDate();
    node.setProperty( "lastModifiedDate", lastModifiedDate );

    // handle the steps
    for ( int stepNr = 0; stepNr < transMeta.nrSteps(); stepNr++ ) {
      StepMeta stepMeta = transMeta.getStep( stepNr );
      if ( stepMeta != null ) {
        IAnalyzer<StepMeta> stepAnalyzer = getStepAnalyzer( stepMeta );
        if ( stepAnalyzer == null ) {
          stepAnalyzer = new KettleStepAnalyzer();
          stepAnalyzer.setMetaverseBuilder( metaverseBuilder );
          stepAnalyzer.setMetaverseObjectFactory( metaverseObjectFactory );
        }
        IMetaverseNode stepNode = stepAnalyzer.analyze( stepMeta );
        metaverseBuilder.addLink( node, GraphConst.LINK_CONTAINS, stepNode );
      }
    }

    metaverseBuilder.addNode( node );
    return node;
  }

  /**
   * Returns a set of strings corresponding to which types of content are supported by this analyzer
   * 
   * @return the supported types (as a set of Strings)
   * 
   * @see org.pentaho.platform.api.metaverse.IDocumentAnalyzer#getSupportedTypes()
   */
  public Set<String> getSupportedTypes() {
    return defaultSupportedTypes;
  }


  protected IAnalyzer<StepMeta> getStepAnalyzer( StepMeta stepMeta ) {

    // TODO Look for implementing analyzers for this step.
    //
    // Choices might include:
    //
    // - Class.forName(<step name + StepAnalyzer>)
    // - Annotation
    // - PentahoSystem.get()
    //
    // If none can be found, a default handler should be returned.

    IAnalyzer<StepMeta> stepAnalyzer = new KettleStepAnalyzer();
    stepAnalyzer.setMetaverseObjectFactory( metaverseObjectFactory );
    stepAnalyzer.setMetaverseBuilder( metaverseBuilder );

    return stepAnalyzer;
  }

}
