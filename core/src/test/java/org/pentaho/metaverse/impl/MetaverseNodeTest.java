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


package org.pentaho.metaverse.impl;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.MetaverseLogicalIdGenerator;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetaverseNodeTest {

  private MetaverseNode node;
  private Vertex v;
  private Vertex v2;

  @Before
  public void setUp() {
    v = mock( Vertex.class );
    v2 = mock( Vertex.class );
    when( v.id() ).thenReturn( "my.id" );
    node = new MetaverseNode( v );
  }

  @Test
  public void testGetName() {
    VertexProperty<Object> property = missingProperty();
    when( v.property( "name" ) ).thenReturn( property );
    assertNull( node.getName() );
    verify( v ).property( "name" );
  }

  @Test
  public void testSetName() {
    VertexProperty<Object> property = presentProperty( "myName" );
    when( v.property( "name" ) ).thenReturn( property );
    when( v.value( "name" ) ).thenReturn( "myName" );

    node.setName( "myName" );

    assertEquals( "myName", node.getName() );
    verify( v ).property( "name", "myName" );
  }

  @Test
  public void testGetType() {
    VertexProperty<Object> property = missingProperty();
    when( v.property( "type" ) ).thenReturn( property );
    assertNull( node.getType() );
    verify( v ).property( "type" );
  }

  @Test
  public void testSetType() {
    VertexProperty<Object> property = presentProperty( "myType" );
    when( v.property( "type" ) ).thenReturn( property );
    when( v.value( "type" ) ).thenReturn( "myType" );

    node.setType( "myType" );

    assertEquals( "myType", node.getType() );
    verify( v ).property( "type", "myType" );
    verify( v ).property( eq( DictionaryConst.PROPERTY_CATEGORY ), any() );
  }

  @Test
  public void testGetStringID() {
    assertEquals( "my.id", node.getStringID() );
    verify( v ).id();
  }

  @Test
  public void testGetID() {
    assertEquals( "my.id", node.getId() );
    verify( v ).id();
  }

  @Test
  public void testGetStringID_null() {
    when( v.id() ).thenReturn( null );
    assertNull( node.getStringID() );
    verify( v ).id();
  }

  @Test
  public void testSetProperty() {
    node.setProperty( "test", "test" );
    verify( v ).property( "test", "test" );
  }

  @Test
  public void testGetPropertyKeys() {
    when( v.keys() ).thenReturn( Collections.<String>emptySet() );
    assertSame( Collections.<String>emptySet(), node.getPropertyKeys() );
    verify( v ).keys();
  }

  @Test
  public void testRemoveProperty() {
    VertexProperty<Object> property = presentProperty( "value" );
    when( v.property( "test" ) ).thenReturn( property );
    when( v.value( "test" ) ).thenReturn( "value" );

    assertEquals( "value", node.removeProperty( "test" ) );
    verify( property ).remove();
  }

  @Test
  public void testAddEdge() {
    Edge edge = mock( Edge.class );
    when( v.addEdge( "uses", v2 ) ).thenReturn( edge );
    assertSame( edge, node.addEdge( "uses", v2 ) );
  }

  @Test
  public void testGetEdges() {
    Iterator<Edge> edges = Collections.<Edge>emptyIterator();
    when( v.edges( Direction.BOTH ) ).thenReturn( edges );
    assertSame( edges, node.getEdges( Direction.BOTH ) );
  }

  @Test
  public void testGetVertices() {
    Iterator<Vertex> vertices = Collections.<Vertex>emptyIterator();
    when( v.vertices( Direction.BOTH, "uses" ) ).thenReturn( vertices );
    assertSame( vertices, node.getVertices( Direction.BOTH, "uses" ) );
  }

  @Test
  public void testRemove() {
    node.remove();
    verify( v ).remove();
  }

  @Test
  public void testGetProperty() {
    VertexProperty<Object> property = presentProperty( "value" );
    when( v.property( "test" ) ).thenReturn( property );
    when( v.value( "test" ) ).thenReturn( "value" );
    assertEquals( "value", node.getProperty( "test" ) );
  }

  @Test
  public void testGetProperties() {
    Map<String, Object> props = new HashMap<String, Object>() {{
      put( "path", "/Users/home/admin" );
      put( "lastModified", new Date() );
    }};
    mockProperties( props );

    Map<String, Object> resultProps = node.getProperties();
    assertEquals( props, resultProps );
  }

  @Test
  public void testGetProperties_noPropertyKeys() {
    when( v.keys() ).thenReturn( null );
    Map<String, Object> resultProps = node.getProperties();
    assertTrue( resultProps.isEmpty() );
  }

  @Test
  public void testGetProperties_withBaseVertex() {
    Map<String, Object> props = new HashMap<String, Object>() {{
      put( "_NAME_", "name" );
    }};
    mockProperties( props );

    Map<String, Object> resultProps = node.getProperties();
    assertEquals( props, resultProps );
  }

  @Test
  public void testRemoveProperties() {
    VertexProperty<Object> nameProperty = presentProperty( "name" );
    VertexProperty<Object> ageProperty = presentProperty( "age" );
    when( v.property( "name" ) ).thenReturn( nameProperty );
    when( v.property( "age" ) ).thenReturn( ageProperty );

    node.removeProperties( new HashSet<String>() {{
      add( "name" );
      add( "age" );
    }} );

    verify( nameProperty ).remove();
    verify( ageProperty ).remove();
  }

  @Test
  public void testremoveProperties_null() {
    node.removeProperties( null );
    verify( v, never() ).property( anyString() );
  }

  @Test
  public void testClearProperties() {
    Map<String, Object> props = new HashMap<String, Object>() {{
      put( "path", "/Users/home/admin" );
      put( "lastModified", new Date() );
    }};
    mockProperties( props );

    node.clearProperties();

    for ( String key : props.keySet() ) {
      verify( v, times( 2 ) ).property( key );
    }
  }

  @Test
  public void testClearProperties_null() {
    when( v.keys() ).thenReturn( null );
    node.clearProperties();
    verify( v, never() ).property( anyString(), any() );
  }

  @Test
  public void testContainsKey() {
    VertexProperty<Object> property = presentProperty( "value" );
    when( v.property( "test" ) ).thenReturn( property );
    when( v.value( "test" ) ).thenReturn( "value" );
    assertTrue( node.containsKey( "test" ) );
  }

  @Test
  public void testSetProperties_null() {
    node.setProperties( null );
    verify( v, never() ).property( anyString(), any() );
  }

  @Test
  public void testSetProperties() {
    Map<String, Object> props = new HashMap<String, Object>() {{
      put( "path", "/Users/home/admin" );
      put( "lastModified", new Date() );
    }};
    node.setProperties( props );
    verify( v ).property( eq( "path" ), any() );
    verify( v ).property( eq( "lastModified" ), any() );
  }

  @Test
  public void testGetLogicalId() {
    VertexProperty<Object> nameProperty = presentProperty( "testName" );
    VertexProperty<Object> namespaceProperty = presentProperty( "" );
    VertexProperty<Object> typeProperty = presentProperty( "testType" );
    VertexProperty<Object> zzzProperty = presentProperty( "last" );
    when( v.property( "name" ) ).thenReturn( nameProperty );
    when( v.property( "namespace" ) ).thenReturn( namespaceProperty );
    when( v.property( "type" ) ).thenReturn( typeProperty );
    when( v.property( "zzz" ) ).thenReturn( zzzProperty );
    when( v.value( "name" ) ).thenReturn( "testName" );
    when( v.value( "namespace" ) ).thenReturn( "" );
    when( v.value( "type" ) ).thenReturn( "testType" );
    when( v.value( "zzz" ) ).thenReturn( "last" );

    MetaverseNode spyNode = spy( new MetaverseNode( v ) );
    doNothing().when( spyNode ).setProperty( eq( DictionaryConst.PROPERTY_LOGICAL_ID ), any() );
    when( spyNode.getPropertyKeys() ).thenReturn( new HashSet<String>() {{
      add( "name" );
      add( "namespace" );
      add( "type" );
      add( "zzz" );
    }} );

    assertEquals( "{\"name\":\"testName\",\"namespace\":\"\",\"type\":\"testType\"}", spyNode.getLogicalId() );

    ILogicalIdGenerator idGenerator = new MetaverseLogicalIdGenerator( "type", "zzz", "name" );
    spyNode.setLogicalIdGenerator( idGenerator );
    assertNotNull( spyNode.logicalIdGenerator );
    assertEquals( "{\"name\":\"testName\",\"type\":\"testType\",\"zzz\":\"last\"}", spyNode.getLogicalId() );
  }

  @SuppressWarnings( "unchecked" )
  private <T> VertexProperty<Object> presentProperty( T value ) {
    VertexProperty<Object> property = mock( VertexProperty.class );
    when( property.isPresent() ).thenReturn( true );
    when( property.value() ).thenReturn( value );
    return property;
  }

  @SuppressWarnings( "unchecked" )
  private VertexProperty<Object> missingProperty() {
    VertexProperty<Object> property = mock( VertexProperty.class );
    when( property.isPresent() ).thenReturn( false );
    return property;
  }

  private void mockProperties( Map<String, Object> props ) {
    when( v.keys() ).thenReturn( props.keySet() );
    for ( Map.Entry<String, Object> entry : props.entrySet() ) {
      VertexProperty<Object> property = presentProperty( entry.getValue() );
      when( v.property( entry.getKey() ) ).thenReturn( property );
      when( v.value( entry.getKey() ) ).thenReturn( entry.getValue() );
    }
  }
}
