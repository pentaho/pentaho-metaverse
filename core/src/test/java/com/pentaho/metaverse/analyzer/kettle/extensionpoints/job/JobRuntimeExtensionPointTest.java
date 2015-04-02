/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.job;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.impl.MetaverseBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;

import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.IMetaverseBuilder;

@RunWith( MockitoJUnitRunner.class )
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

  @Mock
  private IMetaverseBuilder mockBuilder;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
  }

  @Before
  public void setUp() throws Exception {
    jobExtensionPoint = new JobRuntimeExtensionPoint();
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
    job.setArguments( new String[]{ "arg0", "arg1" } );
  }

  @Test
  public void testCallExtensionPoint() throws Exception {
    JobRuntimeExtensionPoint spyJobExtensionPoint = spy( jobExtensionPoint );
    when( spyJobExtensionPoint.getMetaverseBuilder( Mockito.any( Job.class ) ) ).thenReturn( mockBuilder );
    spyJobExtensionPoint.callExtensionPoint( null, job );
    List<JobListener> listeners = job.getJobListeners();
    assertNotNull( listeners );
    assertTrue( listeners.contains( spyJobExtensionPoint ) );
  }

  @Test
  public void testJobFinished() throws Exception {
    JobRuntimeExtensionPoint ext = spy( jobExtensionPoint );
    ext.jobFinished( null );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Job.class ) );

    ext.jobFinished( job );
    verify( ext, times( 1 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Job.class ) );

    Job mockJob = spy( job );
    Result result = mock( Result.class );
    when( mockJob.getResult() ).thenReturn( result );
    ext.jobFinished( mockJob );
    verify( ext, times( 2 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Job.class ) );

    // Test IOException handling during execution profile output
    doThrow( new IOException() ).when( ext ).writeLineageInfo( Mockito.any( LineageHolder.class ) );
    Exception ex = null;
    try {
      ext.jobFinished( mockJob );
    } catch ( Exception e ) {
      ex = e;
    }
    assertNotNull( ex );
    verify( ext, times( 3 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Job.class ) );

    // TODO more asserts

  }

  @Test
  public void testJobStarted() throws Exception {
    // Test jobStarted for coverage, it should do nothing
    jobExtensionPoint.jobStarted( null );
  }

  private void createExecutionProfile( Job job ) {
    IExecutionProfile executionProfile = mock( IExecutionProfile.class );

    IExecutionData executionData = mock( IExecutionData.class );
    when( executionProfile.getExecutionData() ).thenReturn( executionData );
    JobRuntimeExtensionPoint.getLineageHolder( job ).setExecutionProfile( executionProfile );
  }
}
