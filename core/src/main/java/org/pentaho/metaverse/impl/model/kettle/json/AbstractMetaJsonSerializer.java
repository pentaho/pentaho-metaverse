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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.model.BaseResourceInfo;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IInfo;
import org.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * User: RFellows Date: 12/15/14
 */
public abstract class AbstractMetaJsonSerializer<T extends AbstractMeta> extends StdSerializer<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger( AbstractMetaJsonSerializer.class );

  public static final String JSON_PROPERTY_PARAMETERS = "parameters";
  public static final String JSON_PROPERTY_STEPS = "steps";
  public static final String JSON_PROPERTY_CONNECTIONS = "connections";
  public static final String JSON_PROPERTY_HOPS = "hops";
  public static final String JSON_PROPERTY_VARIABLES = "variables";
  public static final String JSON_PROPERTY_CREATED_DATE = DictionaryConst.PROPERTY_CREATED;
  public static final String JSON_PROPERTY_LAST_MODIFIED_DATE = DictionaryConst.PROPERTY_LAST_MODIFIED;
  public static final String JSON_PROPERTY_CREATED_BY = DictionaryConst.PROPERTY_CREATED_BY;
  public static final String JSON_PROPERTY_LAST_MODIFIED_BY = DictionaryConst.PROPERTY_LAST_MODIFIED_BY;
  public static final String JSON_PROPERTY_PATH = DictionaryConst.PROPERTY_PATH;
  public static final String JSON_PROPERTY_REPOSITORY = "repository";

  private LineageRepository lineageRepository;

  protected AbstractMetaJsonSerializer( Class<T> aClass ) {
    super( aClass );
  }

  protected AbstractMetaJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  protected AbstractMetaJsonSerializer( Class<?> aClass, boolean b ) {
    super( aClass, b );
  }


  public LineageRepository getLineageRepository() {
    return lineageRepository;
  }

  public void setLineageRepository( LineageRepository repo ) {
    this.lineageRepository = repo;
  }

  @Override
  public void serialize( T meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException, JsonGenerationException {

    json.writeStartObject();
    json.writeStringField( IInfo.JSON_PROPERTY_CLASS, meta.getClass().getName() );
    json.writeStringField( IInfo.JSON_PROPERTY_NAME, meta.getName() );
    json.writeStringField( IInfo.JSON_PROPERTY_DESCRIPTION, meta.getDescription() );
    json.writeObjectField( JSON_PROPERTY_CREATED_DATE, meta.getCreatedDate() );
    json.writeObjectField( JSON_PROPERTY_LAST_MODIFIED_DATE, meta.getModifiedDate() );
    json.writeStringField( JSON_PROPERTY_CREATED_BY, meta.getCreatedUser() );
    json.writeStringField( JSON_PROPERTY_LAST_MODIFIED_BY, meta.getModifiedUser() );
    json.writeStringField( JSON_PROPERTY_PATH, meta.getFilename() );
    if ( meta.getRepository() != null ) {
      json.writeStringField( JSON_PROPERTY_REPOSITORY, meta.getRepository().getName() );
    }

    serializeParameters( meta, json );
    serializeVariables( meta, json );
    serializeSteps( meta, json );
    serializeConnections( meta, json );
    serializeHops( meta, json );

    json.writeEndObject();
  }

  protected abstract void serializeHops( T meta, JsonGenerator json ) throws IOException;

  protected void serializeConnections( T meta, JsonGenerator json ) throws IOException {
    // connections
    json.writeArrayFieldStart( JSON_PROPERTY_CONNECTIONS );
    for ( DatabaseMeta dbmeta : meta.getDatabases() ) {
      BaseResourceInfo resourceInfo = (BaseResourceInfo) ExternalResourceInfoFactory.createDatabaseResource( dbmeta );
      resourceInfo.setInput( true );
      json.writeObject( resourceInfo );
    }
    json.writeEndArray();
  }

  protected abstract void serializeSteps( T meta, JsonGenerator json ) throws IOException;

  protected abstract List<String> getUsedVariables( T meta );

  protected void serializeVariables( T meta, JsonGenerator json ) throws IOException {
    json.writeArrayFieldStart( JSON_PROPERTY_VARIABLES );
    List<String> variables = getUsedVariables( meta );
    if ( variables != null ) {
      for ( String param : variables ) {
        ParamInfo paramInfo = new ParamInfo( param, meta.getVariable( param ) );
        json.writeObject( paramInfo );
      }
    }
    json.writeEndArray();
  }

  protected void serializeParameters( T meta, JsonGenerator json ) throws IOException {
    json.writeArrayFieldStart( JSON_PROPERTY_PARAMETERS );
    String[] parameters = meta.listParameters();
    if ( parameters != null ) {
      for ( String param : parameters ) {
        try {
          ParamInfo paramInfo = new ParamInfo( param, null, meta.getParameterDefault( param ),
              meta.getParameterDescription( param ) );
          json.writeObject( paramInfo );
        } catch ( UnknownParamException e ) {
          LOGGER.warn( Messages.getString( "WARNING.Serialization.Trans.Param", param ), e );
        }
      }
    }
    json.writeEndArray();
  }
}
