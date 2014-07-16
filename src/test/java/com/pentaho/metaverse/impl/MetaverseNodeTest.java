/*!
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

package com.pentaho.metaverse.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.tinkerpop.blueprints.Direction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import com.tinkerpop.blueprints.Vertex;

/**
 * @author mburgess
 * 
 */
public class MetaverseNodeTest {

  MetaverseNode node;

  @Mock
  Vertex v;

  @Mock
  Vertex v2;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    v = (Vertex) mock( Vertex.class );
    when( v.getId() ).thenReturn( "my.id" );
    node = new MetaverseNode( v );
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetName() {
    assertNull( node.getName() );
    verify( v, times( 1 ) ).getProperty( "name" );
  }

  @Test
  public void testSetName() {
    MetaverseNode myNode = new MetaverseNode( v );
    when( v.getProperty( "name" ) ).thenReturn( "myName" );
    myNode.setName( "myName" );
    assertEquals( myNode.getName(), "myName" );
    verify( v, times( 1 ) ).getProperty( "name" );
  }

  @Test
  public void testGetType() {
    assertNull( node.getType() );
    verify( v ).getProperty( "type" );
  }

  @Test
  public void testSetType() {
    MetaverseNode myNode = new MetaverseNode( v );
    when( v.getProperty( "type" ) ).thenReturn( "myType" );
    myNode.setType( "myType" );
    assertEquals( myNode.getType(), "myType" );
    verify( v, times( 1 ) ).getProperty( "type" );
  }

  @Test
  public void testGetStringID() {
    when( v.getId() ).thenReturn( "my.id" );
    assertNotNull( node.getStringID() );
    verify( v, atLeastOnce() ).getId();
  }

  @Test
  public void testGetID() {
    when( v.getId() ).thenReturn( "my.id" );
    assertNotNull( node.getId() );
    verify( v, atLeastOnce() ).getId();
  }

  @Test
  public void testGetStringID_null() {
    when( v.getId() ).thenReturn( null );
    assertNull( node.getStringID() );
    verify( v ).getId();
  }

  @Test
  public void testSetProperty() {
    // verify delegate
    node.setProperty( "test", "test" );
    verify( v ).setProperty( "test", "test" );
  }

  @Test
  public void testGetPropertyKeys() {
    // verify delegate
    when( v.getPropertyKeys() ).thenReturn( null );
    node.getPropertyKeys();

    verify( v ).getPropertyKeys();
  }

  @Test
  public void testRemoveProperty() {
    // verify delegate
    when( v.removeProperty( anyString() ) ).thenReturn( null );
    node.removeProperty( "test" );

    verify( v ).removeProperty( "test" );
  }

  @Test
  public void testAddEdge() {
    // verify delegate
    when( v.addEdge( anyString(), any( Vertex.class ) ) ).thenReturn( null );
    node.addEdge( "uses", v2 );

    verify( v ).addEdge( "uses", v2 );
  }

  @Test
  public void testGetEdges() {
    // verify delegate
    when( v.getEdges( any( Direction.class ) ) ).thenReturn( null );
    node.getEdges( Direction.BOTH );

    verify( v ).getEdges( Direction.BOTH );
  }

  @Test
  public void testGetVertices() {
    // verify delegate
    when( v.getVertices( any( Direction.class ), anyString() ) ).thenReturn( null );
    node.getVertices( Direction.BOTH, "uses" );

    verify( v ).getVertices( Direction.BOTH, "uses" );
  }

  @Test
  public void testQuery() {
    // verify delegate
    when( v.query() ).thenReturn( null );
    node.query();

    verify( v ).query();
  }

  @Test
  public void testRemove() {
    // verify delegate
    node.remove();
    verify( v ).remove();
  }

  @Test
  public void testGetProperty() {
    // verify delegate
    when( v.getProperty( anyString() ) ).thenReturn( null );
    node.getProperty( "test" );
    verify( v ).getProperty( "test" );
  }

  @Test
  public void testVertexDelegateCalls() {
    // just test that we are delegating our calls to the underlying vertex (pass-through, called once)
    when( v.getEdges( any( Direction.class ) ) ).thenReturn( null );
    when( v.addEdge( anyString(), any( Vertex.class ) ) ).thenReturn( null );
    when( v.getVertices( any( Direction.class ), anyString() ) ).thenReturn( null );
    when( v.query() ).thenReturn( null );
    when( v.getPropertyKeys() ).thenReturn( null );
    when( v.getProperty( anyString() ) ).thenReturn( null );

    node.getStringID();
  }

}
