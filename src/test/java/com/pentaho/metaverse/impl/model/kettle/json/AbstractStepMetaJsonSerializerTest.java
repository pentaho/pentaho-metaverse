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
import com.pentaho.metaverse.api.model.kettle.IFieldInfo;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * User: RFellows Date: 12/1/14
 */
@RunWith( MockitoJUnitRunner.class )
public class AbstractStepMetaJsonSerializerTest {

  public static final String STEP_META_NAME = "StepMetaName";
  public static final String CLASS = "@class";
  public static final String NAME = "name";
  public static final String TYPE = "type";
  public static final String TRANSFORMS = "transforms";

  AbstractStepMetaJsonSerializer<BaseStepMeta> serializer;
  LineageRepository repo;
  @Mock JsonGenerator json;
  @Mock SerializerProvider provider;
  BaseStepMeta spyMeta;
  StepMeta spyParent;
  TransMeta spyParentTrans;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    repo = new LineageRepository();
    serializer = new BaseStepMetaJsonSerializer( BaseStepMeta.class, repo );

    spyMeta = spy( new BaseStepMeta() );
    spyParent = spy( new StepMeta() );
    spyParentTrans = spy( new TransMeta() );

    when( spyMeta.getParentStepMeta() ).thenReturn( spyParent );
    when( spyParent.getName() ).thenReturn( STEP_META_NAME );

  }

  @Test
  public void testSerializeBasic() throws Exception {
    AbstractStepMetaJsonSerializer<BaseStepMeta> spySerializer = spy( serializer );

    spySerializer.serialize( spyMeta, json, provider );

    verify( json ).writeStartObject();
    verify( json ).writeStringField( CLASS, spyMeta.getClass().getName() );
    verify( json ).writeStringField( NAME, spyParent.getName() );
    verify( json ).writeStringField( eq( TYPE ), anyString() );

    // make sure the templated methods are called
    verify( spySerializer ).writeRepoAttributes( spyMeta, json );
    verify( spySerializer ).writeCustomProperties( spyMeta, json, provider );
    verify( spySerializer ).writeInputFields( spyParent, json );
    verify( spySerializer ).writeOutputFields( spyParent, json );

    verify( json ).writeArrayFieldStart( TRANSFORMS );
    verify( spySerializer ).writeFieldTransforms( spyMeta, json, provider );
    verify( json ).writeEndObject();
  }

  @Test
  public void testSerializeBasic_NullParent() throws Exception {
    when( spyMeta.getParentStepMeta() ).thenReturn( null );

    AbstractStepMetaJsonSerializer<BaseStepMeta> spySerializer = spy( serializer );

    spySerializer.serialize( spyMeta, json, provider );

    verify( json ).writeStartObject();
    verify( json, times( 0 ) ).writeStringField( CLASS, spyMeta.getClass().getName() );
    verify( json, times( 0 ) ).writeStringField( NAME, spyParent.getName() );
    verify( json, times( 0 ) ).writeStringField( eq( TYPE ), anyString() );

    // make sure the templated methods are called
    verify( spySerializer, times( 0 ) ).writeCustomProperties( spyMeta, json, provider );
    verify( spySerializer, times( 0 ) ).writeInputFields( spyParent, json );
    verify( spySerializer, times( 0 ) ).writeOutputFields( spyParent, json );

    verify( json, times( 0 ) ).writeArrayFieldStart( TRANSFORMS );
    verify( spySerializer, times( 0 ) ).writeFieldTransforms( spyMeta, json, provider );

    verify( json ).writeEndObject();
  }

  @Test
  public void testWriteInputFields() throws Exception {
    serializer = new BaseStepMetaJsonSerializer( BaseStepMeta.class );
    serializer.setLineageRepository( repo );

    when( spyParent.getParentTransMeta() ).thenReturn( spyParentTrans );

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    List<ValueMetaInterface> vml = new ArrayList<ValueMetaInterface>();
    ValueMetaInterface col1 = mock( ValueMetaInterface.class );
    ValueMetaInterface col2 = mock( ValueMetaInterface.class );
    ValueMetaInterface col3 = mock( ValueMetaInterface.class );
    vml.add( col1 );
    vml.add( col2 );
    vml.add( col3 );
    when( rmi.getValueMetaList() ).thenReturn( vml );

    when( spyParentTrans.getPrevStepFields( any( StepMeta.class ) ) ).thenReturn( rmi );
    serializer.writeInputFields( spyParent, json );

    verify( json, times( 3 ) ).writeObject( any( IFieldInfo.class ) );

  }

  @Test
  public void testWriteOutputFields() throws Exception {
    serializer = new BaseStepMetaJsonSerializer( BaseStepMeta.class );
    serializer.setLineageRepository( repo );

    when( spyParent.getParentTransMeta() ).thenReturn( spyParentTrans );

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    List<ValueMetaInterface> vml = new ArrayList<ValueMetaInterface>();
    ValueMetaInterface col1 = mock( ValueMetaInterface.class );
    ValueMetaInterface col2 = mock( ValueMetaInterface.class );
    ValueMetaInterface col3 = mock( ValueMetaInterface.class );
    vml.add( col1 );
    vml.add( col2 );
    vml.add( col3 );
    when( rmi.getValueMetaList() ).thenReturn( vml );

    when( spyParentTrans.getStepFields( any( StepMeta.class ) ) ).thenReturn( rmi );
    serializer.writeOutputFields( spyParent, json );

    verify( json, times( 3 ) ).writeObject( any( IFieldInfo.class ) );

  }

}
