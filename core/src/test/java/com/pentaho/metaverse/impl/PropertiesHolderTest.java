package com.pentaho.metaverse.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PropertiesHolderTest {

  PropertiesHolder props = new PropertiesHolder();

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetProperty() throws Exception {
    // No properties yet, pick a random one and make sure it's not there
    assertNull( props.getProperty( "myProperty" ) );

  }

  @Test
  public void testGetPropertyAsString() throws Exception {
    Date now = Calendar.getInstance().getTime();
    props.setProperty( "myProperty", now );
    assertEquals( now.toString(), props.getPropertyAsString( "myProperty" ) );

  }

  @Test
  public void testSetProperty() throws Exception {
    assertNull( props.getProperty( "myProperty" ) );
    props.setProperty( "myProperty", "myValue" );
    assertEquals( "myValue", props.getProperty( "myProperty" ) );
  }

  @Test
  public void testRemoveProperty() throws Exception {
    assertTrue( props.getPropertyKeys().isEmpty() );
    String value = "myValue";
    props.setProperty( "myProperty", value );
    assertEquals( props.getPropertyKeys().size(), 1 );
    Object o = props.removeProperty( "myProperty" );
    assertTrue( props.getPropertyKeys().isEmpty() );
    assertEquals( value, o );
  }

  @Test
  public void testGetPropertyKeys() throws Exception {
    props.setProperty( "a", 1 );
    props.setProperty( "b", 2 );
    Set<String> keys = props.getPropertyKeys();
    assertNotNull( keys );
    assertEquals( keys.size(), 2 );
    assertTrue( keys.contains( "a" ) );
    assertTrue( keys.contains( "b" ) );
  }

  @Test
  public void testGetProperties() throws Exception {
    Map<String, Object> getProps = props.getProperties();
    assertNotNull( getProps );
    assertTrue( getProps.isEmpty() );
    props.setProperty( "a", "hello" );
    props.setProperty( "b", "world!" );
    getProps = props.getProperties();
    assertNotNull( getProps );
    assertFalse( getProps.isEmpty() );
    assertEquals( getProps.size(), 2 );
    assertTrue( getProps.containsKey( "a" ) );
    assertTrue( getProps.containsKey( "b" ) );
    assertTrue( getProps.containsValue( "hello" ) );
    assertTrue( getProps.containsValue( "world!" ) );
  }

  @Test
  public void testSetProperties() throws Exception {
    Map<String, Object> getProps = props.getProperties();
    assertNotNull( getProps );
    assertTrue( getProps.isEmpty() );
    Map<String, Object> setProps = new HashMap<String, Object>( 3 );
    setProps.put( "a", 1 );
    setProps.put( "b", 2 );
    props.setProperties( setProps );
    assertEquals( getProps.size(), 2 );
    setProps.put( "c", 3 );
    props.setProperties( setProps );
    assertEquals( getProps.size(), 3 );
  }

  @Test
  public void testRemoveProperties_noKeys() throws Exception {
    Map<String, Object> spyProps = spy( props.properties );
    props.removeProperties( null );

    verify( spyProps, never() ).remove( anyString() );
  }

  @Test
  public void testRemoveProperties() throws Exception {
    Map<String, Object> setProps = new HashMap<String, Object>( 3 );
    setProps.put( "a", 1 );
    setProps.put( "b", 2 );
    setProps.put( "c", 3 );
    props.setProperties( setProps );
    Map<String, Object> getProps = props.getProperties();
    assertEquals( getProps.size(), 3 );
    Set<String> removeProps = new HashSet<String>( 2 );
    removeProps.add( "a" );
    removeProps.add( "c" );
    props.removeProperties( removeProps );
    assertEquals( getProps.size(), 1 );
    assertTrue( getProps.containsKey( "b" ) );
    assertEquals( getProps.get( "b" ), 2 );
  }

  @Test
  public void testClearProperties() throws Exception {
    Map<String, Object> setProps = new HashMap<String, Object>( 3 );
    setProps.put( "a", 1 );
    setProps.put( "b", 2 );
    setProps.put( "c", 3 );
    props.setProperties( setProps );
    Map<String, Object> getProps = props.getProperties();
    assertEquals( getProps.size(), 3 );
    props.clearProperties();
    assertNotNull( getProps );
    assertTrue( getProps.isEmpty() );
  }

  @Test
  public void testContainsKey_nullProperties() throws Exception {
    props.properties = null;
    assertFalse( props.containsKey( "nope" ) );
  }

  @Test
  public void testToString() throws Exception {
    PropertiesHolder props = new PropertiesHolder();
    assertEquals( "{}", props.toString() );

    Map<String, Object> setProps = new HashMap<String, Object>( 3 );
    setProps.put( "a", 1 );
    setProps.put( "b", 2 );
    setProps.put( "c", 3 );
    props.setProperties( setProps );
    System.out.println( props.toString() );
    assertTrue( props.toString().contains( "b=2" ) );
    assertTrue( props.toString().contains( "c=3" ) );
    assertTrue( props.toString().contains( "a=1" ) );
  }

  @Test
  public void testToString_nullProperties() throws Exception {
    props.properties = null;
    assertNotNull( props.toString() );
  }
}
