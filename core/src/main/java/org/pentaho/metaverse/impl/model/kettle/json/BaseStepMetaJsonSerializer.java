/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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
