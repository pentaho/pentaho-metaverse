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
