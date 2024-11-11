/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.io.IOException;

/**
 * User: RFellows Date: 11/17/14
 */
public class TableOutputStepMetaJsonSerializer extends AbstractStepMetaJsonSerializer<TableOutputMeta> {

  private static TableOutputStepMetaJsonSerializer instance;

  private TableOutputStepMetaJsonSerializer() {
    this( TableOutputMeta.class, LineageRepository.getInstance() );
  }

  public static TableOutputStepMetaJsonSerializer getInstance() {
    if ( null == instance ) {
      instance = new TableOutputStepMetaJsonSerializer();
    }
    return instance;
  }

  @VisibleForTesting
  TableOutputStepMetaJsonSerializer( Class<TableOutputMeta> aClass ) {
    super( aClass );
  }

  @VisibleForTesting
  TableOutputStepMetaJsonSerializer( Class<TableOutputMeta> aClass, LineageRepository repo ) {
    super( aClass, repo );
  }

  @Override
  protected void writeCustomProperties(
    TableOutputMeta meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException, JsonGenerationException {

  }
}
