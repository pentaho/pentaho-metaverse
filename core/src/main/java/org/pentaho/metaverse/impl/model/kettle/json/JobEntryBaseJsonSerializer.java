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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.io.IOException;

/**
 * User: RFellows Date: 12/15/14
 */
public class JobEntryBaseJsonSerializer extends AbstractJobEntryJsonSerializer<JobEntryBase> {


  private static JobEntryBaseJsonSerializer instance;

  private JobEntryBaseJsonSerializer() {
    this( JobEntryBase.class );
    this.setLineageRepository( LineageRepository.getInstance() );
  }

  public static JobEntryBaseJsonSerializer getInstance() {
    if ( null == instance ) {
      instance = new JobEntryBaseJsonSerializer();
    }
    return instance;
  }

  @VisibleForTesting
  JobEntryBaseJsonSerializer( Class<JobEntryBase> aClass ) {
    super( aClass );
  }

  @VisibleForTesting
  JobEntryBaseJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  @VisibleForTesting
  JobEntryBaseJsonSerializer( Class<?> aClass, boolean b ) {
    super( aClass, b );
  }

  @Override
  protected void writeCustom( JobEntryBase meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException {
    // nothing custom yet
  }
}
