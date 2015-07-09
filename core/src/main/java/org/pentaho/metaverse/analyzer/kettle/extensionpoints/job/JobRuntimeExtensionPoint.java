/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.TransformationRuntimeExtensionPoint;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IParamInfo;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.impl.model.ExecutionData;
import org.pentaho.metaverse.impl.model.ExecutionProfile;
import org.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.metaverse.util.MetaverseBeanUtil;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * An extension point to gather runtime data for an execution of a job into an ExecutionProfile object
 */
@ExtensionPoint(
  description = "Job Runtime metadata extractor",
  extensionPointId = "JobStart",
  id = "jobRuntimeMetaverse" )
public class JobRuntimeExtensionPoint extends BaseRuntimeExtensionPoint implements JobListener {

  private IDocumentAnalyzer documentAnalyzer;

  private static Map<Job, LineageHolder> lineageHolderMap = new ConcurrentHashMap<Job, LineageHolder>();
  private static final Logger LOG = LoggerFactory.getLogger( JobRuntimeExtensionPoint.class );

  public static LineageHolder getLineageHolder( Job job ) {
    LineageHolder holder = lineageHolderMap.get( job );
    if ( holder == null ) {
      holder = new LineageHolder();
      lineageHolderMap.put( job, holder );
    }
    return holder;
  }

  public static void putLineageHolder( Job job, LineageHolder holder ) {
    lineageHolderMap.put( job, holder );
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

      // Create and populate an execution profile with what we know so far
      ExecutionProfile executionProfile = new ExecutionProfile();
      populateExecutionProfile( executionProfile, job );

      IMetaverseBuilder builder = getMetaverseBuilder( job );

      // Add the job finished listener
      job.addJobListener( this );

      // Analyze the current transformation
      if ( documentAnalyzer != null ) {
        documentAnalyzer.setMetaverseBuilder( builder );

        // Create a document for the Trans
        final String clientName = executionProfile.getExecutionEngine().getName();
        final INamespace namespace = new Namespace( clientName );

        final IMetaverseNode designNode = builder.getMetaverseObjectFactory()
          .createNodeObject( clientName, clientName, DictionaryConst.NODE_TYPE_LOCATOR );
        builder.addNode( designNode );

        final JobMeta jobMeta = job.getJobMeta();

        String id = getFilename( jobMeta );

        IDocument metaverseDocument = builder.getMetaverseObjectFactory().createDocumentObject();

        metaverseDocument.setNamespace( namespace );
        metaverseDocument.setContent( jobMeta );
        metaverseDocument.setStringID( id );
        metaverseDocument.setName( jobMeta.getName() );
        metaverseDocument.setExtension( "kjb" );
        metaverseDocument.setMimeType( URLConnection.getFileNameMap().getContentTypeFor( "job.kjb" ) );
        metaverseDocument.setContext( new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
        String normalizedPath;
        try {
          normalizedPath = KettleAnalyzerUtil.normalizeFilePath( id );
        } catch ( MetaverseException e ) {
          normalizedPath = id;
        }
        metaverseDocument.setProperty( DictionaryConst.PROPERTY_NAME, job.getName() );
        metaverseDocument.setProperty( DictionaryConst.PROPERTY_PATH, normalizedPath );
        metaverseDocument.setProperty( DictionaryConst.PROPERTY_NAMESPACE, namespace.getNamespaceId() );

        Runnable analyzerRunner = MetaverseUtil.getAnalyzerRunner( documentAnalyzer, metaverseDocument );

        MetaverseCompletionService.getInstance().submit( analyzerRunner, id );
      }

      // Save the lineage objects for later
      LineageHolder holder = getLineageHolder( job );
      holder.setExecutionProfile( executionProfile );
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
  public void jobFinished( Job job ) throws KettleException {

    if ( job == null ) {
      return;
    }

    // Get the current execution profile for this transformation
    LineageHolder holder = getLineageHolder( job );
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
    IExecutionProfile executionProfile = getLineageHolder( job ).getExecutionProfile();
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

    // Add the execution profile information to the lineage graph
    addRuntimeLineageInfo( holder );

    // Export the lineage info (execution profile, lineage graph, etc.)
    try {
      writeLineageInfo( holder );
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
      filename = job.getJobMeta().getName();
    }
    if ( filename == null ) {
      filename = "";
    }
    return filename;
  }

  protected IMetaverseBuilder getMetaverseBuilder( Job job ) {
    if ( job != null ) {
      if ( job.getParentJob() == null && job.getParentTrans() == null ) {
        return (IMetaverseBuilder) MetaverseBeanUtil.getInstance().get( "IMetaverseBuilderPrototype" );
      } else {
        if ( job.getParentJob() != null ) {
          // Get the builder for the job
          return JobRuntimeExtensionPoint.getLineageHolder( job.getParentJob() ).getMetaverseBuilder();
        } else {
          return TransformationRuntimeExtensionPoint.getLineageHolder( job.getParentTrans() ).getMetaverseBuilder();
        }
      }
    }
    return null;
  }

  /**
   * Sets the document analyzer for this extension point
   *
   * @param analyzer The document analyzer for this extension point
   */
  public void setDocumentAnalyzer( IDocumentAnalyzer analyzer ) {
    this.documentAnalyzer = analyzer;
  }

  /**
   * Gets the document analyzer of this extension point
   *
   * @return IDocumentAnalyzer - The document analyzer for this extension point
   */
  public IDocumentAnalyzer getDocumentAnalyzer() {
    return documentAnalyzer;
  }

  public static String getFilename( JobMeta jobMeta ) {
    String filename = jobMeta.getFilename();
    if ( filename == null ) {
      filename = "";
    }
    return filename;
  }
}
