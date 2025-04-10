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
    assertEquals( stepField, stepField2 );
    assertNotEquals( stepField, stepField3 );
    assertNotEquals( stepField2, stepField3 );
  }
}
