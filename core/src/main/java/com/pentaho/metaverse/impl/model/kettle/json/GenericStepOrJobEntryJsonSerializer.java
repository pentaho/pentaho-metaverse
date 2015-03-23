/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.pentaho.metaverse.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;
import com.pentaho.metaverse.analyzer.kettle.step.IStepExternalResourceConsumerProvider;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.io.IOException;

/**
 * User: RFellows Date: 12/15/14
 */
public abstract class GenericStepOrJobEntryJsonSerializer<T> extends StdSerializer<T> {
  public static final String JSON_PROPERTY_TYPE = "type";
  public static final String JSON_PROPERTY_ATTRIBUTES = "attributes";
  public static final String JSON_PROPERTY_FIELDS = "fields";
  public static final String JSON_PROPERTY_EXTERNAL_RESOURCES = "externalResources";

  private LineageRepository lineageRepository;
  private IStepExternalResourceConsumerProvider stepConsumerProvider;
  private IJobEntryExternalResourceConsumerProvider jobEntryConsumerProvider;

  public GenericStepOrJobEntryJsonSerializer( Class<T> aClass ) {
    super( aClass );
  }

  public GenericStepOrJobEntryJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  public GenericStepOrJobEntryJsonSerializer( Class<?> aClass, boolean b ) {
    super( aClass, b );
  }

  public LineageRepository getLineageRepository() {
    return lineageRepository;
  }

  public void setLineageRepository( LineageRepository repo ) {
    this.lineageRepository = repo;
  }

  public IStepExternalResourceConsumerProvider getStepExternalResourceConsumerProvider() {
    return stepConsumerProvider;
  }

  public void setStepExternalResourceConsumerProvider( IStepExternalResourceConsumerProvider stepConsumerProvider ) {
    this.stepConsumerProvider = stepConsumerProvider;
  }

  public IJobEntryExternalResourceConsumerProvider getJobEntryExternalResourceConsumerProvider() {
    return jobEntryConsumerProvider;
  }

  public void setJobEntryExternalResourceConsumerProvider(
    IJobEntryExternalResourceConsumerProvider jobEntryConsumerProvider ) {
    this.jobEntryConsumerProvider = jobEntryConsumerProvider;
  }

  @Override
  public void serialize( T meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException, JsonGenerationException {
    json.writeStartObject();
    writeBasicInfo( meta, json );
    writeRepoAttributes( meta, json );
    writeExternalResources( meta, json, serializerProvider );
    writeCustom( meta, json, serializerProvider );
    json.writeEndObject();
  }

  protected abstract void writeCustom( T meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException;

  protected abstract void writeExternalResources( T meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException;

  protected abstract void writeRepoAttributes( T meta, JsonGenerator json ) throws IOException;

  protected abstract void writeBasicInfo( T meta, JsonGenerator json ) throws IOException;
}
