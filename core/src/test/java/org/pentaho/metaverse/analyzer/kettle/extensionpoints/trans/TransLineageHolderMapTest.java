/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import com.google.common.collect.MapMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.analyzer.kettle.ExternalResourceCache;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransLineageHolderMap
 */
@RunWith( MockitoJUnitRunner.class )
public class TransLineageHolderMapTest {

  TransLineageHolderMap transLineageHolderMap;

  // Will be a spy
  LineageHolder mockHolder;

  @Mock
  IMetaverseBuilder mockBuilder;

  @Mock
  IMetaverseBuilder defaultBuilder;

  @Mock
  IExecutionProfile mockExecutionProfile;

  @Mock
  private Trans trans;

  @Mock
  private Trans parentTrans;

  @Mock
  private StepMeta parentStepMeta;

  @Mock
  private TransMeta parentTransMeta;

  @Mock
  private Job parentJob;

  @Mock
  private BaseFileInputMeta meta;

  @Mock
  private BaseFileInputMeta meta2;

  @Mock
  private BaseFileInputStep input;

  @Mock
  private TransMeta transMeta;

  @Mock
  private RowMetaInterface rowMetaInterface;

  private String path1 = "/path/to/file1";
  private String path1a = "/another/path/to/file1a";
  private String path2 = "/another/path/to/file2";
  private String sharedPath = "/shared/file";

  private String[] filePaths = { path1, path1a, sharedPath };
  private String[] filePaths2 = { path2, sharedPath };

  private StepMeta spyMeta;
  private StepMeta spyMeta2;

  private void initMetas() {

    when( trans.getTransMeta() ).thenReturn( transMeta );
    when( trans.getName() ).thenReturn( "trans" );
    when( trans.getFilename() ).thenReturn( "trans.ktr" );
    when( parentTrans.getTransMeta() ).thenReturn( parentTransMeta );
    when( parentTrans.getName() ).thenReturn( "parentTrans" );
    when( parentTrans.getFilename() ).thenReturn( "parentTrans.ktr" );

    when( parentJob.getJobname() ).thenReturn( "parentJob" );
    when( parentJob.getFilename() ).thenReturn( "parentJob.kjb" );

    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    when( transMeta.getFilename() ).thenReturn( "my_file" );

    spyMeta = spy( new StepMeta( "test", meta ) );
    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.writesToFile() ).thenReturn( true );
    when( meta.getFilePaths( false) ).thenReturn( filePaths );

    spyMeta2 = spy( new StepMeta( "test2", meta2 ) );
    when( meta2.getParentStepMeta() ).thenReturn( spyMeta2 );
    when( spyMeta2.getParentTransMeta() ).thenReturn( transMeta );
    when( meta2.writesToFile() ).thenReturn( true );
    when( meta2.getFilePaths( false) ).thenReturn( filePaths2 );

    when( transMeta.getSteps() ).thenReturn( Arrays.asList( new StepMeta[] { spyMeta, spyMeta2 } ) );

