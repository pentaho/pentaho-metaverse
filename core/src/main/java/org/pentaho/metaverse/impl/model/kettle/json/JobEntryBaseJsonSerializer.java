/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.job.entry.JobEntryBase;

import java.io.IOException;

/**
 * User: RFellows Date: 12/15/14
 */
public class JobEntryBaseJsonSerializer extends AbstractJobEntryJsonSerializer<JobEntryBase> {
  public JobEntryBaseJsonSerializer( Class<JobEntryBase> aClass ) {
    super( aClass );
  }

  public JobEntryBaseJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  public JobEntryBaseJsonSerializer( Class<?> aClass, boolean b ) {
    super( aClass, b );
  }

  @Override
  protected void writeCustom( JobEntryBase meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException {
    // nothing custom yet
  }
}
