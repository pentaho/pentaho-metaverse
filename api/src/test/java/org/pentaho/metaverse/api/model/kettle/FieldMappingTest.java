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


package org.pentaho.metaverse.api.model.kettle;

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