    when( input.getStepMetaInterface() ).thenReturn( meta );
    when( input.getStepMeta() ).thenReturn( spyMeta );
    when( input.getTrans() ).thenReturn( trans );
  }

  @Before
  public void setUp() throws Exception {
    Whitebox.setInternalState( JobLineageHolderMap.getInstance(), "lineageHolderMap",
            Collections.synchronizedMap( new MapMaker().weakKeys().makeMap() ) );
    Whitebox.setInternalState( TransLineageHolderMap.getInstance(), "lineageHolderMap",
            Collections.synchronizedMap( new MapMaker().weakKeys().makeMap() ) );
    Whitebox.setInternalState( ExternalResourceCache.getInstance(), "transMap", new ConcurrentHashMap() );
    Whitebox.setInternalState( ExternalResourceCache.getInstance(), "resourceMap", new ConcurrentHashMap() );
    transLineageHolderMap = TransLineageHolderMap.getInstance();
    mockHolder = spy( new LineageHolder() );
    transLineageHolderMap.setDefaultMetaverseBuilder( defaultBuilder );
  }

  @After
  public void cleanUp() throws Exception {
    Whitebox.setInternalState( transLineageHolderMap, "lineageHolderMap",
      Collections.synchronizedMap( new MapMaker().weakKeys().makeMap() ) );
  }

  @After
  public void tearDown() {
    transLineageHolderMap.setDefaultMetaverseBuilder( null );
  }

  @Test
  public void testGetSetInstance() throws Exception {
    assertNotNull( transLineageHolderMap );
    TransLineageHolderMap.setInstance( transLineageHolderMap );
    assertEquals( transLineageHolderMap, TransLineageHolderMap.getInstance() );
  }

  @Test
  public void testGetPutLineageHolder() throws Exception {

    Trans trans = mock( Trans.class );

    LineageHolder holder = transLineageHolderMap.getLineageHolder( trans );
    assertNotNull( holder ); // We always get a (perhaps empty) holder
    assertFalse( holder == mockHolder );
    assertNull( holder.getExecutionProfile() );
    assertNull( holder.getMetaverseBuilder() );

    mockHolder.setMetaverseBuilder( mockBuilder );
    mockHolder.setExecutionProfile( mockExecutionProfile );
    transLineageHolderMap.putLineageHolder( trans, mockHolder );

    holder = transLineageHolderMap.getLineageHolder( trans );
    assertNotNull( holder ); // We always get a (perhaps empty) holder
    assertTrue( holder == mockHolder );
    assertTrue( holder.getExecutionProfile() == mockExecutionProfile );
    assertTrue( holder.getMetaverseBuilder() == mockBuilder );
  }

  @Test
  public void testRemoveLineageHolderWithParentTrans() throws Exception {
    initMetas();
    when( meta.isAcceptingFilenames() ).thenReturn( true );

    // initialize the lineage holder for this transformation
    transLineageHolderMap.getLineageHolder( trans );

    // test with parent transformation being set
    when( trans.getParentTrans() ).thenReturn( parentTrans );

    when( input.environmentSubstitute( Mockito.any( String.class ) ) ).thenReturn( "/path/to/row/file" );

    KettleAnalyzerUtil.getResourcesFromRow( input, rowMetaInterface, new String[] { "id", "name" } );

    Field resourceMapField = ExternalResourceCache.class.getDeclaredField( "resourceMap" );
    resourceMapField.setAccessible( true );
    Field transMapField = ExternalResourceCache.class.getDeclaredField( "transMap" );
    transMapField.setAccessible( true );

    Map<String, ExternalResourceCache.ExternalResourceValues> resourceMap = (Map) resourceMapField.get(
      ExternalResourceCache.getInstance() );
    Map<String, ExternalResourceCache.ExternalResourceValues> transMap = (Map) transMapField.get(
      ExternalResourceCache.getInstance() );
    assertEquals( 1, resourceMap.size() );
    assertEquals( 1, transMap.size() );

    Field lineageHolderMapField = transLineageHolderMap.getClass().getDeclaredField( "lineageHolderMap" );
    lineageHolderMapField.setAccessible( true );

    transLineageHolderMap.removeLineageHolder( trans );
    // make sure the trans map entry was NOT removed and its resources were NOT removed from the
    // ExternalResourceCache, since trans has a parent
    resourceMap = (Map) resourceMapField.get( ExternalResourceCache.getInstance() );
    transMap = (Map) transMapField.get( ExternalResourceCache.getInstance() );
    Map<Trans, LineageHolder> lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNotNull( lineageHolderMap.get( trans ) );
    assertEquals( 1, resourceMap.size() ); // the resource map has only one resource
    assertEquals( 1, transMap.size() );
    assertEquals( 2, lineageHolderMap.size() );

    // make sure that removing the parent trans removes it and its sub-transformations from the map and also removed
    // sub-transformation resources from ExternalResourceCache
    transLineageHolderMap.removeLineageHolder( parentTrans );
    resourceMap = (Map) resourceMapField.get( ExternalResourceCache.getInstance() );
    transMap = (Map) transMapField.get( ExternalResourceCache.getInstance() );
    lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNull( lineageHolderMap.get( parentTrans ) );
    assertNull( lineageHolderMap.get( trans ) );
    assertEquals( 0, resourceMap.size() );
    assertEquals( 0, transMap.size() );
    assertEquals( 0, lineageHolderMap.size() );
  }

  @Test
  public void testRemoveLineageHolderWithParentJob() throws Exception {
    initMetas();
    when( meta.isAcceptingFilenames() ).thenReturn( true );

    // initialize the lineage holder for this transformation
    transLineageHolderMap.getLineageHolder( trans );

    // test with parent transformation being set
    when( trans.getParentJob() ).thenReturn( parentJob );

    when( input.environmentSubstitute( Mockito.any( String.class ) ) ).thenReturn( "/path/to/row/file" );

    KettleAnalyzerUtil.getResourcesFromRow( input, rowMetaInterface, new String[] { "id", "name" } );

    Field resourceMapField = ExternalResourceCache.class.getDeclaredField( "resourceMap" );
    resourceMapField.setAccessible( true );
    Field transMapField = ExternalResourceCache.class.getDeclaredField( "transMap" );
    transMapField.setAccessible( true );

    Map<String, Collection<IExternalResourceInfo>> resourceMap = (Map) resourceMapField.get(
      ExternalResourceCache.getInstance() );
    Map<String, ExternalResourceCache.ExternalResourceValues> transMap = (Map) transMapField.get(
      ExternalResourceCache.getInstance() );
    assertEquals( 1, resourceMap.size() );
    assertEquals( 1, transMap.size() );

    Field lineageHolderMapField = transLineageHolderMap.getClass().getDeclaredField( "lineageHolderMap" );
    lineageHolderMapField.setAccessible( true );

    transLineageHolderMap.removeLineageHolder( trans );
    // make sure the trans map entry was NOT removed and its resources were NOT removed from the
    // ExternalResourceCache, since trans has a parent
    resourceMap = (Map) resourceMapField.get( ExternalResourceCache.getInstance() );
    transMap = (Map) transMapField.get( ExternalResourceCache.getInstance() );
    Map<Trans, LineageHolder> lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNotNull( lineageHolderMap.get( trans ) );
    assertEquals( 1, resourceMap.size() ); // the resource map has only one resource
    assertEquals( 1, transMap.size() );
    // lineageHolderMap will only have one member, the parent job will be in the JobLineageHolderMap
    assertEquals( 1, lineageHolderMap.size() );

    // make sure that removing the parent trans removes it and its sub-transformations from the map and also removed
    // sub-transformation resources from KettleAnalyzerUtil.resourceMap
    JobLineageHolderMap.getInstance().removeLineageHolder( parentJob );
    resourceMap = (Map) resourceMapField.get( ExternalResourceCache.getInstance() );
    transMap = (Map) transMapField.get( ExternalResourceCache.getInstance() );
    lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNull( lineageHolderMap.get( parentJob ) );
    assertNull( lineageHolderMap.get( trans ) );
    assertEquals( 0, resourceMap.size() );
    assertEquals( 0, transMap.size() );
    assertEquals( 0, lineageHolderMap.size() );
  }

  @Test
  public void testGetMetaverseBuilder() throws Exception {

    assertNull( transLineageHolderMap.getMetaverseBuilder( null ) );

    Trans trans = mock( Trans.class );
    when( trans.getParentJob() ).thenReturn( null );
    when( trans.getParentTrans() ).thenReturn( null );

    mockHolder.setMetaverseBuilder( mockBuilder );
    transLineageHolderMap.putLineageHolder( trans, mockHolder );

    IMetaverseBuilder builder = transLineageHolderMap.getMetaverseBuilder( trans );
    assertNotNull( builder );

    Trans parentTrans = mock( Trans.class );
    when( parentTrans.getParentJob() ).thenReturn( null );
    when( parentTrans.getParentTrans() ).thenReturn( null );

    when( trans.getParentTrans() ).thenReturn( parentTrans );

    LineageHolder mockParentHolder = spy( new LineageHolder() );

    IMetaverseBuilder mockParentBuilder = mock( IMetaverseBuilder.class );
    transLineageHolderMap.putLineageHolder( parentTrans, mockParentHolder );
    mockParentHolder.setMetaverseBuilder( null );

    assertEquals( defaultBuilder, transLineageHolderMap.getMetaverseBuilder( trans ) );

    mockParentHolder.setMetaverseBuilder( mockParentBuilder );
    builder = transLineageHolderMap.getMetaverseBuilder( trans );
    assertNotNull( builder );
  }
}
