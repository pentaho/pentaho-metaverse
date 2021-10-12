/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.IParamInfo;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.JdbcResourceInfo;
import org.pentaho.metaverse.api.model.kettle.MetaverseExtensionPoint;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.impl.model.ExecutionProfile;
import org.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

  private static final Logger log = LogManager.getLogger( TransformationRuntimeExtensionPoint.class );

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
    if ( o != null && o instanceof Trans ) {
      Trans trans = ( (Trans) o );

      // Only generate lineage/execution information for "real" transformations, not the preview or debug ones. Whether
      // in Preview or Debug mode in Spoon, trans.isPreview() returns true, so just check that.
      if ( trans.isPreview() || !isRuntimeEnabled() ) {
        return;
      }
      trans.addTransListener( this );

      createExecutionProfile( logChannelInterface, trans );
    }
  }

  @Override
  protected LineageHolder getLineageHolder( final Object o ) {
    if ( o instanceof Trans ) {
      Trans trans = ( (Trans) o );
      return TransLineageHolderMap.getInstance().getLineageHolder( trans );
    }
    return null;
  }

  /**
   * Called when a transformation is started (if this object has been registered as a listener for the Trans)
   *
   * @param trans a reference to the started transformation
   * @throws org.pentaho.di.core.exception.KettleException
   */
  @Override
  public void transStarted( Trans trans ) throws KettleException {

  }

  protected void runAnalyzers( Trans trans ) throws KettleException {

    // Only generate lineage/execution information for "real" transformations, not the preview or debug ones. Whether
    // in Preview or Debug mode in Spoon, trans.isPreview() returns true, so just check that.
    if ( trans == null || trans.isPreview() || !isRuntimeEnabled() ) {
      return;
    }

    IMetaverseBuilder builder = TransLineageHolderMap.getInstance().getMetaverseBuilder( trans );
    final LineageHolder holder = TransLineageHolderMap.getInstance().getLineageHolder( trans );
    IDocumentAnalyzer documentAnalyzer = getDocumentAnalyzer();

    if ( documentAnalyzer != null ) {
      documentAnalyzer.setMetaverseBuilder( builder );

      // Create a document for the Trans
      final String clientName = getExecutionEngineInfo().getName();
      final INamespace namespace = new Namespace( clientName );

      final IMetaverseNode designNode = builder.getMetaverseObjectFactory()
        .createNodeObject( clientName, clientName, DictionaryConst.NODE_TYPE_LOCATOR );
      builder.addNode( designNode );

      final String id = TransExtensionPointUtil.getFilename( trans.getTransMeta() );
      final IDocument metaverseDocument = KettleAnalyzerUtil.buildDocument( builder, trans.getTransMeta(), id, namespace );
      final Runnable analyzerRunner = MetaverseUtil.getAnalyzerRunner( documentAnalyzer, metaverseDocument );

      // set the lineage task, so that we can wait for it to finish before proceeding to write out the graph
      holder.setLineageTask( MetaverseCompletionService.getInstance().submit( analyzerRunner,
        metaverseDocument.getStringID() ) );
    }

    // Save the lineage objects for later
    holder.setMetaverseBuilder( builder );
  }

  protected void populateExecutionProfile( IExecutionProfile executionProfile, Trans trans ) {
    TransMeta transMeta = trans.getTransMeta();

    String filename = trans.getFilename();

    if ( filename == null ) {
      filename = transMeta.getPathAndName();
    }

    String filePath = null;
    if ( trans.getRepository() == null ) {
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
    executionProfile.setName( transMeta.getName() );
    executionProfile.setType( DictionaryConst.NODE_TYPE_TRANS );
    executionProfile.setDescription( transMeta.getDescription() );

    // Set execution engine information
    executionProfile.setExecutionEngine( getExecutionEngineInfo() );

    IExecutionData executionData = executionProfile.getExecutionData();

    // Store execution information (client, server, user, etc.)
    executionData.setEndTime( new Timestamp( new Date().getTime() ) );
    KettleClientEnvironment.ClientType clientType = KettleClientEnvironment.getInstance().getClient();
    executionData.setClientExecutor( clientType == null ? "DI Server" : clientType.name() );
    executionData.setExecutorUser( trans.getExecutingUser() );
    executionData.setExecutorServer( trans.getExecutingServer() );

    Result result = trans.getResult();
    if ( result != null ) {
      executionData.setFailureCount( result.getNrErrors() );
    }

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
          log.error( "Couldn't find transformation parameter: " + param, e );
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
  public void transFinished( final Trans trans ) throws KettleException {

    if ( trans == null ) {
      return;
    }

    if ( trans.isPreview() ) {
      return;
    }

    log.info( Messages.getString( "INFO.TransformationFinished", trans.getName() ) );
    if ( shouldCreateGraph( trans ) ) {
      runAnalyzers( trans );
    }

    if ( allowedAsync() ) {
      createLineGraphAsync( trans );
    } else {
      createLineGraph( trans );
    }
  }

  protected void createLineGraphAsync( Trans trans ) {
    // Need to spin this processing off into its own thread, so we don't hold up normal PDI processing
    Thread lineageWorker = new Thread( new Runnable() {

      @Override
      public void run() {
        createLineGraph( trans );
      }
    } );

    lineageWorker.start();
  }

  private void removeSensitiveDataFromHolder( LineageHolder holder ) {
    if ( holder.getExecutionProfile() == null ) {
      return;
    }
    Map<String, List<IExternalResourceInfo>> map =
      holder.getExecutionProfile().getExecutionData().getExternalResources();
    map.entrySet().stream().forEach( list -> {
      if ( list == null ) {
        return;
      }
      list.getValue().forEach( resourceInfo ->
        resourceInfo.cleanupSensitiveData()
      );

    } );
  }

  protected void createLineGraph( final Trans trans ) {
    log.info( Messages.getString( "INFO.WrittingGraphForTransformation", trans.getName() ) );
    try {
      // Get the current execution profile for this transformation
      LineageHolder holder = TransLineageHolderMap.getInstance().getLineageHolder( trans );

      Future lineageTask = holder.getLineageTask();
      if ( lineageTask != null ) {
        try {
          lineageTask.get();
        } catch ( InterruptedException e ) {
          // Do nothing
        } catch ( ExecutionException e ) {
          log.warn( Messages.getString( "ERROR.CouldNotWriteLineageGraph", trans.getName(),
            Const.NVL( e.getLocalizedMessage(), "Unspecified" ) ) );
          log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), e );
        }
      }
      IExecutionProfile executionProfile = holder.getExecutionProfile();
      if ( executionProfile == null ) {
        // Note that this should NEVER happen, this is purely a preventative measure...
        // Something's wrong here, the transStarted method didn't properly store the execution profile. We should know
        // the same info, so populate a new ExecutionProfile using the current Trans
        // TODO: Beware duplicate profiles!

        executionProfile = new ExecutionProfile();
      }
      populateExecutionProfile( executionProfile, trans );
      removeSensitiveDataFromHolder( holder );
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
        log.warn( Messages.getString( "ERROR.CouldNotWriteExecutionProfile", trans.getName(),
          Const.NVL( e.getLocalizedMessage(), "Unspecified" ) ) );
        log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), e );
      }

      try {
        if ( shouldCreateGraph( trans ) ) {
          // Add the execution profile information to the lineage graph
          addRuntimeLineageInfo( holder );

          if ( lineageWriter != null && !"none".equals( lineageWriter.getOutputStrategy() ) ) {
            lineageWriter.outputLineageGraph( holder );
            // lineage has been written - call the appropriate extension point
            ExtensionPointHandler.callExtensionPoint(
              trans.getLogChannel(), MetaverseExtensionPoint.TransLineageWriteEnd.id, trans );
          }
        }
      } catch ( IOException e ) {
        log.warn( Messages.getString( "ERROR.CouldNotWriteExecutionProfile", trans.getName(),
          Const.NVL( e.getLocalizedMessage(), "Unspecified" ) ) );
        log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), e );
      }
    } catch ( Throwable t ) {
      log.warn( Messages.getString( "ERROR.ErrorDuringAnalysis", trans.getName(),
        Const.NVL( t.getLocalizedMessage(), "Unspecified" ) ) );
      log.debug( Messages.getString( "ERROR.ErrorDuringAnalysisStackTrace" ), t );
    }

    // cleanup to prevent unnecessary memory usage - we no longer need this Trans in the TransLineageHolderMap
    TransLineageHolderMap.getInstance().removeLineageHolder( trans );
  }
}
