/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.impl.model.kettle.json.KettleObjectMapper;
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
    assertTrue( json.contains( "\"@class\" : \"org.pentaho.metaverse.api.model.BaseResourceInfo\"" ) );
    assertTrue( json.contains( "\"@class\" : \"org.pentaho.metaverse.api.model.JdbcResourceInfo\"" ) );

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
