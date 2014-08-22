package com.pentaho.metaverse.graph;

import java.util.Set;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * A utility class for graphs
 * @author jdixon
 *
 */
public class GraphUtil {

  /**
   * Hides the constructor so that this class cannot be instanced
   */
  protected GraphUtil() {
    throw new UnsupportedOperationException();
  }

  /**
   * Clones a provided vertex into a new graph. The graph should not be the graph that the
   * provided vertex belongs to.
   * @param vertex The vertex to clone
   * @param g The graph to clone the vertex into.
   * @return The vertex in the sub-graph
   */
  public static Vertex cloneVertexIntoGraph( Vertex vertex, Graph g ) {
    Vertex clone = g.getVertex( vertex.getId() );
    if ( clone != null ) {
      return clone;
    }
    clone = g.addVertex( vertex.getId() );
    Set<String> keys = vertex.getPropertyKeys();
    for ( String key : keys ) {
      Object value = vertex.getProperty( key );
      clone.setProperty( key, value );
    }
    return clone;
  }

}
