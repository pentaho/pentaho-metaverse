package com.pentaho.metaverse.graph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pentaho.metaverse.messages.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.engine.core.system.PentahoBase;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;
import com.pentaho.dictionary.MetaverseLink;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.impl.MetaverseNode;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * An implementation of an IMetaverseReader that uses a Blueprints graph as the underlying storage
 * @author jdixon
 *
 */
public class BlueprintsGraphMetaverseReader extends PentahoBase implements IMetaverseReader {

  private static final long serialVersionUID = -3813738340722424284L;
  private static final Log LOGGER = LogFactory.getLog( BlueprintsGraphMetaverseReader.class );

  private Graph graph;

  /**
   * Constructor that accepts a Graph
   * @param graph the Graph to read from
   */
  public BlueprintsGraphMetaverseReader( Graph graph ) {
    this.graph = graph;
  }

  /**
   * Gets the complete, underlying graph
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
    MetaverseNode node = new MetaverseNode( vertex );
    return node;
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
          link.setLabel( edge.getLabel() );
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
    return getGraph();
  }

  @Override
  public String export() {
    Graph graph = getGraph();
    // convert the graph to an export format, GraphML for now

    OutputStream out = new ByteArrayOutputStream();
    try {
      GraphMLWriter writer = new GraphMLWriter();
      writer.outputGraph( graph, out );
      out.close();
      return out.toString();
    } catch ( IOException e ) {
      error( Messages.getString( "ERROR.Graph.Export" ), e );
    }
    return null;
  }

  @Override
  public Graph search( List<String> resultTypes, List<String> startNodeIDs ) {

    Graph g = new TinkerGraph();

    for ( String startNodeID : startNodeIDs ) {
      Graph subGraph = getGraph( startNodeID );
      if ( subGraph != null ) {
        // traverse look for paths to the results
        Vertex startVertex = subGraph.getVertex( startNodeID );
        GraphPath path = new GraphPath();
        Set<Object> done = new HashSet<Object>();
        Map<Object, GraphPath> shortestPaths = new HashMap<Object, GraphPath>();
        traverseGraph( startVertex, subGraph, resultTypes, path, done, shortestPaths );

        Iterator<Map.Entry<Object, GraphPath>> paths = shortestPaths.entrySet().iterator();
        while ( paths.hasNext() ) {
          Map.Entry<Object, GraphPath> entry = paths.next();
          GraphPath shortPath = entry.getValue();
          shortPath.addToGraph( g );
        }

      }
    }
    return g;
  }

  private void traverseGraph( Vertex startVertex, Graph subGraph, List<String> resultTypes, GraphPath path,
      Set<Object> done, Map<Object, GraphPath> shortestPaths ) {
    boolean isTargetType = resultTypes.contains( startVertex.getProperty( DictionaryConst.PROPERTY_TYPE ) );
    if ( !isTargetType && done.contains( startVertex.getId() ) ) {
      return;
    }
    path.addVertex( startVertex );
    if ( isTargetType ) {
      // this is one of our target types
      GraphPath shortestPath = shortestPaths.get( startVertex.getId() );
      if ( shortestPath == null || path.getLength() < shortestPath.getLength()  ) {
        shortestPaths.put( startVertex.getId(), path.clone() );
      }
      if ( done.contains( startVertex.getId() ) ) {
        path.pop();
        return;
      }
    }
    done.add( startVertex.getId() );
    Iterator<Edge> edges = startVertex.getEdges( Direction.IN ).iterator();
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      if ( done.contains( edge.getId() ) ) {
        continue;
      }
      path.addEdge( edge );
      Vertex nextVertex = edge.getVertex( Direction.OUT );
      traverseGraph( nextVertex, subGraph, resultTypes, path, done, shortestPaths );
      path.pop();
    }
    edges = startVertex.getEdges( Direction.OUT ).iterator();
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      if ( done.contains( edge.getId() ) ) {
        continue;
      }
      path.addEdge( edge );
      Vertex nextVertex = edge.getVertex( Direction.IN );
      traverseGraph( nextVertex, subGraph, resultTypes, path, done, shortestPaths );
      path.pop();
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
    return g;
  }

  /**
   * Traces all of the 
   * @param vertex The source vertex to traverse from
   * @param clone The clone of the source vertex in the sub-graph
   * @param direction The direction to traverse the graph in
   * @param graph1 The source graph
   * @param graph2 The sub-graph being built
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

  @Override public Log getLogger() {
    return LOGGER;
  }
}
