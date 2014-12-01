package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionEngine;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionEngine;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.pentaho.metaverse.impl.model.ExecutionProfileUtil;
import com.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.version.BuildVersion;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extension point to gather runtime data for an execution of a job into an ExecutionProfile object
 */
@ExtensionPoint(
    description = "Job Runtime metadata extractor",
    extensionPointId = "JobStart",
    id = "jobRuntimeMetaverse" )
public class JobRuntimeExtensionPoint implements ExtensionPointInterface, JobListener {

  private Map<Job, IExecutionProfile> profileMap = new HashMap<Job, IExecutionProfile>();

  /**
   * Callback when a job is about to be started
   *
   * @param logChannelInterface A reference to the log in this context (the Job object's log)
   * @param o                   The object being operated on (Job in this case)
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {

    // Job Started listeners get called after the extension point is invoked, so just add a job listener
    if ( o != null && o instanceof Job ) {
      Job job = ( (Job) o );

      // Add the job finished listener
      job.addJobListener( this );

      JobMeta jobMeta = job.getJobMeta();

      File f = new File( job.getFilename() );

      String filePath = null;
      try {
        filePath = f.getCanonicalPath();
      } catch ( IOException e ) {
        e.printStackTrace();
      }

      ExecutionProfile executionProfile = new ExecutionProfile();

      // Set artifact information (path, type, description, etc.)
      executionProfile.setPath( filePath );
      executionProfile.setType( DictionaryConst.NODE_TYPE_JOB );
      executionProfile.setDescription( jobMeta.getDescription() );

      // Set execution engine information
      IExecutionEngine executionEngine = new ExecutionEngine();
      executionEngine.setName( "Pentaho Data Integration" );
      executionEngine.setVersion( BuildVersion.getInstance().getVersion() );
      executionEngine.setDescription(
          "Pentaho data integration prepares and blends data to create a complete picture of your business "
              + "that drives actionable insights." );
      executionProfile.setExecutionEngine( executionEngine );

      IExecutionData executionData = executionProfile.getExecutionData();

      // Store execution information (client, server, user, etc.)
      executionData.setStartTime( new Timestamp( new Date().getTime() ) );
      executionData.setClientExecutor( KettleClientEnvironment.getInstance().getClient().name() );
      executionData.setExecutorUser( job.getExecutingUser() );
      executionData.setExecutorServer( job.getExecutingServer() );

      // Store variables
      List<String> vars = jobMeta.getUsedVariables();
      Map<Object, Object> variableMap = executionData.getVariables();
      for ( String var : vars ) {
        String value = job.getVariable( var );
        variableMap.put( var, value );
      }

      // Store parameters
      String[] params = job.listParameters();
      List<IParamInfo<String>> paramList = executionData.getParameters();
      if ( params != null ) {
        for ( String param : params ) {
          try {
            ParamInfo paramInfo = new ParamInfo( param, job.getParameterDescription( param ),
                job.getParameterDefault( param ) );
            paramList.add( paramInfo );
          } catch ( UnknownParamException e ) {
            e.printStackTrace();
          }
        }
      }

      // Store arguments
      String[] args = job.getArguments();
      List<Object> argList = executionData.getArguments();
      if ( args != null ) {
        argList.addAll( Arrays.asList( args ) );
      }

      // Save the execution profile for later
      profileMap.put( job, executionProfile );
      System.out.println( "Added job profile: " + job.getName() );
    }
  }

  /**
   * The job has finished.
   *
   * @param job
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void jobFinished( Job job ) throws KettleException {

    // Get the current execution profile for this job
    IExecutionProfile executionProfile = profileMap.remove( job );
    if ( executionProfile == null ) {
      System.out.println( "Couldn't find job profile for: " + job.getName() );
    } else {
      ExecutionData executionData = (ExecutionData) executionProfile.getExecutionData();
      executionData.setFailureCount( job.getResult().getNrErrors() );
    }

    // TODO where to persist the execution profile?
    try {
      ExecutionProfileUtil.dumpExecutionProfile( System.out, executionProfile );
    } catch ( IOException e ) {
      throw new KettleException( e );
    }

  }

  @Override
  public void jobStarted( Job job ) throws KettleException {
    // Do nothing, this method has already been called before the extension point could add the listener
  }

  @ExtensionPoint(
      description = "Job step external resource listener",
      extensionPointId = "JobBeforeJobEntryExecution",
      id = "jobEntryExternalResource" )
  public static class ExternalResourceConsumerListener implements ExtensionPointInterface {


    /**
     * This method is called by the Kettle code when a step is about to start
     *
     * @param log    the logging channel to log debugging information to
     * @param object The subject object that is passed to the plugin code
     * @throws org.pentaho.di.core.exception.KettleException In case the plugin decides that an error has occurred
     *                                                       and the parent process should stop.
     */
    @Override
    public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
      JobExecutionExtension jobExec = (JobExecutionExtension) object;
      // TODO get job entries that use external resources, match them to this step
    }
  }
}
