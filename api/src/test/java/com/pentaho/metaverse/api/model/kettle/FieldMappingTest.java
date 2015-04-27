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

package com.pentaho.metaverse.api.model.kettle;

import org.junit.Test;

import static org.junit.Assert.*;

public class FieldMappingTest {

  FieldMapping mapping;

  @Test
  public void testEmptyConstructor() throws Exception {
    mapping = new FieldMapping();
    assertNull( mapping.getSourceFieldName() );
    assertNull( mapping.getTargetFieldName() );
  }

  @Test
  public void testConstructor() throws Exception {
    mapping = new FieldMapping( "from", "to" );
    assertEquals( "from", mapping.getSourceFieldName() );
    assertEquals( "to", mapping.getTargetFieldName() );
  }

  @Test
  public void testSetSourceFieldName() throws Exception {
    mapping = new FieldMapping();
    assertNull( mapping.getSourceFieldName() );
    mapping.setSourceFieldName( "from" );
    assertEquals( "from", mapping.getSourceFieldName() );
  }

  @Test
  public void testSetTargetFieldName() throws Exception {
    mapping = new FieldMapping();
    assertNull( mapping.getTargetFieldName() );
    mapping.setTargetFieldName( "to" );
    assertEquals( "to", mapping.getTargetFieldName() );
  }

  @Test
  public void testEquals() throws Exception {
    mapping = new FieldMapping( "from", "to" );
    assertTrue( mapping.equals( mapping ) );

    assertFalse( mapping.equals( "string" ) );

    FieldMapping anotherMapping = new FieldMapping( "source", "destination" );
    assertFalse( mapping.equals( anotherMapping ) );

    anotherMapping = new FieldMapping( "from", "destination" );
    assertFalse( mapping.equals( anotherMapping ) );

    anotherMapping = new FieldMapping( "source", "to" );
    assertFalse( mapping.equals( anotherMapping ) );

    FieldMapping equivMapping = new FieldMapping( "from", "to" );
    assertTrue( mapping.equals( equivMapping ) );
  }
}