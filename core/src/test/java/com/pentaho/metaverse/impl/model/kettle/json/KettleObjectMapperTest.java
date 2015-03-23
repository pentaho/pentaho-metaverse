/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class KettleObjectMapperTest {

  private static final String TRANS_JSON = "{\n" +
    "  \"@class\" : \"org.pentaho.di.trans.TransMeta\",\n" +
    "  \"name\" : \"mongo_input\",\n" +
    "  \"description\" : null,\n" +
    "  \"created\" : 1422284845742,\n" +
    "  \"lastmodified\" : 1422284845742,\n" +
    "  \"createdby\" : \"-\",\n" +
    "  \"lastmodifiedby\" : \"-\",\n" +
    "  \"path\" : \"src/it/resources/repo/validation/mongo_input.ktr\",\n" +
    "  \"parameters\" : [ ],\n" +
    "  \"variables\" : [ ],\n" +
    "  \"steps\" : [ ],\n" +
    "  \"connections\" : [  ],\n" +
    "  \"hops\" : [  ]\n" +
    "}";

  KettleObjectMapper mapper;

  List<StdSerializer> serializers;

  List<StdDeserializer> deserializers;

  @Before
  public void setUp() throws Exception {
    serializers = new ArrayList<StdSerializer>();
    deserializers = new ArrayList<StdDeserializer>();
  }

  @Test
  public void testConstructor() {
    mapper = new KettleObjectMapper( null, null );
    mapper = new KettleObjectMapper( serializers, null );
    mapper = new KettleObjectMapper( serializers, deserializers );
  }

  @Test
  public void testReadValue() throws Exception {
    StdDeserializer deserializer = new TransMetaJsonDeserializer( TransMeta.class, null );
    deserializers.add( deserializer );
    mapper = new KettleObjectMapper( null, deserializers );
    assertNotNull( mapper );
    mapper.readValue( TRANS_JSON, TransMeta.class );
  }

  @Test
  public void testWriteValueAsString() throws Exception {
    StdSerializer serializer = new TransMetaJsonSerializer( TransMeta.class );
    serializers.add( serializer );
    mapper = new KettleObjectMapper( serializers, null );
    mapper.writeValueAsString( new TransMeta() );
  }
}
