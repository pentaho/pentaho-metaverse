package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

public class JobRuntimeExtensionPointTest {
  
  JobRuntimeExtensionPoint jobExtensionPoint;
  LogChannelInterface logChannelInterface;
  Job job;
  JobMeta jobMeta;
  
  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
  }

  @Before
  public void setUp() throws Exception {
    jobExtensionPoint = new JobRuntimeExtensionPoint();
    jobMeta = new JobMeta();
    jobMeta.setFilename( "test.kjb" );
    job = new Job(null, jobMeta);
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

    Job mockJob= spy( job );
    Result result = mock( Result.class );
    when( mockJob.getResult() ).thenReturn( result );
    ext.callExtensionPoint( null, mockJob );
    ext.jobFinished( mockJob );
    verify( ext, times( 1 ) ).populateExecutionProfile(
        Mockito.any( IExecutionProfile.class ), Mockito.any( Job.class ) );

    // TODO more asserts

  }

  @Test
  public void testJobStarted() throws Exception {

  }
}
