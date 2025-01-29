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
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
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
    Vertex vertex = getGraph().getVertex( id );
    if ( vertex == null ) {
      return null;
    }
    MetaverseUtil.enhanceVertex( vertex );
    MetaverseNode node = new MetaverseNode( vertex );
    return node;
  }

  @Override
  public List<IMetaverseNode> findNodes( String property, String value ) {
    Iterable<Vertex> vertices = getGraph().getVertices( property, value );
    if ( vertices == null ) {
      return null;
    }
    List<IMetaverseNode> result = new ArrayList<IMetaverseNode>();
    Iterator<Vertex> verticesIt = vertices.iterator();
    while ( verticesIt.hasNext() ) {
      MetaverseNode node = new MetaverseNode( verticesIt.next() );
      result.add( node );
    }
    return result;
  }

  @Override
  public IMetaverseLink findLink( String leftNodeID, String linkType, String rightNodeID, Direction direction ) {
    Vertex vertex = getGraph().getVertex( leftNodeID );
    if ( vertex == null ) {
      return null;
    }
    Iterable<Edge> edges = linkType == null ? vertex.getEdges( direction ) : vertex.getEdges( direction, linkType );
    IMetaverseLink link = new MetaverseLink();
    IMetaverseNode node1 = new MetaverseNode( vertex );
    Direction opDirection = direction == Direction.IN ? Direction.OUT : Direction.IN;
    Vertex vertex2 = null;
    if ( rightNodeID != null ) {
      Iterator<Edge> it = edges.iterator();
      while ( it.hasNext() ) {
        Edge edge = it.next();
        if ( rightNodeID.equals( (String) edge.getVertex( opDirection ).getId() ) ) {
          vertex2 = edge.getVertex( opDirection );
          IMetaverseNode node2 = new MetaverseNode( vertex2 );
          String label = edge.getLabel();
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

    Graph g = new TinkerGraph();

    for ( String startNodeID : startNodeIDs ) {
      if ( graph != null ) {
        // traverse look for paths to the results
        Vertex startVertex = graph.getVertex( startNodeID );
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
    boolean isTargetType = resultTypes == null
      || resultTypes.size() == 0
      || resultTypes.contains( startVertex.getProperty( DictionaryConst.PROPERTY_TYPE ) );
    if ( !isTargetType && done.contains( startVertex.getId() ) ) {
      return;
    }
    path.addVertex( startVertex );
    if ( isTargetType ) {
      // this is one of our target types
      if ( shortestOnly ) {
        GraphPath shortestPath = shortestPaths.get( startVertex.getId() );
        if ( shortestPath == null || path.getLength() < shortestPath.getLength() ) {
          shortestPaths.put( startVertex.getId(), path.clone() );
          if ( done.contains( startVertex.getId() ) ) {
            path.pop();
            return;
          }
        }
      } else {
        shortestPaths.put( path.toString(), path.clone() );
      }
    }
    done.add( startVertex.getId() );
    if ( direction == Direction.IN ) {
      Iterator<Edge> edges = startVertex.getEdges( Direction.IN ).iterator();
      while ( edges.hasNext() ) {
        Edge edge = edges.next();
        if ( done.contains( edge.getId() ) ) {
          continue;
        }
        path.addEdge( edge );
        Vertex nextVertex = edge.getVertex( Direction.OUT );
        traverseGraph( nextVertex, subGraph, resultTypes, path, done, shortestPaths, direction, shortestOnly );
        path.pop();
      }
    }
    if ( direction == Direction.OUT ) {
      Iterator<Edge> edges = startVertex.getEdges( Direction.OUT ).iterator();
      while ( edges.hasNext() ) {
        Edge edge = edges.next();
        if ( done.contains( edge.getId() ) ) {
          continue;
        }
        path.addEdge( edge );
        Vertex nextVertex = edge.getVertex( Direction.IN );
        traverseGraph( nextVertex, subGraph, resultTypes, path, done, shortestPaths, direction, shortestOnly );
        path.pop();
      }
      // go upstream to find structure
      if ( path.getLength() > 1 ) {
        edges = startVertex.getEdges( Direction.IN ).iterator();
        while ( edges.hasNext() ) {
          Edge edge = edges.next();
          if ( done.contains( edge.getId() ) ) {
            continue;
          }
          path.addEdge( edge );
          Vertex nextVertex = edge.getVertex( Direction.OUT );
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
    Vertex root = getGraph().getVertex( id );
    if ( root == null ) {
      return null;
    }
    Graph g = new TinkerGraph();
    // find the upstream nodes
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
    Iterator<Edge> edges = vertex.getEdges( direction ).iterator();
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      if ( edgeTypes != null && !edgeTypes.contains( edge.getLabel() ) ) {
        continue;
      }
      Vertex nextVertex = edge.getVertex( opDirection );
      Vertex target = GraphUtil.cloneVertexIntoGraph( nextVertex, graph2 );
      Vertex node1 = direction == Direction.IN ? target : clone;
      Vertex node2 = direction == Direction.IN ? clone : target;
      String edgeId = (String) node1.getId() + ">" + (String) node2.getId();
      if ( graph2.getEdge( edgeId ) == null ) {
        if ( direction == Direction.IN ) {
          graph2.addEdge( edgeId, target, clone, edge.getLabel() );
        } else {
          graph2.addEdge( edgeId, clone, target, edge.getLabel() );
        }
      }
      traceVertices( nextVertex, target, direction, graph1, graph2, edgeTypes );
      if ( direction == Direction.OUT ) {
        traceVertices( nextVertex, target, Direction.IN, graph1, graph2, DictionaryHelper.STRUCTURAL_LINK_TYPES );
      }
    }
  }

  /**
   * Adds localized types and categories, add node color information
   *
   * @param g The graph to enhance
   * @return The enhanced graph
   */
  protected Graph enhanceGraph( Graph g ) {

    // TODO should we clone the graph?
    Iterator<Vertex> vertices = g.getVertices().iterator();
    // enhance the vertixes
    while ( vertices.hasNext() ) {
      Vertex vertex = vertices.next();
      MetaverseUtil.enhanceVertex( vertex );
    }
    Iterator<Edge> edges = g.getEdges().iterator();
    // enhance the vertixes
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      MetaverseUtil.enhanceEdge( edge );
    }
    return g;
  }


}
