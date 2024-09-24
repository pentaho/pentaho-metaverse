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

package org.pentaho.metaverse.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.dictionary.DictionaryConst;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 9/24/14
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class MetaverseLogicalIdGeneratorTest {

  MetaverseLogicalIdGenerator idGenerator;
  ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private IHasProperties node;

  @Before
  public void setUp() throws Exception {
    String[] logicalIdKeys = new String[] { "name", "age", "address", "birthday" };
    idGenerator = new MetaverseLogicalIdGenerator( logicalIdKeys );
  }

  @Test
  public void testGenerateLogicalId() throws Exception {
    when( node.getProperty( "name" ) ).thenReturn( "john doe" );
    when( node.getProperty( "age" ) ).thenReturn( 30 );
    when( node.getProperty( "address" ) ).thenReturn( "1234 Pentaho Way Orlando, FL 12345" );
    Calendar cal = GregorianCalendar.getInstance();
    cal.set( 1976, Calendar.JANUARY, 1, 0, 0, 0 );
    when( node.getProperty( "birthday" ) ).thenReturn( cal.getTime() );
    when( node.getPropertyKeys() ).thenReturn( new HashSet<String>() {{
      add( "address" );
      add( "age" );
      add( "birthday" );
      add( "name" );
    }} );

    // make sure there is no logicalid on the node initially
    assertNull( node.getProperty( DictionaryConst.PROPERTY_LOGICAL_ID ) );

    String logicalId = idGenerator.generateId( node );

    // it should come out in alphabetical order by key
    assertEquals( "{\"address\":\"1234 Pentaho Way Orlando, FL 12345\",\"age\":\"30\",\"birthday\":\"1976-01-01 00:00:00\",\"name\":\"john doe\"}",
      logicalId );

    // make sure the json string is parseable
    JsonNode jsonObject = objectMapper.readTree( logicalId );
    JsonNode address = jsonObject.get( "address" );
    assertEquals( "1234 Pentaho Way Orlando, FL 12345", address.textValue() );

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );

  }

  @Test
  public void testGenerateLogicalId_escapedCharacters() throws Exception {
    when( node.getProperty( "name" ) ).thenReturn( "john\ndoe" );
    when( node.getProperty( "age" ) ).thenReturn( 30 );
    when( node.getProperty( "address" ) ).thenReturn( "1234 Pentaho Way\\Orlando, FL 12345" );
    Calendar cal = GregorianCalendar.getInstance();
    cal.set( 1976, Calendar.JANUARY, 1, 0, 0, 0 );
    when( node.getProperty( "birthday" ) ).thenReturn( cal.getTime() );
    when( node.getPropertyKeys() ).thenReturn( new HashSet<String>() {{
      add( "address" );
      add( "age" );
      add( "birthday" );
      add( "name" );
    }} );

    // make sure there is no logicalid on the node initially
    assertNull( node.getProperty( DictionaryConst.PROPERTY_LOGICAL_ID ) );

    String logicalId = idGenerator.generateId( node );

    // it should come out in alphabetical order by key
    assertEquals( "{\"address\":\"1234 Pentaho Way\\\\Orlando, FL 12345\",\"age\":\"30\",\"birthday\":\"1976-01-01 00:00:00\",\"name\":\"john\\ndoe\"}",
      logicalId );

    // make sure the json string is parseable
    JsonNode jsonObject = objectMapper.readTree( logicalId );
    JsonNode address = jsonObject.get( "address" );
    assertEquals( "1234 Pentaho Way\\Orlando, FL 12345", address.textValue() );
    JsonNode name = jsonObject.get( "name" );
    assertEquals( "john\ndoe", name.textValue() );
    System.out.println(jsonObject.toString());

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );

  }

  @Test
  public void testGenerateLogicalId_duplicateKey() throws Exception {
    when( node.getProperty( "name" ) ).thenReturn( "john doe" );
    when( node.getProperty( "age" ) ).thenReturn( 30 );
    when( node.getProperty( "address" ) ).thenReturn( "1234 Pentaho Way Orlando, FL 12345" );
    Calendar cal = GregorianCalendar.getInstance();
    cal.set( 1976, Calendar.JANUARY, 1, 0, 0, 0 );
    when( node.getProperty( "birthday" ) ).thenReturn( cal.getTime() );
    when( node.getPropertyKeys() ).thenReturn( new HashSet<String>() {{
      add( "address" );
      add( "age" );
      add( "birthday" );
      add( "name" );
    }} );

    String logicalId = idGenerator.generateId( node );

    // it should come out in alphabetical order by key
    assertEquals( "{\"address\":\"1234 Pentaho Way "
        + "Orlando, FL 12345\",\"age\":\"30\",\"birthday\":\"1976-01-01 00:00:00\",\"name\":\"john doe\"}",
      logicalId );

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );
  }

  @Test
  public void testGenerateLogicalId_nullValue() throws Exception {
    when( node.getProperty( "name" ) ).thenReturn( "john doe" );
    when( node.getProperty( "age" ) ).thenReturn( null );
    when( node.getProperty( "address" ) ).thenReturn( "1234 Pentaho Way Orlando, FL 12345" );
    Calendar cal = GregorianCalendar.getInstance();
    cal.set( 1976, Calendar.JANUARY, 1, 0, 0, 0 );
    when( node.getProperty( "birthday" ) ).thenReturn( cal.getTime() );
    when( node.getPropertyKeys() ).thenReturn( new HashSet<String>() {{
      add( "address" );
      add( "age" );
      add( "birthday" );
      add( "name" );
    }} );

    String logicalId = idGenerator.generateId( node );

    // it should come out in alphabetical order by key
    assertEquals( "{\"address\":\"1234 Pentaho Way "
        + "Orlando, FL 12345\",\"age\":\"\",\"birthday\":\"1976-01-01 00:00:00\",\"name\":\"john doe\"}",
      logicalId );

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );
  }

  @Test
  public void testGenerateLogicalId_nested() throws Exception {
    String[] logicalIdKeys = new String[] { "name", "type", "dataSource" };
    idGenerator = new MetaverseLogicalIdGenerator( logicalIdKeys );

    String expected = "{\"dataSource\":{\"name\":\"Sampledata\",\"type\":\"Database Connection\"}," +
      "\"name\":\"SALES_DATA\",\"type\":\"Database Table\"}";

    when( node.getProperty( "name" ) ).thenReturn( "SALES_DATA" );
    when( node.getProperty( "type" ) ).thenReturn( "Database Table" );
    when( node.getProperty( "dataSource" ) ).thenReturn( "{\"name\":\"Sampledata\",\"type\":\"Database Connection\"}" );
    when( node.getPropertyKeys() ).thenReturn( new HashSet<String>() {{
      add( "dataSource" );
      add( "name" );
      add( "type" );
    }} );

    String logicalId = idGenerator.generateId( node );
    assertEquals( expected, logicalId );

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );
  }

  @Test
  public void testGenerateLogicalId_noKeys() throws Exception {
    String[] logicalIdKeys = new String[]{};
    idGenerator.setLogicalIdPropertyKeys( logicalIdKeys );
    String logicalId = idGenerator.generateId( node );

    // it should come out in alphabetical order by key
    assertEquals( null, logicalId );

    // make sure no call was made to add the logical id as a property since it should be null
    verify( node, never() ).setProperty( eq( DictionaryConst.PROPERTY_LOGICAL_ID ), anyString() );
  }

  @Test
  public void testGenerateLogicalId_notAllRequiredPropertiesAvailable() throws Exception {
    when( node.getProperty( anyString() )).thenReturn( null );
    when( node.getProperty( "name" ) ).thenReturn( "john doe" );
    when( node.getProperty( "age" ) ).thenReturn( 30 );

    // don't set the address property on the node

    Calendar cal = GregorianCalendar.getInstance();
    cal.set( 1976, Calendar.JANUARY, 1, 0, 0, 0 );
    when( node.getProperty( "birthday" ) ).thenReturn( cal.getTime() );

    when( node.getPropertyKeys() ).thenReturn( new HashSet<String>() {{
//      add( "address" );
      add( "age" );
      add( "birthday" );
      add( "name" );
    }} );

    // make sure there is no logicalid on the node initially
    assertNull( node.getProperty( DictionaryConst.PROPERTY_LOGICAL_ID ) );

    String logicalId = idGenerator.generateId( node );

    // it should come out in alphabetical order by key
    // it should also include address in the id but have no value for it since it wasn't set on the node
    assertEquals( "{\"address\":\"\",\"age\":\"30\",\"birthday\":\"1976-01-01 00:00:00\",\"name\":\"john doe\"}",
      logicalId );

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );
  }
}
