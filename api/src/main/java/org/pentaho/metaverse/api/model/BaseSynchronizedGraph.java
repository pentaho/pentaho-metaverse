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


package org.pentaho.metaverse.api.model;


import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

/**
 * A Graph that provides thread-safe modification
 */
public class BaseSynchronizedGraph implements Graph {

  /**
   * The underlying graph
   */
  protected final IdGraph<KeyIndexableGraph> graph;

  /**
   * Creates a new synchronized graph
   *
   * @param graph The underlying graph
   */
  public BaseSynchronizedGraph( IdGraph<KeyIndexableGraph> graph ) {
    this.graph = graph;
  }

  @Override
  public Features getFeatures() {
    return graph.getFeatures();
  }

  @Override
  public Vertex addVertex( Object id ) {
    Vertex vertex;
    synchronized ( graph ) {
      if ( id == null ) {
        vertex = graph.addVertex( id );
      } else {
        vertex = getVertex( id );
        if ( vertex == null ) {
          vertex = graph.addVertex( id );
        }
      }
      return vertex;
    }
  }

  @Override
  public Vertex getVertex( Object id ) {
    return graph.getVertex( id );
  }

  @Override
  public void removeVertex( Vertex vertex ) {
    synchronized ( graph ) {
      graph.removeVertex( vertex );
    }
  }

  @Override
  public Iterable<Vertex> getVertices() {
    return graph.getVertices();
  }

  @Override
  public Iterable<Vertex> getVertices( String key, Object value ) {
    synchronized ( graph ) {
      return graph.getVertices( key, value );
    }
  }

  @Override
  public Edge addEdge( Object id, Vertex outVertex, Vertex inVertex, String label ) {
    Edge edge;
    synchronized ( graph ) {
      if ( id == null ) {
        edge = graph.addEdge( id, outVertex, inVertex, label );
      } else {
        edge = getEdge( id );
        if ( edge == null ) {
          edge = graph.addEdge( id, outVertex, inVertex, label );
        }
      }
      return edge;
    }
  }

  @Override
  public Edge getEdge( Object id ) {
    return graph.getEdge( id );
  }

  @Override
  public void removeEdge( Edge edge ) {
    synchronized ( graph ) {
      graph.removeEdge( edge );
    }
  }

  @Override
  public Iterable<Edge> getEdges() {
    return graph.getEdges();
  }

  @Override
  public Iterable<Edge> getEdges( String key, Object value ) {
    return graph.getEdges( key, value );
  }

  @Override
  public GraphQuery query() {
    return graph.query();
  }

  @Override
  public void shutdown() {
    synchronized ( graph ) {
      graph.shutdown();
    }
  }

  public Graph getGraph() {
    return this.graph;
  }
}
