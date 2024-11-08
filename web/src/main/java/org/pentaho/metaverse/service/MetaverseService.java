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


package org.pentaho.metaverse.service;

import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.pentaho.metaverse.api.IDocumentLocator;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.ILineageCollector;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.api.model.LineageRequest;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.messages.Messages;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * REST endpoints for the accessing lineage artifacts.
 */
@Path( "/api" )
@ExternallyManagedLifecycle
public class MetaverseService {

  private IMetaverseReader metaverseReader;
  private IDocumentLocatorProvider documentLocatorProvider;
  private ILineageCollector lineageCollector;
  private static final String DATE_FORMAT = "yyyyMMdd";
  private int count;

  public static final int OK = 200;
  public static final int BAD_REQUEST = 400;
  public static final int SERVER_ERROR = 500;

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
  @Facet( name = "Unsupported" )
  @StatusCodes ( {
    @ResponseCode ( code = OK, condition = "Successfully created and returned graph." ),
    @ResponseCode ( code = SERVER_ERROR, condition = "Server Error." )
  } )
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
   * Download a zip file of all known lineage related artifacts.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho-di/osgi/cxf/lineage/api/download/all
   * </p>
   *
   * @return A zip file containing lineage artifacts
   */
  @GET
  @Path( "/download/all" )
  @Produces( { "application/zip" } )
  @StatusCodes ( {
    @ResponseCode ( code = OK, condition = "Successfully created and returned the zip file." ),
    @ResponseCode ( code = SERVER_ERROR, condition = "Server Error." )
  } )
  public Response download() {
    return download( null, null );
  }

  /**
   * Download a zip file of all known lineage related artifacts created on or after a specific date.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho-di/osgi/cxf/lineage/api/download/all/20150101
   * </p>
   *
   * @param startingDate a date string in the format YYYYMMDD
   * @return A zip file containing lineage artifacts
   */
  @GET
  @Path( "/download/all/{startingDate}" )
  @Produces( { "application/zip" } )
  @StatusCodes ( {
    @ResponseCode ( code = OK, condition = "Successfully created and returned the zip file." ),
    @ResponseCode ( code = BAD_REQUEST, condition = "Bad request, invalid starting date provided." ),
    @ResponseCode ( code = SERVER_ERROR, condition = "Server Error." )
  } )
  public Response download( @PathParam( "startingDate" ) final String startingDate ) {
    return download( startingDate, null );
  }

  /**
   * Download a zip file of all known lineage related artifacts created between (inclusive) two dates.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho-di/osgi/cxf/lineage/api/download/all/20150101/20151231
   * </p>
   *
   * @param startingDate a date string in the format YYYYMMDD
   * @param endingDate   a date string in the format YYYYMMDD
   *
   * @return A zip file containing lineage artifacts
   */
  @GET
  @Path( "/download/all/{startingDate}/{endingDate}" )
  @Produces( { "application/zip" } )
  @StatusCodes ( {
    @ResponseCode ( code = OK, condition = "Successfully created and returned the zip file." ),
    @ResponseCode ( code = BAD_REQUEST, condition = "Bad request, invalid starting and/or ending date provided." ),
    @ResponseCode ( code = SERVER_ERROR, condition = "Server Error." )
  } )
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

  /**
   * Download a zip file of all known lineage related artifacts for a specified file.
   *
   * <p><b>Example Request:</b><br />
   *    POST pentaho-di/osgi/cxf/lineage/api/download/file
   * <br /><b>POST data:</b>
   *  <pre function="syntax.xml">
   *    { "path": "/home/suzy/test.ktr" }
   *  </pre>
   * </p>
   *
   * @param request {@link LineageRequest} containing the path to the file of interest
   *
   * @return A zip file containing lineage artifacts
   */
  @POST
  @Path( "/download/file" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( { "application/zip" } )
  @StatusCodes ( {
    @ResponseCode ( code = OK, condition = "Successfully created and returned the zip file." ),
    @ResponseCode ( code = SERVER_ERROR, condition = "Server Error." )
  } )
  public Response downloadFile( LineageRequest request ) {
    return downloadFile( request, null, null );
  }

  /**
   * Download a zip file of all known lineage related artifacts for a specified file created on or after a specified
   * date
   *
   * <p><b>Example Request:</b><br />
   *    POST pentaho-di/osgi/cxf/lineage/api/download/file/20150101
   * <br /><b>POST data:</b>
   *  <pre function="syntax.xml">
   *    { "path": "/home/suzy/test.ktr" }
   *  </pre>
   * </p>
   *
   * @param request      {@link LineageRequest} containing the path to the file of interest
   * @param startingDate a date string in the format YYYYMMDD
   *
   * @return A zip file containing lineage artifacts
   */
  @POST
  @Path( "/download/file/{startingDate}" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( { "application/zip" } )
  @StatusCodes ( {
    @ResponseCode ( code = OK, condition = "Successfully created and returned the zip file." ),
    @ResponseCode ( code = BAD_REQUEST, condition = "Bad request, invalid starting date provided." ),
    @ResponseCode ( code = SERVER_ERROR, condition = "Server Error." )
  } )
  public Response downloadFile( LineageRequest request, @PathParam( "startingDate" ) String startingDate ) {
    return downloadFile( request, startingDate, null );
  }

  /**
   * Download a zip file of all known lineage related artifacts for a specified file
   * created between two dates (inclusive)
   *
   * <p><b>Example Request:</b><br />
   *    POST pentaho-di/osgi/cxf/lineage/api/download/file/20150101/20151231
   * <br /><b>POST data:</b>
   *  <pre function="syntax.xml">
   *    { "path": "/home/suzy/test.ktr" }
   *  </pre>
   * </p>
   *
   * @param request      {@link LineageRequest} containing the path to the file of interest
   * @param startingDate a date string in the format YYYYMMDD
   * @param endingDate   a date string in the format YYYYMMDD
   *
   * @return A zip file containing lineage artifacts
   */
  @POST
  @Path( "/download/file/{startingDate}/{endingDate}" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( { "application/zip" } )
  @StatusCodes ( {
    @ResponseCode ( code = OK, condition = "Successfully created and returned the zip file." ),
    @ResponseCode ( code = BAD_REQUEST, condition = "Bad request, invalid starting and/or ending date provided." ),
    @ResponseCode ( code = SERVER_ERROR, condition = "Server Error." )
  } )
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
