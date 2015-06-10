/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

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

    for ( Object item : path ) {
      if ( item instanceof Vertex ) {
        Vertex vertex = (Vertex) item;
        Vertex v = g.getVertex( vertex.getId() );
        if ( v == null ) {
          v = GraphUtil.cloneVertexIntoGraph( vertex, g );
        }
      } else if ( item instanceof Edge ) {
        Edge edge = (Edge) item;
        Edge e = g.getEdge( edge.getId() );
        if ( e == null ) {
          Vertex v1 = g.getVertex( edge.getVertex( Direction.OUT ) );
          if ( v1 == null ) {
            v1 = GraphUtil.cloneVertexIntoGraph( edge.getVertex( Direction.OUT ), g );
          }
          Vertex v2 = g.getVertex( edge.getVertex( Direction.IN ) );
          if ( v2 == null ) {
            v2 = GraphUtil.cloneVertexIntoGraph( edge.getVertex( Direction.IN ), g );
          }
          e = g.addEdge( edge.getId(), v1, v2, edge.getLabel() );
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
        str.append( ( (Vertex) obj ).getId() );
      }
    }
    return str.toString();
  }

}
