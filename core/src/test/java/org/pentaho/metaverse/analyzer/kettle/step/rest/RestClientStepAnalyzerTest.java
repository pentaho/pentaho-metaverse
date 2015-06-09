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
 *
 */

package org.pentaho.metaverse.analyzer.kettle.step.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 5/11/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class RestClientStepAnalyzerTest {

  private RestClientStepAnalyzer analyzer;

  @Mock RestMeta meta;
  @Mock StepNodes stepNodes;
  @Mock INamespace mockNamespace;

  IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    analyzer = spy( new RestClientStepAnalyzer() );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( MetaverseTestUtils.getMetaverseObjectFactory() );

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
  public void testGetUsedFields_methodInField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.isDynamicMethod() ).thenReturn( true );
    when( meta.getMethodFieldName() ).thenReturn( "method" );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( "method", stepNodes );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "method", stepNodes );
  }

  @Test
  public void testGetUsedFields_methodInFieldNoFieldSet() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.isDynamicMethod() ).thenReturn( true );
    when( meta.getMethodFieldName() ).thenReturn( null );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer, never() ).createStepFields( anyString(), any( StepNodes.class ) );
  }

  @Test
  public void testGetUsedFields_bodyInField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.getBodyField() ).thenReturn( "body" );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( "body", stepNodes );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "body", stepNodes );
  }

  @Test
  public void testGetUsedFields_parameterField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.getParameterField() ).thenReturn( new String[] { "param1", "param2" } );
    doReturn( stepNodes ).when( analyzer ).getInputs();
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), eq( stepNodes ) );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );

    verify( analyzer ).createStepFields( "param1", stepNodes );
    verify( analyzer ).createStepFields( "param2", stepNodes );
  }

  @Test
  public void testGetUsedFields_headerField() throws Exception {
    Set<StepField> fields = new HashSet<>();
    when( meta.getHeaderField() ).thenReturn( new String[] { "header1", "header2" } );
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
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( RestMeta.class ) );
  }
}