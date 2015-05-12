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

package com.pentaho.metaverse.analyzer.kettle.step.rest;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rest.RestMeta;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 5/11/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class RestClientStepAnalyzerTest {

  private RestClientStepAnalyzer analyzer;

  @Mock
  private RestMeta mockMeta;

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

  private String[] prevStepNames = new String[]{ "previousStep" };

  @Mock
  private RowMetaInterface prevStepFields;

  @Mock
  private ValueMetaInterface mockField;

  @Before
  public void setUp() throws Exception {
    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    when( mockMeta.getParentStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockField.getOrigin() ).thenReturn( "previousStep" );
    when( mockField.getName() ).thenReturn( "previousField" );

    when( prevStepFields.searchValueMeta( anyString() ) ).thenReturn( mockField );
    when( prevStepFields.getFieldNames() ).thenReturn( prevStepNames );

    when( mockTransMeta.getPrevStepNames( mockStepMeta ) ).thenReturn( prevStepNames );
    when( mockTransMeta.getPrevStepNames( anyString() ) ).thenReturn( prevStepNames );
    when( mockTransMeta.getPrevStepFields( eq( mockStepMeta ), any( ProgressMonitorListener.class ) ) ).thenReturn( prevStepFields );

    when( mockTransMeta.getStepFields( eq( mockStepMeta ), any( ProgressMonitorListener.class ) ) ).thenReturn( prevStepFields );

    analyzer = new RestClientStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );

    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_JOB, mockNamespace );

  }

  @Test
  public void testAnalyze_urlDefined() throws Exception {
    when( mockMeta.isUrlInField() ).thenReturn( false );
    when( mockTransMeta.environmentSubstitute( anyString() ) ).thenReturn( "http://my.rest.url" );

    IMetaverseNode node = analyzer.analyze( descriptor, mockMeta );
    assertNotNull( node );

    // should add the step node as well as the resource node
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );
    verify( mockBuilder ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ),
      any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_urlFromField() throws Exception {
    when( mockMeta.isUrlInField() ).thenReturn( true );
    when( mockMeta.getUrlField() ).thenReturn( "URL" );
    when( mockTransMeta.environmentSubstitute( anyString() ) ).thenReturn( "http://my.rest.url" );

    IMetaverseNode node = analyzer.analyze( descriptor, mockMeta );
    assertNotNull( node );

    // should add the step node as well as the resource node
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );
    verify( mockBuilder ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ),
      any( IMetaverseNode.class ) );
    // it should use the field providing the url
    verify( mockBuilder ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_methodFromField() throws Exception {
    when( mockMeta.isDynamicMethod() ).thenReturn( true );
    when( mockMeta.getMethodFieldName() ).thenReturn( "METHOD" );

    IMetaverseNode node = analyzer.analyze( descriptor, mockMeta );
    assertNotNull( node );

    // it should use the field providing the method
    verify( mockBuilder ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_bodyFromField() throws Exception {
    when( mockMeta.getBodyField() ).thenReturn( "BODY" );

    IMetaverseNode node = analyzer.analyze( descriptor, mockMeta );
    assertNotNull( node );

    // it should use the field providing the body
    verify( mockBuilder ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_headersFromFields() throws Exception {
    String[] headerNames = new String[]{ "header1", "header2", "header3" };
    String[] headerFields = new String[] { "field1", "field2", "field3" };
    when( mockMeta.getHeaderName() ).thenReturn( headerNames );
    when( mockMeta.getHeaderField() ).thenReturn( headerFields );
    IMetaverseNode node = analyzer.analyze( descriptor, mockMeta );
    assertNotNull( node );

    // it should use the field providing the body
    verify( mockBuilder, times( headerFields.length ) ).addLink(
      any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_paramsFromFields() throws Exception {
    String[] names = new String[]{ "param1", "param2", "param3" };
    String[] fields = new String[] { "field1", "field2", "field3" };
    when( mockMeta.getParameterName() ).thenReturn( names );
    when( mockMeta.getParameterField() ).thenReturn( fields );
    IMetaverseNode node = analyzer.analyze( descriptor, mockMeta );
    assertNotNull( node );

    // it should use the field providing the body
    verify( mockBuilder, times( fields.length ) ).addLink(
      any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( RestMeta.class ) );
  }
}