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
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GraphPathTest {

  @Test
  public void testAddToGraphClonesVerticesAndEdges() {
    Graph source = new TinkerGraph();
    Vertex v1 = source.addVertex( "v1" );
    v1.setProperty( "name", "start" );
    Vertex v2 = source.addVertex( "v2" );
    v2.setProperty( "name", "end" );
    Edge e1 = source.addEdge( "e1", v1, v2, "links" );

    GraphPath path = new GraphPath();
    path.addVertex( v1 );
    path.addEdge( e1 );
    path.addVertex( v2 );

    Graph target = new TinkerGraph();
    path.addToGraph( target );

    Vertex clonedV1 = target.getVertex( "v1" );
    Vertex clonedV2 = target.getVertex( "v2" );
    Edge clonedE1 = target.getEdge( "e1" );

    assertNotNull( clonedV1 );
    assertNotNull( clonedV2 );
    assertNotNull( clonedE1 );
    assertEquals( "start", clonedV1.getProperty( "name" ) );
    assertEquals( "end", clonedV2.getProperty( "name" ) );
    assertEquals( "links", clonedE1.getLabel() );
    assertEquals( 2, countVertices( target ) );
    assertEquals( 1, countEdges( target ) );
  }

  private int countVertices( Graph graph ) {
    int count = 0;
    for ( Vertex ignored : graph.getVertices() ) {
      count++;
    }
    return count;
  }

  private int countEdges( Graph graph ) {
    int count = 0;
    for ( Edge ignored : graph.getEdges() ) {
      count++;
    }
    return count;
  }
}
