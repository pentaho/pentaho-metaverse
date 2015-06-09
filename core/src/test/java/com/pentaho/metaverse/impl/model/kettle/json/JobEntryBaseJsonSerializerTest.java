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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.IInfo;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
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
    verify( json, atLeastOnce() ).writeObjectField( JobEntryBaseJsonSerializer.JSON_PROPERTY_FIELDS, anyList() );
    verify( json, atLeastOnce() ).writeObjectField( JobEntryBaseJsonSerializer.JSON_PROPERTY_ATTRIBUTES, anyMap() );
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