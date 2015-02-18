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
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.ExternalResourceConsumerMap;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.IStepExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.step.GenericStepMetaAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzerProvider;
import com.pentaho.metaverse.analyzer.kettle.step.IFieldLineageMetadataProvider;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.model.IInfo;
import com.pentaho.metaverse.api.model.kettle.IFieldInfo;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.impl.model.kettle.FieldMapping;
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
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * User: RFellows Date: 12/1/14
 */
@RunWith( MockitoJUnitRunner.class )
public class AbstractStepMetaJsonSerializerTest {

  public static final String STEP_META_NAME = "StepMetaName";

  AbstractStepMetaJsonSerializer<BaseStepMeta> serializer;
  LineageRepository repo;
  @Mock
  JsonGenerator json;
  @Mock
  SerializerProvider provider;
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
    IFieldLineageMetadataProvider mapper = mock( IFieldLineageMetadataProvider.class );
    when( spySerializer.getFieldLineageMetadataProvider( spyMeta ) ).thenReturn( mapper );
    when( mapper.getInputFields( spyMeta ) ).thenReturn( new HashMap<String, RowMetaInterface>() );

    spySerializer.serialize( spyMeta, json, provider );

    verify( json ).writeStartObject();
    verify( json ).writeStringField( IInfo.JSON_PROPERTY_CLASS, spyMeta.getClass().getName() );
    verify( json ).writeStringField( IInfo.JSON_PROPERTY_NAME, spyParent.getName() );
    verify( json ).writeStringField( eq( AbstractStepMetaJsonSerializer.JSON_PROPERTY_TYPE ), anyString() );

    // make sure the templated methods are called
    verify( spySerializer ).writeRepoAttributes( spyMeta, json );
    verify( spySerializer ).writeCustomProperties( spyMeta, json, provider );
    verify( spySerializer ).writeInputFields( spyMeta, json );
    verify( spySerializer ).writeOutputFields( spyParent, json );

    verify( json ).writeArrayFieldStart( AbstractStepMetaJsonSerializer.JSON_PROPERTY_TRANSFORMS );
    verify( spySerializer ).writeFieldTransforms( spyMeta, json, provider );
    verify( json ).writeEndObject();

