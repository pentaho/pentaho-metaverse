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
import com.pentaho.metaverse.api.model.IInfo;
import com.pentaho.metaverse.impl.model.BaseResourceInfo;
import com.pentaho.metaverse.impl.model.DatabaseResourceInfoUtil;
import com.pentaho.metaverse.impl.model.ParamInfo;
import com.pentaho.metaverse.impl.model.kettle.HopInfo;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * User: RFellows Date: 11/17/14
 */
public class TransMetaJsonSerializer extends StdSerializer<TransMeta> {

  public static final String JSON_PROPERTY_PARAMETERS = "parameters";
  public static final String JSON_PROPERTY_STEPS = "steps";
  public static final String JSON_PROPERTY_CONNECTIONS = "connections";
  public static final String JSON_PROPERTY_HOPS = "hops";
  public static final String JSON_PROPERTY_VARIABLES = "variables";

  private static final Logger LOGGER = LoggerFactory.getLogger( TransMetaJsonSerializer.class );

  public TransMetaJsonSerializer( Class<TransMeta> aClass ) {
    super( aClass );
  }

  private LineageRepository lineageRepository;

  public LineageRepository getLineageRepository() {
    return lineageRepository;
  }

  public void setLineageRepository( LineageRepository repo ) {
    this.lineageRepository = repo;
  }

  @Override
  public void serialize( TransMeta meta, JsonGenerator json,
                                   SerializerProvider serializerProvider ) throws IOException, JsonGenerationException {

    json.writeStartObject();
    json.writeStringField( IInfo.JSON_PROPERTY_CLASS, meta.getClass().getName() );
    json.writeStringField( IInfo.JSON_PROPERTY_NAME, meta.getName() );
    json.writeStringField( IInfo.JSON_PROPERTY_DESCRIPTION, meta.getDescription() );

    serializeParameters( meta, json );
    serializeVariables( meta, json );
    serializeSteps( meta, json );
    serializeConnections( meta, json );
    serializeHops( meta, json );

    json.writeEndObject();

  }

  protected void serializeParameters( TransMeta meta, JsonGenerator json ) throws IOException {
    json.writeArrayFieldStart( JSON_PROPERTY_PARAMETERS );
    String[] parameters = meta.listParameters();
    if ( parameters != null ) {
      for ( String param : meta.listParameters() ) {
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

  protected void serializeVariables( TransMeta meta, JsonGenerator json ) throws IOException {
    json.writeArrayFieldStart( JSON_PROPERTY_VARIABLES );
    List<String> variables = meta.getUsedVariables();
    if ( variables != null ) {
      for ( String param : variables ) {
        ParamInfo paramInfo = new ParamInfo( param, meta.getVariable( param ) );
        json.writeObject( paramInfo );
      }
    }
    json.writeEndArray();
  }

  protected void serializeSteps( TransMeta meta, JsonGenerator json ) throws IOException {
    json.writeArrayFieldStart( JSON_PROPERTY_STEPS );
    for ( StepMeta stepMeta : meta.getSteps() ) {
      BaseStepMeta step = getBaseStepMetaFromStepMeta( stepMeta );
      LineageRepository repo = getLineageRepository();
      String id = stepMeta.getObjectId() == null ? stepMeta.getName() : stepMeta.getObjectId().toString();
      ObjectId stepId = new StringObjectId( id );
      try {
        step.saveRep( repo, null, null, stepId );
      } catch ( KettleException e ) {
        LOGGER.warn( Messages.getString( "INFO.Serialization.Trans.Step", stepMeta.getName() ), e );
      }
      json.writeObject( step );
    }
    json.writeEndArray();
  }

  protected void serializeConnections( TransMeta meta, JsonGenerator json ) throws IOException {
    // connections
    json.writeArrayFieldStart( JSON_PROPERTY_CONNECTIONS );
    for ( DatabaseMeta dbmeta : meta.getDatabases() ) {
      BaseResourceInfo resourceInfo = (BaseResourceInfo) DatabaseResourceInfoUtil.createDatabaseResource( dbmeta );
      resourceInfo.setInput( true );
      json.writeObject( resourceInfo );
    }
    json.writeEndArray();
  }

  protected void serializeHops( TransMeta meta, JsonGenerator json ) throws IOException {
    // Hops
    json.writeArrayFieldStart( JSON_PROPERTY_HOPS );
    int numberOfHops = meta.nrTransHops();
    for ( int i = 0; i < numberOfHops; i++ ) {
      TransHopMeta hopMeta = meta.getTransHop( i );
      HopInfo hopInfo = new HopInfo( hopMeta );
      json.writeObject( hopInfo );
    }
    json.writeEndArray();
  }

  protected BaseStepMeta getBaseStepMetaFromStepMeta( StepMeta stepMeta ) {

    // Attempt to discover a BaseStepMeta from the given StepMeta
    BaseStepMeta baseStepMeta = new BaseStepMeta();
    baseStepMeta.setParentStepMeta( stepMeta );

    if ( stepMeta != null ) {
      StepMetaInterface smi = stepMeta.getStepMetaInterface();
      if ( smi instanceof BaseStepMeta ) {
        baseStepMeta = (BaseStepMeta) smi;
      }
    }
    return baseStepMeta;
  }
}
