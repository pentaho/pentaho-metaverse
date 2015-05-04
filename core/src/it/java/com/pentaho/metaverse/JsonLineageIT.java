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

import com.pentaho.metaverse.impl.model.kettle.json.KettleObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: RFellows Date: 10/31/14
 */
public class JsonLineageIT {

  private KettleObjectMapper mapper;

  @BeforeClass
  public static void init() throws Exception {
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution/system/pentahoObjects.spring.xml" );
  }

  @Before
  public void setUp() throws Exception {
    mapper = PentahoSystem.get( KettleObjectMapper.class, "kettleObjectMapper", null );
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

    // make sure that we got an external resource of file and DB type
    assertTrue( json.contains( "\"@class\" : \"com.pentaho.metaverse.api.model.BaseResourceInfo\"" ) );
    assertTrue( json.contains( "\"@class\" : \"com.pentaho.metaverse.api.model.JdbcResourceInfo\"" ) );

    // now deserialize it
    TransMeta rehydrated = mapper.readValue( json, TransMeta.class );

    assertEquals( tm.getName(), rehydrated.getName() );

    json = mapper.writeValueAsString( rehydrated );
    jsonOut = new File( IntegrationTestUtil.getOutputPath( tm.getName() + ".after.json" ) );
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

  @Test
  public void testGetXMLDataJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/validation/get_xml_data.ktr";
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
  public void testXMLOutputJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/validation/xml_output.ktr";
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
  public void testFilterRowsJsonSerialize() throws Exception {

    String ktrPath = "src/it/resources/repo/validation/filter_rows.ktr";
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
