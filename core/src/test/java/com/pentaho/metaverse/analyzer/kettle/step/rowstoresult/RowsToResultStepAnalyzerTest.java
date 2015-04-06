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

package com.pentaho.metaverse.analyzer.kettle.step.rowstoresult;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.AnalysisContext;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowstoresult.RowsToResultMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 4/3/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class RowsToResultStepAnalyzerTest {

  protected RowsToResultStepAnalyzer _analyzer;
  protected RowsToResultStepAnalyzer analyzer;

  @Mock
  protected RowsToResultMeta meta;
  @Mock
  protected IMetaverseBuilder builder;
  @Mock
  protected INamespace namespace;
  @Mock
  protected INamespace parentNamespace;
  @Mock
  protected IComponentDescriptor descriptor;
  @Mock
  protected StepMeta parentStepMeta;
  @Mock
  protected TransMeta parentTransMeta;
  @Mock
  protected TransMeta transMeta;
  @Mock
  protected RowMetaInterface outputFields;

  @Before
  public void setUp() throws Exception {

    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( builder.getMetaverseObjectFactory() ).thenReturn( factory );

    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
    when( descriptor.getParentNamespace() ).thenReturn( namespace );
    when( descriptor.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getContext() ).thenReturn( new AnalysisContext( DictionaryConst.CONTEXT_DEFAULT, null ) );

    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( parentStepMeta.getName() ).thenReturn( "parentStepMeta" );

    _analyzer = new RowsToResultStepAnalyzer();
    _analyzer.setMetaverseBuilder( builder );
    analyzer = spy( _analyzer );

    doReturn( outputFields ).when( analyzer ).getOutputFields( meta );
    List<ValueMetaInterface> fields = new ArrayList<ValueMetaInterface>();
    ValueMetaInterface field1 = mock( ValueMetaInterface.class );
    ValueMetaInterface field2 = mock( ValueMetaInterface.class );
    when( field1.getName() ).thenReturn( "one" );
    when( field2.getName() ).thenReturn( "two" );
    when( field1.getTypeDesc() ).thenReturn( "String" );
    when( field2.getTypeDesc() ).thenReturn( "String" );
    fields.add( field1 );
    fields.add( field2 );
    when( outputFields.getValueMetaList() ).thenReturn( fields );
  }

  @Test
  public void testAnalyze() throws Exception {
    analyzer.analyze( descriptor, meta );
    verify( builder, atLeast( 2 ) ).addNode( any( IMetaverseNode.class ) );
    verify( builder, atLeast( 2 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_CREATES ),
      any( IMetaverseNode.class ) );
    verify( builder, times( 2 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ),
      any( IMetaverseNode.class ) );
    verify( builder, times( 2 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_noOutputRowMetaInterface() throws Exception {
    doReturn( null ).when( analyzer ).getOutputFields( meta );

    analyzer.analyze( descriptor, meta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_noValueMetasInRowMetaInterface() throws Exception {
    when( outputFields.getValueMetaList() ).thenReturn( null );

    analyzer.analyze( descriptor, meta );
  }

  @Test
  public void testGetSupportedSteps() {
    RowsToResultStepAnalyzer analyzer = new RowsToResultStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( RowsToResultMeta.class ) );
  }
}
