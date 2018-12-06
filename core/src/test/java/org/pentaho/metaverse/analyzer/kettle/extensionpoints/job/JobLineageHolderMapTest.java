/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.collect.MapMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransLineageHolderMap;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

  @Mock
  private Job job;

  @Mock
  private Trans parentTrans;

  @Mock
  private TransMeta parentTransMeta;

  @Mock
  private Job parentJob;

  @Mock
  private JobMeta jobMeta;

  private void initMetas() {

    when( job.getJobMeta() ).thenReturn( jobMeta );
    when( parentTrans.getTransMeta() ).thenReturn( parentTransMeta );
  }

  @Before
  public void setUp() throws Exception {
    jobLineageHolderMap = JobLineageHolderMap.getInstance();
    mockHolder = spy( new LineageHolder() );
    jobLineageHolderMap.setDefaultMetaverseBuilder( defaultBuilder );
  }

  @After
  public void cleanUp() throws Exception {
    Whitebox.setInternalState( jobLineageHolderMap, "lineageHolderMap",
      Collections.synchronizedMap( new MapMaker().weakKeys().makeMap() ) );
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

  @Test
  public void testRemoveLineageHolderWithParentTrans() throws Exception {
    initMetas();

    // initialize the lineage holder for this transformation
    jobLineageHolderMap.getLineageHolder( job );

    // test with parent transformation being set
    when( job.getParentTrans() ).thenReturn( parentTrans );

    Field lineageHolderMapField = jobLineageHolderMap.getClass().getDeclaredField( "lineageHolderMap" );
    lineageHolderMapField.setAccessible( true );

    jobLineageHolderMap.removeLineageHolder( job );
    // make sure the trans map entry was NOT removed and its resources were NOT removed from the
    // KettleAnalyzerUtil.resourceMap, since trans has a parent
    Map<Trans, LineageHolder> lineageHolderMap = (Map) lineageHolderMapField.get( jobLineageHolderMap );
    assertNotNull( lineageHolderMap.get( job ) );
    // lineageHolderMap will only have one member, the parent trans will be in the TransLineageHolderMap
    assertEquals( 1, lineageHolderMap.size() );

    // make sure that removing the parent trans removes it and its sub-transformations from the map and also removed
    // sub-transformation resources from KettleAnalyzerUtil.resourceMap
    TransLineageHolderMap.getInstance().removeLineageHolder( parentTrans );
    lineageHolderMap = (Map) lineageHolderMapField.get( jobLineageHolderMap );
    assertNull( lineageHolderMap.get( parentTrans ) );
    assertNull( lineageHolderMap.get( job ) );
    assertEquals( 0, lineageHolderMap.size() );
  }

  @Test
  public void testRemoveLineageHolderWithParentJob() throws Exception {
    initMetas();

    // initialize the lineage holder for this transformation
    jobLineageHolderMap.getLineageHolder( job );

    // test with parent transformation being set
    when( job.getParentJob() ).thenReturn( parentJob );

    Field lineageHolderMapField = jobLineageHolderMap.getClass().getDeclaredField( "lineageHolderMap" );
    lineageHolderMapField.setAccessible( true );

    jobLineageHolderMap.removeLineageHolder( job );
    // make sure the trans map entry was NOT removed and its resources were NOT removed from the
    // KettleAnalyzerUtil.resourceMap, since trans has a parent
    Map<Trans, LineageHolder> lineageHolderMap = (Map) lineageHolderMapField.get( jobLineageHolderMap );
    assertNotNull( lineageHolderMap.get( job ) );
    assertEquals( 2, lineageHolderMap.size() );

    // make sure that removing the parent trans removes it and its sub-transformations from the map and also removed
    // sub-transformation resources from KettleAnalyzerUtil.resourceMap
    jobLineageHolderMap.removeLineageHolder( parentJob );
    lineageHolderMap = (Map) lineageHolderMapField.get( jobLineageHolderMap );
    assertNull( lineageHolderMap.get( parentJob ) );
    assertNull( lineageHolderMap.get( job ) );
    assertEquals( 0, lineageHolderMap.size() );
  }
}
