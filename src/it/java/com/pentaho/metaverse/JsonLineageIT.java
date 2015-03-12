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

package com.pentaho.metaverse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.ExternalResourceConsumerMap;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.IExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.MetaverseKettleLifecycleHandler;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginRegistrar;
import com.pentaho.metaverse.analyzer.kettle.plugin.ExternalResourceConsumerPluginType;
import com.pentaho.metaverse.analyzer.kettle.step.csvfileinput.CsvFileInputExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputExternalResourceConsumer;
import com.pentaho.metaverse.analyzer.kettle.step.textfileinput.TextFileInputExternalResourceConsumer;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import com.pentaho.metaverse.impl.model.kettle.json.AbstractStepMetaJsonSerializer;
import com.pentaho.metaverse.impl.model.kettle.json.BaseStepMetaJsonSerializer;
import com.pentaho.metaverse.impl.model.kettle.json.JobEntryBaseJsonSerializer;
import com.pentaho.metaverse.impl.model.kettle.json.JobMetaJsonSerializer;
import com.pentaho.metaverse.impl.model.kettle.json.TableOutputStepMetaJsonSerializer;
import com.pentaho.metaverse.impl.model.kettle.json.TransMetaJsonDeserializer;
import com.pentaho.metaverse.impl.model.kettle.json.TransMetaJsonSerializer;
import com.pentaho.metaverse.util.MetaverseUtil;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * User: RFellows Date: 10/31/14
 */
public class JsonLineageIT {

  private ObjectMapper mapper;

  @BeforeClass
  public static void init() throws Exception {
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution/system/pentahoObjects.spring.xml" );

    PluginRegistry registry = PluginRegistry.getInstance();
    MetaverseKettleLifecycleHandler metaverseKettleLifecycleHandler = new MetaverseKettleLifecycleHandler();

    ExternalResourceConsumerPluginRegistrar registrar = new ExternalResourceConsumerPluginRegistrar();
    registrar.init( registry );
    PluginRegistry.init();

    TextFileInputExternalResourceConsumer tfiConsumer = new TextFileInputExternalResourceConsumer();
    TableOutputExternalResourceConsumer toConsumer = new TableOutputExternalResourceConsumer();
    CsvFileInputExternalResourceConsumer csviConsumer = new CsvFileInputExternalResourceConsumer();

    // Create a fake plugin to exercise the map building logic for Text File Input
    PluginInterface mockPlugin = mock( PluginInterface.class );
    when( mockPlugin.getIds() ).thenReturn( new String[]{ "TextFileInputExternalResourceConsumer" } );
    when( mockPlugin.getName() ).thenReturn( "TextFileInputExternalResourceConsumer" );
    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
    classMap.put( IExternalResourceConsumer.class, tfiConsumer.getClass().getName() );

    doReturn( IExternalResourceConsumer.class ).when( mockPlugin ).getMainType();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockPlugin );

    // Then add a class to the map to get through plugin registration
    when( mockPlugin.getClassMap() ).thenReturn( classMap );

    // Create a fake plugin to exercise the map building logic for Table Output
    PluginInterface mockPlugin2 = mock( PluginInterface.class );
    when( mockPlugin2.getIds() ).thenReturn( new String[]{ "TableOutputExternalResourceConsumer" } );
    when( mockPlugin2.getName() ).thenReturn( "TableOutputExternalResourceConsumer" );
    Map<Class<?>, String> classMap2 = new HashMap<Class<?>, String>();
    classMap2.put( IExternalResourceConsumer.class, toConsumer.getClass().getName() );

