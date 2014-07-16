package com.pentaho.metaverse.graph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.engine.core.system.PentahoBase;

import com.pentaho.metaverse.api.GraphConst;
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
public abstract class BaseGraphMetaverseReader extends PentahoBase implements IMetaverseReader {

  private static final long serialVersionUID = -3813738340722424284L;

  /**
   * Subclasses must implement this method and return the instance of Graph that the system
   * should use.
   * @return The Blueprint graph
   */
  protected abstract Graph getGraph();

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
    // TODO Auto-generated method stub
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
      error( "Could not export the metaverse graph", e );
    }
    return null;
  }

  @Override
  public Graph search( List<String> resultTypes, List<String> startNodeIDs ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Graph getGraph( String id ) {
    Vertex root = getGraph().getVertex( id );
    if ( root == null ) {
      return null;
    }
    Graph g = new TinkerGraph();
    // find the upstream nodes
    Vertex clone = cloneVertexIntoGraph( root, g );
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
    // first clo
    Direction opDirection = direction == Direction.IN ? Direction.OUT : Direction.IN;
    Iterator<Edge> edges = vertex.getEdges( direction ).iterator();
    while ( edges.hasNext() ) {
      Edge edge = edges.next();
      if ( edgeTypes != null && !edgeTypes.contains( edge.getLabel() ) ) {
        continue;
      }
      Vertex nextVertex = edge.getVertex( opDirection );
      Vertex target = cloneVertexIntoGraph( nextVertex, graph2 );
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
        traceVertices( nextVertex, target, Direction.IN, graph1, graph2, GraphConst.STRUCTURAL_LINK_MAP );
      }
    }
  }

  /**
   * Clones a provided vertex into a new graph. The graph should not be the graph that the
   * provided vertex belongs to.
   * @param vertex The vertex to clone
   * @param g The graph to clone the vertex into.
   * @return The vertex in the sub-graph
   */
  private Vertex cloneVertexIntoGraph( Vertex vertex, Graph g ) {
    Vertex clone = g.getVertex( vertex.getId() );
    if ( clone != null ) {
      return clone;
    }
    clone = g.addVertex( vertex.getId() );
    Set<String> keys = vertex.getPropertyKeys();
    for ( String key : keys ) {
      String value = vertex.getProperty( key );
      clone.setProperty( key, value );
    }
    return clone;
  }

}
