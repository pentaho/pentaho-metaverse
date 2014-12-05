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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import com.pentaho.metaverse.impl.model.kettle.json.BaseStepMetaJsonSerializer;
import com.pentaho.metaverse.impl.model.kettle.json.TableOutputStepMetaJsonSerializer;
import com.pentaho.metaverse.impl.model.kettle.json.TransMetaJsonDeserializer;
import com.pentaho.metaverse.impl.model.kettle.json.TransMetaJsonSerializer;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * User: RFellows Date: 10/31/14
 */
public class JsonLineageIT {

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void testJsonSerialze() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable( SerializationFeature.INDENT_OUTPUT );
    mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    mapper.enable( SerializationFeature.WRAP_EXCEPTIONS );

    LineageRepository writeRepo = new LineageRepository();
    LineageRepository readRepo = new LineageRepository();

    SimpleModule transModule = new SimpleModule( "PDIModule", new Version( 1, 0, 0, null ) );
    TransMetaJsonSerializer transMetaJsonSerializer = new TransMetaJsonSerializer( TransMeta.class );
    transMetaJsonSerializer.setLineageRepository( writeRepo );
    transModule.addSerializer( transMetaJsonSerializer );

    BaseStepMetaJsonSerializer baseStepMetaJsonSerializer = new BaseStepMetaJsonSerializer( BaseStepMeta.class );

    baseStepMetaJsonSerializer.setLineageRepository( writeRepo );
    transModule.addSerializer( baseStepMetaJsonSerializer );

    transModule.addSerializer( new TableOutputStepMetaJsonSerializer( TableOutputMeta.class, writeRepo ) );

    transModule.addDeserializer( TransMeta.class, new TransMetaJsonDeserializer( TransMeta.class, readRepo ) );

    mapper.registerModule( transModule );

    String ktrPath = "src/it/resources/repo/samples/file_to_table.ktr";
    TransMeta tm = new TransMeta( ktrPath, null, true, null, null );

    String json = mapper.writeValueAsString( tm );
    File jsonOut = new File( "src/it/resources/tmp/" + tm.getName() + ".json" );
    FileUtils.writeStringToFile( jsonOut, json );

    // now deserilaize it
    TransMeta rehydrated = mapper.readValue( json, TransMeta.class );

//    String ktr = rehydrated.getXML();
//    FileUtils.writeStringToFile( new File( "src/it/resources/tmp/" + tm.getName() + ".after.ktr" ), ktr );
    assertEquals( tm.getName(), rehydrated.getName() );

    json = mapper.writeValueAsString( rehydrated );
    jsonOut = new File( "src/it/resources/tmp/" + tm.getName() + ".after.json" );
    FileUtils.writeStringToFile( jsonOut, json );
  }
}
