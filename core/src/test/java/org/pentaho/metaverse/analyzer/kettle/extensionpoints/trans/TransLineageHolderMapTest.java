/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import com.google.common.collect.MapMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransLineageHolderMap
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
    lenient().when( trans.getTransMeta() ).thenReturn( transMeta );
    lenient().when( trans.getName() ).thenReturn( "trans" );
    lenient().when( trans.getFilename() ).thenReturn( "trans.ktr" );
    lenient().when( parentTrans.getTransMeta() ).thenReturn( parentTransMeta );
    lenient().when( parentTrans.getName() ).thenReturn( "parentTrans" );
    lenient().when( parentTrans.getFilename() ).thenReturn( "parentTrans.ktr" );

    lenient().when( parentJob.getJobname() ).thenReturn( "parentJob" );
    lenient().when( parentJob.getFilename() ).thenReturn( "parentJob.kjb" );

    lenient().when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    lenient().when( transMeta.getFilename() ).thenReturn( "my_file" );

    spyMeta = spy( new StepMeta( "test", meta ) );
    lenient().when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    lenient().when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    lenient().when( meta.writesToFile() ).thenReturn( true );
    lenient().when( meta.getFilePaths( false) ).thenReturn( filePaths );

    spyMeta2 = spy( new StepMeta( "test2", meta2 ) );
    lenient().when( meta2.getParentStepMeta() ).thenReturn( spyMeta2 );
    lenient().when( spyMeta2.getParentTransMeta() ).thenReturn( transMeta );
    lenient().when( meta2.writesToFile() ).thenReturn( true );
    lenient().when( meta2.getFilePaths( false) ).thenReturn( filePaths2 );

    lenient().when( transMeta.getSteps() ).thenReturn( Arrays.asList( new StepMeta[] { spyMeta, spyMeta2 } ) );

    lenient().when( input.getStepMetaInterface() ).thenReturn( meta );
    lenient().when( input.getStepMeta() ).thenReturn( spyMeta );
    lenient().when( input.getTrans() ).thenReturn( trans );
  }

  @Before
  public void setUp() throws Exception {
    ReflectionTestUtils.setField( JobLineageHolderMap.getInstance(), "lineageHolderMap",
      Collections.synchronizedMap( new MapMaker().weakKeys().makeMap() ) );
    ReflectionTestUtils.setField( TransLineageHolderMap.getInstance(), "lineageHolderMap",
            Collections.synchronizedMap( new MapMaker().weakKeys().makeMap() ) );
    transLineageHolderMap = TransLineageHolderMap.getInstance();
    mockHolder = spy( new LineageHolder() );
    transLineageHolderMap.setDefaultMetaverseBuilder( defaultBuilder );
  }

  @After
  public void cleanUp() throws Exception {
    ReflectionTestUtils.setField( transLineageHolderMap, "lineageHolderMap",
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

    // initialize the lineage holder for this transformation
    transLineageHolderMap.getLineageHolder( trans );

    // test with parent transformation being set
    when( trans.getParentTrans() ).thenReturn( parentTrans );

    when( input.environmentSubstitute( Mockito.<String>any() ) ).thenReturn( "/path/to/row/file" );

    KettleAnalyzerUtil.getResourcesFromRow( input, rowMetaInterface, new String[] { "id", "name" } );

    Field lineageHolderMapField = transLineageHolderMap.getClass().getDeclaredField( "lineageHolderMap" );
    lineageHolderMapField.setAccessible( true );

    transLineageHolderMap.removeLineageHolder( trans );
    // make sure the trans map entry was NOT removed
    Map<Trans, LineageHolder> lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNotNull( lineageHolderMap.get( trans ) );
    assertEquals( 2, lineageHolderMap.size() );

    // make sure that removing the parent trans removes it and its sub-transformations from the map and also removed
    // sub-transformation resources from ExternalResourceCache
    transLineageHolderMap.removeLineageHolder( parentTrans );
    lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNull( lineageHolderMap.get( parentTrans ) );
    assertNull( lineageHolderMap.get( trans ) );
    assertEquals( 0, lineageHolderMap.size() );
  }

  @Test
  public void testRemoveLineageHolderWithParentJob() throws Exception {
    initMetas();

    // initialize the lineage holder for this transformation
    transLineageHolderMap.getLineageHolder( trans );

    // test with parent transformation being set
    when( trans.getParentJob() ).thenReturn( parentJob );

    when( input.environmentSubstitute( Mockito.<String>any() ) ).thenReturn( "/path/to/row/file" );

    KettleAnalyzerUtil.getResourcesFromRow( input, rowMetaInterface, new String[] { "id", "name" } );

    Field lineageHolderMapField = transLineageHolderMap.getClass().getDeclaredField( "lineageHolderMap" );
    lineageHolderMapField.setAccessible( true );

    transLineageHolderMap.removeLineageHolder( trans );
    // make sure the trans map entry was NOT removed
    Map<Trans, LineageHolder> lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNotNull( lineageHolderMap.get( trans ) );
    // lineageHolderMap will only have one member, the parent job will be in the JobLineageHolderMap
    assertEquals( 1, lineageHolderMap.size() );

    // make sure that removing the parent trans removes it and its sub-transformations from the map and also removed
    // sub-transformation resources from KettleAnalyzerUtil.resourceMap
    JobLineageHolderMap.getInstance().removeLineageHolder( parentJob );
    lineageHolderMap = (Map) lineageHolderMapField.get( transLineageHolderMap );
    assertNull( lineageHolderMap.get( parentJob ) );
    assertNull( lineageHolderMap.get( trans ) );
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

    // getting a new builder in TransLineageHolderMap.getDefaultBuilder should never fail now
    // unsure how it could in the first place other than unit tests
    assertNotEquals( defaultBuilder, transLineageHolderMap.getMetaverseBuilder( trans ) );

    mockParentHolder.setMetaverseBuilder( mockParentBuilder );
    builder = transLineageHolderMap.getMetaverseBuilder( trans );
    assertNotNull( builder );
  }
}
