/*
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

package com.pentaho.metaverse.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.api.IMetaverseService;
import com.pentaho.metaverse.messages.Messages;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import org.pentaho.platform.api.metaverse.MetaverseLocatorException;

/**
 * REST endpoint for the metaverse. Includes Impact Analysis and Lineage
 */
@Path( "/metaverse/api/service" )
public class MetaverseService implements IMetaverseService {

  private static final int DEFAULT_DELAY = 1000;
  private IMetaverseReader metaverseReader;
  private IDocumentLocatorProvider documentLocatorProvider;
  private int count;
  private int delay = DEFAULT_DELAY;

  /**
   * Creates a new metaverse service using a provided metaverse reader (to pass calls to), 
   * and locator provider (to rebuild the metaverse).
   * @param metaverseReader The metaverse reader to use
   * @param documentLocatorProvider The document locator provider to use
   */
  public MetaverseService( IMetaverseReader metaverseReader, IDocumentLocatorProvider documentLocatorProvider ) {
    setMetaverseReader( metaverseReader );
    setDocumentLocatorProvider( documentLocatorProvider );
  }

  public void setMetaverseReader( IMetaverseReader metaverseReader ) {
    this.metaverseReader = metaverseReader;
  }

  public void setDocumentLocatorProvider( IDocumentLocatorProvider documentLocatorProvider ) {
    this.documentLocatorProvider = documentLocatorProvider;
  }

  protected int getDelay() {
    return delay;
  }

  protected void setDelay( int delay ) {
    this.delay = delay;
  }

  /**
   * Export the entire metaverse as <a href="http://graphml.graphdrawing.org/">graphml</a>
   *
   * @return Response XML (graphml) representing the entire metaverse if successful,
   * otherwise a Response with an error status
   */
  @GET
  @Path( "/export" )
  @Produces( { MediaType.APPLICATION_XML } )
  public Response export() {
    // TODO figure out how to have the metaverse ready before our first call to the service
    if ( count++ == 0 ) {
      prepareMetaverse();
    }

    if ( metaverseReader == null ) {
      return Response.serverError().entity( Messages.getString( "ERROR.MetaverseReader.IsNull" ) ).build();
    }
    return Response.ok( metaverseReader.exportToXml(), MediaType.APPLICATION_XML ).build();
  }

  /**
   * Makes sure that the metaverse is fully populated.
   */
  protected void prepareMetaverse() {
    try {
      if ( documentLocatorProvider != null ) {
        Set<IDocumentLocator> locators = documentLocatorProvider.getDocumentLocators();
        if ( locators != null ) {
          for ( IDocumentLocator locator : locators ) {
            locator.startScan();
          }
        }
      }

        MetaverseCompletionService.getInstance().waitTillEmpty();
    } catch ( MetaverseLocatorException e ) {
      e.printStackTrace();
    } catch ( InterruptedException e ) {
      e.printStackTrace();
    } catch ( ExecutionException e ) {
      e.printStackTrace();
    }
  }

  @Override
  public IMetaverseNode findNode( String id ) {
    return metaverseReader.findNode( id );
  }

  @Override
  public IMetaverseLink findLink( String leftNodeID, String linkType, String rightNodeID, Direction direction ) {
    return metaverseReader.findLink( leftNodeID, linkType, rightNodeID, direction );
  }

  @Override
  public Graph getMetaverse() {
    return metaverseReader.getMetaverse();
  }

  @Override
  public String exportFormat( String format ) {
    return metaverseReader.exportFormat( format );
  }

  @Override
  public String exportToXml() {
    return metaverseReader.exportToXml();
  }

  @Override
  public Graph search( List<String> resultTypes, List<String> startNodeIDs, boolean shortestOnly ) {
    return metaverseReader.search( resultTypes, startNodeIDs, shortestOnly );
  }

  @Override
  public Graph getGraph( String id ) {
    return metaverseReader.getGraph( id );
  }

  @Override
  public List<IMetaverseNode> findNodes( String property, String value ) {
    return metaverseReader.findNodes( property, value );
  }

}
