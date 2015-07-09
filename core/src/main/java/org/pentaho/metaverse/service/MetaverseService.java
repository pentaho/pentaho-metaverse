/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.service;

import org.pentaho.metaverse.api.IDocumentLocator;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.ILineageCollector;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.api.model.LineageRequest;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.messages.Messages;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * REST endpoint for the metaverse. Includes Impact Analysis and Lineage
 */
@Path( "/api" )
public class MetaverseService {

  private IMetaverseReader metaverseReader;
  private IDocumentLocatorProvider documentLocatorProvider;
  private ILineageCollector lineageCollector;
  private static final String DATE_FORMAT = "yyyyMMdd";
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

  public void setLineageCollector( ILineageCollector collector ) {
    this.lineageCollector = collector;
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

  @GET
  @Path( "/download/all" )
  @Produces( { "application/zip" } )
  public Response download() {
    return download( null, null );
  }

  @GET
  @Path( "/download/all/{startingDate}" )
  @Produces( { "application/zip" } )
  public Response download( @PathParam( "startingDate" ) final String startingDate ) {
    return download( startingDate, null );
  }

  @GET
  @Path( "/download/all/{startingDate}/{endingDate}" )
  @Produces( { "application/zip" } )
  public Response download( @PathParam( "startingDate" ) final String startingDate,
    @PathParam( "endingDate" ) String endingDate ) {

    if ( lineageCollector == null ) {
      return Response.serverError().entity( Messages.getString( "ERROR.LineageCollector.IsNull" ) ).build();
    }
    String filename = "pentaho-lineage";
    if ( startingDate != null ) {
      filename += "_" + startingDate;
    }
    if ( endingDate != null ) {
      filename += "-" + endingDate;
    }
    filename += ".zip";

    try {
      final List<String> artifacts = lineageCollector.listArtifacts( startingDate, endingDate );
      StreamingOutput stream = new LineageStreamingOutput( artifacts, lineageCollector );
      return Response.ok( stream )
        .header( "Content-Type", "application/zip" )
        .header( "Content-Disposition", "inline; filename=" + filename )
        .build();
    } catch ( IllegalArgumentException e ) {
      // illegal date format, return an error to the caller
      throw new BadRequestException( Response.status( Response.Status.BAD_REQUEST )
        .entity(
          "Invalid date string provided. All dates must be valid and conform to this format " + DATE_FORMAT ).build() );
    }
  }

  @POST
  @Path( "/download/file" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( { "application/zip" } )
  public Response downloadFile( LineageRequest request ) {
    return downloadFile( request, null, null );
  }

  @POST
  @Path( "/download/file/{startingDate}" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( { "application/zip" } )
  public Response downloadFile( LineageRequest request, @PathParam( "startingDate" ) String startingDate ) {
    return downloadFile( request, startingDate, null );
  }

  @POST
  @Path( "/download/file/{startingDate}/{endingDate}" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( { "application/zip" } )
  public Response downloadFile( LineageRequest request, @PathParam( "startingDate" ) String startingDate,
    @PathParam( "endingDate" ) String endingDate ) {

    if ( lineageCollector == null ) {
      return Response.serverError().entity( Messages.getString( "ERROR.LineageCollector.IsNull" ) ).build();
    }
    if ( request == null || request.getPath() == null ) {
      return Response.serverError().entity( Messages.getString( "ERROR.NoPathFound" ) ).build();
    }

    try {
      final List<String> artifacts = lineageCollector.listArtifactsForFile(
        request.getPath(),
        startingDate,
        endingDate );

      File f = new File( request.getPath() );

      String filename = f.getName() + "_lineage";
      if ( startingDate != null ) {
        filename += "_" + startingDate;
      }
      if ( endingDate != null ) {
        filename += "-" + endingDate;
      }
      filename += ".zip";

      StreamingOutput stream = new LineageStreamingOutput( artifacts, lineageCollector );
      return Response.ok( stream )
        .header( "Content-Type", "application/zip" )
        .header( "Content-Disposition", "inline; filename=" + filename )
        .build();
    } catch ( IllegalArgumentException e ) {
      // illegal date format, return an error to the caller
      throw new BadRequestException( Response.status( Response.Status.BAD_REQUEST )
        .entity(
          "Invalid date string provided. All dates must be valid and conform to this format " + DATE_FORMAT ).build() );
    }
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
