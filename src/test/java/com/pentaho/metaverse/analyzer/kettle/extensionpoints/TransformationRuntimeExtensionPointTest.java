package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class TransformationRuntimeExtensionPointTest {

  private static final String TEST_SERVER = "test.pentaho.com";
  private static final String TEST_USER = "joe";
  private static final String TEST_TRANS_NAME = "test transformation";
  private static final String TEST_TRANS_PATH = "/path/to/test/transformation.ktr";
  private static final String TEST_TRANS_DESCRIPTION = "This is a test transformation.";
  private static final String TEST_VAR_NAME = "testVariable";
  private static final String TEST_VAR_VALUE = "testVariableValue";
  private static final String TEST_PARAM_NAME = "testParam";
  private static final String TEST_PARAM_VALUE = "testParamValue";
  private static final String TEST_PARAM_DEFAULT_VALUE = "testParamDefaultValue";
  private static final String TEST_PARAM_DESCRIPTION = "Test parameter description";


  TransformationRuntimeExtensionPoint transExtensionPoint;
  Trans trans;
  TransMeta transMeta;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    transExtensionPoint = new TransformationRuntimeExtensionPoint();

    transMeta = spy( new TransMeta() );
    transMeta.setName( TEST_TRANS_NAME );
    transMeta.setDescription( TEST_TRANS_DESCRIPTION );
    transMeta.setFilename( TEST_TRANS_PATH );

    trans = new Trans( transMeta );
    trans.setExecutingServer( TEST_SERVER );
    trans.setExecutingUser( TEST_USER );
    trans.setVariable( TEST_VAR_NAME, TEST_VAR_VALUE );
    when( transMeta.getUsedVariables() ).thenReturn( Collections.singletonList( TEST_VAR_NAME ) );
    trans.addParameterDefinition( TEST_PARAM_NAME, TEST_PARAM_DEFAULT_VALUE, TEST_PARAM_DESCRIPTION );
    trans.setParameterValue( TEST_PARAM_NAME, TEST_PARAM_VALUE );
    trans.setArguments( new String[]{ "arg0", "arg1" } );

  }

  @Test
  public void testCallExtensionPoint() throws Exception {
    transExtensionPoint.callExtensionPoint( null, trans );
    List<TransListener> listeners = trans.getTransListeners();
    assertNotNull( listeners );
    assertTrue( listeners.contains( transExtensionPoint ) );
  }

  @Test
  public void testTransStarted() throws Exception {
    TransformationRuntimeExtensionPoint ext = spy( transExtensionPoint );
    ext.transStarted( null );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    ext.transStarted( trans );
    verify( ext, times( 1 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );
    // TODO more asserts
  }

  @Test
  public void testTransFinished() throws Exception {
    TransformationRuntimeExtensionPoint ext = spy( transExtensionPoint );
    ext.transFinished( null );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    ext.transFinished( trans );
    verify( ext, times( 1 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    Trans mockTrans = spy( trans );
    Result result = mock( Result.class );
    when( mockTrans.getResult() ).thenReturn( result );
    ext.transFinished( mockTrans );
    verify( ext, times( 2 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    // Test IOException handling during execution profile output
    doThrow( new IOException() ).when( ext ).writeExecutionProfile(
      Mockito.any( PrintStream.class ), Mockito.any( IExecutionProfile.class ) );
    Exception ex = null;
    try {
      ext.transFinished( mockTrans );
    } catch ( Exception e ) {
      ex = e;
    }
    assertNotNull( ex );
    verify( ext, times( 3 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    // TODO more asserts
  }

  @Test
  public void testTransActive() {
    // Test transActive for coverage, it should do nothing
    transExtensionPoint.transActive( null );
  }

  @Test
  public void testCallStepExtensionPoint() throws Exception {
    TransformationRuntimeExtensionPoint.ExternalResourceConsumerListener stepExtensionPoint =
      new TransformationRuntimeExtensionPoint.ExternalResourceConsumerListener();
    StepMetaDataCombi stepCombi = mock( StepMetaDataCombi.class );
    BaseStepMeta bsm = mock( BaseStepMeta.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    stepCombi.meta = (StepMetaInterface) bsm;
    stepCombi.step = mock( StepInterface.class );
    stepCombi.stepMeta = mock( StepMeta.class );

    stepExtensionPoint.callExtensionPoint( null, stepCombi );
    Map<Class<? extends BaseStepMeta>, List<IStepExternalResourceConsumer>> stepConsumerMap =
      ExternalResourceConsumerMap.getInstance().getStepConsumerMap();
    List<IStepExternalResourceConsumer> consumers = new ArrayList<IStepExternalResourceConsumer>();
    stepConsumerMap.put( bsm.getClass(), consumers );
    stepExtensionPoint.callExtensionPoint( null, stepCombi );
    IStepExternalResourceConsumer consumer = mock( IStepExternalResourceConsumer.class );
    when( consumer.getResourcesFromMeta( Mockito.any() ) ).thenReturn( Collections.emptyList() );
    consumers.add( consumer );
    Trans mockTrans = mock( Trans.class );
    when( stepCombi.step.getTrans() ).thenReturn( mockTrans );
    stepExtensionPoint.callExtensionPoint( null, stepCombi );
    when( consumer.isDataDriven( Mockito.any() ) ).thenReturn( Boolean.TRUE );
    stepExtensionPoint.callExtensionPoint( null, stepCombi );
  }

  @Test
  public void testCallStepAddExternalResources() {
    TransformationRuntimeExtensionPoint.ExternalResourceConsumerListener stepExtensionPoint =
      new TransformationRuntimeExtensionPoint.ExternalResourceConsumerListener();
    stepExtensionPoint.addExternalResources( null, null );
    StepInterface mockStep = mock( StepInterface.class );
    Trans mockTrans = mock( Trans.class );
    when( mockStep.getTrans() ).thenReturn( mockTrans );
    createExecutionProfile( mockTrans );
    Collection<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    stepExtensionPoint.addExternalResources( externalResources, mockStep );
    IExternalResourceInfo externalResource = mock( IExternalResourceInfo.class );
    externalResources.add( externalResource );
    stepExtensionPoint.addExternalResources( externalResources, mockStep );
  }

  @Test
  public void testStepExternalConsumerRowListener() throws Exception {
    IStepExternalResourceConsumer consumer = mock( IStepExternalResourceConsumer.class );
    StepInterface mockStep = mock( StepInterface.class );
    Trans mockTrans = mock( Trans.class );
    when( mockStep.getTrans() ).thenReturn( mockTrans );
    createExecutionProfile( mockTrans );

    TransformationRuntimeExtensionPoint.StepExternalConsumerRowListener listener =
      new TransformationRuntimeExtensionPoint.StepExternalConsumerRowListener( consumer, mockStep );


    RowMetaInterface rmi = mock( RowMetaInterface.class );
    Object[] row = new Object[0];

    listener.rowReadEvent( rmi, row );
  }

  private void createExecutionProfile( Trans trans ) {
    IExecutionProfile executionProfile = mock( IExecutionProfile.class );

    IExecutionData executionData = mock( IExecutionData.class );
    when( executionProfile.getExecutionData() ).thenReturn( executionData );
    TransformationRuntimeExtensionPoint.profileMap.put( trans, executionProfile );
  }
}
