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
