/*
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

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
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
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValueMapperStepAnalyzerTest {

  public static final String STEP_NAME = "ValueMapperTest";
  private ValueMapperStepAnalyzer analyzer = null;

  @Mock
  private IMetaverseComponentDescriptor descriptor;

  @Mock
  private INamespace namespace;

  @Mock
  private ValueMapperMeta valueMapperMeta;

  @Mock
  private IMetaverseBuilder builder;

  @Mock
  private TransMeta transMeta;

  @Mock
  private RowMetaInterface prevRowMeta;

  @Mock
  ValueMetaInterface valueMetaInterfaceMock;

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( builder.getMetaverseObjectFactory() ).thenReturn( factory );
//    when( namespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );
    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
//    when( descriptor.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );
    when( descriptor.getParentNamespace() ).thenReturn( namespace );
    when( descriptor.getNamespaceId() ).thenReturn( "namespace" );

    when( valueMapperMeta.getFieldToUse() ).thenReturn( "Country" );
    when( valueMapperMeta.getSourceValue() ).thenReturn( new String[] { "United States", "Canada" } );
    when( valueMapperMeta.getTargetValue() ).thenReturn( new String[] { "USA", "CA" } );

    analyzer = new ValueMapperStepAnalyzer();
    analyzer.setMetaverseBuilder( builder );
    descriptor = new MetaverseComponentDescriptor( STEP_NAME, DictionaryConst.NODE_TYPE_TRANS, namespace );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testAnalyze_NullMeta() throws Exception {
    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {
    StepMeta stepMeta = new StepMeta( STEP_NAME, valueMapperMeta );
    StepMeta spyStepMeta = spy( stepMeta );

    when( valueMapperMeta.getParentStepMeta() ).thenReturn( spyStepMeta );
    when( spyStepMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( transMeta.getPrevStepFields( spyStepMeta ) ).thenReturn( prevRowMeta );

    IMetaverseNode node = analyzer.analyze( descriptor, valueMapperMeta );
    assertNotNull( node );

    assertEquals( stepMeta.getName(), node.getName() );

    verify( builder ).addNode( any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_OverwiteExistingField() throws Exception {
    StepMeta stepMeta = new StepMeta( STEP_NAME, valueMapperMeta );
    StepMeta spyStepMeta = spy( stepMeta );

    ValueMapperStepAnalyzer spyAnalyzer = spy( analyzer );

    when( valueMapperMeta.getParentStepMeta() ).thenReturn( spyStepMeta );
    when( spyStepMeta.getParentTransMeta() ).thenReturn( transMeta );

    when( transMeta.getPrevStepFields( spyStepMeta ) ).thenReturn( prevRowMeta );

    when( valueMapperMeta.getTargetField() ).thenReturn( null );

    when( builder.addLink( any( IMetaverseNode.class ), anyString(), any( IMetaverseNode.class ) ) ).thenReturn( builder );

    when( builder.updateNode( any( IMetaverseNode.class ) ) ).thenReturn( builder );

    IMetaverseNode node = spyAnalyzer.analyze( descriptor, valueMapperMeta );
    assertNotNull( node );

    verify( builder, times( 1 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );

    verify( builder, times( 1 ) ).updateNode( any( IMetaverseNode.class ) );

    // make sure that we didn't get into the handling of new field creation instead of overwriting the existing one
    verify( spyAnalyzer, times( 0 ) ).processFieldChangeRecord( any( IMetaverseComponentDescriptor.class ),
      any( IMetaverseNode.class ), any( ComponentDerivationRecord.class ) );

    verify( builder, times( 0 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ),
      any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_WithTargetField() throws Exception {
    StepMeta stepMeta = new StepMeta( STEP_NAME, valueMapperMeta );
    StepMeta spyStepMeta = spy( stepMeta );

    ValueMapperStepAnalyzer spyAnalyzer = spy( analyzer );

    when( valueMapperMeta.getParentStepMeta() ).thenReturn( spyStepMeta );
    when( spyStepMeta.getParentTransMeta() ).thenReturn( transMeta );

    when( transMeta.getPrevStepFields( spyStepMeta ) ).thenReturn( prevRowMeta );
    when( prevRowMeta.searchValueMeta( anyString() ) ).thenReturn( valueMetaInterfaceMock );

    when( valueMapperMeta.getTargetField() ).thenReturn( "CTR" );

    when( builder.addLink( any( IMetaverseNode.class ), anyString(), any( IMetaverseNode.class ) ) ).thenReturn( builder );

    when( builder.updateNode( any( IMetaverseNode.class ) ) ).thenReturn( builder );

    IMetaverseNode node = spyAnalyzer.analyze( descriptor, valueMapperMeta );
    assertNotNull( node );

    assertEquals( descriptor.getType(), node.getType() );
    assertEquals( descriptor.getName(), node.getName() );

    verify( builder, times( 1 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );

    verify( builder, times( 0 ) ).updateNode( any( IMetaverseNode.class ) );

    verify( spyAnalyzer, times( 1 ) ).processFieldChangeRecord( any( IMetaverseComponentDescriptor.class ),
      any( IMetaverseNode.class ), any( ComponentDerivationRecord.class ) );

    verify( builder, times( 1 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ),
      any( IMetaverseNode.class ) );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testAnalyze_WithTargetField_NoInputFieldMeta() throws Exception {
    StepMeta stepMeta = new StepMeta( STEP_NAME, valueMapperMeta );
    StepMeta spyStepMeta = spy( stepMeta );

    ValueMapperStepAnalyzer spyAnalyzer = spy( analyzer );

    when( valueMapperMeta.getParentStepMeta() ).thenReturn( spyStepMeta );
    when( spyStepMeta.getParentTransMeta() ).thenReturn( transMeta );

    when( transMeta.getPrevStepFields( spyStepMeta ) ).thenReturn( prevRowMeta );

    when( valueMapperMeta.getTargetField() ).thenReturn( "CTR" );

    when( builder.addLink( any( IMetaverseNode.class ), anyString(), any( IMetaverseNode.class ) ) ).thenReturn( builder );

    when( builder.updateNode( any( IMetaverseNode.class ) ) ).thenReturn( builder );

    IMetaverseNode node = analyzer.analyze( descriptor, valueMapperMeta );
  }

  @Test
  public void testGetSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( ValueMapperMeta.class ) );
  }

}
