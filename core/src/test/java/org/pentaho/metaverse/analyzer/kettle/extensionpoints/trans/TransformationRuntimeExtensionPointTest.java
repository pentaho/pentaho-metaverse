/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.api.ILineageWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionProfile;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
  ILineageWriter lineageWriter;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    transExtensionPoint = new TransformationRuntimeExtensionPoint();
    transExtensionPoint.setRuntimeEnabled( true );
    lineageWriter = mock( ILineageWriter.class );
    transExtensionPoint.setLineageWriter( lineageWriter );

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
    TransLineageHolderMap originalHolderMap = TransLineageHolderMap.getInstance();
    TransLineageHolderMap transLineageHolderMap = spy( originalHolderMap );
    when( transLineageHolderMap.getMetaverseBuilder( Mockito.any( Trans.class ) ) )
      .thenReturn( mock( IMetaverseBuilder.class ) );
    TransLineageHolderMap.setInstance( transLineageHolderMap );
    ext.transStarted( null );
    verify( ext, never() ).startAnalyzer( Mockito.any( Trans.class ) );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    ext.transStarted( trans );
    verify( ext, never() ).startAnalyzer( Mockito.any( Trans.class ) );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );
    // Restore the original holder map
    TransLineageHolderMap.setInstance( originalHolderMap );

    Trans mockTrans = spy( trans );
    ext.transStarted( mockTrans );
    verify( ext, never() ).startAnalyzer( mockTrans );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );
  }

  @Test
  public void testTransFinished() throws Exception {
    TransformationRuntimeExtensionPoint ext = spy( transExtensionPoint );
    ext.transFinished( null );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    ext.transFinished( trans );
    verify( ext, times( 1 ) ).startAnalyzer( trans );
    verify( ext, times( 1 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );

    Trans mockTrans = spy( trans );
    Result result = mock( Result.class );
    when( mockTrans.getResult() ).thenReturn( result );
    ext.transFinished( mockTrans );
    // The logic in transFinished() is now in a thread, so we can't verify methods were called
    verify( ext, times( 1 ) ).startAnalyzer( mockTrans );
    verify( ext, times( 2 ) ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );
  }

  @Test
  public void testTransFinishedNotAsync() throws Exception {
    TransformationRuntimeExtensionPoint ext = spy( transExtensionPoint );
    when( ext.allowedAsync() ).thenReturn( false );
    ext.transFinished( trans );

    verify( ext ).createLineGraph( trans );
    verify( ext, never() ).createLineGraphAsync( trans );
  }

  @Test
  public void testTransFinishedAsync() throws Exception {
    TransformationRuntimeExtensionPoint ext = spy( transExtensionPoint );
    when( ext.allowedAsync() ).thenReturn( true );
    ext.transFinished( trans );

    verify( ext ).createLineGraphAsync( trans );
  }

  @Test
  public void testTransActive() {
    // Test transActive for coverage, it should do nothing
    transExtensionPoint.transActive( null );
  }

  @Test
  public void testPreviewTrans() throws Exception {
    TransformationRuntimeExtensionPoint ext = spy( transExtensionPoint );
    trans.setPreview( true );
    ext.callExtensionPoint( null, trans );
    verify( ext, never() ).populateExecutionProfile(
      Mockito.any( IExecutionProfile.class ), Mockito.any( Trans.class ) );
  }

  @Test
  public void testRuntimeDisabled() throws Exception {
    transExtensionPoint.setRuntimeEnabled( false );
    transExtensionPoint.callExtensionPoint( null, trans );
    List<TransListener> listeners = trans.getTransListeners();
    assertNotNull( listeners );
    assertFalse( listeners.contains( transExtensionPoint ) );
  }
}
