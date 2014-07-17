package com.pentaho.metaverse.graph;

import java.util.ArrayList;
import java.util.List;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class GraphPath {

  private List<Object> path = new ArrayList<Object>();

  public void addVertex( Vertex vertex ) {
    path.add( vertex );
  }

  public void addEdge( Edge edge ) {
    path.add( edge );
  }

  public void pop() {
    path.remove( path.size() - 1 );
  }

  public int getLength() {
    return path.size();
  }

  public GraphPath clone() {
    GraphPath clone = new GraphPath();
    for ( Object o : path ) {
      clone.add( o );
    }
    return clone;
  }

  /**
   * A protected method using in shallow cloning
   * @param o
   */
  protected void add( Object o ) {
    path.add( o );
  }

  public void addToGraph( Graph g ) {

    for ( Object item : path ) {
      if ( item instanceof Vertex ) {
        Vertex vertex = (Vertex) item;
        Vertex v = g.getVertex( vertex.getId() );
        if ( v == null ) {
          v = g.addVertex( vertex.getId() );
          // clone this
        }
      } else if ( item instanceof Edge ) {
        Edge edge = (Edge) item;
        Edge e = g.getEdge( edge.getId() );
        if ( e == null ) {
          Vertex v1 = g.getVertex( edge.getVertex( Direction.IN ) );
          if ( v1 == null ) {
            v1 = GraphUtil.cloneVertexIntoGraph( edge.getVertex( Direction.OUT ), g );
          }
          Vertex v2 = g.getVertex( edge.getVertex( Direction.OUT ) );
          if ( v2 == null ) {
            if ( v2 == null ) {
              v2 = GraphUtil.cloneVertexIntoGraph( edge.getVertex( Direction.IN ), g );
            }
          }
          e = g.addEdge( edge.getId(), v1, v2, edge.getLabel() );
        }
      }

    }

  }

}
