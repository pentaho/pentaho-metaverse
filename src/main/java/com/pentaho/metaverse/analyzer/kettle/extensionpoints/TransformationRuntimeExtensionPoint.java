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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionEngine;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionEngine;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.pentaho.metaverse.impl.model.ExecutionProfileUtil;
import com.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.version.BuildVersion;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An extension point to gather runtime data for an execution of a transformation into an ExecutionProfile object
 */
@ExtensionPoint(
  description = "Transformation Runtime metadata extractor",
  extensionPointId = "TransformationStartThreads",
  id = "transRuntimeMetaverse" )
public class TransformationRuntimeExtensionPoint implements ExtensionPointInterface, TransListener {

  protected static Map<Trans, IExecutionProfile> profileMap = new ConcurrentHashMap<Trans, IExecutionProfile>();

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

    // Save the execution profile for later
    profileMap.put( trans, executionProfile );
  }

  protected void populateExecutionProfile( IExecutionProfile executionProfile, Trans trans ) {
    TransMeta transMeta = trans.getTransMeta();

    String filename = trans.getFilename();

    String filePath = null;
    try {
      filePath = new File( filename ).getCanonicalPath();
    } catch ( IOException e ) {
      // TODO ?
    }

    // Set artifact information (path, type, description, etc.)
    executionProfile.setPath( filePath );
    executionProfile.setType( DictionaryConst.NODE_TYPE_TRANS );
    executionProfile.setDescription( transMeta.getDescription() );

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
    executionData.setExecutorUser( trans.getExecutingUser() );
    executionData.setExecutorServer( trans.getExecutingServer() );

    // Store variables
    List<String> vars = transMeta.getUsedVariables();
    Map<Object, Object> variableMap = executionData.getVariables();
    for ( String var : vars ) {
      String value = trans.getVariable( var );
      variableMap.put( var, value );
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
          e.printStackTrace();
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
    IExecutionProfile executionProfile = profileMap.remove( trans );
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
    try {
      writeExecutionProfile( System.out, executionProfile );
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
  }

  protected void writeExecutionProfile( PrintStream out, IExecutionProfile executionProfile ) throws IOException {
    // TODO where to persist the execution profile?
    ExecutionProfileUtil.dumpExecutionProfile( out, executionProfile );
  }

  @ExtensionPoint(
    description = "Transformation step external resource listener",
    extensionPointId = "StepBeforeStart",
    id = "stepExternalResource" )
  public static class ExternalResourceConsumerListener implements ExtensionPointInterface {


    /**
     * This method is called by the Kettle code when a step is about to start
     *
     * @param log    the logging channel to log debugging information to
     * @param object The subject object that is passed to the plugin code
     * @throws KettleException In case the plugin decides that an error has occurred
     *                         and the parent process should stop.
     */
    @Override
    public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
      StepMetaDataCombi stepCombi = (StepMetaDataCombi) object;
      if ( stepCombi != null ) {
        StepMetaInterface meta = stepCombi.meta;
        StepMeta stepMeta = stepCombi.stepMeta;
        StepInterface step = stepCombi.step;

        if ( meta != null ) {
          Class<?> metaClass = meta.getClass();
          if ( BaseStepMeta.class.isAssignableFrom( metaClass ) ) {
            @SuppressWarnings( "unchecked" )
            List<IStepExternalResourceConsumer> stepConsumers =
              ExternalResourceConsumerMap.getInstance().getStepExternalResourceConsumers(
                (Class<? extends BaseStepMeta>) metaClass );
            if ( stepConsumers != null ) {
              for ( IStepExternalResourceConsumer stepConsumer : stepConsumers ) {
                // We might know enough at this point, so call the consumer
                Collection<IExternalResourceInfo> resources = stepConsumer.getResourcesFromMeta( stepMeta );
                addExternalResources( resources, step );

                // Add a RowListener if the step is data-driven
                if ( stepConsumer.isDataDriven( stepMeta ) ) {
                  stepCombi.step.addRowListener(
                    new StepExternalConsumerRowListener( stepConsumer, stepMeta ) );
                }
              }
            }
          }
        }
      }
    }

    protected void addExternalResources( Collection<IExternalResourceInfo> resources, StepInterface step ) {
      if ( resources != null ) {
        // Add the resources to the execution profile
        IExecutionProfile executionProfile = profileMap.get( step.getTrans() );
        if ( executionProfile != null ) {
          String stepName = step.getStepname();
          Map<String, List<IExternalResourceInfo>> resourceMap =
            executionProfile.getExecutionData().getExternalResources();
          List<IExternalResourceInfo> externalResources = resourceMap.get( stepName );
          if ( externalResources == null ) {
            externalResources = new LinkedList<IExternalResourceInfo>();
          }
          externalResources.addAll( resources );
          resourceMap.put( stepName, externalResources );
        }
        // TODO store by step?
      }
    }
  }


  public static class StepExternalConsumerRowListener extends RowAdapter {

    private IStepExternalResourceConsumer stepExternalResourceConsumer;
    private StepMeta stepMeta;

    public StepExternalConsumerRowListener(
      IStepExternalResourceConsumer stepExternalResourceConsumer, StepMeta stepMeta ) {
      this.stepExternalResourceConsumer = stepExternalResourceConsumer;
      this.stepMeta = stepMeta;
    }

    /**
     * Called when rows are read by the step to which this listener is attached
     *
     * @param rowMeta The metadata (value types, e.g.) of the associated row data
     * @param row     An array of Objects corresponding to the row data
     * @see org.pentaho.di.trans.step.RowListener#rowReadEvent(org.pentaho.di.core.row.RowMetaInterface,
     * Object[])
     */
    @Override
    public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {

      stepExternalResourceConsumer.getResourcesFromRow( stepMeta, rowMeta, row );
      //TODO
    }
  }
}
