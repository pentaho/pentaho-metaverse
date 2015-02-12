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
package com.pentaho.metaverse.client;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StepFieldTargetTest {

  StepFieldTarget stepFieldTarget;

  @Before
  public void setUp() {
    stepFieldTarget = new StepFieldTarget();
  }


  @Test
  public void testGetSetTargetFieldName() throws Exception {
    stepFieldTarget = new StepFieldTarget( "testStep", "testField", "testTargetField" );
    assertEquals( "testTargetField", stepFieldTarget.getTargetFieldName() );
    stepFieldTarget.setTargetFieldName( "newTargetField" );
    assertEquals( "newTargetField", stepFieldTarget.getTargetFieldName() );

  }

  @Test
  public void testToString() {
    stepFieldTarget = new StepFieldTarget( "testStep", "testField", "testTargetField" );
    assertEquals( "{ step:testStep, field:testField, targetfield:testTargetField }", stepFieldTarget.toString() );
  }

  @Test
  public void testEquals() throws Exception {
    stepFieldTarget = new StepFieldTarget( "testStep", "testField", "testTargetField" );
    StepFieldTarget stepFieldTarget2 = new StepFieldTarget( "testStep", "testField", "testTargetField" );
    StepFieldTarget stepFieldTarget3 = new StepFieldTarget( "testStep2", "testField", "testTargetField" );
    assertTrue( stepFieldTarget.equals( stepFieldTarget2 ) );
    assertFalse( stepFieldTarget.equals( stepFieldTarget3 ) );
    assertFalse( stepFieldTarget2.equals( stepFieldTarget3 ) );
  }
}
