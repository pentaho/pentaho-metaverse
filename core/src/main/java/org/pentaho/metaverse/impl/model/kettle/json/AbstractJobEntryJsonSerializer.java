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
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.IInfo;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: RFellows Date: 12/15/14
 */
public abstract class AbstractJobEntryJsonSerializer<T extends JobEntryBase>
  extends GenericStepOrJobEntryJsonSerializer<T> {

  public AbstractJobEntryJsonSerializer( Class<T> aClass ) {
    super( aClass );
  }

  public AbstractJobEntryJsonSerializer( JavaType javaType ) {
    super( javaType );
  }

  public AbstractJobEntryJsonSerializer( Class<?> aClass, boolean b ) {
    super( aClass, b );
  }

  @Override
  protected void writeBasicInfo( T meta, JsonGenerator json ) throws IOException {
    json.writeStringField( IInfo.JSON_PROPERTY_CLASS, meta.getClass().getName() );
    json.writeStringField( IInfo.JSON_PROPERTY_NAME, meta.getName() );
    json.writeStringField( JSON_PROPERTY_TYPE, getStepType( meta ) );
  }

  protected String getStepType( T entry ) {
    String stepType = null;
    try {
      stepType = PluginRegistry.getInstance().findPluginWithId(
          JobEntryPluginType.class, entry.getPluginId() ).getName();
    } catch ( Throwable t ) {
      stepType = entry.getClass().getSimpleName();
    }
    return stepType;
  }

  protected void writeRepoAttributes( T meta, JsonGenerator json ) throws IOException {

    ObjectId jobId = meta.getObjectId() == null ? new StringObjectId( meta.getName() ) : meta.getObjectId();

    LineageRepository repo = getLineageRepository();
    if ( repo != null ) {
      Map<String, Object> attrs = repo.getJobEntryAttributesCache( jobId );
      json.writeObjectField( JSON_PROPERTY_ATTRIBUTES, attrs );

      List<Map<String, Object>> fields = repo.getJobEntryFieldsCache( jobId );
      json.writeObjectField( JSON_PROPERTY_FIELDS, fields );
    }

  }
  protected void writeExternalResources( T meta, JsonGenerator json, SerializerProvider serializerProvider )
    throws IOException, JsonGenerationException {

    json.writeArrayFieldStart( JSON_PROPERTY_EXTERNAL_RESOURCES );
    JobMeta jobMeta = new JobMeta();
    if ( meta.getParentJob() != null && meta.getParentJob().getJobMeta() != null ) {
      jobMeta = meta.getParentJob().getJobMeta();
    }
    List<ResourceReference> dependencies = meta.getResourceDependencies( jobMeta );
    for ( ResourceReference dependency : dependencies ) {
      for ( ResourceEntry resourceEntry : dependency.getEntries() ) {
        IExternalResourceInfo resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry );
        json.writeObject( resourceInfo );
      }
    }
    json.writeEndArray();
  }

}
