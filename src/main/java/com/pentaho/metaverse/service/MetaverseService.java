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

import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.metaverse.IDocumentLocator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * REST endpoint for the metaverse. Includes Impact Analysis and Lineage
 */
@Path( "/metaverse/api/service" )
public class MetaverseService {

  private IMetaverseReader metaverseReader;
  private IDocumentLocatorProvider documentLocatorProvider;
  private int count;
  private int delay = 1000;

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

  @GET
  @Path( "/export" )
  @Produces( { MediaType.APPLICATION_XML } )
  public Response export() {
    // TODO: figure out how to have the metaverse ready before our first call to the service
    if ( count++ == 0 ) {
      prepareMetaverse();
      try {
        Thread.sleep( getDelay() );
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }

    if ( metaverseReader == null ) {
      return Response.serverError().entity( Messages.getString( "ERROR.MetaverseReader.IsNull" ) ).build();
    }
    return Response.ok( metaverseReader.export(), MediaType.APPLICATION_XML ).build();
  }

  protected void prepareMetaverse() {
    if ( documentLocatorProvider != null ) {
      Set<IDocumentLocator> locators = documentLocatorProvider.getDocumentLocators();
      if ( locators != null ) {
        for ( IDocumentLocator locator : locators ) {
          locator.startScan();
        }
      }
    }
  }

}
