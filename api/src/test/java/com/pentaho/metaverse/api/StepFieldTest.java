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
package com.pentaho.metaverse.api;

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