    doReturn( IExternalResourceConsumer.class ).when( mockPlugin2 ).getMainType();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockPlugin2 );

    // Then add a class to the map to get through plugin registration
    when( mockPlugin2.getClassMap() ).thenReturn( classMap2 );

    // Create a fake plugin to exercise the map building logic for CSV Input
    PluginInterface mockPlugin3 = mock( PluginInterface.class );
    when( mockPlugin3.getIds() ).thenReturn( new String[]{ "CsvFileInputExternalResourceConsumer" } );
    when( mockPlugin3.getName() ).thenReturn( "CsvFileInputExternalResourceConsumer" );
    Map<Class<?>, String> classMap3 = new HashMap<Class<?>, String>();
    classMap3.put( IExternalResourceConsumer.class, csviConsumer.getClass().getName() );

    doReturn( IExternalResourceConsumer.class ).when( mockPlugin3 ).getMainType();
    registry.registerPlugin( ExternalResourceConsumerPluginType.class, mockPlugin3 );

    // Then add a class to the map to get through plugin registration
    when( mockPlugin3.getClassMap() ).thenReturn( classMap3 );

    metaverseKettleLifecycleHandler.onEnvironmentInit();

  }

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    mapper.enable( SerializationFeature.INDENT_OUTPUT );
    mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    mapper.enable( SerializationFeature.WRAP_EXCEPTIONS );

    LineageRepository writeRepo = new LineageRepository();
    LineageRepository readRepo = new LineageRepository();

    SimpleModule transModule = new SimpleModule( "PDIModule", new Version( 1, 0, 0, null ) );
    TransMetaJsonSerializer transMetaJsonSerializer = new TransMetaJsonSerializer( TransMeta.class );
    transMetaJsonSerializer.setLineageRepository( writeRepo );
    transModule.addSerializer( transMetaJsonSerializer );

    JobMetaJsonSerializer jobMetaJsonSerializer = new JobMetaJsonSerializer( JobMeta.class );
    jobMetaJsonSerializer.setLineageRepository( writeRepo );
    transModule.addSerializer( jobMetaJsonSerializer );

    BaseStepMetaJsonSerializer baseStepMetaJsonSerializer = new BaseStepMetaJsonSerializer( BaseStepMeta.class );
    JobEntryBaseJsonSerializer jobEntryBaseJsonSerializer = new JobEntryBaseJsonSerializer(
      JobEntryBase.class );

    baseStepMetaJsonSerializer.setLineageRepository( writeRepo );
    jobEntryBaseJsonSerializer.setLineageRepository( writeRepo );
    transModule.addSerializer( baseStepMetaJsonSerializer );
    transModule.addSerializer( jobEntryBaseJsonSerializer );

    transModule.addSerializer( new TableOutputStepMetaJsonSerializer( TableOutputMeta.class, writeRepo ) );
    transModule.addDeserializer( TransMeta.class, new TransMetaJsonDeserializer( TransMeta.class, readRepo ) );

    transModule.addSerializer(
      new AbstractStepMetaJsonSerializer<CsvInputMeta>( CsvInputMeta.class, writeRepo ) {
        @Override
        protected void writeCustomProperties(
          CsvInputMeta meta, JsonGenerator json, SerializerProvider serializerProvider )
          throws IOException {

        }
      }
    );

    mapper.registerModule( transModule );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  @Test
  public void testFileToTableJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/samples/file_to_table.ktr";
    TransMeta tm = new TransMeta( ktrPath, null, true, null, null );

    String json = mapper.writeValueAsString( tm );
    File jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".json" ) );
    FileUtils.writeStringToFile( jsonOut, json );

    // now deserialize it
    TransMeta rehydrated = mapper.readValue( json, TransMeta.class );

    assertEquals( tm.getName(), rehydrated.getName() );

    json = mapper.writeValueAsString( rehydrated );
    jsonOut = new File(IntegrationTestUtil.getOutputPath( tm.getName() + ".after.json" ) );
    FileUtils.writeStringToFile( jsonOut, json );
  }

  @Test
  public void testGettingStartedJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/demo/Getting Started Transformation.ktr";
    TransMeta tm = new TransMeta( ktrPath, null, true, null, null );

    String json = mapper.writeValueAsString( tm );
    File jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".json" ) );
    FileUtils.writeStringToFile( jsonOut, json );

    // now deserialize it
    TransMeta rehydrated = mapper.readValue( json, TransMeta.class );

    assertEquals( tm.getName(), rehydrated.getName() );

    json = mapper.writeValueAsString( rehydrated );
    jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".after.json" ) );
    FileUtils.writeStringToFile( jsonOut, json );
  }

  @Test
  public void testMergeJoinJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/validation/merge_join.ktr";
    TransMeta tm = new TransMeta( ktrPath, null, true, null, null );

    String json = mapper.writeValueAsString( tm );
    File jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".json" ) );
    FileUtils.writeStringToFile( jsonOut, json );

    // now deserialize it
    TransMeta rehydrated = mapper.readValue( json, TransMeta.class );

    assertEquals( tm.getName(), rehydrated.getName() );

    json = mapper.writeValueAsString( rehydrated );
    jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".after.json" ) );
    FileUtils.writeStringToFile( jsonOut, json );
  }

  @Test
  public void testJobSerialization() throws Exception {
    String kjbPath = "src/it/resources/repo/samples/launch_transformation_job.kjb";
    JobMeta meta = new JobMeta( kjbPath, null );

    String json = mapper.writeValueAsString( meta );
    File jsonOut = new File( IntegrationTestUtil.getOutputPath( meta.getName() + ".json" ) );
    FileUtils.writeStringToFile( jsonOut, json );

    // TODO: now deserialize it

  }

  @Test
  public void testCsvInputJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/CSV input.ktr";
    TransMeta tm = new TransMeta( ktrPath, null, true, null, null );

    String json = mapper.writeValueAsString( tm );
    File jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".json" ) );
    FileUtils.writeStringToFile( jsonOut, json );

    // now deserialize it
    TransMeta rehydrated = mapper.readValue( json, TransMeta.class );

    assertEquals( tm.getName(), rehydrated.getName() );

    json = mapper.writeValueAsString( rehydrated );
    jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".after.json" ) );
    FileUtils.writeStringToFile( jsonOut, json );
  }

  @Test
  public void testMongoDbInputJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/validation/mongo_input.ktr";
    TransMeta tm = new TransMeta( ktrPath, null, true, null, null );

    String json = mapper.writeValueAsString( tm );
    File jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".json" ) );
    FileUtils.writeStringToFile( jsonOut, json );

    // now deserialize it
    TransMeta rehydrated = mapper.readValue( json, TransMeta.class );

    assertEquals( tm.getName(), rehydrated.getName() );

    json = mapper.writeValueAsString( rehydrated );
    jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".after.json" ) );
    FileUtils.writeStringToFile( jsonOut, json );
  }
}
