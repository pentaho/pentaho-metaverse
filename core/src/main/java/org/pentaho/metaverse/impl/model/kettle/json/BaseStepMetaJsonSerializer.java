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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.io.IOException;

/**
 * User: RFellows Date: 11/17/14
 */
public class BaseStepMetaJsonSerializer extends AbstractStepMetaJsonSerializer<BaseStepMeta> {

  private static BaseStepMetaJsonSerializer instance;

  private BaseStepMetaJsonSerializer() {
    this( BaseStepMeta.class, LineageRepository.getInstance() );
  }

  public static BaseStepMetaJsonSerializer getInstance() {
    if ( null == instance ) {
      instance = new BaseStepMetaJsonSerializer();
    }
    return instance;
  }

  @VisibleForTesting
  BaseStepMetaJsonSerializer( Class<BaseStepMeta> aClass ) {
    super( aClass );
  }

  @VisibleForTesting
  BaseStepMetaJsonSerializer( Class<BaseStepMeta> aClass, LineageRepository repo ) {
    super( aClass, repo );
  }

  @Override
  protected void writeCustomProperties( BaseStepMeta meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException, JsonGenerationException {
    // nothing custom here
  }

}
