/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.pentaho.metaverse.impl.model.ParamInfo;

/**
 * An extension point to gather runtime data for an execution of a job into an ExecutionProfile object
 */
@ExtensionPoint(
  description = "Job Runtime metadata extractor",
  extensionPointId = "JobStart",
  id = "jobRuntimeMetaverse" )
public class JobRuntimeExtensionPoint extends BaseRuntimeExtensionPoint implements JobListener {

  protected static Map<Job, IExecutionProfile> profileMap = new ConcurrentHashMap<Job, IExecutionProfile>();

  public static Map<Job, IExecutionProfile> getProfileMap() {
    return profileMap;
  }

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
    if ( o instanceof Job ) {
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
      executionProfile.setExecutionEngine( getExecutionEngineInfo() );

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

    if ( job == null ) {
      return;
    }

    // Get the current execution profile for this job
    IExecutionProfile executionProfile = profileMap.remove( job );
    if ( executionProfile == null ) {
      // Something's wrong here, the transStarted method didn't properly store the execution profile. We should know
      // the same info, so populate a new ExecutionProfile using the current Trans
      // TODO: Beware duplicate profiles!

      executionProfile = new ExecutionProfile();
      populateExecutionProfile( executionProfile, job );
    }
    ExecutionData executionData = (ExecutionData) executionProfile.getExecutionData();
    Result result = job.getResult();
    if ( result != null ) {
      executionData.setFailureCount( result.getNrErrors() );
    }
    try {
      writeExecutionProfile( System.out, executionProfile );
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
  }

  protected void populateExecutionProfile( IExecutionProfile executionProfile, Job job ) {
    JobMeta jobMeta = job.getJobMeta();

    String filename = job.getFilename();

    String filePath = null;
    try {
      filePath = new File( filename ).getCanonicalPath();
    } catch ( IOException e ) {
      // TODO ?
    }

    // Set artifact information (path, type, description, etc.)
    executionProfile.setPath( filePath );
    executionProfile.setType( DictionaryConst.NODE_TYPE_TRANS );
    executionProfile.setDescription( jobMeta.getDescription() );

    // Set execution engine information
    executionProfile.setExecutionEngine( getExecutionEngineInfo() );

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
  }

  @Override
  public void jobStarted( Job job ) throws KettleException {
    // Do nothing, this method has already been called before the extension point could add the listener
  }
}
