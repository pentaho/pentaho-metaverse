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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.KettleAnalyzerUtil;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobRuntimeExtensionPoint;
import com.pentaho.metaverse.api.AnalysisContext;
import com.pentaho.metaverse.api.IDocument;
import com.pentaho.metaverse.api.IDocumentAnalyzer;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseException;
import com.pentaho.metaverse.api.Namespace;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.impl.MetaverseCompletionService;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.pentaho.metaverse.impl.model.ParamInfo;
import com.pentaho.metaverse.util.MetaverseBeanUtil;
import com.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;

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
 * An extension point to gather runtime data for an execution of a transformation into an ExecutionProfile object
 */
@ExtensionPoint(
  description = "Transformation Runtime metadata extractor",
  extensionPointId = "TransformationStartThreads",
  id = "transRuntimeMetaverse" )
public class TransformationRuntimeExtensionPoint extends BaseRuntimeExtensionPoint implements TransListener {

  private IDocumentAnalyzer documentAnalyzer;

  private static Map<Trans, LineageHolder> lineageHolderMap = new ConcurrentHashMap<Trans, LineageHolder>();

  public static LineageHolder getLineageHolder( Trans t ) {
    LineageHolder holder = lineageHolderMap.get( t );
    if ( holder == null ) {
      holder = new LineageHolder();
      putLineageHolder( t, holder );
    }
    return holder;
  }

  public static void putLineageHolder( Trans t, LineageHolder holder ) {
    lineageHolderMap.put( t, holder );
  }

  /**
   * Callback when a transformation is about to be started
   *
   * @param logChannelInterface A reference to the log in this context (the Trans object's log)
   * @param o                   The object being operated on (Trans in this case)
   * @throws KettleException
   */
  @Override
  public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {

    // Transformation Started listeners get called after the extension point is invoked, so just add a trans listener
    if ( o instanceof Trans ) {
      Trans trans = ( (Trans) o );
      trans.addTransListener( this );
    }

  }

