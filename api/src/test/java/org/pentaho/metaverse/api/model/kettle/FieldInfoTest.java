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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.ValueMetaInterface;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


/**
 * User: RFellows Date: 12/3/14
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
