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

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.AnalysisContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.pentaho.metaverse.api.IHasProperties;
import com.pentaho.metaverse.api.ILogicalIdGenerator;
import com.pentaho.metaverse.api.INamespace;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author mburgess
 * 
 */
public class MetaverseDocumentTest {

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
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testNullProperties() {
    MetaverseDocument document = new MetaverseDocument();
    assertNull( document.getName() );
    assertNull( document.getStringID() );
    assertNull( document.getType() );
    assertNull( document.getContent() );
    assertNull( document.getExtension() );
    assertNull( document.getMimeType() );
  }

  @Test
  public void testSetName() {
    MetaverseDocument document = new MetaverseDocument();
    document.setName( "myName" );
    assertEquals( "myName", document.getName() );
  }

  @Test
  public void testSetStringID() {
    MetaverseDocument document = new MetaverseDocument();
    document.setStringID( "myID" );
    assertEquals( "myID", document.getStringID() );
  }

  @Test
  public void testSetType() {
    MetaverseDocument document = new MetaverseDocument();
    document.setType( "myType" );
    assertEquals( "myType", document.getType() );
  }

  @Test
  public void testGetNamespace() throws Exception {
    MetaverseDocument document = new MetaverseDocument();
    document.setStringID( "id" );
    INamespace mockNamespace = mock( INamespace.class );
    when( mockNamespace.getNamespaceId() ).thenReturn( "namespace-id" );
    document.setNamespace( mockNamespace );
    assertEquals( "namespace-id", document.getNamespaceId() );
    assertEquals( mockNamespace, document.getNamespace() );
    assertEquals( "id", document.getStringID() );
    verify( mockNamespace ).getNamespaceId();
  }

  @Test
  public void testGetLogicalId() throws Exception {
    MetaverseDocument myNode = new MetaverseDocument();

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
    MetaverseDocument myNode = new MetaverseDocument();
    myNode.setStringID( "myId" );
    myNode.setName( "testName" );
    myNode.setType( "testType" );
    myNode.setLogicalIdGenerator( null );

    assertEquals( "myId", myNode.getLogicalId() );
  }

  @Test
  public void testGetLogicalId_nullLogicalIdGeneration() throws Exception {
    MetaverseDocument myNode = new MetaverseDocument();
    myNode.setStringID( "myId" );
    ILogicalIdGenerator generator = mock( ILogicalIdGenerator.class );
    when( generator.generateId( any( IHasProperties.class ) ) ).thenReturn( null );

    myNode.setName( "testName" );
    myNode.setType( "testType" );
    myNode.setLogicalIdGenerator( generator );

    assertEquals( "myId", myNode.getLogicalId() );
  }

  @Test
  public void testGetLogicalId_isNotDirty() throws Exception {
    MetaverseDocument myNode = new MetaverseDocument();
    myNode.setStringID( "myId" );
    myNode.setName( "testName" );
    myNode.setType( "testType" );
    assertEquals( "{\"name\":\"testName\",\"namespace\":\"\",\"type\":\"testType\"}", myNode.getLogicalId() );
    myNode.setDirty( true );
    assertEquals( "{\"name\":\"testName\",\"namespace\":\"\",\"type\":\"testType\"}", myNode.getLogicalId() );
  }

  @Test
  public void testDelegateNamespaceCalls() throws Exception {
    MetaverseDocument doc = new MetaverseDocument();
    doc.setStringID( "id" );
    INamespace mockNs = mock( INamespace.class );

    doc.setNamespace( mockNs );

    doc.getParentNamespace();
    verify( mockNs, times( 1 ) ).getParentNamespace();

    doc.getSiblingNamespace( "Brother", "A" );
    verify( mockNs, times( 1 ) ).getSiblingNamespace( eq( "Brother" ), eq( "A" ) );
  }

  @Test
  public void testContext() throws Exception {
    MetaverseDocument doc = new MetaverseDocument();
    doc.setContext( new AnalysisContext( DictionaryConst.CONTEXT_DEFAULT ) );
    assertEquals( DictionaryConst.CONTEXT_DEFAULT, doc.getContext().getContextName() );
  }
}