  /**
   * Called when a transformation is started (if this object has been registered as a listener for the Trans)
   *
   * @param trans a reference to the started transformation
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void transStarted( Trans trans ) throws KettleException {
    if ( trans == null ) {
      return;
    }

    // Create and populate an execution profile with what we know so far
    ExecutionProfile executionProfile = new ExecutionProfile();
    populateExecutionProfile( executionProfile, trans );

    IMetaverseBuilder builder = getMetaverseBuilder( trans );

    // Analyze the current transformation
    if ( documentAnalyzer != null ) {
      documentAnalyzer.setMetaverseBuilder( builder );

      // Create a document for the Trans TODO - fix this!
      final String clientName = "PDI Engine";
      final INamespace namespace = new Namespace( clientName );

      final IMetaverseNode designNode = builder.getMetaverseObjectFactory()
        .createNodeObject( clientName, clientName, DictionaryConst.NODE_TYPE_LOCATOR );
      builder.addNode( designNode );

      final TransMeta transMeta = trans.getTransMeta();

      String id = TransExtensionPointUtil.getFilename( transMeta );

      IDocument metaverseDocument = builder.getMetaverseObjectFactory().createDocumentObject();

      metaverseDocument.setNamespace( namespace );
      metaverseDocument.setContent( transMeta );
      metaverseDocument.setStringID( id );
      metaverseDocument.setName( transMeta.getName() );
      metaverseDocument.setExtension( "ktr" );
      metaverseDocument.setMimeType( URLConnection.getFileNameMap().getContentTypeFor( "trans.ktr" ) );
      metaverseDocument.setContext( new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
      String normalizedPath;
      try {
        normalizedPath = KettleAnalyzerUtil.normalizeFilePath( id );
      } catch ( MetaverseException e ) {
        normalizedPath = id;
      }
      metaverseDocument.setProperty( DictionaryConst.PROPERTY_NAME, trans.getName() );
      metaverseDocument.setProperty( DictionaryConst.PROPERTY_PATH, normalizedPath );
      metaverseDocument.setProperty( DictionaryConst.PROPERTY_NAMESPACE, namespace.getNamespaceId() );

      Runnable analyzerRunner = MetaverseUtil.getAnalyzerRunner( documentAnalyzer, metaverseDocument );

      MetaverseCompletionService.getInstance().submit( analyzerRunner, id );
    }

    // Save the lineage objects for later
    LineageHolder holder = getLineageHolder( trans );
    holder.setExecutionProfile( executionProfile );
    holder.setMetaverseBuilder( builder );


  }

  protected void populateExecutionProfile( IExecutionProfile executionProfile, Trans trans ) {
    TransMeta transMeta = trans.getTransMeta();

    String filename = trans.getFilename();

    if ( filename == null ) {
      filename = transMeta.getPathAndName();
    }

    String filePath = null;
    try {
      filePath = KettleAnalyzerUtil.normalizeFilePath( filename );
    } catch ( Exception e ) {
      // TODO ?
    }

    // Set artifact information (path, type, description, etc.)
    executionProfile.setPath( filePath );
    executionProfile.setName( transMeta.getName() );
    executionProfile.setType( DictionaryConst.NODE_TYPE_TRANS );
    executionProfile.setDescription( transMeta.getDescription() );

    // Set execution engine information
    executionProfile.setExecutionEngine( getExecutionEngineInfo() );

    IExecutionData executionData = executionProfile.getExecutionData();

    // Store execution information (client, server, user, etc.)
    executionData.setStartTime( new Timestamp( new Date().getTime() ) );
    KettleClientEnvironment.ClientType clientType = KettleClientEnvironment.getInstance().getClient();
    executionData.setClientExecutor( clientType == null ? "DI Server" : clientType.name() );
    executionData.setExecutorUser( trans.getExecutingUser() );
    executionData.setExecutorServer( trans.getExecutingServer() );

    // Store variables
    List<String> vars = transMeta.getUsedVariables();
    Map<Object, Object> variableMap = executionData.getVariables();
    for ( String var : vars ) {
      String value = trans.getVariable( var );
      if ( var != null && value != null ) {
        variableMap.put( var, value );
      }
    }

    // Store parameters
    String[] params = trans.listParameters();
    List<IParamInfo<String>> paramList = executionData.getParameters();
    if ( params != null ) {
      for ( String param : params ) {
        try {
          ParamInfo paramInfo = new ParamInfo( param, trans.getParameterDescription( param ),
            trans.getParameterDefault( param ) );
          paramList.add( paramInfo );
        } catch ( UnknownParamException e ) {
          e.printStackTrace(); // TODO logging?
        }
      }
    }

    // Store arguments
    String[] args = trans.getArguments();
    List<Object> argList = executionData.getArguments();
    if ( args != null ) {
      argList.addAll( Arrays.asList( args ) );
    }
  }

  /**
   * This transformation went from an inactive to an active state.
   *
   * @param trans
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void transActive( Trans trans ) {
    // Do nothing here
  }

  /**
   * The transformation has finished.
   *
   * @param trans
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void transFinished( Trans trans ) throws KettleException {

    if ( trans == null ) {
      return;
    }

    // Get the current execution profile for this transformation
    LineageHolder holder = getLineageHolder( trans );
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
    IExecutionProfile executionProfile = holder.getExecutionProfile();
    if ( executionProfile == null ) {
      // Something's wrong here, the transStarted method didn't properly store the execution profile. We should know
      // the same info, so populate a new ExecutionProfile using the current Trans
      // TODO: Beware duplicate profiles!

      executionProfile = new ExecutionProfile();
      populateExecutionProfile( executionProfile, trans );
    }
    ExecutionData executionData = (ExecutionData) executionProfile.getExecutionData();
    Result result = trans.getResult();
    if ( result != null ) {
      executionData.setFailureCount( result.getNrErrors() );
    }

    // Export the lineage info (execution profile, lineage graph, etc.)
    try {
      if ( lineageWriter != null ) {
        lineageWriter.outputExecutionProfile( holder );
      }
    } catch ( IOException e ) {
      throw new KettleException( e );
    }

    // Only create a lineage graph for this trans if it has no parent. If it does, the parent will incorporate the
    // lineage information into its own graph
    try {
      Job parentJob = trans.getParentJob();
      Trans parentTrans = trans.getParentTrans();

      if ( parentJob == null && parentTrans == null ) {
        // Add the execution profile information to the lineage graph
        addRuntimeLineageInfo( holder );

        if ( lineageWriter != null ) {
          lineageWriter.outputLineageGraph( holder );
        }
      }
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
  }

  protected IMetaverseBuilder getMetaverseBuilder( Trans trans ) {
    if ( trans != null ) {
      if ( trans.getParentJob() == null && trans.getParentTrans() == null ) {
        return (IMetaverseBuilder) MetaverseBeanUtil.getInstance().get( "IMetaverseBuilderPrototype" );
      } else {
        if ( trans.getParentJob() != null ) {
          // Get the builder for the job
          return JobRuntimeExtensionPoint.getLineageHolder( trans.getParentJob() ).getMetaverseBuilder();
        } else {
          return TransformationRuntimeExtensionPoint.getLineageHolder( trans.getParentTrans() ).getMetaverseBuilder();
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
}
