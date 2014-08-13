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
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
}
