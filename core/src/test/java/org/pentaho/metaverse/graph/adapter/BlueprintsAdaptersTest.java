package org.pentaho.metaverse.graph.adapter;

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

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class BlueprintsAdaptersTest {

  @Test
  public void testConstructor() throws Exception {
    java.lang.reflect.Constructor<BlueprintsAdapters> constructor =
      BlueprintsAdapters.class.getDeclaredConstructor();
    constructor.setAccessible( true );

    try {
      constructor.newInstance();
      Assert.fail( "Expected UnsupportedOperationException" );
    } catch ( java.lang.reflect.InvocationTargetException e ) {
      Assert.assertTrue( e.getCause() instanceof UnsupportedOperationException );
    }
  }

  @Test
  public void testWrapGraphAndAddVertexAndEdge() {
    TinkerGraph graph = new TinkerGraph();
    GraphAdapter adapter = BlueprintsAdapters.wrap( graph );

    VertexAdapter v1 = adapter.addVertex( "v1" );
    v1.setProperty( "name", "source" );
    VertexAdapter v2 = adapter.addVertex( "v2" );
    EdgeAdapter edge = adapter.addEdge( "e1", v1, v2, "links" );

    assertNotNull( adapter.getVertex( "v1" ) );
    assertNotNull( adapter.getVertex( "v2" ) );
    assertNotNull( adapter.getEdge( "e1" ) );
    assertEquals( "source", adapter.getVertex( "v1" ).getProperty( "name" ) );
    assertEquals( "links", edge.getLabel() );
    assertEquals( "v1", edge.getVertex( DirectionAdapter.OUT ).getId() );
    assertEquals( "v2", edge.getVertex( DirectionAdapter.IN ).getId() );
  }

  @Test
  public void testWrapAndUnwrapVertexAndEdge() {
    TinkerGraph graph = new TinkerGraph();
    Vertex vertex = graph.addVertex( "v1" );
    Vertex other = graph.addVertex( "v2" );
    Edge edge = graph.addEdge( "e1", vertex, other, "links" );

    VertexAdapter wrappedVertex = BlueprintsAdapters.wrap( vertex );
    EdgeAdapter wrappedEdge = BlueprintsAdapters.wrap( edge );

    assertSame( vertex, BlueprintsAdapters.unwrap( wrappedVertex ) );
    assertSame( edge, BlueprintsAdapters.unwrap( wrappedEdge ) );
    assertEquals( "v1", wrappedVertex.getId() );
    assertEquals( "links", wrappedEdge.getLabel() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testUnwrapUnsupportedVertexAdapterThrows() {
    BlueprintsAdapters.unwrap( new VertexAdapter() {
      @Override public Object getId() { return "v1"; }
      @Override public <T> T getProperty( String key ) { return null; }
      @Override public void setProperty( String key, Object value ) { }
      @Override public java.util.Set<String> getPropertyKeys() { return java.util.Collections.emptySet(); }
      @Override public Iterable<EdgeAdapter> getEdges( DirectionAdapter direction, String... labels ) {
        return java.util.Collections.emptyList();
      }
      @Override public void remove() { }
    } );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testUnwrapUnsupportedEdgeAdapterThrows() {
    BlueprintsAdapters.unwrap( new EdgeAdapter() {
      @Override public Object getId() { return "e1"; }
      @Override public String getLabel() { return "links"; }
      @Override public VertexAdapter getVertex( DirectionAdapter direction ) { return null; }
      @Override public void setProperty( String key, Object value ) { }
      @Override public void remove() { }
    } );
  }
}
