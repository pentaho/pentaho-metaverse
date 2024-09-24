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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metaverse.api.model.kettle.HopInfo;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * User: RFellows Date: 11/17/14
 */
public class TransMetaJsonSerializer extends AbstractMetaJsonSerializer<TransMeta> {

  private static TransMetaJsonSerializer instance;

  private TransMetaJsonSerializer() {
    this( TransMeta.class );
    this.setLineageRepository( LineageRepository.getInstance() );
  }

  public static TransMetaJsonSerializer getInstance() {
    if ( null == instance ) {
      instance = new TransMetaJsonSerializer();
    }
    return instance;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger( TransMetaJsonSerializer.class );

  @VisibleForTesting
  TransMetaJsonSerializer( Class<TransMeta> aClass ) {
    super( aClass );
  }

  @VisibleForTesting
  TransMetaJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  @VisibleForTesting
  TransMetaJsonSerializer( Class<?> aClass, boolean b ) {
    super( aClass, b );
  }

  @Override protected List<String> getUsedVariables( TransMeta meta ) {
    return meta.getUsedVariables();
  }

  @Override
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

  @Override
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
