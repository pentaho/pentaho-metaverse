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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pentaho.metaverse.impl.model.kettle.FieldInfo;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: RFellows Date: 11/17/14
 */
public abstract class AbstractStepMetaJsonSerializer<T extends BaseStepMeta> extends StdSerializer<T> {
  protected AbstractStepMetaJsonSerializer( Class<T> aClass ) {
    super( aClass );
  }

  protected AbstractStepMetaJsonSerializer( Class<T> aClass, LineageRepository repo ) {
    super( aClass );
    setLineageRepository( repo );
  }

  private LineageRepository lineageRepository;

  public LineageRepository getLineageRepository() {
    return lineageRepository;
  }

  public void setLineageRepository( LineageRepository repo ) {
    this.lineageRepository = repo;
  }

  @Override public void serialize( T meta, JsonGenerator json,
                                   SerializerProvider serializerProvider ) throws IOException, JsonGenerationException {
    json.writeStartObject();

    StepMeta parentStepMeta = meta.getParentStepMeta();
    if ( parentStepMeta != null ) {
      json.writeStringField( "@class", meta.getClass().getName() );
      json.writeStringField( "name", parentStepMeta.getName() );
      json.writeStringField( "type", getStepType( parentStepMeta ) );

      writeRepoAttributes( meta, json );

      writeCustomProperties( meta, json, serializerProvider );

      writeInputFields( parentStepMeta, json );
      writeOutputFields( parentStepMeta, json );

      json.writeArrayFieldStart( "transforms" );
      writeFieldTransforms( meta, json, serializerProvider );
      json.writeEndArray();

    }

    json.writeEndObject();

  }

  protected void writeRepoAttributes( T meta, JsonGenerator json ) throws IOException {

    String id = meta.getObjectId() == null ? meta.getParentStepMeta().getName() : meta.getObjectId().toString();
    ObjectId stepId = new StringObjectId( id );

    LineageRepository repo = getLineageRepository();
    if ( repo != null ) {
      Map<String, Object> attrs = repo.getStepAttributesCache( stepId );
      json.writeObjectField( "attributes", attrs );

      List<Map<String, Object>> fields = repo.getStepFieldsCache( stepId );
      json.writeObjectField( "fields", fields );
    }
  }

  /**
   * The wrapping array will already be constructed for you, just add serializations of the transforms
   * as objects.
   * <code>
   *   ...
   *   "transforms" : [
   *     // your transform objects here
   *   ]
   *   ...
   * </code>
   * @param meta
   * @param json
   * @param serializerProvider
   * @throws IOException
   * @throws JsonGenerationException
   */
  protected abstract void writeFieldTransforms( T meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException, JsonGenerationException;

  /**
   * Free-for-all. put anything specific to your StepMeta here.
   * @param meta
   * @param json
   * @param serializerProvider
   * @throws IOException
   * @throws JsonGenerationException
   */
  protected abstract void writeCustomProperties( T meta, JsonGenerator json,
      SerializerProvider serializerProvider ) throws IOException, JsonGenerationException;


  protected void writeInputFields( StepMeta parentStepMeta, JsonGenerator json ) throws IOException {
    TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
    if ( parentTransMeta != null ) {
      try {
        RowMetaInterface prevStepFields = parentTransMeta.getPrevStepFields( parentStepMeta );
        writeFields( json, prevStepFields, "inputFields" );
      } catch ( KettleStepException e ) {
        e.printStackTrace();
      }
    }
  }

  protected void writeOutputFields( StepMeta parentStepMeta, JsonGenerator json ) throws IOException {
    TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
    if ( parentTransMeta != null ) {
      try {
        RowMetaInterface stepFields = parentTransMeta.getStepFields( parentStepMeta );
        writeFields( json, stepFields, "outputFields" );
      } catch ( KettleStepException e ) {
        e.printStackTrace();
      }
    }
  }

  protected void writeFields( JsonGenerator json, RowMetaInterface fields, String arrayObjectName ) throws IOException {
    json.writeArrayFieldStart( arrayObjectName );
    List<ValueMetaInterface> valueMetaInterfaces = fields.getValueMetaList();
    for ( ValueMetaInterface valueMetaInterface : valueMetaInterfaces ) {
      FieldInfo fieldInfo = new FieldInfo( valueMetaInterface );
      json.writeObject( fieldInfo );
    }
    json.writeEndArray();
  }

  protected String getStepType( StepMeta parentStepMeta ) {
    String stepType = null;
    try {
      stepType = PluginRegistry.getInstance().findPluginWithId(
        StepPluginType.class, parentStepMeta.getStepID() ).getName();
    } catch ( Throwable t ) {
      stepType = parentStepMeta.getStepID();
    }
    return stepType;
  }
}
