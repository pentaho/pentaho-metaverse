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
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

import java.io.IOException;

/**
 * User: RFellows Date: 11/17/14
 */
public class TableOutputStepMetaJsonSerializer extends AbstractStepMetaJsonSerializer<TableOutputMeta> {
  public TableOutputStepMetaJsonSerializer( Class<TableOutputMeta> aClass ) {
    super( aClass );
  }

  public TableOutputStepMetaJsonSerializer( Class<TableOutputMeta> aClass, LineageRepository repo ) {
    super( aClass, repo );
  }

  @Override
  protected void writeCustomProperties(
    TableOutputMeta meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException, JsonGenerationException {

  }
}
