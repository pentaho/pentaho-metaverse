/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
import org.pentaho.metaverse.api.ICatalogLineageClientProvider;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.ILineageWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.api.model.kettle.MetaverseExtensionPoint;
import org.pentaho.metaverse.graph.GraphCatalogWriter;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest( { ExtensionPointHandler.class, MetaverseConfig.class, KettleAnalyzerUtil.class } )
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class JobRuntimeExtensionPointTest {

  private static final String TEST_SERVER = "test.pentaho.com";
  private static final String TEST_USER = "joe";
  private static final String TEST_JOB_NAME = "test job";
  private static final String TEST_JOB_PATH = "/path/to/test/job.kjb";
  private static final String TEST_JOB_DESCRIPTION = "This is a test job.";
  private static final String TEST_VAR_NAME = "testVariable";
  private static final String TEST_VAR_VALUE = "testVariableValue";
  private static final String TEST_PARAM_NAME = "testParam";
  private static final String TEST_PARAM_VALUE = "testParamValue";
  private static final String TEST_PARAM_DEFAULT_VALUE = "testParamDefaultValue";
  private static final String TEST_PARAM_DESCRIPTION = "Test parameter description";

  JobRuntimeExtensionPoint jobExtensionPoint;
  Job job;
  JobMeta jobMeta;
  ILineageWriter lineageWriter;

  @Mock
  private IMetaverseBuilder mockBuilder;
  @Mock
  private ICatalogLineageClientProvider clientProvider;
  @Mock ConnectionDetails connectionDetails;

  @Mock ConnectionManager connectionManager;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    KettleEnvironment.init(); // init LogChannelInterface instance in test class
  }

  @Before
  public void setUp() throws Exception {
    MemoryMetaStore metaStore = new MemoryMetaStore();
    connectionManager = ConnectionManager.getInstance();
    connectionManager.setMetastoreSupplier( () -> metaStore );

    // (re)initialize the default state of MetaverseConfig
    setupMetaverseConfig( true, true );
    connectionManager = spy (connectionManager );
    when (  connectionManager.getConnectionDetails( BaseRuntimeExtensionPoint.DEFAULT_CATALOG_CONNECTION_NAME ) ).thenReturn( connectionDetails );
    Map<String, String> map = new HashMap<>();
    map.put("url", "someurl" );
    map.put("username", "devuser" );
    map.put("password", "password" );
    map.put("tokenUrl", "someurl" );
    map.put("clientId", "id" );
    map.put("clientSecret", "" );
    when ( connectionDetails.getProperties() ).thenReturn( map );
    jobExtensionPoint = new JobRuntimeExtensionPoint();
    jobExtensionPoint.setCatalogLineageClientProvider( clientProvider );
    jobExtensionPoint.setRuntimeEnabled( true );
    lineageWriter = mock( ILineageWriter.class );
    jobExtensionPoint.setLineageWriter( lineageWriter );

    jobMeta = spy( new JobMeta() );
    jobMeta.setName( TEST_JOB_NAME );
    jobMeta.setFilename( TEST_JOB_PATH );
    jobMeta.setDescription( TEST_JOB_DESCRIPTION );
    job = new Job( null, jobMeta );
    job.setExecutingServer( TEST_SERVER );
    job.setExecutingUser( TEST_USER );
    job.setVariable( TEST_VAR_NAME, TEST_VAR_VALUE );
    when( jobMeta.getUsedVariables() ).thenReturn( Collections.singletonList( TEST_VAR_NAME ) );
    job.addParameterDefinition( TEST_PARAM_NAME, TEST_PARAM_DEFAULT_VALUE, TEST_PARAM_DESCRIPTION );
    job.setParameterValue( TEST_PARAM_NAME, TEST_PARAM_VALUE );
    job.setArguments( new String[] { "arg0", "arg1" } );
  }

  private JobLineageHolderMap mockBuilder() {
    JobLineageHolderMap originalHolderMap = JobLineageHolderMap.getInstance();
    JobLineageHolderMap jobLineageHolderMap = spy( originalHolderMap );
    when( jobLineageHolderMap.getMetaverseBuilder( any( Job.class ) ) ).thenReturn( mockBuilder );
    JobLineageHolderMap.setInstance( jobLineageHolderMap );

    final IMetaverseObjectFactory objectFactory = mock( IMetaverseObjectFactory.class );
    final IDocument metaverseDocument = mock( IDocument.class );
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( objectFactory );
    when( objectFactory.createDocumentObject() ).thenReturn( metaverseDocument );

    return originalHolderMap;
  }

  @Test
  public void testCallExtensionPoint() throws Exception {
    JobLineageHolderMap originalHolderMap = mockBuilder();
    jobExtensionPoint.callExtensionPoint( null, job );
    List<JobListener> listeners = job.getJobListeners();
    assertNotNull( listeners );
    assertTrue( listeners.contains( jobExtensionPoint ) );

    // Restore original JobLineageHolderMap for use by others
    JobLineageHolderMap.setInstance( originalHolderMap );
  }

  @Test
  public void testJobMetaVariablesAreCombinedWithExistingJobVariables() throws Exception {
    JobLineageHolderMap originalHolderMap = mockBuilder();

    JobRuntimeExtensionPoint extensionPoint = new JobRuntimeExtensionPoint();
    IDocumentAnalyzer documentAnalyzer = Mockito.mock( IDocumentAnalyzer.class );

    final IMetaverseObjectFactory objectFactory = mock( IMetaverseObjectFactory.class );
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( objectFactory );
    final IDocument document = mock( IDocument.class );
    when( objectFactory.createDocumentObject() ).thenReturn( document );
    job.setVariable( "dontloseme", "okipromise" );
    extensionPoint.callExtensionPoint( null, job );
    assertEquals( "okipromise", job.getVariable( "dontloseme" ) );

    // Restore original JobLineageHolderMap for use by others
    JobLineageHolderMap.setInstance( originalHolderMap );
  }

  @Test
  public void testJobFinished() throws Exception {
    JobRuntimeExtensionPoint ext = spy( jobExtensionPoint );
    ext.jobFinished( null );
    verify( ext, never() ).populateExecutionProfile(
            any( IExecutionProfile.class ), eq( job ) );

    ext.jobFinished( job );
    verify( ext, times( 1 ) ).populateExecutionProfile( any( IExecutionProfile.class ), eq( job ) );
    verify( ext, times( 1 ) ).runAnalyzers( eq( job ) );
    verify( ext, times( 2 ) ).shouldCreateGraph( eq( job ) );
    verify( lineageWriter, times( 1 ) ).outputLineageGraph( any( LineageHolder.class ) );

    // Restore the original holder map
    Job mockJob = spy( job );
    Result result = mock( Result.class );
    when( mockJob.getResult() ).thenReturn( result );
    ext.jobFinished( mockJob );
    verify( ext, times( 1 ) ).populateExecutionProfile( any( IExecutionProfile.class ), eq( mockJob ) );
  }

  @Test
  public void testJobFinishedNotAsync() throws Exception {
    JobLineageHolderMap originalHolderMap = mockBuilder();

    JobRuntimeExtensionPoint ext = spy( jobExtensionPoint );
    when( ext.allowedAsync() ).thenReturn( false );

    // mock the LineageHolder, since we remove it now at the end of jobFinished
    final LineageHolder holder = Mockito.mock( LineageHolder.class );
    when( JobLineageHolderMap.getInstance().getLineageHolder( job ) ).thenReturn( holder );
    PowerMockito.mockStatic( ExtensionPointHandler.class );
    ext.jobFinished( job );
    verify( ext, times( 1 ) ).populateExecutionProfile( any( IExecutionProfile.class ), eq( job ) );
    verify( ext, times( 1 ) ).runAnalyzers( eq( job ) );
    verify( ext, times( 1 ) ).createLineGraph( job );
    verify( ext, never() ).createLineGraphAsync( job );
    verify( lineageWriter, times( 1 ) ).outputLineageGraph( holder );

    PowerMockito.verifyStatic();
    // the job doesn't have a parent, extension point should be called
    ExtensionPointHandler.callExtensionPoint( any( LogChannelInterface.class ),
      Mockito.eq( MetaverseExtensionPoint.JobLineageWriteEnd.id ), eq( job ) );

    // Restore original JobLineageHolderMap for use by others
    JobLineageHolderMap.setInstance( originalHolderMap );

    // reset the lineageWriter mock, since we're going to be verifying calls on it again
    Mockito.reset( lineageWriter );
    // set a parent trans  - since MetaverseConfig.generateSubGraph() returns true, lineageWriter.outputLineageGraph
    // should still get called
    job.setParentJob( new Job() );
    ext.jobFinished( job );
    verify( lineageWriter, times( 1 ) ).outputLineageGraph( JobLineageHolderMap.getInstance().getLineageHolder( job ) );

    // configure MetaverseConfig.generateSubGraph() to returns false, lineageWriter.outputLineageGraph should never
    // be called
    setupMetaverseConfig( true, false );
    // reset the lineageWriter mock, since we're going to be verifying calls on it again
    Mockito.reset( lineageWriter );

    ext.jobFinished( job );
    verify( lineageWriter, never() ).outputLineageGraph( JobLineageHolderMap.getInstance().getLineageHolder( job ) );

    Whitebox.setInternalState( job, "parentJob", (Object[]) null );

    setupMetaverseConfig( true, true );
    // set a parent trans  - since MetaverseConfig.generateSubGraph() returns true, lineageWriter.outputLineageGraph
    // should still get called
    job.setParentTrans( new Trans() );
    ext.jobFinished( job );
    verify( lineageWriter, times( 1 ) )
      .outputLineageGraph( JobLineageHolderMap.getInstance().getLineageHolder( job ) );

    // reset the lineageWriter mock, since we're going to be verifying calls on it again
    Mockito.reset( lineageWriter );
    // configure MetaverseConfig.generateSubGraph() to returns false, lineageWriter.outputLineageGraph should never
    // be called
    setupMetaverseConfig( true, false );
    ext.jobFinished( job );
    verify( lineageWriter, never() )
      .outputLineageGraph( JobLineageHolderMap.getInstance().getLineageHolder( job ) );
  }

  @Test
  public void testJobFinishedAsync() throws Exception {
    JobLineageHolderMap originalHolderMap = mockBuilder();

    JobRuntimeExtensionPoint ext = spy( jobExtensionPoint );
    when( ext.allowedAsync() ).thenReturn( true );
    ext.jobFinished( job );
    verify( ext ).createLineGraphAsync( job );

    // Restore original JobLineageHolderMap for use by others
    JobLineageHolderMap.setInstance( originalHolderMap );
  }

  @Test
  public void testJobStarted() throws Exception {
    // Test jobStarted for coverage, it should do nothing
    jobExtensionPoint.jobStarted( null );
  }

  @Test
  public void testRuntimeDisabled() throws Exception {
    jobExtensionPoint.setRuntimeEnabled( false );
    jobExtensionPoint.callExtensionPoint( null, job );
    List<JobListener> listeners = job.getJobListeners();
    assertNotNull( listeners );
    assertFalse( listeners.contains( jobExtensionPoint ) );
  }

  private void createExecutionProfile( Job job ) {
    IExecutionProfile executionProfile = mock( IExecutionProfile.class );

    IExecutionData executionData = mock( IExecutionData.class );
    when( executionProfile.getExecutionData() ).thenReturn( executionData );
    JobLineageHolderMap.getInstance().getLineageHolder( job ).setExecutionProfile( executionProfile );
  }

  private void setupMetaverseConfig( final boolean consolidateSubGraphs, final boolean generateSubGraphs ) {
    PowerMockito.mockStatic( MetaverseConfig.class );
    Mockito.when( MetaverseConfig.consolidateSubGraphs() ).thenReturn( consolidateSubGraphs );
    Mockito.when( MetaverseConfig.generateSubGraphs() ).thenReturn( generateSubGraphs );
    Mockito.when( MetaverseConfig.getInstance() ).thenCallRealMethod();
    PowerMockito.mockStatic( KettleAnalyzerUtil.class );
    Mockito.when( KettleAnalyzerUtil.consolidateSubGraphs() ).thenReturn( consolidateSubGraphs );
  }
}
