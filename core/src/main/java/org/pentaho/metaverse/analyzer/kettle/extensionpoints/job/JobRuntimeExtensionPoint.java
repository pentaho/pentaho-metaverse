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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.JobAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.ICatalogLineageClientProvider;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IParamInfo;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.api.model.kettle.MetaverseExtensionPoint;
import org.pentaho.metaverse.graph.GraphCatalogWriter;
import org.pentaho.metaverse.graph.GraphMLWriter;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.impl.VfsLineageWriter;
import org.pentaho.metaverse.impl.model.ExecutionProfile;
import org.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Supplier;

/**
 * An extension point to gather runtime data for an execution of a job into an ExecutionProfile object
 */
@ExtensionPoint(
        description = "Job Runtime metadata extractor",
        extensionPointId = "JobStart",
        id = "jobRuntimeMetaverse" )
public class JobRuntimeExtensionPoint extends BaseRuntimeExtensionPoint implements JobListener {

  private static final Logger log = LogManager.getLogger( JobRuntimeExtensionPoint.class );
  private static final String DEFAULT_CATALOG_CONNECTION_NAME = "catalog-vfs-connection";
  private static final String KETTLE_CATALOG_LINEAGE_CONNECTION_NAME = "KETTLE_CATALOG_LINEAGE_CONNECTION_NAME";

  private ICatalogLineageClientProvider catalogLineageClientProvider;

