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

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GraphUtilTest {

  @Test( expected = UnsupportedOperationException.class )
  public void testProtectedConstructor() {
    new GraphUtil();
  }

  @Test
  public void testCloneVertexIntoGraph() throws Exception {
    Graph source = new TinkerGraph();
    Vertex original = source.addVertex( "v1" );
    original.setProperty( "name", "node-1" );
    original.setProperty( "count", 2 );

    Graph target = new TinkerGraph();
    Vertex clone = GraphUtil.cloneVertexIntoGraph( original, target );

    assertNotNull( clone );
    assertEquals( original.getId(), clone.getId() );
    assertEquals( original.getProperty( "name" ), clone.getProperty( "name" ) );
    assertEquals( original.getProperty( "count" ), clone.getProperty( "count" ) );
    assertEquals( 1, countVertices( target ) );
  }

  @Test
  public void testCloneVertexIntoGraphReturnsExistingVertex() throws Exception {
    Graph source = new TinkerGraph();
    Vertex original = source.addVertex( "v1" );
    original.setProperty( "name", "source-name" );

    Graph target = new TinkerGraph();
    Vertex existing = target.addVertex( "v1" );
    existing.setProperty( "name", "existing-name" );

    Vertex clone = GraphUtil.cloneVertexIntoGraph( original, target );

    assertEquals( existing.getId(), clone.getId() );
    assertEquals( "existing-name", clone.getProperty( "name" ) );
    assertEquals( 1, countVertices( target ) );
  }

  private int countVertices( Graph graph ) {
    int count = 0;
    for ( Vertex ignored : graph.getVertices() ) {
      count++;
    }
    return count;
  }
}
