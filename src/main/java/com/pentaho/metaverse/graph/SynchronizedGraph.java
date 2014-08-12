/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

/**
 * A Graph that provides thread-safe modification
 */
public class SynchronizedGraph implements Graph {

  protected final IdGraph graph;

  public SynchronizedGraph( IdGraph graph ) {
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
    return graph.getVertices( key, value );
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
}
