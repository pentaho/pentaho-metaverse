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

package com.pentaho.metaverse.impl;

import com.pentaho.dictionary.DictionaryConst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.pentaho.metaverse.api.IHasProperties;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: RFellows Date: 9/24/14
 */
@RunWith( MockitoJUnitRunner.class )
public class MetaverseLogicalIdGeneratorTest {

  MetaverseLogicalIdGenerator idGenerator;

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
    when( node.getProperty( "address" ) ).thenReturn( "1234 Pentaho Way\nOrlando, FL 12345" );
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
    assertEquals( "{\"address\":\"1234 Pentaho Way\nOrlando, FL 12345\",\"age\":\"30\",\"birthday\":\"1976-01-01 00:00:00\",\"name\":\"john doe\"}",
      logicalId );

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );

  }

  @Test
  public void testGenerateLogicalId_duplicateKey() throws Exception {
    when( node.getProperty( "name" ) ).thenReturn( "john doe" );
    when( node.getProperty( "age" ) ).thenReturn( 30 );
    when( node.getProperty( "address" ) ).thenReturn( "1234 Pentaho Way\nOrlando, FL 12345" );
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
    assertEquals( "{\"address\":\"1234 Pentaho Way\n"
        + "Orlando, FL 12345\",\"age\":\"30\",\"birthday\":\"1976-01-01 00:00:00\",\"name\":\"john doe\"}",
      logicalId );

    // make sure a call was made to add the logical id as a property
    verify( node ).setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );
  }

  @Test
  public void testGenerateLogicalId_nullValue() throws Exception {
    when( node.getProperty( "name" ) ).thenReturn( "john doe" );
    when( node.getProperty( "age" ) ).thenReturn( null );
    when( node.getProperty( "address" ) ).thenReturn( "1234 Pentaho Way\nOrlando, FL 12345" );
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
    assertEquals( "{\"address\":\"1234 Pentaho Way\n"
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
