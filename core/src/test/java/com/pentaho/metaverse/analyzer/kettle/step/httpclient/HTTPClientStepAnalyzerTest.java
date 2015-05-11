/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle.step.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.http.HTTP;
import org.pentaho.di.trans.steps.http.HTTPMeta;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;

@RunWith( MockitoJUnitRunner.class )
public class HTTPClientStepAnalyzerTest {

  private HTTPClientStepAnalyzer httpClientStepAnalyzer;

  @Mock
  private HTTP mockHTTP;

  @Mock
  private HTTPMeta mockHTTPMeta;

  @Mock
  private StepMeta mockStepMeta;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockRowMetaInterface;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private INamespace mockNamespace;

  private IMetaverseObjectFactory mockFactory;

  private IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {

    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    httpClientStepAnalyzer = new HTTPClientStepAnalyzer();
    httpClientStepAnalyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_JOB, mockNamespace );

    when( mockHTTP.getStepMetaInterface() ).thenReturn( mockHTTPMeta );
    when( mockHTTP.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockHTTPMeta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_nullInput() throws Exception {
    httpClientStepAnalyzer.analyze( null, null );
  }

  @Test
  public void testAnalyze_noParmsOrHeaders() throws Exception {

    StepMeta meta = new StepMeta( "test", mockHTTPMeta );
    StepMeta spyMeta = spy( meta );

    String urls = "http://seylermartialarts.com";

    when( mockHTTPMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockHTTPMeta.getUrl() ).thenReturn( urls );

    IMetaverseNode result = httpClientStepAnalyzer.analyze( descriptor, mockHTTPMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockHTTPMeta, times( 1 ) ).getUrl();

    // make sure the step node is added as well as the file node
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there is a "readby" link added
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_paramsAndHeaders() throws Exception {

    StepMeta meta = new StepMeta( "test", mockHTTPMeta );
    StepMeta spyMeta = spy( meta );

    String url = "http://seylermartialarts.com";

    when( mockHTTPMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockHTTPMeta.getUrl() ).thenReturn( url );
    when( mockHTTPMeta.getArgumentField() ).thenReturn( new String[]{ "Argument Field A", "Argument Field B" } );
    when( mockHTTPMeta.getArgumentParameter() ).thenReturn( new String[]{ "Argument Param 1", "Argument Parameter 2" } );
    when( mockHTTPMeta.getHeaderField() ).thenReturn( new String[]{ "Header Field 1", "Header Field 2" } );
    when( mockHTTPMeta.getHeaderParameter() ).thenReturn( new String[]{ "Header Param A", "Header Param B" } );
    // set up the input fields
    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[]{ "id", "name" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override
      public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "id" ) {
          return new ValueMetaString( "id" );
        }
        if ( args[0] == "name" ) {
          return new ValueMetaString( "name" );
        }
        return null;
      }
    } );

    IMetaverseNode result = httpClientStepAnalyzer.analyze( descriptor, mockHTTPMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockHTTPMeta, times( 1 ) ).getUrl();

    // make sure the step node, the file node, and the field nodes
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there are "readby" and "uses" links added (file, and each field)
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );
    verify( mockBuilder, times( 4 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_UrlFromField() throws Exception {

    StepMeta meta = new StepMeta( "test", mockHTTPMeta );
    StepMeta spyMeta = spy( meta );

    when( mockHTTPMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockHTTPMeta.getUrl() ).thenReturn( null );
    when( mockHTTPMeta.isUrlInField() ).thenReturn( true );
    when( mockHTTPMeta.getUrl() ).thenReturn( "http://seylermartialarts" );
    when( mockHTTPMeta.getArgumentField() ).thenReturn( new String[]{ "FieldA", "FieldB" } );
    when( mockHTTPMeta.getArgumentParameter() ).thenReturn( new String[]{ "Param1", "Param2" } );
    when( mockHTTPMeta.getHeaderField() ).thenReturn( new String[]{ "Header Field 1", "Header Field 2" } );
    when( mockHTTPMeta.getHeaderParameter() ).thenReturn( new String[]{ "Header Param A", "Header Param B" } );
    when( mockHTTPMeta.getUrlField() ).thenReturn( "Url Field" );

    IMetaverseNode result = httpClientStepAnalyzer.analyze( descriptor, mockHTTPMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockHTTPMeta, never() ).getUrl();
    verify( mockHTTPMeta, times( 2 ) ).isUrlInField();
    verify( mockHTTPMeta, times( 2 ) ).getUrlField();

    // make sure the step node and the field nodes have been added, but NOT the incoming stream field
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // Verify there are no READBY links (usually from file to step)
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

    // make sure there "uses" links added (each field, and the filename stream field)
    verify( mockBuilder, times( 5 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testGetSupportedSteps() {
    HTTPClientStepAnalyzer analyzer = new HTTPClientStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( HTTPMeta.class ) );
  }

  @Test
  public void testHTTPClientExternalResourceConsumer() throws Exception {
    HTTPClientExternalResourceConsumer consumer = new HTTPClientExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", mockHTTPMeta );
    StepMeta spyMeta = spy( meta );

    when( mockHTTPMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockHTTPMeta.getUrl() ).thenReturn( "http://seylermartialarts.com" );
    when( mockHTTPMeta.isUrlInField() ).thenReturn( false );

    assertFalse( consumer.isDataDriven( mockHTTPMeta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( mockHTTPMeta );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );


    when( mockHTTPMeta.isUrlInField() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( mockHTTPMeta ) );
    assertTrue( consumer.getResourcesFromMeta( mockHTTPMeta ).isEmpty() );
    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenReturn( "/path/to/row/file" );
    resources = consumer.getResourcesFromRow( mockHTTP, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockHTTP, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( HTTPMeta.class, consumer.getMetaClass() );
  }
}
