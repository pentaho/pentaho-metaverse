/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.api;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class StepFieldTest {

  private StepField stepField;

  @Before
  public void setUp() throws Exception {
    stepField = new StepField();
  }

  @Test
  public void testCopyConstructor() {
    stepField = new StepField( "testStep", "testField" );
    assertEquals( "testStep", stepField.getStepName() );
    assertEquals( "testField", stepField.getFieldName() );
    StepField newStepField = new StepField( stepField );
    assertEquals( "testStep", newStepField.getStepName() );
    assertEquals( "testField", newStepField.getFieldName() );
  }

  @Test
  public void testGetSetFieldName() throws Exception {
    stepField = new StepField( "testStep", "testField" );
    assertEquals( "testField", stepField.getFieldName() );
    stepField.setFieldName( "newField" );
    assertEquals( "newField", stepField.getFieldName() );
  }

  @Test
  public void testGetSetStepName() throws Exception {
    stepField = new StepField( "testStep", "testField" );
    assertEquals( "testStep", stepField.getStepName() );
    stepField.setStepName( "newStep" );
    assertEquals( "newStep", stepField.getStepName() );
  }

  @Test
  public void testToMap() {
    stepField = new StepField( "testStep", "testField" );
    Map<String, String> map = stepField.toMap();
    assertEquals( 1, map.size() );
    assertTrue( map.containsKey( "testStep" ) );
    assertEquals( "testField", map.get( "testStep" ) );
  }

  @Test
  public void testToString() {
    stepField = new StepField( "testStep", "testField" );
    assertEquals( "{ step:testStep, field:testField }", stepField.toString() );
  }

  @Test
  public void testEquals() {
    stepField = new StepField( "testStep", "testField" );
    StepField stepField2 = new StepField( "testStep", "testField" );
    StepField stepField3 = new StepField( "testStep2", "testField" );
    assertTrue( stepField.equals( stepField2 ) );
    assertFalse( stepField.equals( stepField3 ) );
    assertFalse( stepField2.equals( stepField3 ) );
  }
}
