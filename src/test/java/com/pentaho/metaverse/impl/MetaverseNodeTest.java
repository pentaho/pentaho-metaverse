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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  Map<String, Object> vertexProps;

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

  @Test
  public void testGetProperties() throws Exception {
    Map<String, Object> props = new HashMap<String, Object>(){{
      put( "path", "/Users/home/admin" );
      put( "lastModified", new Date() );
    }};

    node.setProperties( props );
    when( v.getPropertyKeys() ).thenReturn( props.keySet() );
    Map<String, Object> resultProps = node.getProperties();
    assertEquals( props.size(), resultProps.size() );

    for( String key : props.keySet() ) {
      assertTrue( resultProps.containsKey( key ) );
    }
  }

  @Test
  public void testGetProperties_noProperyKeys() throws Exception {
    when( v.getPropertyKeys() ).thenReturn( null );
    Map<String, Object> resultProps = node.getProperties();
    assertEquals( 0, resultProps.size() );
  }

  @Test
  public void testGetProperties_withBaseVertex() throws Exception {

    Map<String, Object> props = new HashMap<String, Object>();

    vertexProps = new HashMap<String, Object>(){{
      put( "_NAME_", "name" );
    }};

    when( v.getPropertyKeys() ).thenReturn( vertexProps.keySet() );

    Map<String, Object> resultProps = node.getProperties();
    assertEquals( vertexProps.size(), resultProps.size() );

    for( String key : props.keySet() ) {
      assertEquals( props.get( key ), resultProps.get( key ) );
    }
  }

  @Test
  public void testRemoveProperties() throws Exception {
    Set<String> remove = new HashSet<String>() {{
      add( "name" );
      add( "age" );
    }};
    node.removeProperties( remove );
    verify( v ).removeProperty( eq( "name" ) );
    verify( v ).removeProperty( eq( "age" ) ) ;
  }

  @Test
  public void testremoveProperties_null() throws Exception {
    node.removeProperties( null );
    verify( v, never() ).removeProperty( anyString() );
  }

  @Test
  public void testClearProperties() throws Exception {
    Map<String, Object> props = new HashMap<String, Object>(){{
      put( "path", "/Users/home/admin" );
      put( "lastModified", new Date() );
    }};
    when( v.getPropertyKeys() ).thenReturn( props.keySet() );

    node.setProperties( props );
    node.clearProperties();

    for ( String key : props.keySet() ) {
      verify( v ).removeProperty( key );
    }
  }

  @Test
  public void testClearProperties_null() throws Exception {
    node.clearProperties();
    when( v.getPropertyKeys() ).thenReturn( null );
    verify( v, never() ).removeProperty( anyString() );
  }

  @Test
  public void testContainsKey() throws Exception {
    node.containsKey( "test" );
    verify( v ).getProperty( "test" );
  }

  @Test
  public void testSetProperties_null() throws Exception {
    node.setProperties( null );
    verify( v, never() ).setProperty( anyString(), any() );
  }

  @Test
  public void testSetProperties() throws Exception {
    Map<String, Object> props = new HashMap<String, Object>(){{
      put( "path", "/Users/home/admin" );
      put( "lastModified", new Date() );
    }};
    node.setProperties( props );
    verify( v, times( 1 ) ).setProperty( eq("path"), any() );
    verify( v, times( 1 ) ).setProperty( eq("lastModified"), any() );
  }

  @Test
  public void testSetLogicalIdPropertyKeys() throws Exception {
    MetaverseNode node = new MetaverseNode( v );
    when( v.getProperty( "name" ) ).thenReturn( "testName" );
    when( v.getProperty( "zzz" ) ).thenReturn( "last" );
    when( v.getProperty( "type" ) ).thenReturn( "testType" );

    MetaverseNode spyNode = spy( node );

    assertNull( spyNode.logicalIdPropertyKeys );
    spyNode.setLogicalIdPropertyKeys(  "type", "zzz", "name" );

    verify( spyNode ).generateLogicalId();
    verify( spyNode ).setProperty( eq( "logicalId" ), anyString() );

    // logical id should be sorted based on key
    assertEquals( "[name=testName][type=testType][zzz=last]", spyNode.getLogicalId() );
    assertNotNull( spyNode.logicalIdPropertyKeys );
    assertEquals( 3, spyNode.logicalIdPropertyKeys.size() );
  }

  @Test
  public void testGetLogicalId_noLogicalId() throws Exception {
    MetaverseNode node = new MetaverseNode( v );
    when( v.getProperty( "name" ) ).thenReturn( "testName" );
    when( v.getProperty( "type" ) ).thenReturn( "testType" );
    when( v.getId() ).thenReturn( "myId" );

    assertNull( node.logicalIdPropertyKeys );
    assertEquals( "myId", node.getLogicalId() );
  }
}
