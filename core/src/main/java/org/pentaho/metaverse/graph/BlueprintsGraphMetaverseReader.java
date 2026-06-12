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


package org.pentaho.metaverse.graph;

import com.google.common.annotations.VisibleForTesting;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.dictionary.MetaverseLink;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.impl.MetaverseNode;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of an IMetaverseReader that uses a Blueprints graph as the underlying storage
 *
 * @author jdixon
 */
public class BlueprintsGraphMetaverseReader implements IMetaverseReader {

  private static final long serialVersionUID = -3813738340722424284L;
  private static final Logger LOGGER = LoggerFactory.getLogger( BlueprintsGraphMetaverseReader.class );

  private Graph graph;

  private static BlueprintsGraphMetaverseReader instance;

  public static BlueprintsGraphMetaverseReader getInstance() {
    if ( null == instance ) {
      instance = new BlueprintsGraphMetaverseReader( SynchronizedGraphFactory.getDefaultGraph() );
    }
    return instance;
  }

  /**
   * Constructor that accepts a Graph
   *
   * @param graph the Graph to read from
   */
  @VisibleForTesting
  BlueprintsGraphMetaverseReader( Graph graph ) {
    this.graph = graph;
  }

  /**
   * Gets the complete, underlying graph
   *
   * @return the entire Graph
   */
  protected Graph getGraph() {
    return graph;
  }

  @Override
  public IMetaverseNode findNode( String id ) {
    Iterator<Vertex> it = getGraph().vertices( id );
    if ( !it.hasNext() ) {
      return null;
    }
    Vertex vertex = it.next();
    MetaverseUtil.enhanceVertex( vertex );
    MetaverseNode node = new MetaverseNode( vertex );
    return node;
  }

  @Override
  public List<IMetaverseNode> findNodes( String property, String value ) {
    List<Vertex> vertices = getGraph().traversal().V().has( property, value ).toList();
    if ( vertices == null ) {
      return null;
    }
    List<IMetaverseNode> result = new ArrayList<IMetaverseNode>();
    for ( Vertex v : vertices ) {
      MetaverseNode node = new MetaverseNode( v );
      result.add( node );
    }
    return result;
  }

