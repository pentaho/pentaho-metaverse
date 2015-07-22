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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

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
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
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
import org.pentaho.metaverse.util.MetaverseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLConnection;
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

  private static final Logger log = LoggerFactory.getLogger( TransformationRuntimeExtensionPoint.class );

  private IDocumentAnalyzer documentAnalyzer;

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

    // Only generate lineage/execution information for "real" transformations, not the preview or debug ones. Whether
    // in Preview or Debug mode in Spoon, trans.isPreview() returns true, so just check that.
    if ( trans.isPreview() ) {
      return;
    }

    // Create and populate an execution profile with what we know so far
    ExecutionProfile executionProfile = new ExecutionProfile();
    populateExecutionProfile( executionProfile, trans );

    IMetaverseBuilder builder = TransLineageHolderMap.getInstance().getMetaverseBuilder( trans );

    // Analyze the current transformation
    if ( documentAnalyzer != null ) {
      documentAnalyzer.setMetaverseBuilder( builder );

      // Create a document for the Trans
      final String clientName = executionProfile.getExecutionEngine().getName();
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
    LineageHolder holder = TransLineageHolderMap.getInstance().getLineageHolder( trans );
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
    if ( trans.getRepository() == null ) {
      try {
        filePath = KettleAnalyzerUtil.normalizeFilePath( filename );
      } catch ( Exception e ) {
        log.error( "Couldn't normalize file path: " + filename, e );
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

    // Need to spin this processing off into its own thread, so we don't hold up normal PDI processing
    Thread lineageWorker = new Thread( new Runnable() {

      @Override
      public void run() {

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
              log.error( "Error during generation of lineage graph for " + trans.getName(), e );
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
            log.error( "Error while writing out execution profile for " + trans.getName(), e );
          }

          // Only create a lineage graph for this trans if it has no parent. If it does, the parent will incorporate the
          // lineage information into its own graph
          try {
            Job parentJob = trans.getParentJob();
            Trans parentTrans = trans.getParentTrans();

            if ( parentJob == null && parentTrans == null ) {
              // Add the execution profile information to the lineage graph
              addRuntimeLineageInfo( holder );

              if ( lineageWriter != null && !"none".equals( lineageWriter.getOutputStrategy() ) ) {
                lineageWriter.outputLineageGraph( holder );
              }
            }
          } catch ( IOException e ) {
            log.error( "Error while writing out lineage graph for " + trans.getName(), e );
          }
        } catch ( Throwable t ) {
          t.printStackTrace(); // TODO logger?
        }
      }
    } );

    lineageWorker.start();
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
