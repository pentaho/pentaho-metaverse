package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;

import com.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public class JobRuntimeExtensionPointTest {

  private static final String TEST_SERVER = "test.pentaho.com";
  private static final String TEST_USER = "joe";
  private static final String TEST_JOB_NAME = "test jobformation";
  private static final String TEST_JOB_PATH = "/path/to/test/jobformation.ktr";
  private static final String TEST_JOB_DESCRIPTION = "This is a test jobformation.";
  private static final String TEST_VAR_NAME = "testVariable";
  private static final String TEST_VAR_VALUE = "testVariableValue";
  private static final String TEST_PARAM_NAME = "testParam";
  private static final String TEST_PARAM_VALUE = "testParamValue";
  private static final String TEST_PARAM_DEFAULT_VALUE = "testParamDefaultValue";
  private static final String TEST_PARAM_DESCRIPTION = "Test parameter description";

  JobRuntimeExtensionPoint jobExtensionPoint;
  Job job;
  JobMeta jobMeta;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
  }

  @Before
  public void setUp() throws Exception {
    jobExtensionPoint = new JobRuntimeExtensionPoint();
    jobMeta = spy( new JobMeta() );
    jobMeta.setFilename( "test.kjb" );
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
    jobExtensionPoint.callExtensionPoint( null, job );
    List<JobListener> listeners = job.getJobListeners();
    assertNotNull( listeners );
    assertTrue( listeners.contains( jobExtensionPoint ) );
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
    doThrow( new IOException() ).when( ext ).writeExecutionProfile(
      Mockito.any( PrintStream.class ), Mockito.any( IExecutionProfile.class ) );
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
    JobRuntimeExtensionPoint.profileMap.put( job, executionProfile );
  }
}
