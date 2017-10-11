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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.ValueMetaInterface;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


/**
 * User: RFellows Date: 12/3/14
 */
@RunWith( MockitoJUnitRunner.class )
public class FieldInfoTest {

  FieldInfo fieldInfo;
  @Mock ValueMetaInterface vmi;

  @Before
  public void setUp() throws Exception {
    fieldInfo = new FieldInfo();

    when( vmi.getName() ).thenReturn( "name" );
    when( vmi.getComments() ).thenReturn( "description" );
    when( vmi.getLength() ).thenReturn( 50 );
    when( vmi.getPrecision() ).thenReturn( 3 );
    when( vmi.getTypeDesc() ).thenReturn( "String" );

  }

  @Test
  public void testConstructor_ValueMetaInterface() throws Exception {
    fieldInfo = new FieldInfo( vmi );
    assertEquals( vmi.getLength(), fieldInfo.getLength().intValue() );
    assertEquals( vmi.getPrecision(), fieldInfo.getPrecision().intValue() );
    assertEquals( vmi.getName(), fieldInfo.getName() );
    assertEquals( vmi.getTypeDesc(), fieldInfo.getDataType() );
    assertEquals( vmi.getComments(), fieldInfo.getDescription() );
  }

  @Test
  public void testGettersSetters() throws Exception {
    assertEquals( null, fieldInfo.getDataType());
    fieldInfo.setDataType( "String" );
    assertEquals( "String", fieldInfo.getDataType() );

    assertEquals( null, fieldInfo.getLength() );
    fieldInfo.setLength( 45 );
    assertEquals( Integer.valueOf( 45 ), fieldInfo.getLength() );

    assertEquals( null, fieldInfo.getPrecision() );
    fieldInfo.setPrecision( 4 );
    assertEquals( Integer.valueOf( 4 ), fieldInfo.getPrecision() );

    assertEquals( null, fieldInfo.getName() );
    fieldInfo.setName( "Name" );
    assertEquals( "Name", fieldInfo.getName() );

    assertEquals( null, fieldInfo.getDescription() );
    fieldInfo.setDescription( "Description" );
    assertEquals( "Description", fieldInfo.getDescription() );
  }
}
