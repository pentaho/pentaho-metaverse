/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class SynchronizedGraphTest {

  private SynchronizedGraph synchronizedGraph;

  @Mock IdGraph mockGraph;
  @Mock Vertex mockVertex;
  @Mock Edge mockEdge;

  @Before
  public void setUp() throws Exception {
    synchronizedGraph = new SynchronizedGraph( mockGraph );
  }

  @Test
  public void testAddVertex_nullId() throws Exception {
    synchronizedGraph.addVertex( null );
    verify( mockGraph, times( 1 ) ).addVertex( null );
  }

  @Test
  public void testAddVertex_withExistingId() throws Exception {
    when( mockGraph.getVertex( "id" ) ).thenReturn( mockVertex );
    synchronizedGraph.addVertex( "id" );
    verify( mockGraph, times( 1 ) ).getVertex( "id" );
    verify( mockGraph, never() ).addVertex( anyString() );
  }

  @Test
  public void testAddVertex_withNewId() throws Exception {
    when( mockGraph.getVertex( "id" ) ).thenReturn( null );
    synchronizedGraph.addVertex( "id" );
    verify( mockGraph, times( 1 ) ).getVertex( "id" );
    verify( mockGraph, times( 1 ) ).addVertex( "id" );
  }

  @Test
  public void testAddEdge_nullId() throws Exception {
    synchronizedGraph.addEdge( null, mockVertex, mockVertex, "self link" );
    verify( mockGraph, times( 1 ) ).addEdge( null, mockVertex, mockVertex, "self link" );
  }

  @Test
  public void testAddEdge_withExistingId() throws Exception {
    when( mockGraph.getEdge( "id" ) ).thenReturn( mockEdge );
    synchronizedGraph.addEdge( "id", mockVertex, mockVertex, "self link" );
    verify( mockGraph, times( 1 ) ).getEdge( "id" );
    verify( mockGraph, never() ).addEdge( anyString(), eq(mockVertex), eq(mockVertex), anyString() );
  }

  @Test
  public void testAddEdge_withNewId() throws Exception {
    when( mockGraph.getEdge( "id" ) ).thenReturn( null );
    synchronizedGraph.addEdge( "id", mockVertex, mockVertex, "self link" );
    verify( mockGraph, times( 1 ) ).getEdge( "id" );
    verify( mockGraph, times( 1 ) ).addEdge( "id", mockVertex, mockVertex, "self link" );
  }

  @Test
  public void testDelegateMethodsAreCalled() throws Exception {
    synchronizedGraph.removeVertex( mockVertex );
    verify( mockGraph, times( 1 ) ).removeVertex( mockVertex );

    synchronizedGraph.removeEdge( mockEdge );
    verify( mockGraph, times( 1 ) ).removeEdge( mockEdge );

    synchronizedGraph.getEdge( "id" );
    verify( mockGraph, times( 1 ) ).getEdge( "id" );

    synchronizedGraph.getEdges();
    verify( mockGraph, times( 1 ) ).getEdges();

    synchronizedGraph.getEdges( "key", "value" );
    verify( mockGraph, times( 1 ) ).getEdges( "key", "value" );

    synchronizedGraph.getFeatures();
    verify( mockGraph, times( 1 ) ).getFeatures();

    synchronizedGraph.getVertex( "id" );
    verify( mockGraph, times( 1 ) ).getVertex( "id" );

    synchronizedGraph.getVertices();
    verify( mockGraph, times( 1 ) ).getVertices();

    synchronizedGraph.getVertices( "key", "value" );
    verify( mockGraph, times( 1 ) ).getVertices( "key", "value" );

    synchronizedGraph.query();
    verify( mockGraph, times( 1 ) ).query();

    synchronizedGraph.shutdown();
    verify( mockGraph, times( 1 ) ).shutdown();
  }
}
