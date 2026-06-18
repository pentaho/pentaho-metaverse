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

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SynchronizedGraphTest {

  private SynchronizedGraph synchronizedGraph;

  @Before
  public void setUp() {
    synchronizedGraph = new SynchronizedGraph( TinkerGraph.open() );
  }

  @Test
  public void testAddVertexWithExistingId() {
    Vertex original = synchronizedGraph.addVertexWithId( "id" );
    Vertex duplicate = synchronizedGraph.addVertexWithId( "id" );

    assertSame( original, duplicate );
    assertEquals( 1, countVertices( synchronizedGraph.vertices() ) );
  }

  @Test
  public void testAddVertexWithNewId() {
    Vertex vertex = synchronizedGraph.addVertexWithId( "id" );
    assertNotNull( vertex );
    assertEquals( "id", vertex.id() );
  }

  @Test
  public void testGraphDelegates() throws Exception {
    Vertex source = synchronizedGraph.addVertex( T.id, "source" );
    Vertex target = synchronizedGraph.addVertex( T.id, "target" );
    Edge edge = source.addEdge( "self link", target, T.id, "edge-id" );

    assertSame( source, synchronizedGraph.getVertex( "source" ) );
    assertSame( edge, synchronizedGraph.getEdge( "edge-id" ) );
    assertEquals( 2, countVertices( synchronizedGraph.vertices() ) );
    assertEquals( 1, countEdges( synchronizedGraph.edges() ) );
    assertEquals( 1, countEdges( synchronizedGraph.edges( "edge-id" ) ) );
    assertNotNull( synchronizedGraph.variables() );
    assertNotNull( synchronizedGraph.configuration() );
    assertTrue( synchronizedGraph.getGraph() instanceof TinkerGraph );

    synchronizedGraph.close();
  }

  private int countVertices( Iterator<Vertex> vertices ) {
    int count = 0;
    while ( vertices.hasNext() ) {
      count++;
      vertices.next();
    }
    return count;
  }

  private int countEdges( Iterator<Edge> edges ) {
    int count = 0;
    while ( edges.hasNext() ) {
      count++;
      edges.next();
    }
    return count;
  }
}
