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

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.Iterator;

/**
 * A Graph that provides thread-safe modification
 */
public class BaseSynchronizedGraph implements Graph {

  /**
   * The underlying graph
   */
  protected final TinkerGraph graph;

  /**
   * Creates a new synchronized graph
   *
   * @param graph The underlying graph
   */
  public BaseSynchronizedGraph( TinkerGraph graph ) {
    this.graph = graph;
  }

  /**
   * Get-or-create semantics: returns existing vertex with given id, or creates a new one.
   *
   * @param id the vertex id
   * @return the vertex
   */
  public Vertex addVertexWithId( Object id ) {
    synchronized ( graph ) {
      Vertex v = getVertex( id );
      if ( v == null ) {
        v = graph.addVertex( T.id, id );
      }
      return v;
    }
  }

  @Override
  public Vertex addVertex( Object... keyValues ) {
    synchronized ( graph ) {
      return graph.addVertex( keyValues );
    }
  }

  /**
   * Helper for backward compatibility: look up a vertex by id.
   *
   * @param id the vertex id
   * @return the vertex, or null if not found
   */
  public Vertex getVertex( Object id ) {
    Iterator<Vertex> it = graph.vertices( id );
    return it.hasNext() ? it.next() : null;
  }

  /**
   * Helper for backward compatibility: look up an edge by id.
   *
   * @param id the edge id
   * @return the edge, or null if not found
   */
  public Edge getEdge( Object id ) {
    Iterator<Edge> it = graph.edges( id );
    return it.hasNext() ? it.next() : null;
  }

  @Override
  public Iterator<Vertex> vertices( Object... vertexIds ) {
    return graph.vertices( vertexIds );
  }

  @Override
  public Iterator<Edge> edges( Object... edgeIds ) {
    return graph.edges( edgeIds );
  }

  @Override
  public <C extends GraphComputer> C compute( Class<C> graphComputerClass ) throws IllegalArgumentException {
    return graph.compute( graphComputerClass );
  }

  @Override
  public GraphComputer compute() throws IllegalArgumentException {
    return graph.compute();
  }

  @Override
  public Transaction tx() {
    return graph.tx();
  }

  @Override
  public void close() throws Exception {
    synchronized ( graph ) {
      graph.close();
    }
  }

  @Override
  public Variables variables() {
    return graph.variables();
  }

  @Override
  public Configuration configuration() {
    return graph.configuration();
  }

  public Graph getGraph() {
    return this.graph;
  }
}
