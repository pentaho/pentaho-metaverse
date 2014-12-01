package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionEngine;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IParamInfo;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionEngine;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.pentaho.metaverse.impl.model.ParamInfo;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.version.BuildVersion;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extension point to gather runtime data for an execution of a transformation into an ExecutionProfile object
 */
@ExtensionPoint(
  description = "Transformation Runtime metadata extractor",
  extensionPointId = "TransformationStartThreads",
  id = "transRuntimeMetaverse" )
public class TransformationRuntimeExtensionPoint implements ExtensionPointInterface, TransListener {

  private Map<Trans, IExecutionProfile> profileMap = new HashMap<Trans, IExecutionProfile>();

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

    TransMeta transMeta = trans.getTransMeta();

    File f = new File( trans.getFilename() );

    String filePath = null;
    try {
      filePath = f.getCanonicalPath();
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    ExecutionProfile executionProfile = new ExecutionProfile();

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

    // Save the execution profile for later
    profileMap.put( trans, executionProfile );
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

    // Get the current execution profile for this transformation
    IExecutionProfile executionProfile = profileMap.remove( trans );
    ExecutionData executionData = (ExecutionData) executionProfile.getExecutionData();
    executionData.setFailureCount( trans.getResult().getNrErrors() );

    // TODO where to persist the execution profile?
    try {
      dumpExecutionProfile( System.out, executionProfile );
    } catch ( IOException e ) {
      throw new KettleException( e );
    }

  }

  // TODO move this somewhere else?
  protected void dumpExecutionProfile( PrintStream out, IExecutionProfile executionProfile ) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable( SerializationFeature.INDENT_OUTPUT );
    mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    mapper.enable( SerializationFeature.WRAP_EXCEPTIONS );
    try {
      out.println( mapper.writeValueAsString( executionProfile ) );
    } catch ( JsonProcessingException jpe ) {
      throw new IOException( jpe );
    }
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
      // TODO get steps that use external resources, match them to this step
    }
  }
}
