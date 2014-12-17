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
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.BaseRuntimeExtensionPoint;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
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
public class TransformationRuntimeExtensionPoint extends BaseRuntimeExtensionPoint implements TransListener {

  private static Map<Trans, IExecutionProfile> profileMap = new ConcurrentHashMap<Trans, IExecutionProfile>();

  public static Map<Trans, IExecutionProfile> getProfileMap() {
    return profileMap;
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
    executionProfile.setExecutionEngine( getExecutionEngineInfo() );

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
}
