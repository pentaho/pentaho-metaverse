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

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.Set;

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
    Iterator<Vertex> existing = g.vertices( vertex.id() );
    if ( existing.hasNext() ) {
      return existing.next();
    }
    Vertex clone = g.addVertex( T.id, vertex.id() );
    Set<String> keys = vertex.keys();
    for ( String key : keys ) {
      Object value = vertex.property( key ).isPresent() ? vertex.value( key ) : null;
      if ( value != null ) {
        clone.property( key, value );
      }
    }
    return clone;
  }

}
