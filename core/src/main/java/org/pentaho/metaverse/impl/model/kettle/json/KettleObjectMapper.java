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


package org.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: RFellows Date: 2/16/15
 */
public class KettleObjectMapper {

  private ObjectMapper mapper;
  private SimpleModule transModule;

  private static KettleObjectMapper instance;

  public static KettleObjectMapper getInstance() {
    if ( null == instance ) {
      instance = new KettleObjectMapper();
    }
    return instance;
  }

  private KettleObjectMapper() {
    List<StdSerializer> serializers = new ArrayList<>();
    serializers.add( BaseStepMetaJsonSerializer.getInstance() );
    serializers.add( TableOutputStepMetaJsonSerializer.getInstance() );
    serializers.add( TransMetaJsonSerializer.getInstance() );
    serializers.add( JobEntryBaseJsonSerializer.getInstance() );
    serializers.add( JobMetaJsonSerializer.getInstance() );

    List<StdDeserializer> deserializers = new ArrayList<>();
    deserializers.add( TransMetaJsonDeserializer.getInstance() );
    doInit( serializers, deserializers );
  }

  @VisibleForTesting
  KettleObjectMapper( List<StdSerializer> serializers, List<StdDeserializer> deserializers ) {
    doInit( serializers, deserializers );
  }

  private void doInit( List<StdSerializer> serializers, List<StdDeserializer> deserializers ) {
    mapper = new ObjectMapper();
    mapper.enable( SerializationFeature.INDENT_OUTPUT );
    mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    mapper.enable( SerializationFeature.WRAP_EXCEPTIONS );

    transModule = new SimpleModule( "PDIModule", new Version( 1, 0, 0, null ) );

    if ( !CollectionUtils.isEmpty( serializers ) ) {
      for ( StdSerializer serializer : serializers ) {
        transModule.addSerializer( serializer );
      }
    }

    if ( !CollectionUtils.isEmpty( deserializers ) ) {
      for ( StdDeserializer deserializer : deserializers ) {
        transModule.addDeserializer( deserializer.getValueClass(), deserializer );
      }
    }

    mapper.registerModule( transModule );
  }

  public <T> T readValue( String json, Class<T> clazz ) throws IOException {
    return mapper.readValue( json, clazz );
  }

  public String writeValueAsString( Object obj ) throws JsonProcessingException {
    return mapper.writeValueAsString( obj );
  }
}