  @Override
  public IMetaverseLink findLink( String leftNodeID, String linkType, String rightNodeID, Direction direction ) {
    Iterator<Vertex> vit = getGraph().vertices( leftNodeID );
    if ( !vit.hasNext() ) {
      return null;
    }
    Vertex vertex = vit.next();
    Iterator<Edge> edges = linkType == null ? vertex.edges( direction ) : vertex.edges( direction, linkType );
    IMetaverseLink link = new MetaverseLink();
    IMetaverseNode node1 = new MetaverseNode( vertex );
    Direction opDirection = direction == Direction.IN ? Direction.OUT : Direction.IN;
    Vertex vertex2 = null;
    if ( rightNodeID != null ) {
      while ( edges.hasNext() ) {
        Edge edge = edges.next();
        Vertex oppV = direction == Direction.IN ? edge.outVertex() : edge.inVertex();
        if ( rightNodeID.equals( oppV.id().toString() ) ) {
          vertex2 = oppV;
          IMetaverseNode node2 = new MetaverseNode( vertex2 );
          String label = edge.label();
          link.setLabel( label );
          String localized = Messages.getString( MetaverseUtil.MESSAGE_PREFIX_LINKTYPE + label );
          if ( !localized.startsWith( "!" ) ) {
            link.setProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED, localized );
          }
          if ( direction == Direction.OUT ) {
            link.setFromNode( node1 );
            link.setToNode( node2 );
          } else {
            link.setFromNode( node2 );
            link.setToNode( node1 );
          }
          return link;
        }
      }
    }
    return null;
  }

  @Override
  public Graph getMetaverse() {
    Graph graph = getGraph();
    graph = enhanceGraph( graph );
    return graph;
  }

  @Override
  public String exportToXml() {
    return exportFormat( FORMAT_XML );
  }

  @Override
  public String exportFormat( String format ) {
    OutputStream out = new ByteArrayOutputStream();
    try {
      exportToStream( format, out );
    } catch ( IOException e ) {
      LOGGER.error( Messages.getString( "ERROR.Graph.Export" ), e );
    } finally {
      try {
        out.close();
      } catch ( IOException e ) {
        LOGGER.error( Messages.getString( "ERROR.Graph.Export" ), e );
      }
    }
    return out.toString();
  }

  /**
   * Exports the metaverse graph by writing it to an output stream
   *
   * @param format The format for the export: XML, JSON, or CSV
   * @param out    The output stream to write to
   * @throws IOException Thrown if there is an I/O issue
   */
  public void exportToStream( String format, OutputStream out ) throws IOException {
    String fmt = format;
    if ( fmt == null ) {
      // default to graphml
      fmt = FORMAT_XML;
    }
    Graph graph = getGraph();
    graph = enhanceGraph( graph );
    // convert the graph to an export format, GraphML for now
    if ( fmt.equalsIgnoreCase( FORMAT_XML ) ) {
      GraphMLWriter writer = new GraphMLWriter();
      writer.outputGraph( graph, out );
    } else if ( fmt.equalsIgnoreCase( FORMAT_JSON ) ) {
      GraphSONWriter writer = new GraphSONWriter();
      writer.outputGraph( graph, out );
    } else if ( fmt.equalsIgnoreCase( FORMAT_CSV ) ) {
      GraphCsvWriter writer = new GraphCsvWriter();
      writer.outputGraph( graph, out );
    }
  }

  @Override
  public Graph search( List<String> resultTypes, List<String> startNodeIDs, boolean shortestOnly ) {

    Graph g = TinkerGraph.open();

    for ( String startNodeID : startNodeIDs ) {
      if ( graph != null ) {
        // traverse look for paths to the results
        Iterator<Vertex> startIt = graph.vertices( startNodeID );
        if ( !startIt.hasNext() ) {
          continue;
        }
        Vertex startVertex = startIt.next();
        GraphPath path = new GraphPath();
        Set<Object> done = new HashSet<Object>();
        Map<Object, GraphPath> shortestPaths = new HashMap<Object, GraphPath>();
        traverseGraph( startVertex, graph, resultTypes, path, done, shortestPaths, Direction.IN, shortestOnly );
        done = new HashSet<Object>();
        traverseGraph( startVertex, graph, resultTypes, path, done, shortestPaths, Direction.OUT, shortestOnly );
        Iterator<Map.Entry<Object, GraphPath>> paths = shortestPaths.entrySet().iterator();
        while ( paths.hasNext() ) {
          Map.Entry<Object, GraphPath> entry = paths.next();
          GraphPath shortPath = entry.getValue();
          shortPath.addToGraph( g );
        }

      }
    }
    g = enhanceGraph( g );
    return g;
  }

  private void traverseGraph( Vertex startVertex, Graph subGraph, List<String> resultTypes, GraphPath path,
                              Set<Object> done, Map<Object, GraphPath> shortestPaths, Direction direction, boolean shortestOnly ) {
    String startType = startVertex.property( DictionaryConst.PROPERTY_TYPE ).isPresent()
      ? startVertex.<String>value( DictionaryConst.PROPERTY_TYPE ) : null;
    boolean isTargetType = resultTypes == null
      || resultTypes.size() == 0
      || resultTypes.contains( startType );
    if ( !isTargetType && done.contains( startVertex.id() ) ) {
      return;
    }
    path.addVertex( startVertex );
    if ( isTargetType ) {
      // this is one of our target types
      if ( shortestOnly ) {
        GraphPath shortestPath = shortestPaths.get( startVertex.id() );
        if ( shortestPath == null || path.getLength() < shortestPath.getLength() ) {
          shortestPaths.put( startVertex.id(), path.clone() );
          if ( done.contains( startVertex.id() ) ) {
            path.pop();
            return;
          }
        }
      } else {
        shortestPaths.put( path.toString(), path.clone() );
      }
    }
    done.add( startVertex.id() );
    if ( direction == Direction.IN ) {
      Iterator<Edge> edges = startVertex.edges( Direction.IN );
      while ( edges.hasNext() ) {
        Edge edge = edges.next();
        if ( done.contains( edge.id() ) ) {
          continue;
        }
        path.addEdge( edge );
        Vertex nextVertex = edge.outVertex();
        traverseGraph( nextVertex, subGraph, resultTypes, path, done, shortestPaths, direction, shortestOnly );
        path.pop();
      }
    }
    if ( direction == Direction.OUT ) {
      Iterator<Edge> edges = startVertex.edges( Direction.OUT );
      while ( edges.hasNext() ) {
        Edge edge = edges.next();
        if ( done.contains( edge.id() ) ) {
          continue;
        }
        path.addEdge( edge );
        Vertex nextVertex = edge.inVertex();
        traverseGraph( nextVertex, subGraph, resultTypes, path, done, shortestPaths, direction, shortestOnly );
        path.pop();
      }
      // go upstream to find structure
      if ( path.getLength() > 1 ) {
        edges = startVertex.edges( Direction.IN );
        while ( edges.hasNext() ) {
          Edge edge = edges.next();
          if ( done.contains( edge.id() ) ) {
            continue;
          }
          path.addEdge( edge );
          Vertex nextVertex = edge.outVertex();
          // go upstream to find structure
          traverseGraph( nextVertex, subGraph, resultTypes, path, done, shortestPaths, Direction.IN, shortestOnly );
          path.pop();
        }
      }
    }
    path.pop();
  }

  @Override
  public Graph getGraph( String id ) {
    Iterator<Vertex> rootIt = getGraph().vertices( id );
    if ( !rootIt.hasNext() ) {
      return null;
    }
    Vertex root = rootIt.next();
    Graph g = TinkerGraph.open();
    Vertex clone = GraphUtil.cloneVertexIntoGraph( root, g );
    traceVertices( root, clone, Direction.IN, getGraph(), g, null );
    traceVertices( root, clone, Direction.OUT, getGraph(), g, null );
    g = enhanceGraph( g );
    return g;
  }

  /**
   * Traces all of the
   *
   * @param vertex    The source vertex to traverse from
   * @param clone     The clone of the source vertex in the sub-graph
   * @param direction The direction to traverse the graph in
   * @param graph1    The source graph
   * @param graph2    The sub-graph being built
   * @param edgeTypes The types of edges to include in the traversal. Can be null (all edges).
   */
  private void traceVertices( Vertex vertex, Vertex clone, Direction direction,
                              Graph graph1, Graph graph2, Set<String> edgeTypes ) {
    Direction opDirection = direction == Direction.IN ? Direction.OUT : Direction.IN;
    Iterator<Edge> edges = vertex.edges( direction );
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      if ( edgeTypes != null && !edgeTypes.contains( edge.label() ) ) {
        continue;
      }
      Vertex nextVertex = direction == Direction.IN ? edge.outVertex() : edge.inVertex();
      Vertex target = GraphUtil.cloneVertexIntoGraph( nextVertex, graph2 );
      Vertex node1 = direction == Direction.IN ? target : clone;
      Vertex node2 = direction == Direction.IN ? clone : target;
      String edgeId = node1.id().toString() + ">" + node2.id().toString();
      Iterator<Edge> existingEdge = graph2.edges( edgeId );
      if ( !existingEdge.hasNext() ) {
        if ( direction == Direction.IN ) {
          target.addEdge( edge.label(), clone, T.id, edgeId );
        } else {
          clone.addEdge( edge.label(), target, T.id, edgeId );
        }
      }
      traceVertices( nextVertex, target, direction, graph1, graph2, edgeTypes );
      if ( direction == Direction.OUT ) {
        traceVertices( nextVertex, target, Direction.IN, graph1, graph2, DictionaryHelper.STRUCTURAL_LINK_TYPES );
      }
    }
  }

  protected Graph enhanceGraph( Graph g ) {
    // TODO should we clone the graph?
    Iterator<Vertex> vertices = g.vertices();
    while ( vertices.hasNext() ) {
      Vertex vertex = vertices.next();
      MetaverseUtil.enhanceVertex( vertex );
    }
    Iterator<Edge> edges = g.edges();
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      MetaverseUtil.enhanceEdge( edge );
    }
    return g;
  }


}
