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
import com.pentaho.metaverse.impl.MetaverseCompletionService;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.api.metaverse.MetaverseLocatorException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * REST endpoint for the metaverse. Includes Impact Analysis and Lineage
 */
@Path( "/metaverse/api/service" )
public class MetaverseService {

  private IMetaverseReader metaverseReader;
  private IDocumentLocatorProvider documentLocatorProvider;
  private int count;

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

  /**
   * Export the entire metaverse.
   *
   * @param headers HttpHeaders associated with this service call. Used to determine request "Accepts" type
   *                that drives generation of the export format
   *
   * @return Response
   * <ul>
   *   <li>
   *     If the Accept header is application/json, the response is
   *     <a href="https://github.com/tinkerpop/blueprints/wiki/GraphSON-Reader-and-Writer-Library">graphson</a>
   *     formatted
   *   </li>
   *   <li>If the Accept header is text/plain, the response is a CSV</li>
   *   <li>
   *     Otherwise, the response is application/xml and formatted as
   *     <a href="http://graphml.graphdrawing.org/">graphml</a>
   *   </li>
   * </ul>
   * If there is an error encountered, an Error status is returned
   */
  @GET
  @Path( "/export" )
  @Consumes( { MediaType.WILDCARD } )
  @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN } )
  public Response export( @Context HttpHeaders headers ) {
    List<MediaType> acceptTypes = headers.getAcceptableMediaTypes();
    String mediaType = acceptTypes.get( 0 ).toString();
    String format;

    if ( MediaType.APPLICATION_JSON.toString().equals( mediaType ) ) {
      format = IMetaverseReader.FORMAT_JSON;
    } else if ( MediaType.TEXT_PLAIN.toString().equals( mediaType ) ) {
      format = IMetaverseReader.FORMAT_CSV;
    } else {
      format = IMetaverseReader.FORMAT_XML;
      mediaType = MediaType.APPLICATION_XML;
    }

    // TODO figure out how to have the metaverse ready before our first call to the service
    prepareMetaverse();

    if ( metaverseReader == null ) {
      return Response.serverError().entity( Messages.getString( "ERROR.MetaverseReader.IsNull" ) ).build();
    }
    return Response.ok( metaverseReader.exportFormat( format ), mediaType ).build();
  }

  /**
   * Makes sure that the metaverse is fully populated.
   */
  protected void prepareMetaverse() {
    if ( count++ == 0 ) {
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
  }
}
