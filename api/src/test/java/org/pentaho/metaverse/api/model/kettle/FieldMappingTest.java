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
