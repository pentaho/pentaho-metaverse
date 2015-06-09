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
import org.pentaho.metaverse.api.model.kettle.HopInfo;
import com.pentaho.metaverse.impl.model.kettle.LineageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.success.JobEntrySuccess;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class JobMetaJsonSerializerTest {

  JobMetaJsonSerializer serializer;
  LineageRepository repo = new LineageRepository();

  @Mock JsonGenerator json;
  @Mock SerializerProvider provider;
  @Mock JobMeta meta;

  @Before
  public void setUp() throws Exception {
    serializer = new JobMetaJsonSerializer( JobMeta.class );
    serializer.setLineageRepository( repo );
  }

  @Test
  public void testSerializeHops() throws Exception {
    JobHopMeta jobHopMeta = mock( JobHopMeta.class );
    JobEntryCopy fromJobEntry = mock( JobEntryCopy.class );
    JobEntryCopy toJobEntry = mock( JobEntryCopy.class );

    when( meta.nrJobHops() ).thenReturn( 2 );
    when( meta.getJobHop( anyInt() ) ).thenReturn( jobHopMeta );

    when( jobHopMeta.getFromEntry() ).thenReturn( fromJobEntry );
    when( jobHopMeta.getToEntry() ).thenReturn( toJobEntry );
    when( jobHopMeta.isEnabled() ).thenReturn( true );

    when( fromJobEntry.getName() ).thenReturn( "from" );
    when( toJobEntry.getName() ).thenReturn( "to" );

    serializer.serializeHops( meta, json );
    verify( json, times( 2 ) ).writeObject( any( HopInfo.class ) );
  }

  @Test
  public void testSerializeSteps() throws Exception {
    when( meta.nrJobEntries() ).thenReturn( 2 );
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    JobEntryInterface jobEntryInterface = new JobEntrySuccess( "name", "description" );

    when( meta.getJobEntry( anyInt() ) ).thenReturn( jobEntryCopy );
    when( meta.getName() ).thenReturn( "JobMeta" );
    when( jobEntryCopy.getName() ).thenReturn( "JobEntry" );
    when( jobEntryCopy.getEntry() ).thenReturn( jobEntryInterface );

    serializer.serializeSteps( meta, json );
    verify( json, times( 2 ) ).writeObject( jobEntryInterface );
  }

  @Test
  public void testConstructors() throws Exception {
    JavaType type = mock( JavaType.class );
    serializer = new JobMetaJsonSerializer( type );
    serializer = new JobMetaJsonSerializer( JobMeta.class, true );
  }

  @Test
  public void testGetUsedVariables() throws Exception {
    serializer.getUsedVariables( meta );
    verify( meta ).getUsedVariables();
  }
}