    verify( spySerializer ).writeExternalResources( spyMeta, json, provider );

  }

  @Test
  public void testSerializeBasic_NullParent() throws Exception {
    when( spyMeta.getParentStepMeta() ).thenReturn( null );

    AbstractStepMetaJsonSerializer<BaseStepMeta> spySerializer = spy( serializer );

    spySerializer.serialize( spyMeta, json, provider );

    verify( json ).writeStartObject();
    verify( json, times( 0 ) ).writeStringField( IInfo.JSON_PROPERTY_CLASS, spyMeta.getClass().getName() );
    verify( json, times( 0 ) ).writeStringField( IInfo.JSON_PROPERTY_NAME, spyParent.getName() );
    verify( json, times( 0 ) ).writeStringField( eq( AbstractStepMetaJsonSerializer.JSON_PROPERTY_TYPE ), anyString() );

    // make sure the templated methods are called
    verify( spySerializer, times( 0 ) ).writeCustomProperties( spyMeta, json, provider );
    verify( spySerializer, times( 0 ) ).writeInputFields( spyMeta, json );
    verify( spySerializer, times( 0 ) ).writeOutputFields( spyParent, json );

    verify( json, times( 0 ) ).writeArrayFieldStart( AbstractStepMetaJsonSerializer.JSON_PROPERTY_TRANSFORMS );
    verify( spySerializer, times( 0 ) ).writeFieldTransforms( spyMeta, json, provider );

    verify( json ).writeEndObject();

  }

  @Test
  public void testWriteInputFields() throws Exception {
    serializer = new BaseStepMetaJsonSerializer( BaseStepMeta.class );
    serializer.setLineageRepository( repo );

    when( spyParent.getParentTransMeta() ).thenReturn( spyParentTrans );

    IFieldLineageMetadataProvider mapper = mock( IFieldLineageMetadataProvider.class );
    AbstractStepMetaJsonSerializer spy = spy( serializer );
    when( spy.getFieldLineageMetadataProvider( spyMeta ) ).thenReturn( mapper );

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    List<ValueMetaInterface> vml = new ArrayList<ValueMetaInterface>();
    ValueMetaInterface col1 = mock( ValueMetaInterface.class );
    ValueMetaInterface col2 = mock( ValueMetaInterface.class );
    ValueMetaInterface col3 = mock( ValueMetaInterface.class );
    vml.add( col1 );
    vml.add( col2 );
    vml.add( col3 );
    when( rmi.getValueMetaList() ).thenReturn( vml );

    HashMap<String, RowMetaInterface> fieldMetaMap = new HashMap<String, RowMetaInterface>( 1 );
    fieldMetaMap.put( "prev step name", rmi );
    when( mapper.getInputFields( spyMeta ) ).thenReturn( fieldMetaMap );
    spy.writeInputFields( spyMeta, json );

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

  @Test
  public void testWriteExternalResources() throws Exception {
    ExternalResourceConsumerMap mockConsumerMap = mock( ExternalResourceConsumerMap.class );
    Queue<IStepExternalResourceConsumer> consumers = new ConcurrentLinkedQueue<IStepExternalResourceConsumer>();
    Set<IExternalResourceInfo> externalResources = new HashSet<IExternalResourceInfo>();

    IExternalResourceInfo info = mock( IExternalResourceInfo.class );
    externalResources.add( info );

    IStepExternalResourceConsumer consumer = mock( IStepExternalResourceConsumer.class );
    when( consumer.getResourcesFromMeta( anyObject() ) ).thenReturn( externalResources );
    consumers.add( consumer );

    Class<? extends BaseStepMeta> stepMetaClass = BaseStepMeta.class;
    when( mockConsumerMap.getStepExternalResourceConsumers( any( stepMetaClass.getClass() ) ) ).thenReturn(
      consumers );

    serializer.setExternalResourceConsumerMap( mockConsumerMap );

    serializer.writeExternalResources( spyMeta, json, provider );

    verify( mockConsumerMap ).getStepExternalResourceConsumers( any( stepMetaClass.getClass() ) );
    verify( json ).writeArrayFieldStart( AbstractStepMetaJsonSerializer.JSON_PROPERTY_EXTERNAL_RESOURCES );
    verify( consumer ).getResourcesFromMeta( anyObject() );
    verify( json, times( externalResources.size() ) ).writeObject( any( IExternalResourceInfo.class ) );
    verify( json ).writeEndArray();
  }

  @Test
  public void testGetStepFieldMapper_noProviderAvailable() throws Exception {
    IStepAnalyzerProvider provider = mock( IStepAnalyzerProvider.class );
    when( provider.getAnalyzers( any( Set.class ) ) ).thenReturn( null );

    serializer.setStepAnalyzerProvider( provider );
    IFieldLineageMetadataProvider handler = serializer.getFieldLineageMetadataProvider( spyMeta );
    assertTrue( handler instanceof GenericStepMetaAnalyzer );
  }

  @Test
  public void testGetStepFieldMapper() throws Exception {
    IStepAnalyzerProvider provider = mock( IStepAnalyzerProvider.class );
    IStepAnalyzer<DummyTransMeta> analyzer = mock( IStepAnalyzer.class, withSettings().extraInterfaces( IFieldLineageMetadataProvider.class ) );
    Set<IStepAnalyzer> analyzers = new HashSet<IStepAnalyzer>( 1 );
    analyzers.add( analyzer );
    when( provider.getAnalyzers( any( Set.class ) ) ).thenReturn( analyzers );

    serializer.setStepAnalyzerProvider( provider );
    IFieldLineageMetadataProvider handler = serializer.getFieldLineageMetadataProvider( spyMeta );
    assertFalse( handler instanceof GenericStepMetaAnalyzer );
  }

  @Test
  public void testWriteFieldTransforms() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = new HashSet<ComponentDerivationRecord>();

    ComponentDerivationRecord change1 = mock( ComponentDerivationRecord.class );
    ComponentDerivationRecord change2 = mock( ComponentDerivationRecord.class );

    when( change1.hasDelta() ).thenReturn( true );
    when( change2.hasDelta() ).thenReturn( false );

    changeRecords.add( change1 );
    changeRecords.add( change2 );

    IFieldLineageMetadataProvider mapper = mock( IFieldLineageMetadataProvider.class );
    AbstractStepMetaJsonSerializer spy = spy( serializer );
    when( spy.getFieldLineageMetadataProvider( spyMeta ) ).thenReturn( mapper );
    when( mapper.getChangeRecords( spyMeta ) ).thenReturn( changeRecords );

    spy.writeFieldTransforms( spyMeta, json, provider );

    verify( json ).writeObject( change1 );
    verify( json, never() ).writeObject( change2 );

  }

  @Test
  public void testWriteFieldMappings() throws Exception {
    Set<IFieldMapping> mappings = new HashSet<IFieldMapping>();
    FieldMapping fieldMapping1 = new FieldMapping( "full name", "first name" );
    FieldMapping fieldMapping2 = new FieldMapping( "full name", "last name" );
    mappings.add( fieldMapping1 );
    mappings.add( fieldMapping2 );

    IFieldLineageMetadataProvider mapper = mock( IFieldLineageMetadataProvider.class );
    AbstractStepMetaJsonSerializer spy = spy( serializer );
    when( spy.getFieldLineageMetadataProvider( spyMeta ) ).thenReturn( mapper );
    when( mapper.getFieldMappings( spyMeta ) ).thenReturn( mappings );

    spy.writeFieldMappings( spyMeta, json, provider );

    verify( json ).writeObject( fieldMapping1 );
    verify( json ).writeObject( fieldMapping2 );

  }
}
