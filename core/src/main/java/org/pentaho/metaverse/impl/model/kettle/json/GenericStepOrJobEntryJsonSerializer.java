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
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryExternalResourceConsumerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumerProvider;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

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
