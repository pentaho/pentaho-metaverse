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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A path of vertex of edges. This is used by the MetaverseReader to answer searches by external callers.
 * @author jdixon
 *
 */
public class GraphPath {

  private List<Object> path = new ArrayList<Object>();

  /**
   * Adds a vertex to the path.
   * 
   * @param vertex The vertex to add
   */
  public void addVertex( Vertex vertex ) {
    path.add( vertex );
  }

  /**
   * Adds a edge to the path.
   * 
   * @param edge The edge to add.
   */
  public void addEdge( Edge edge ) {
    path.add( edge );
  }

  /**
   * Removes a vertex or an edge from the end of the path.
   * 
   * @return The vertex or edge removed
   */
  public Object pop() {
    Object obj = path.remove( path.size() - 1 );
    return obj;
  }

  public int getLength() {
    return path.size();
  }

  /**
   * Returns a clone of this GraphPath
   * 
   * @return The cloned path.
   */
  public GraphPath clone() {
    GraphPath clone = new GraphPath();
    for ( Object o : path ) {
      clone.add( o );
    }
    return clone;
  }

  /**
   * A protected method using in shallow cloning.
   * 
   * @param o The object to add
   */
  protected void add( Object o ) {
    path.add( o );
  }

  /**
   * Adds this path to a graph.
   * 
   * @param g The graph to add this path to.
   */
  public void addToGraph( Graph g ) {

    for ( Object item : path ) {
      if ( item instanceof Vertex ) {
        Vertex vertex = (Vertex) item;
        Iterator<Vertex> existing = g.vertices( vertex.id() );
        if ( !existing.hasNext() ) {
          GraphUtil.cloneVertexIntoGraph( vertex, g );
        }
      } else if ( item instanceof Edge ) {
        Edge edge = (Edge) item;
        Iterator<Edge> existingEdge = g.edges( edge.id() );
        if ( !existingEdge.hasNext() ) {
          Vertex outV = edge.outVertex();
          Vertex inV = edge.inVertex();
          Iterator<Vertex> v1it = g.vertices( outV.id() );
          Vertex v1 = v1it.hasNext() ? v1it.next() : GraphUtil.cloneVertexIntoGraph( outV, g );
          Iterator<Vertex> v2it = g.vertices( inV.id() );
          Vertex v2 = v2it.hasNext() ? v2it.next() : GraphUtil.cloneVertexIntoGraph( inV, g );
          v1.addEdge( edge.label(), v2, T.id, edge.id() );
        }
      }

    }

  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    for ( Object obj : path ) {
      if ( obj instanceof Vertex ) {
        if ( str.length() > 0 ) {
          str.append( "->" );
        }
        str.append( ( (Vertex) obj ).id() );
      }
    }
    return str.toString();
  }

}
