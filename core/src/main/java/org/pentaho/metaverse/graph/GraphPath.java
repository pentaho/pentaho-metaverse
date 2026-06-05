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

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.pentaho.metaverse.graph.adapter.BlueprintsAdapters;
import org.pentaho.metaverse.graph.adapter.DirectionAdapter;
import org.pentaho.metaverse.graph.adapter.EdgeAdapter;
import org.pentaho.metaverse.graph.adapter.GraphAdapter;
import org.pentaho.metaverse.graph.adapter.VertexAdapter;

import java.util.ArrayList;
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
    GraphAdapter graph = BlueprintsAdapters.wrap( g );

    for ( Object item : path ) {
      VertexAdapter vertex = toVertexAdapter( item );
      if ( vertex != null ) {
        VertexAdapter v = graph.getVertex( vertex.getId() );
        if ( v == null ) {
          v = GraphUtil.cloneVertexIntoGraph( vertex, graph );
        }
      } else {
        EdgeAdapter edge = toEdgeAdapter( item );
        if ( edge == null ) {
          continue;
        }
        EdgeAdapter e = graph.getEdge( edge.getId() );
        if ( e == null ) {
          VertexAdapter v1 = graph.getVertex( edge.getVertex( DirectionAdapter.OUT ).getId() );
          if ( v1 == null ) {
            v1 = GraphUtil.cloneVertexIntoGraph( edge.getVertex( DirectionAdapter.OUT ), graph );
          }
          VertexAdapter v2 = graph.getVertex( edge.getVertex( DirectionAdapter.IN ).getId() );
          if ( v2 == null ) {
            v2 = GraphUtil.cloneVertexIntoGraph( edge.getVertex( DirectionAdapter.IN ), graph );
          }
          graph.addEdge( edge.getId(), v1, v2, edge.getLabel() );
        }
      }

    }

  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    for ( Object obj : path ) {
      if ( obj instanceof Vertex || obj instanceof VertexAdapter ) {
        VertexAdapter vertex = toVertexAdapter( obj );
        if ( str.length() > 0 ) {
          str.append( "->" );
        }
        str.append( vertex.getId() );
      }
    }
    return str.toString();
  }

  private VertexAdapter toVertexAdapter( Object item ) {
    if ( item instanceof VertexAdapter ) {
      return (VertexAdapter) item;
    }
    if ( item instanceof Vertex ) {
      return BlueprintsAdapters.wrap( (Vertex) item );
    }
    return null;
  }

  private EdgeAdapter toEdgeAdapter( Object item ) {
    if ( item instanceof EdgeAdapter ) {
      return (EdgeAdapter) item;
    }
    if ( item instanceof Edge ) {
      return BlueprintsAdapters.wrap( (Edge) item );
    }
    return null;
  }

}
