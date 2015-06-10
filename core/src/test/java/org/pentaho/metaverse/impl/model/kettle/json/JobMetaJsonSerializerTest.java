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
import org.pentaho.metaverse.api.model.kettle.HopInfo;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

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
