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

package com.pentaho.metaverse.impl.model.kettle;

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
