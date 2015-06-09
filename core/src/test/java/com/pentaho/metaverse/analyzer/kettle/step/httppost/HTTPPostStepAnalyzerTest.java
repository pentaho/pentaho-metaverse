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

package com.pentaho.metaverse.analyzer.kettle.step.httppost;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOST;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;

@RunWith( MockitoJUnitRunner.class )
public class HTTPPostStepAnalyzerTest {

  private HTTPPostStepAnalyzer analyzer;

  @Mock HTTPPOST mockHTTPPost;
  @Mock HTTPPOSTMeta meta;
  @Mock StepNodes stepNodes;
  @Mock INamespace mockNamespace;

  IComponentDescriptor descriptor;

  @Mock StepMeta mockStepMeta;
  @Mock TransMeta mockTransMeta;
  @Mock RowMetaInterface mockRowMetaInterface;

  @Before
  public void setUp() throws Exception {

    analyzer = spy( new HTTPPostStepAnalyzer() );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( MetaverseTestUtils.getMetaverseObjectFactory() );

    when( mockHTTPPost.getStepMetaInterface() ).thenReturn( meta );
    when( mockHTTPPost.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
  }

  @Test
  public void testGetUsedFields_urlInField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getUrlField() ).thenReturn( "url" );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( "url", stepNodes );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "url", stepNodes );
  }

  @Test
  public void testGetUsedFields_urlInFieldNoFieldSet() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getUrlField() ).thenReturn( null );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer, never() ).createStepFields( anyString(), any( StepNodes.class ) );
  }

  @Test
  public void testGetUsedFields_getQueryField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.getQueryField() ).thenReturn( new String[] { "param1", "param2" } );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), eq( stepNodes ) );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "param1", stepNodes );
    verify( analyzer ).createStepFields( "param2", stepNodes );
  }

  @Test
  public void testGetUsedFields_getArgumentField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.getArgumentField() ).thenReturn( new String[] { "header1", "header2" } );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), eq( stepNodes ) );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "header1", stepNodes );
    verify( analyzer ).createStepFields( "header2", stepNodes );
  }

  @Test
  public void testGetInputRowMetaInterface() throws Exception {
    Map<String, RowMetaInterface> inputs = new HashMap<>();
    doReturn( inputs ).when( analyzer ).getInputFields( meta );

    Map<String, RowMetaInterface> inputRowMetaInterfaces = analyzer.getInputRowMetaInterfaces( meta );
    assertEquals( inputs, inputRowMetaInterfaces );
  }

  @Test
  public void testCreateResourceNode() throws Exception {
    IExternalResourceInfo res = mock( IExternalResourceInfo.class );
    when( res.getName() ).thenReturn( "http://my.rest.url" );

    IMetaverseNode resourceNode = analyzer.createResourceNode( res );
    assertNotNull( resourceNode );
    assertEquals( DictionaryConst.NODE_TYPE_WEBSERVICE, resourceNode.getType() );
    assertEquals( "http://my.rest.url", resourceNode.getName() );

  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_WEBSERVICE, analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertNull( analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertFalse( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertTrue( analyzer.isInput() );
  }

  @Test
  public void testGetSupportedSteps() {
    HTTPPostStepAnalyzer analyzer = new HTTPPostStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( HTTPPOSTMeta.class ) );
  }

  @Test
  public void testHTTPPostExternalResourceConsumer() throws Exception {
    HTTPPostExternalResourceConsumer consumer = new HTTPPostExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", this.meta );
    StepMeta spyMeta = spy( meta );

    when( this.meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( this.meta.getUrl() ).thenReturn( "http://seylermartialarts.com" );
    when( this.meta.isUrlInField() ).thenReturn( false );

    assertTrue( consumer.isDataDriven( this.meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( this.meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );


    when( this.meta.isUrlInField() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( this.meta ) );
    assertTrue( consumer.getResourcesFromMeta( this.meta ).isEmpty() );
    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenReturn( "/path/to/row/file" );
    resources = consumer.getResourcesFromRow( mockHTTPPost, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockHTTPPost, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( HTTPPOSTMeta.class, consumer.getMetaClass() );
  }
}
