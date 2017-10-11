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

package org.pentaho.metaverse.impl.model.kettle.json;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.metaverse.analyzer.kettle.step.GenericStepMetaAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.StepExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IFieldLineageMetadataProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.IInfo;
import org.pentaho.metaverse.api.model.kettle.FieldMapping;
import org.pentaho.metaverse.api.model.kettle.IFieldInfo;
import org.pentaho.metaverse.api.model.kettle.IFieldMapping;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    StepExternalResourceConsumerProvider mockConsumerMap = mock( StepExternalResourceConsumerProvider.class );
    List<IStepExternalResourceConsumer> consumers = new ArrayList<IStepExternalResourceConsumer>();
    Set<IExternalResourceInfo> externalResources = new HashSet<IExternalResourceInfo>();

    IExternalResourceInfo info = mock( IExternalResourceInfo.class );
    externalResources.add( info );

    IStepExternalResourceConsumer consumer = mock( IStepExternalResourceConsumer.class );
    when( consumer.getResourcesFromMeta( anyObject() ) ).thenReturn( externalResources );
    consumers.add( consumer );

    Class<? extends BaseStepMeta> stepMetaClass = BaseStepMeta.class;
    when( mockConsumerMap.getExternalResourceConsumers( any( Collection.class ) ) ).thenReturn( consumers );

    serializer.setStepExternalResourceConsumerProvider( mockConsumerMap );

    serializer.writeExternalResources( spyMeta, json, provider );

    verify( mockConsumerMap ).getExternalResourceConsumers( any( Collection.class ) );
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
    List<IStepAnalyzer> analyzers = new ArrayList<IStepAnalyzer>( 1 );
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
