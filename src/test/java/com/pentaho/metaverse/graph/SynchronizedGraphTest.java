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
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
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
