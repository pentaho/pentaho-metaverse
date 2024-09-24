/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.Tokens;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ICatalogLineageClient;
import org.pentaho.metaverse.api.ICatalogLineageClientProvider;
import org.pentaho.metaverse.api.model.catalog.FieldLevelRelationship;
import org.pentaho.metaverse.api.model.catalog.LineageDataResource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The GraphCatalogWriter reads a tinkerpop graph and parses out any input files and output files and database tables
 * and attempts to connect to a Lumada data catalog to record the relationships between the input and output files
 * 
 */
public class GraphCatalogWriter extends BaseGraphWriter {

  private static final Logger log = LogManager.getLogger( GraphCatalogWriter.class );

  private ICatalogLineageClient lineageClient;
  private ICatalogLineageClientProvider catalogLineageClientProvider;

  private String catalogUrl;
  private String catalogUsername;
  private String catalogPassword;
  private String catalogTokenUrl;
  private String catalogClientId;
  private String catalogClientSecret;

  public GraphCatalogWriter( String catalogUrl,
                             String catalogUsername,
                             String catalogPassword,
                             String catalogTokenUrl,
                             String catalogClientId,
                             String catalogClientSecret ) {
    super();
    this.catalogUrl = catalogUrl;
    this.catalogUsername = catalogUsername;
    this.catalogPassword = catalogPassword;
    this.catalogTokenUrl = catalogTokenUrl;
    this.catalogClientId = catalogClientId;
    this.catalogClientSecret = catalogClientSecret;
  }

  private void createCatalogClient( String catalogUrl, String catalogUsername, String catalogPassword, String catalogTokenUrl,
                          String catalogClientId, String catalogClientSecret ) {
    if ( null != this.getCatalogLineageClientProvider() ) {
      try {
        lineageClient = catalogLineageClientProvider.getCatalogLineageClient(
          catalogUrl,
          catalogUsername,
          catalogPassword,
          catalogTokenUrl,
          catalogClientId,
          catalogClientSecret );
      } catch ( Exception e ) {
        // logging at debug level because this could happen if the catalog service isn't loaded yet, or the service
        // may not exist or be enabled in this profile
        log.debug( e );
      }
    }
  }

  public boolean clientConfigured() {
    if ( null == lineageClient ) {
      createCatalogClient( catalogUrl, catalogUsername, catalogPassword, catalogTokenUrl, catalogClientId, catalogClientSecret );
    }
    return null != lineageClient && lineageClient.urlConfigured();
  }

  @Override
  public void outputGraphImpl( Graph graph, OutputStream out ) throws IOException {

    if ( !clientConfigured() ) {
      log.info( "Could not get a catalog client; no catalog lineage processing." );
    }

    log.info( "Stating lineage processing." );

    ArrayList<LineageDataResource> inputSources = new ArrayList<>();
    ArrayList<LineageDataResource> outputTargets = new ArrayList<>();

    // Get input data sources and fields
    GremlinPipeline<Graph, Vertex> inputNodesPipe =
            new GremlinPipeline<Graph, Vertex>( graph )
                    .V()
                    .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP )
                    .in( DictionaryConst.LINK_READBY )
                    .cast( Vertex.class );
    List<Vertex> inputVertexes = inputNodesPipe.toList();
    inputVertexes.forEach( vertex -> processInputs( graph, inputSources, vertex ) );

