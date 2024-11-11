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


package org.pentaho.dictionary;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.metaverse.api.IHasProperties;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.MetaverseLogicalIdGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author mburgess
 * 
 */
public class MetaverseTransientNodeTest {

  MetaverseTransientNode node;

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
    node = new MetaverseTransientNode();
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
  }

  public void testSetName() {
    MetaverseTransientNode myNode = new MetaverseTransientNode();
    myNode.setName( "myName" );
    assertEquals( myNode.getName(), "myName" );
  }

  @Test
  public void testGetType() {
    assertNull( node.getType() );
  }

  @Test
  public void testSetType() {
    MetaverseTransientNode myNode = new MetaverseTransientNode();
    myNode.setType( "myType" );
    assertEquals( myNode.getType(), "myType" );
  }

  @Test
  public void testGetLogicalId() throws Exception {
    MetaverseTransientNode myNode = new MetaverseTransientNode();

    myNode.setName( "testName" );
    myNode.setType( "testType" );
    myNode.setProperty( "zzz", "last" );
    myNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE, "" );
    myNode.setStringID( "myId" );

    // should be using the default logical id generator initially
    assertEquals( "{\"name\":\"testName\",\"namespace\":\"\",\"type\":\"testType\"}", myNode.getLogicalId() );

    ILogicalIdGenerator idGenerator = new MetaverseLogicalIdGenerator( "type", "zzz", "name" );
    myNode.setLogicalIdGenerator( idGenerator );
    assertNotNull( myNode.logicalIdGenerator );

    // logical id should be sorted based on key
    assertEquals( "{\"name\":\"testName\",\"type\":\"testType\",\"zzz\":\"last\"}", myNode.getLogicalId() );
  }

  @Test
  public void testGetLogicalId_nullIdGenerator() throws Exception {
    MetaverseTransientNode myNode = new MetaverseTransientNode( "myId" );
    myNode.setName( "testName" );
    myNode.setType( "testType" );
    myNode.setLogicalIdGenerator( null );

    assertEquals( "myId", myNode.getLogicalId() );
  }

  @Test
  public void testGetLogicalId_nullLogicalIdGeneration() throws Exception {
    MetaverseTransientNode myNode = new MetaverseTransientNode( "myId" );
    ILogicalIdGenerator generator = mock( ILogicalIdGenerator.class );
    when( generator.generateId( any( IHasProperties.class ) ) ).thenReturn( null );

    myNode.setName( "testName" );
    myNode.setType( "testType" );
    myNode.setLogicalIdGenerator( generator );

    assertEquals( "myId", myNode.getLogicalId() );
  }
}
