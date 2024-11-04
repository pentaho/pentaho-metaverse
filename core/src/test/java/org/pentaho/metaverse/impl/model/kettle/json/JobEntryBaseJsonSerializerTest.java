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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.IInfo;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class JobEntryBaseJsonSerializerTest {

  JobEntryBaseJsonSerializer serializer;
  LineageRepository repo = new LineageRepository();

  @Mock JsonGenerator json;
  @Mock SerializerProvider provider;
  @Mock JobEntryBase meta;

  @Before
  public void setUp() throws Exception {
    serializer = new JobEntryBaseJsonSerializer( JobEntryBase.class );
    serializer.setLineageRepository( repo );
    when( meta.getName() ).thenReturn( "JobEntry" );
  }

  @Test
  public void testSerialize() throws Exception {
    JobEntryBaseJsonSerializer spySerializer = spy( serializer );
    spySerializer.serialize( meta, json, provider );

    verify( json ).writeStartObject();
    verify( spySerializer ).writeBasicInfo( meta, json );
    verify( spySerializer ).writeExternalResources( meta, json, provider );
    verify( spySerializer ).writeRepoAttributes( meta, json );
    verify( spySerializer ).writeCustom( meta, json, provider );
    verify( json ).writeEndObject();
  }

  @Test
  public void testWriteBasicInfo() throws Exception {
    serializer.writeBasicInfo( meta, json );
    verify( json ).writeStringField( eq( IInfo.JSON_PROPERTY_CLASS ), anyString() );
    verify( json ).writeStringField( eq( IInfo.JSON_PROPERTY_NAME ), anyString() );
    verify( json ).writeStringField( eq( JobEntryBaseJsonSerializer.JSON_PROPERTY_TYPE ), anyString() );
  }

  @Test
  public void testWriteRepoAttributes() throws Exception {
    serializer.writeRepoAttributes( meta, json );
    verify( json, atLeastOnce() ).writeObjectField( eq( JobEntryBaseJsonSerializer.JSON_PROPERTY_FIELDS ), anyList() );
    verify( json, atLeastOnce() ).writeObjectField( eq( JobEntryBaseJsonSerializer.JSON_PROPERTY_ATTRIBUTES ), anyMap() );
  }

  @Test
  public void testWriteExternalResources() throws Exception {

    Job parentJob = mock( Job.class );
    JobMeta jobMeta = mock( JobMeta.class );
    List<ResourceReference> dependencies = new ArrayList<ResourceReference>();
    ResourceReference resRef = mock( ResourceReference.class );
    dependencies.add( resRef );
    List<ResourceEntry> resources = new ArrayList<ResourceEntry>();
    ResourceEntry resEntry = mock( ResourceEntry.class );
    resources.add( resEntry );

    when( resRef.getEntries() ).thenReturn( resources );
    when( resEntry.getResource() ).thenReturn( "path/to/my/resource" );
    when( resEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.FILE );

    when( meta.getParentJob() ).thenReturn( parentJob );
    when( parentJob.getJobMeta() ).thenReturn( jobMeta );

    when( meta.getResourceDependencies( jobMeta ) ).thenReturn( dependencies );

    serializer.writeExternalResources( meta, json, provider );
    verify( json ).writeArrayFieldStart( JobEntryBaseJsonSerializer.JSON_PROPERTY_EXTERNAL_RESOURCES );
    verify( json ).writeObject( any( IExternalResourceInfo.class ) );
    verify( json ).writeEndArray();
  }

  @Test
  public void testConstructors() throws Exception {
    JavaType type = mock( JavaType.class );
    serializer = new JobEntryBaseJsonSerializer( type );

    serializer = new JobEntryBaseJsonSerializer( JobEntryBase.class, true );

  }
}
