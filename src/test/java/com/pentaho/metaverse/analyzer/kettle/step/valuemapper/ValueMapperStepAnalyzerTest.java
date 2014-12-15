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

package com.pentaho.metaverse.analyzer.kettle.step.valuemapper;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.step.BaseStepAnalyzerTest;
import com.pentaho.metaverse.analyzer.kettle.step.valuemapper.ValueMapperStepAnalyzer;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.model.kettle.FieldMapping;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
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

import java.util.HashSet;
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

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockInRowMetaInterface;

  @Mock
  private RowMetaInterface mockOutRowMetaInterface;

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( builder.getMetaverseObjectFactory() ).thenReturn( factory );
    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
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

  @Test
  public void testGetFieldMappings() throws Exception {
    StepMeta meta = new StepMeta( "test", valueMapperMeta );
    StepMeta spyMeta = spy( meta );

    when( valueMapperMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( spyMeta.getStepID() ).thenReturn( "Select values" );

    // set up the input fields
    String[] inFields = { "field1", "field2" };
    String[] outFields = { "field1", "field2", "field3" };
    final ValueMetaInterface field1 = new ValueMetaNumber( "field1" );
    field1.setOrigin( "originStep" );
    final ValueMetaInterface field2 = new ValueMetaNumber( "field2" );
    field1.setOrigin( "originStep" );
    final ValueMetaInterface field3 = new ValueMetaString( "field3" );
    field3.setOrigin( "test" );

    when( mockInRowMetaInterface.getFieldNames() ).thenReturn( inFields );
    when( mockInRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer(
      new Answer<ValueMetaInterface>() {

        @Override
        public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
          Object[] args = invocation.getArguments();
          if ( args[0] == "field1" ) {
            return field1;
          } else if ( args[0] == "field2" ) {
            return field2;
          }
          return null;
        }
      }
    );

    when( mockOutRowMetaInterface.getFieldNames() ).thenReturn( outFields );
    when( mockOutRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer(
      new Answer<ValueMetaInterface>() {

        @Override
        public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
          Object[] args = invocation.getArguments();
          if ( args[0] == "field1" ) {
            return field1;
          } else if ( args[0] == "field2" ) {
            return field2;
          } else if ( args[0] == "field3" ) {
            return field3;
          }
          return null;
        }
      }
    );

    // set up the input fields
    when( valueMapperMeta.getFieldToUse() ).thenReturn( "field1" );
    when( valueMapperMeta.getTargetField() ).thenReturn( "field3" );
    when( mockTransMeta.getPrevStepFields( spyMeta ) ).thenReturn( mockInRowMetaInterface );
    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockOutRowMetaInterface );
    when( mockOutRowMetaInterface.getFieldNames() ).thenReturn( outFields );

    Set<IFieldMapping> mappings = analyzer.getFieldMappings( valueMapperMeta );

    Set<IFieldMapping> goldenData = new HashSet<IFieldMapping>( 3 );
    goldenData.add( new FieldMapping( "field1", "field1" ) );
    goldenData.add( new FieldMapping( "field2", "field2" ) );
    goldenData.add( new FieldMapping( "field1", "field3" ) );
    assertTrue( mappings.containsAll( goldenData ) );
    assertEquals( goldenData.size(), mappings.size() );
  }

}
