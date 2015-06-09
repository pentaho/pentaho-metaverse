/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
package org.pentaho.metaverse.api.model;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.api.ChangeType;

import static org.junit.Assert.*;

public class OperationTest {

  Operation operation;

  @Before
  public void setUp() throws Exception {
    operation = new Operation( "operationName", "operationDescription" );
  }

  @Test
  public void testGetSetName() throws Exception {
    assertEquals( "operationName", operation.getName() );
    operation.setName( "newName" );
    assertEquals( "newName", operation.getName() );
  }

  @Test
  public void testGetDescription() throws Exception {
    assertEquals( "operationDescription", operation.getDescription() );
    operation.setDescription( "newDescription" );
    assertEquals( "newDescription", operation.getDescription() );
  }

  @Test
  public void testToString() {
    assertEquals( "operationName: operationDescription", operation.toString() );
  }

  @Test
  public void testEquals() {
    assertTrue( operation.equals( operation ) );
    assertFalse( operation.equals( new Object() ) );

    Operation operation2 = new Operation( "operationName", "operationDescription" );
    assertTrue( operation.equals( operation2 ) );
    operation2.setCategory( "Some other category" );
    assertFalse( operation.equals( operation2 ) );

    operation2 = new Operation( "operationName", "operationDescription" );
    operation2.setDescription( "Some other description" );
    assertFalse( operation.equals( operation2 ) );

    operation2 = new Operation( "operationName", "operationDescription" );
    operation2.setName( "Some other name" );
    assertFalse( operation.equals( operation2 ) );

    operation2 = new Operation( "operationName", "operationDescription" );
    operation2.setType( ChangeType.DATA );
    assertFalse( operation.equals( operation2 ) );
  }
}