    // Get output data sources and fields
    GremlinPipeline<Graph, Vertex> outputNodesPipe =
            new GremlinPipeline<Graph, Vertex>( graph )
                    .V()
                    .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP )
                    .out( DictionaryConst.LINK_WRITESTO )
                    .cast( Vertex.class );
    List<Vertex> outputVertexes = outputNodesPipe.toList();
    outputVertexes.forEach( vertex -> processOutputs( graph, outputTargets, vertex ) );

    // Trace output fields to source fields
    linkTargetFieldsToSources( outputTargets, inputSources, graph );

    try {
      lineageClient.processLineage( inputSources, outputTargets );
    } catch ( Exception e ) {
      log.error( e.getMessage(), e );
    }

    log.info( "Lineage processing done." );
  }

  private void processOutputs( Graph graph, ArrayList<LineageDataResource> outputTargets, Vertex vertex ) {
    // handles resources written to by a step that have a PATH property
    String pathProperty = vertex.getProperty( DictionaryConst.PROPERTY_PATH );
    if ( propertyPopulated( pathProperty ) ) {
      LineageDataResource dataResource =
        getLineageDataResourceFromFileVertex( graph, vertex, pathProperty );
      outputTargets.add( dataResource );
    }
    // handles tables written to by a step
    String resourceType = vertex.getProperty( DictionaryConst.PROPERTY_TYPE );
    if ( propertyPopulated( resourceType ) && resourceType.equals( DictionaryConst.NODE_TYPE_DATA_TABLE ) ) {
      String tableName = vertex.getProperty( DictionaryConst.PROPERTY_TABLE );
      if ( propertyPopulated( tableName ) ) {
        LineageDataResource dataResource =
          getLineageDataResourceFromTableVertex( graph, vertex, tableName );
        outputTargets.add( dataResource );
      }
    }
  }

  private LineageDataResource getLineageDataResourceFromTableVertex( Graph graph, Vertex vertex, String tableName ) {
    LineageDataResource dataResource = new LineageDataResource( tableName );
    dataResource.setVertexId( vertex.getId() );
    findDbConnectionProperties( vertex, dataResource, DictionaryConst.LINK_WRITESTO );
    dataResource.setFields( getTableFields( tableName, graph ) );
    dataResource.setDbSchema( vertex.getProperty( DictionaryConst.PROPERTY_SCHEMA ) );
    return dataResource;
  }

  private void processInputs( Graph graph, ArrayList<LineageDataResource> inputSources, Vertex vertex ) {
    // handles resources read by a step that have a PATH property
    String pathProperty = vertex.getProperty( DictionaryConst.PROPERTY_PATH );
    if ( propertyPopulated( pathProperty ) ) {
      LineageDataResource dataResource =
        getLineageDataResourceFromFileVertex( graph, vertex, pathProperty );
      inputSources.add( dataResource );
    }
    // handles resources ready by a step that have a query property
    String queryString = vertex.getProperty( DictionaryConst.PROPERTY_QUERY );
    if ( propertyPopulated( queryString ) ) {
      LineageDataResource dataResource = getLineageDataResourceFromQueryVertex( graph, vertex, queryString );
      inputSources.add( dataResource );
    }
  }

  private LineageDataResource getLineageDataResourceFromQueryVertex( Graph graph, Vertex vertex, String queryString ) {
    LineageDataResource dataResource = new LineageDataResource( queryString );
    dataResource.setVertexId( vertex.getId() );
    findDbConnectionProperties( vertex, dataResource, DictionaryConst.LINK_READBY );
    dataResource.setFields( getQueryFields( queryString, graph ) );
    return dataResource;
  }

  private LineageDataResource getLineageDataResourceFromFileVertex( Graph graph, Vertex vertex, String path ) {
    LineageDataResource dataResource = new LineageDataResource( getSourceName( path ) );
    String fileScheme = vertex.getProperty( DictionaryConst.PROPERTY_FILE_SCHEME );
    if ( null != fileScheme ) {
      switch ( fileScheme ) {
        case "hdfs":
          dataResource.parseHdfsPath( path );
          break;
        case "s3":
        case "s3a":
        case "s3n":
          dataResource.parseS3PvfsPath( path );
          break;
        default: // no-op for now; nothing to do for the default
          dataResource.setPath( path );
          log.debug( String.format( "No file scheme found with path {0}", path ) );
          break;
      }
    }
    dataResource.setVertexId( vertex.getId() );
    dataResource.setFields( getDatasourceFields( path, graph ) );
    return dataResource;
  }

  private void findDbConnectionProperties( Vertex vertex, LineageDataResource dataResource, String readOrWrite ) {
    Direction stepNodeDirection = readOrWrite.equals( DictionaryConst.LINK_READBY ) ? Direction.IN : Direction.OUT;
    Direction stepEdgeDirection = readOrWrite.equals( DictionaryConst.LINK_READBY ) ? Direction.OUT : Direction.IN;
    Iterator<Edge> queryEdges = vertex.getEdges( stepEdgeDirection ).iterator();
    while ( queryEdges.hasNext() ) {
      Edge queryEdge = queryEdges.next();
      // find the step that reads this query
      if ( queryEdge.getLabel().equals( readOrWrite )
        && hasPropertyValue( queryEdge.getVertex( stepNodeDirection ),
          DictionaryConst.PROPERTY_TYPE,
          DictionaryConst.NODE_TYPE_TRANS_STEP ) ) {
        Vertex tableStep = queryEdge.getVertex( stepNodeDirection );
        Iterator<Edge> tableStepEdges = tableStep.getEdges( Direction.IN ).iterator();
        while ( tableStepEdges.hasNext() ) {
          Edge tableEdge = tableStepEdges.next();
          // find the DB connection that is a dependency of this table input step
          if ( tableEdge.getLabel().equals( DictionaryConst.LINK_DEPENDENCYOF )
            && hasPropertyValue( tableEdge.getVertex( Direction.OUT ),
              DictionaryConst.PROPERTY_TYPE,
              DictionaryConst.NODE_TYPE_DATASOURCE ) ) {
            Vertex dbNode = tableEdge.getVertex( Direction.OUT );
            dataResource.setDbHost( dbNode.getProperty( DictionaryConst.PROPERTY_HOST_NAME ) );
            dataResource.setDbName( dbNode.getProperty( DictionaryConst.PROPERTY_DATABASE_NAME ) );
            dataResource.setDbPort( dbNode.getProperty( DictionaryConst.PROPERTY_PORT ) );
          }
        }
      }
    }
  }

  private String getSourceName( String fullName ) {
    String sourceName = null;
    if ( fullName.contains( "/" ) ) {
      sourceName = fullName.substring( fullName.lastIndexOf( "/" ) + 1 );
    } else if ( fullName.contains( "\\" ) ) {
      sourceName = fullName.substring( fullName.lastIndexOf( "\\" ) + 1 );
    } else {
      sourceName = fullName;
    }
    return sourceName;
  }

  private List<String> getDatasourceFields( String sourceName, Graph graph ) {
    GremlinPipeline<Graph, Vertex> inputFieldsPipe =
            new GremlinPipeline<Graph, Vertex>( graph )
                    .V()
                    .has( DictionaryConst.PROPERTY_PATH, Tokens.T.eq, sourceName )
                    .out( DictionaryConst.LINK_CONTAINS )
                    .cast( Vertex.class );
    List<Vertex> inputFieldVertexes = inputFieldsPipe.toList();
    ArrayList<String> fields = new ArrayList<>();
    inputFieldVertexes.forEach( fieldVertex -> fields.add( fieldVertex.getProperty( DictionaryConst.PROPERTY_NAME ) ) );
    return fields;
  }

  private List<String> getQueryFields( String sourceName, Graph graph ) {
    GremlinPipeline<Graph, Vertex> inputFieldsPipe =
      new GremlinPipeline<Graph, Vertex>( graph )
        .V()
        .has( DictionaryConst.PROPERTY_QUERY, Tokens.T.eq, sourceName )
        .out( DictionaryConst.LINK_CONTAINS )
        .cast( Vertex.class );
    List<Vertex> inputFieldVertexes = inputFieldsPipe.toList();
    ArrayList<String> fields = new ArrayList<>();
    inputFieldVertexes.forEach( fieldVertex -> fields.add( fieldVertex.getProperty( DictionaryConst.PROPERTY_NAME ) ) );
    return fields;
  }

  private List<String> getTableFields( String sourceName, Graph graph ) {
    GremlinPipeline<Graph, Vertex> inputFieldsPipe =
      new GremlinPipeline<Graph, Vertex>( graph )
        .V()
        .has( DictionaryConst.PROPERTY_TABLE, Tokens.T.eq, sourceName )
        .out( DictionaryConst.LINK_CONTAINS )
        .cast( Vertex.class );
    List<Vertex> inputFieldVertexes = inputFieldsPipe.toList();
    ArrayList<String> fields = new ArrayList<>();
    inputFieldVertexes.forEach( fieldVertex -> fields.add( fieldVertex.getProperty( DictionaryConst.PROPERTY_NAME ) ) );
    return fields;
  }

  /**
   * Finds all fields associated with the output file and table resources and tries to link them back to
   * fields associated with the input resources.  Any links found will be added to the outputTargets list.
   * @param outputTargets
   * @param inputSources
   * @param graph
   */
  private void linkTargetFieldsToSources( List<LineageDataResource> outputTargets, List<LineageDataResource> inputSources, Graph graph ) {
    for ( LineageDataResource outputTarget : outputTargets ) {
      // returns all fields from previously identified output files
      GremlinPipeline<Graph, Vertex> fileFieldsPipe =
              new GremlinPipeline<Graph, Vertex>( graph )
                      .V()
                      .has( DictionaryConst.PROPERTY_PATH, Tokens.T.eq, outputTarget.getPath() )
                      .out( DictionaryConst.LINK_CONTAINS )
                      .cast( Vertex.class );
      // returns all fields from all previously identified output tables
      GremlinPipeline<Graph, Vertex> tableFieldsPipe =
        new GremlinPipeline<Graph, Vertex>( graph )
          .V()
          .has( DictionaryConst.PROPERTY_TABLE, Tokens.T.eq, outputTarget.getName() )
          .out( DictionaryConst.LINK_CONTAINS )
          .cast( Vertex.class );
      List<Vertex> allVertexes = fileFieldsPipe.toList();
      allVertexes.addAll( tableFieldsPipe.toList() );
      allVertexes.forEach( vertex -> {
        String outputTargetResourceField = vertex.getProperty( DictionaryConst.PROPERTY_NAME );
        List<List<Vertex>> paths = findOrigins( vertex, null );
        paths.forEach( path -> inputSources.forEach( inputSource -> {
          if ( path.get( 0 ).getId().equals( inputSource.getVertexId() ) ) {

            String inputSourceField = path.get( 1 ).getProperty( DictionaryConst.PROPERTY_NAME );
            log.info( "Field path found: " + path );
            FieldLevelRelationship fieldRelationship = new FieldLevelRelationship();
            fieldRelationship.setInputSourceResource( inputSource );
            fieldRelationship.setInputSourceResourceField( inputSourceField );
            fieldRelationship.setOutputTargetResource( outputTarget );
            fieldRelationship.setOutputTargetResourceField( outputTargetResourceField );
            inputSource.addFieldLevelRelationship( fieldRelationship );
            outputTarget.addFieldLevelRelationship( fieldRelationship );
          }
        } ) );
      } );
    }
  }

  /**
   * Method assumes the vertex being processed is a field contained in an output file or output table.
   * Recursively walks backwards across any populates, derives, or contains edges until it hits the last graph vertex in
   * that path (no more such edges to follow), which may be a data file or query read by the transformation.
   *
   * @param vertex vertex of a field in an output file or table
   * @param seenVertices should be null when called by other methods; contains a hash map of vertices already processed
   *                     to avoid recursing through an infinite loop (e.g. read from and write to the same file).
   * @return a List of a paths from the given vertex back across populates, derives, and/or contains edges.  In each
   * path, the first vertex is the beginning of the path, and the last vertex is the original argument passed.
   */
  private List<List<Vertex>> findOrigins( Vertex vertex, Map<Vertex, Object> seenVertices ) {
    ArrayList<List<Vertex>> paths = new ArrayList<>();
    if ( null == seenVertices ) {
      seenVertices = new HashMap<>();
    }
    seenVertices.put( vertex, "" );

    Iterator<Edge> edges = vertex.getEdges( Direction.IN ).iterator();
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      if ( edge.getLabel().equals( DictionaryConst.LINK_POPULATES )
        || edge.getLabel().equals( DictionaryConst.LINK_DERIVES )
        || edge.getLabel().equals( DictionaryConst.LINK_CONTAINS ) ) {
        Vertex nextVertex = edge.getVertex( Direction.OUT );
        // this might not be the most efficient way to avoid processing a cycle, but it works
        if ( null == seenVertices.get( nextVertex ) ) {
          List<List<Vertex>> newPaths = findOrigins( nextVertex, seenVertices );
          newPaths.forEach( path -> path.add( vertex ) );
          paths.addAll( newPaths );
        }
      }
    }
    if ( paths.isEmpty() ) {
      ArrayList<Vertex> thisVertex = new ArrayList<>();
      thisVertex.add( vertex );
      paths.add( thisVertex );
    }
    return paths;
  }

  private boolean propertyPopulated( String propertyVal ) {
    return null != propertyVal && !"".equals( propertyVal );
  }

  private boolean hasPropertyValue( Vertex v, String propertyName, String propertyVal ) {
    String vertexPropertyValue = v.getProperty( propertyName );
    return propertyPopulated( vertexPropertyValue ) && vertexPropertyValue.equals( propertyVal );
  }

  public ICatalogLineageClientProvider getCatalogLineageClientProvider() {
    return catalogLineageClientProvider;
  }

  public void setCatalogLineageClientProvider(
    ICatalogLineageClientProvider catalogLineageClientProvider ) {
    this.catalogLineageClientProvider = catalogLineageClientProvider;
  }
}