  public JobRuntimeExtensionPoint() {
    super();
    this.setDocumentAnalyzer( new JobAnalyzer() );
    this.setupLinageWriter();
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

      // If runtime lineage collection is disabled, don't run any lineage processes/methods
      if ( !isRuntimeEnabled() ) {
        return;
      }

      // Add the job finished listener
      job.addJobListener( this );

      createExecutionProfile( logChannelInterface, job );

      setConsoleLog( logChannelInterface );
    }
  }

  @Override
  protected LineageHolder getLineageHolder( final Object o ) {
    if ( o instanceof Job ) {
      Job job = ( (Job) o );
      return JobLineageHolderMap.getInstance().getLineageHolder( job );
    }
    return null;
  }

  protected void runAnalyzers( final Job job ) throws KettleException {

    // If runtime lineage collection is disabled, don't run any lineage processes/methods
    if ( job != null && isRuntimeEnabled() ) {


      IMetaverseBuilder builder = JobLineageHolderMap.getInstance().getMetaverseBuilder( job );
      final LineageHolder holder = JobLineageHolderMap.getInstance().getLineageHolder( job );
      IDocumentAnalyzer documentAnalyzer = getDocumentAnalyzer();

      if ( documentAnalyzer != null ) {
        documentAnalyzer.setMetaverseBuilder( builder );

        // Create a document for the Trans
        final String clientName = getExecutionEngineInfo().getName();
        final INamespace namespace = new Namespace( clientName );

        final IMetaverseNode designNode = builder.getMetaverseObjectFactory()
          .createNodeObject( clientName, clientName, DictionaryConst.NODE_TYPE_LOCATOR );
        builder.addNode( designNode );

        final JobMeta jobMeta = job.getJobMeta();

        // The variables and parameters in the Job may not have been set on the meta, so we do it here
        // to ensure the job analyzer will have access to the parameter values.
        jobMeta.copyParametersFrom( job );
        jobMeta.activateParameters();
        job.copyVariablesFrom( jobMeta );
        if ( job.getRep() != null ) {
          jobMeta.setRepository( job.getRep() );
        }

        String id = getFilename( jobMeta );
        if ( !id.endsWith( jobMeta.getDefaultExtension() ) ) {
          id += "." + jobMeta.getDefaultExtension();
        }

        final IDocument metaverseDocument = KettleAnalyzerUtil.buildDocument( builder, jobMeta, id, namespace );

        Runnable analyzerRunner = MetaverseUtil.getAnalyzerRunner( documentAnalyzer, metaverseDocument );
        // set the lineage task, so that we can wait for it to finish before proceeding to write out the graph
        holder.setLineageTask( MetaverseCompletionService.getInstance().submit( analyzerRunner, id ) );
      }

      // Save the lineage objects for later
      holder.setMetaverseBuilder( builder );
    }
  }

  /**
   * The job has finished.
   *
   * @param job
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void jobFinished( final Job job ) throws KettleException {

    if ( job == null ) {
      return;
    }

    log.warn( Messages.getString( "INFO.JobAnalyzeStarting", job.getJobname() ) );
    logMinimal( Messages.getString( "INFO.JobAnalyzeStarting", job.getJobname() ) );

    if ( shouldCreateGraph( job ) ) {
      runAnalyzers( job );
    }

    if ( allowedAsync() ) {
      createLineGraphAsync( job );
    } else {
      createLineGraph( job );
    }
  }

  protected void createLineGraphAsync( final Job job ) {
    // Need to spin this processing off into its own thread, so we don't hold up normal PDI processing
    Thread lineageWorker = new Thread( new Runnable() {

      @Override
      public void run() {
        createLineGraph( job );
      }
    } );

    lineageWorker.start();
  }

  protected void createLineGraph( final Job job ) {
    log.info( Messages.getString( "INFO.WrittingGraphForJob", job.getJobname() ) );
    try {
      // Get the current execution profile for this transformation
      LineageHolder holder = JobLineageHolderMap.getInstance().getLineageHolder( job );
      Future lineageTask = holder.getLineageTask();
      if ( lineageTask != null ) {
        try {
          lineageTask.get();
        } catch ( InterruptedException e ) {
          e.printStackTrace(); // TODO logger?
        } catch ( ExecutionException e ) {
          e.printStackTrace(); // TODO logger?
        }
      }

      // Get the current execution profile for this job
      IExecutionProfile executionProfile =
        JobLineageHolderMap.getInstance().getLineageHolder( job ).getExecutionProfile();
      if ( executionProfile == null ) {
        // Note that this should NEVER happen, this is purely a preventative measure...
        // Something's wrong here, the transStarted method didn't properly store the execution profile. We should know
        // the same info, so populate a new ExecutionProfile using the current Trans

        executionProfile = new ExecutionProfile();
      }
      populateExecutionProfile( executionProfile, job );

      // Export the lineage info (execution profile, lineage graph, etc.)
      try {
        if ( lineageWriter != null && !"none".equals( lineageWriter.getOutputStrategy() ) ) {
          // NOTE: This next call to clearOutput needs only to be done once before outputExecutionProfile and
          // outputLineage graph. If the order of these calls changes somehow, make sure to move the call to
          // clearOutput right before the first call to outputXYZ().
          if ( "latest".equals( lineageWriter.getOutputStrategy() ) ) {
            lineageWriter.cleanOutput( holder );
          }
          lineageWriter.outputExecutionProfile( holder );
        }
      } catch ( IOException e ) {
        log.warn( Messages.getString( "ERROR.CouldNotWriteExecutionProfile", job.getName(), e.getMessage() ) );
        log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), e );
      }

      try {
        if ( shouldCreateGraph( job ) ) {
          // Add the execution profile information to the lineage graph
          addRuntimeLineageInfo( holder );

          if ( lineageWriter != null && !"none".equals( lineageWriter.getOutputStrategy() ) ) {
            lineageWriter.outputLineageGraph( holder );
            // lineage has been written - call the appropriate extension point
            ExtensionPointHandler.callExtensionPoint(
              job.getLogChannel(), MetaverseExtensionPoint.JobLineageWriteEnd.id, job );
          }
        }
      } catch ( IOException e ) {
        log.warn( Messages.getString( "ERROR.CouldNotWriteLineageGraph", job.getName(),
          Const.NVL( e.getLocalizedMessage(), "Unspecified" ) ) );
        log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), e );
      }

    } catch ( Throwable t ) {
      log.warn( Messages.getString( "ERROR.ErrorDuringAnalysis", job.getName(),
        Const.NVL( t.getLocalizedMessage(), "Unspecified" ) ) );
      log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), t );
    }
    // cleanup to prevent unnecessary memory usage - we no longer need this Job in the JobLineageHolderMap
    JobLineageHolderMap.getInstance().removeLineageHolder( job );

    log.warn( Messages.getString( "INFO.JobAnalyzeFinished", job.getJobname() ) );
    logMinimal( Messages.getString( "INFO.JobAnalyzeFinished", job.getJobname() ) );

  }

  protected void populateExecutionProfile( IExecutionProfile executionProfile, Job job ) {
    JobMeta jobMeta = job.getJobMeta();

    String filename = getFilename( job );

    String filePath = null;
    if ( job.getRep() == null ) {
      try {
        filePath = KettleAnalyzerUtil.normalizeFilePath( filename );
      } catch ( Exception e ) {
        log.warn( "Couldn't normalize file path: " + filename, e );
        filePath = filename;
      }
    } else {
      filePath = filename;
    }

    // Set artifact information (path, type, description, etc.)
    executionProfile.setPath( filePath );
    executionProfile.setName( jobMeta.getName() );
    executionProfile.setType( DictionaryConst.NODE_TYPE_JOB );
    executionProfile.setDescription( jobMeta.getDescription() );

    // Set execution engine information
    executionProfile.setExecutionEngine( getExecutionEngineInfo() );

    IExecutionData executionData = executionProfile.getExecutionData();

    // Store execution information (client, server, user, etc.)
    executionData.setEndTime( new Timestamp( new Date().getTime() ) );

    KettleClientEnvironment.ClientType clientType = KettleClientEnvironment.getInstance().getClient();
    executionData.setClientExecutor( clientType == null ? "DI Server" : clientType.name() );

    executionData.setExecutorUser( job.getExecutingUser() );
    executionData.setExecutorServer( job.getExecutingServer() );

    Result result = job.getResult();
    if ( result != null ) {
      executionData.setFailureCount( result.getNrErrors() );
    }

    // Store variables
    List<String> vars = jobMeta.getUsedVariables();
    Map<Object, Object> variableMap = executionData.getVariables();
    for ( String var : vars ) {
      String value = job.getVariable( var );
      if ( value != null ) {
        variableMap.put( var, value );
      }
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

  public static String getFilename( Job job ) {
    String filename = job.getFilename();
    if ( filename == null && job.getJobMeta() != null ) {
      filename = getFilename( job.getJobMeta() );
    }
    if ( filename == null ) {
      filename = "";
    }
    return filename;
  }

  public static String getFilename( JobMeta jobMeta ) {
    String filename = jobMeta.getFilename();
    if ( filename == null ) {
      // try to get the dir and name from the job
      if ( jobMeta.getRepositoryDirectory() != null ) {
        String dir = jobMeta.getRepositoryDirectory().getPath();
        String name = jobMeta.getName();
        File f = new File( dir, name );
        filename = f.getPath();
      } else {
        filename = "";
      }
    }
    return filename;
  }

}

