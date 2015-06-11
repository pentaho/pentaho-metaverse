/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * User: RFellows Date: 2/16/15
 */
public class KettleObjectMapper {

  private ObjectMapper mapper;
  private SimpleModule transModule;

  public KettleObjectMapper( List<StdSerializer> serializers, List<StdDeserializer> deserializers ) {
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
