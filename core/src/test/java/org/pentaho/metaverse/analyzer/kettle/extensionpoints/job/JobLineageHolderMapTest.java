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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.job.Job;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JobLineageHolderMap
 */
@RunWith( MockitoJUnitRunner.class )
public class JobLineageHolderMapTest {

  JobLineageHolderMap jobLineageHolderMap;

  // Will be a spy
  LineageHolder mockHolder;

  @Mock
  IMetaverseBuilder mockBuilder;

  @Mock
  IMetaverseBuilder defaultBuilder;

  @Mock
  IExecutionProfile mockExecutionProfile;

  @Before
  public void setUp() throws Exception {
    jobLineageHolderMap = JobLineageHolderMap.getInstance();
    mockHolder = spy( new LineageHolder() );
    jobLineageHolderMap.setDefaultMetaverseBuilder( defaultBuilder );
  }

  @Test
  public void testGetSetInstance() throws Exception {
    assertNotNull( jobLineageHolderMap );
    JobLineageHolderMap.setInstance( jobLineageHolderMap );
    assertEquals( jobLineageHolderMap, JobLineageHolderMap.getInstance() );
  }

  @Test
  public void testGetPutLineageHolder() throws Exception {

    Job job = mock( Job.class );

    LineageHolder holder = jobLineageHolderMap.getLineageHolder( job );
    assertNotNull( holder ); // We always get a (perhaps empty) holder
    assertFalse( holder == mockHolder );
    assertNull( holder.getExecutionProfile() );
    assertNull( holder.getMetaverseBuilder() );

    mockHolder.setMetaverseBuilder( mockBuilder );
    mockHolder.setExecutionProfile( mockExecutionProfile );
    jobLineageHolderMap.putLineageHolder( job, mockHolder );

    holder = jobLineageHolderMap.getLineageHolder( job );
    assertNotNull( holder ); // We always get a (perhaps empty) holder
    assertTrue( holder == mockHolder );
    assertTrue( holder.getExecutionProfile() == mockExecutionProfile );
    assertTrue( holder.getMetaverseBuilder() == mockBuilder );
  }

  @Test
  public void testGetMetaverseBuilder() throws Exception {

    assertNull( jobLineageHolderMap.getMetaverseBuilder( null ) );

    Job job = mock( Job.class );
    when( job.getParentJob() ).thenReturn( null );
    when( job.getParentTrans() ).thenReturn( null );

    mockHolder.setMetaverseBuilder( mockBuilder );
    jobLineageHolderMap.putLineageHolder( job, mockHolder );

    IMetaverseBuilder builder = jobLineageHolderMap.getMetaverseBuilder( job );
    assertNotNull( builder );

    Job parentJob = mock( Job.class );
    when( parentJob.getParentJob() ).thenReturn( null );
    when( parentJob.getParentTrans() ).thenReturn( null );

    when( job.getParentJob() ).thenReturn( parentJob );

    LineageHolder mockParentHolder = spy( new LineageHolder() );

    IMetaverseBuilder mockParentBuilder = mock( IMetaverseBuilder.class );
    jobLineageHolderMap.putLineageHolder( parentJob, mockParentHolder );
    mockParentHolder.setMetaverseBuilder( null );

    assertEquals( defaultBuilder, jobLineageHolderMap.getMetaverseBuilder( job ) );

    mockParentHolder.setMetaverseBuilder( mockParentBuilder );
    builder = jobLineageHolderMap.getMetaverseBuilder( job );
    assertNotNull( builder );

  }
}
