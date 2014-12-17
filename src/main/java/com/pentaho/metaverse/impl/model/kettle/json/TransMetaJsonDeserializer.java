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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.model.IInfo;
import com.pentaho.metaverse.impl.model.JdbcResourceInfo;
import com.pentaho.metaverse.impl.model.JndiResourceInfo;
import com.pentaho.metaverse.impl.model.ParamInfo;
import com.pentaho.metaverse.impl.model.kettle.HopInfo;
import com.pentaho.metaverse.messages.Messages;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: RFellows Date: 11/19/14
 */
public class TransMetaJsonDeserializer extends StdDeserializer<TransMeta> {

  private Repository repository;
  private static final Logger LOGGER = LoggerFactory.getLogger( TransMetaJsonDeserializer.class );

  public TransMetaJsonDeserializer( Class<?> aClass ) {
    super( aClass );
  }
  public TransMetaJsonDeserializer( Class<?> aClass, Repository repository ) {
    super( aClass );
    setRepository( repository );
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  @Override public TransMeta deserialize( JsonParser parser, DeserializationContext context )
    throws IOException, JsonProcessingException {

    TransMeta transMeta = null;
    JsonNode node = parser.getCodec().readTree( parser );

    ObjectMapper mapper = (ObjectMapper) parser.getCodec();

    String name = node.get( IInfo.JSON_PROPERTY_NAME ).textValue();
    String desc = node.get( IInfo.JSON_PROPERTY_DESCRIPTION ).textValue();

    String createdBy = node.get( TransMetaJsonSerializer.JSON_PROPERTY_CREATED_BY ).textValue();
    String modifiedBy = node.get( TransMetaJsonSerializer.JSON_PROPERTY_LAST_MODIFIED_BY ).textValue();
    Date createdDate = new Date( node.get( TransMetaJsonSerializer.JSON_PROPERTY_CREATED_DATE ).asLong() );
    Date modifiedDate = new Date( node.get( TransMetaJsonSerializer.JSON_PROPERTY_LAST_MODIFIED_DATE ).asLong() );
    String path = node.get( TransMetaJsonSerializer.JSON_PROPERTY_PATH ).textValue();

    transMeta = new TransMeta( null, name );
    transMeta.setDescription( desc );

    transMeta.setCreatedDate( createdDate );
    transMeta.setCreatedUser( createdBy );
    transMeta.setModifiedDate( modifiedDate );
    transMeta.setModifiedUser( modifiedBy );
    transMeta.setFilename( path );

    // parameters
    deserializeParameters( transMeta, node, mapper );

    // variables
    deserializeVariables( transMeta, node, mapper );

    // connections
    deserializeConnections( transMeta, node, mapper );

    // steps
    deserializeSteps( transMeta, node, mapper );

    // hops
    deserializeHops( transMeta, node, mapper );

    return transMeta;

  }

  protected void deserializeConnections( TransMeta transMeta, JsonNode node, ObjectMapper mapper ) {
    ArrayNode connectionsArrayNode = (ArrayNode) node.get( TransMetaJsonSerializer.JSON_PROPERTY_CONNECTIONS );
    IExternalResourceInfo conn = null;
    for ( int i = 0; i < connectionsArrayNode.size(); i++ ) {
      JsonNode connNode = connectionsArrayNode.get( i );
      String className = connNode.get( IInfo.JSON_PROPERTY_CLASS ).asText();
      try {
        Class clazz = this.getClass().getClassLoader().loadClass( className );
        conn = (IExternalResourceInfo) clazz.newInstance();
        conn = mapper.readValue( connNode.toString(), conn.getClass() );
        DatabaseMeta dbMeta = null;
        if ( conn instanceof JdbcResourceInfo ) {
          JdbcResourceInfo db = (JdbcResourceInfo) conn;
          dbMeta = new DatabaseMeta(
              db.getName(),
              db.getPluginId(),
              DatabaseMeta.getAccessTypeDesc( DatabaseMeta.TYPE_ACCESS_NATIVE ),
              db.getServer(),
              db.getDatabaseName(),
              String.valueOf( db.getPort() ),
              db.getUsername(),
              db.getPassword() );
        } else if ( conn instanceof JndiResourceInfo ) {
          JndiResourceInfo db = (JndiResourceInfo) conn;
          dbMeta = new DatabaseMeta(
              db.getName(),
              db.getPluginId(),
              DatabaseMeta.getAccessTypeDesc( DatabaseMeta.TYPE_ACCESS_JNDI ),
              null,
              null,
              null,
              null,
              null );
        }
        transMeta.addDatabase( dbMeta );
      } catch ( Exception e ) {
        LOGGER.warn( Messages.getString( "WARNING.Deserialization.Trans.Connections",
            conn.getName(), transMeta.getName() ), e );
      }
    }
  }

  protected void deserializeParameters( TransMeta transMeta, JsonNode node, ObjectMapper mapper ) throws IOException {
    ArrayNode paramsArrayNode = (ArrayNode) node.get( TransMetaJsonSerializer.JSON_PROPERTY_PARAMETERS );
    for ( int i = 0; i < paramsArrayNode.size(); i++ ) {
      JsonNode paramNode = paramsArrayNode.get( i );
      ParamInfo param = mapper.readValue( paramNode.toString(), ParamInfo.class );
      try {
        transMeta.addParameterDefinition( param.getName(), param.getDefaultValue(), param.getDescription() );
      } catch ( DuplicateParamException e ) {
        LOGGER.warn( Messages.getString( "WARNING.Deserialization.Trans.DuplicateParam", param.getName() ), e );
      }
    }
  }

  protected void deserializeVariables( TransMeta transMeta, JsonNode node, ObjectMapper mapper ) throws IOException {
    ArrayNode varsArrayNode = (ArrayNode) node.get( TransMetaJsonSerializer.JSON_PROPERTY_VARIABLES );
    for ( int i = 0; i < varsArrayNode.size(); i++ ) {
      JsonNode paramNode = varsArrayNode.get( i );
      ParamInfo param = mapper.readValue( paramNode.toString(), ParamInfo.class );
      transMeta.setVariable( param.getName(), param.getValue() );
    }
  }

  protected void deserializeSteps( TransMeta transMeta, JsonNode node, ObjectMapper mapper ) throws IOException {
    ArrayNode stepsArrayNode = (ArrayNode) node.get( TransMetaJsonSerializer.JSON_PROPERTY_STEPS );
    String stepName = null;
    for ( int i = 0; i < stepsArrayNode.size(); i++ ) {
      JsonNode stepNode = stepsArrayNode.get( i );
      String className = stepNode.get( IInfo.JSON_PROPERTY_CLASS ).asText();
      stepName = stepNode.get( IInfo.JSON_PROPERTY_NAME ).asText();
      ObjectId stepId = new StringObjectId( stepName );

      // add the step attributes to the repo so they can be found when they are looked up by the readRep impl
      JsonNode attributes = stepNode.get( AbstractStepMetaJsonSerializer.JSON_PROPERTY_ATTRIBUTES );
      writeJsonAttributes( attributes, mapper, stepId );
      JsonNode fields = stepNode.get( AbstractStepMetaJsonSerializer.JSON_PROPERTY_FIELDS );
      writeJsonFields( fields, mapper, stepId );

      try {
        Class clazz = this.getClass().getClassLoader().loadClass( className );
        BaseStepMeta meta = (BaseStepMeta) clazz.newInstance();
        meta.readRep( getRepository(), null, stepId, transMeta.getDatabases() );
        StepMetaInterface smi = (StepMetaInterface) meta;
        StepMeta step = new StepMeta( stepName, smi );
        transMeta.addStep( step );

      } catch ( Exception e ) {
        LOGGER.warn( Messages.getString( "WARNING.Deserialization.Trans.Steps", stepName ), e );
      }
    }
  }

  protected void writeJsonFields( JsonNode fields, ObjectMapper mapper, ObjectId stepId ) throws IOException {
    List<Map<String, Object>> fieldLists = new ArrayList<Map<String, Object>>();
    fieldLists = mapper.readValue( fields.toString(), fieldLists.getClass() );
    int idx = 0;
    for ( Map<String, Object> fieldAttrs : fieldLists ) {
      for ( String s : fieldAttrs.keySet() ) {
        Object val = fieldAttrs.get( s );
        try {
          if ( val instanceof Integer ) {
            repository.saveStepAttribute( null, stepId, idx, s, (Integer) val );
          } else if ( val instanceof Long ) {
            repository.saveStepAttribute( null, stepId, idx, s, (Long) val );
          } else if ( val instanceof Double ) {
            repository.saveStepAttribute( null, stepId, idx, s, (Double) val );
          } else if ( val instanceof Boolean ) {
            repository.saveStepAttribute( null, stepId, idx, s, (Boolean) val );
          } else {
            repository.saveStepAttribute( null, stepId, idx, s, val == null ? null : (String) val );
          }
        } catch ( KettleException e ) {
          LOGGER.info( Messages.getString( "INFO.Deserialization.Trans.SavingAttributes", s,
              String.valueOf( idx ) ), e );
        }
      }
      idx++;
    }
  }

  protected void writeJsonAttributes( JsonNode attributes, ObjectMapper mapper, ObjectId stepId ) throws IOException {
    Map<String, Object> attrs = new HashMap<String, Object>();
    attrs = mapper.readValue( attributes.toString(), attrs.getClass() );

    for ( String s : attrs.keySet() ) {
      Object val = attrs.get( s );
      try {
        if ( val instanceof Integer ) {
          repository.saveStepAttribute( null, stepId, s, (Integer) val );
        } else if ( val instanceof Long ) {
          repository.saveStepAttribute( null, stepId, s, (Long) val );
        } else if ( val instanceof Double ) {
          repository.saveStepAttribute( null, stepId, s, (Double) val );
        } else if ( val instanceof Boolean ) {
          repository.saveStepAttribute( null, stepId, s, (Boolean) val );
        } else {
          repository.saveStepAttribute( null, stepId, s, val == null ? null : (String) val );
        }
      } catch ( KettleException e ) {
        LOGGER.info( Messages.getString( "INFO.Deserialization.Trans.SavingAttributes", s ), e );
      }
    }
  }

  protected void deserializeHops( TransMeta transMeta, JsonNode root, ObjectMapper mapper ) {
    ArrayNode hopsArray = (ArrayNode) root.get( TransMetaJsonSerializer.JSON_PROPERTY_HOPS );
    for ( int i = 0; i < hopsArray.size(); i++ ) {
      JsonNode hopNode = hopsArray.get( i );
      try {
        HopInfo hop = mapper.readValue( hopNode.toString(), HopInfo.class );
        if ( hop != null ) {
          TransHopMeta hopMeta = new TransHopMeta();
          hopMeta.setFromStep( transMeta.findStep( hop.getFromStepName() ) );
          hopMeta.setToStep( transMeta.findStep( hop.getToStepName() ) );
          hopMeta.setEnabled( hop.isEnabled() );
          transMeta.addTransHop( hopMeta );
        }
      } catch ( IOException e ) {
        LOGGER.warn( Messages.getString( "WARNING.Deserialization.Trans.Hops" ), e );
      }
    }
  }

}
