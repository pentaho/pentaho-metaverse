/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
