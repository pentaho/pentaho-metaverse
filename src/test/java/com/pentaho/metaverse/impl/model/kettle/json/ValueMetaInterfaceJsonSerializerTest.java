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

package com.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 12/1/14
 */
@RunWith( MockitoJUnitRunner.class )
public class ValueMetaInterfaceJsonSerializerTest {

  ValueMetaInterfaceJsonSerializer serializer;
  @Mock JsonGenerator json;
  @Mock SerializerProvider provider;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    serializer = new ValueMetaInterfaceJsonSerializer( ValueMetaInterface.class );
  }

  @Test
  public void testSerialize() throws Exception {
    ValueMetaInterface vmi = mock( ValueMetaInterface.class );

    when( vmi.getName() ).thenReturn( "col name" );
    when( vmi.getTypeDesc() ).thenReturn( "String" );
    when( vmi.getPrecision() ).thenReturn( 0 );
    when( vmi.getLength() ).thenReturn( 50 );

    serializer.serialize( vmi, json, provider );

    verify( vmi ).getName();
    verify( vmi ).getTypeDesc();
    verify( vmi ).getPrecision();
    verify( vmi ).getLength();

    verify( json ).writeStartObject();
    verify( json ).writeStringField( "name", vmi.getName() );
    verify( json ).writeStringField( "datatype", vmi.getTypeDesc() );
    verify( json ).writeNumberField( "precision", vmi.getPrecision() );
    verify( json ).writeNumberField( "length", vmi.getLength() );
    verify( json ).writeEndObject();

  }
}
