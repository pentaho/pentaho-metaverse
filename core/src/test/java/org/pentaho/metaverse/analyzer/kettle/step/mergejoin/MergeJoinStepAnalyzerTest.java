/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.mergejoin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class MergeJoinStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private MergeJoinStepAnalyzer analyzer;
  private static final String DEFAULT_STEP_NAME = "testStep";

  @Mock IMetaverseNode node;
  @Mock IMetaverseNode mockFieldNode;
  @Mock MergeJoinMeta mergeJoinMeta;
  @Mock TransMeta parentTransMeta;
  @Mock StepMeta parentStepMeta;
  @Mock IMetaverseBuilder builder;
  @Mock StepIOMetaInterface ioMetaInterface;

  StepNodes inputs;
  StepNodes outputs;
  String[] fields1;
  String[] fields2;

  @Before
  public void setUp() throws Exception {

    lenient().when( mergeJoinMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    lenient().when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    analyzer = spy( new MergeJoinStepAnalyzer() );
    analyzer.setParentStepMeta( parentStepMeta );
    analyzer.setParentTransMeta( parentTransMeta );
    analyzer.setMetaverseBuilder( builder );

    inputs = new StepNodes();
    inputs.addNode( "leftStep", "Country", mockFieldNode );
    inputs.addNode( "leftStep", "State", mockFieldNode );
    inputs.addNode( "rightStep", "CTR", mockFieldNode );
    inputs.addNode( "rightStep", "ST", mockFieldNode );

    when( analyzer.getInputs() ).thenReturn( inputs );

    fields1 = inputs.getFieldNames( "leftStep" ).toArray( new String[] {} );
    fields2 = inputs.getFieldNames( "rightStep" ).toArray( new String[] {} );

    // add some more fields that won't be part of the join keys
    inputs.addNode( "leftStep", "value", mockFieldNode );
    inputs.addNode( "rightStep", "value", mockFieldNode );

    when( parentTransMeta.getPrevStepNames( analyzer.getStepName() ) ).thenReturn(
      inputs.getStepNames().toArray( new String[] {} ) );

    when( mergeJoinMeta.getKeyFields1() ).thenReturn( fields1 );
    when( mergeJoinMeta.getKeyFields2() ).thenReturn( fields2 );

    outputs = new StepNodes();
    outputs.addNode( "out", "Country", mockFieldNode );
    outputs.addNode( "out", "State", mockFieldNode );
    outputs.addNode( "out", "CTR", mockFieldNode );
    outputs.addNode( "out", "ST", mockFieldNode );
    outputs.addNode( "out", "value", mockFieldNode );
    outputs.addNode( "out", "value_1", mockFieldNode );

    when( analyzer.getOutputs() ).thenReturn( outputs );
  }

  @Test
  public void testCustomAnalyze_InnerJoin() throws Exception {

    when( mergeJoinMeta.getJoinType() ).thenReturn( "INNER" );
    analyzer.customAnalyze( mergeJoinMeta, node );

    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_TYPE, "INNER" );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_LEFT, Arrays.asList( fields1 ) );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_RIGHT, Arrays.asList( fields2 ) );

    verify( builder, times( 2 * fields1.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_JOINS ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testCustomAnalyze_LeftJoin() throws Exception {

    when( mergeJoinMeta.getJoinType() ).thenReturn( "LEFT OUTER" );
    analyzer.customAnalyze( mergeJoinMeta, node );

    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_TYPE, "LEFT OUTER" );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_LEFT, Arrays.asList( fields1 ) );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_RIGHT, Arrays.asList( fields2 ) );

    verify( builder, times( fields1.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_JOINS ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testCustomAnalyze_RightJoin() throws Exception {

    when( mergeJoinMeta.getJoinType() ).thenReturn( "RIGHT OUTER" );
    analyzer.customAnalyze( mergeJoinMeta, node );

    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_TYPE, "RIGHT OUTER" );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_LEFT, Arrays.asList( fields1 ) );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_RIGHT, Arrays.asList( fields2 ) );

    verify( builder, times( fields1.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_JOINS ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testCustomAnalyze_FullOuterJoin() throws Exception {

    when( mergeJoinMeta.getJoinType() ).thenReturn( "FULL OUTER" );
    analyzer.customAnalyze( mergeJoinMeta, node );

    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_TYPE, "FULL OUTER" );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_LEFT, Arrays.asList( fields1 ) );
    verify( node ).setProperty( DictionaryConst.PROPERTY_JOIN_FIELDS_RIGHT, Arrays.asList( fields2 ) );

    verify( builder, times( 2 * fields1.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_JOINS ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetChangeRecords() throws Exception {

    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( mergeJoinMeta );
    assertNotNull( changeRecords );

    assertEquals( 1, changeRecords.size() );
    ComponentDerivationRecord cr = changeRecords.iterator().next();
    assertEquals( ChangeType.METADATA, cr.getChangeType() );
    assertEquals( 1, cr.getOperations().size() );
  }

  @Test
  public void testGetUsedFields() throws Exception {

    Set<StepField> usedFields = analyzer.getUsedFields( mergeJoinMeta );
    assertNotNull( usedFields );

    assertEquals( fields1.length + fields2.length, usedFields.size() );
  }

  @Test
  public void testGetInputFields() throws Exception {

    doNothing().when( analyzer ).validateState( null, mergeJoinMeta );

    when( mergeJoinMeta.getStepIOMeta() ).thenReturn( ioMetaInterface );
    List<StreamInterface> infoStreams = mock( List.class );
    when( ioMetaInterface.getInfoStreams() ).thenReturn( infoStreams );
    StreamInterface leftStream = mock( StreamInterface.class );
    StreamInterface rightStream = mock( StreamInterface.class );
    when( infoStreams.get( 0 ) ).thenReturn( leftStream );
    when( infoStreams.get( 1 ) ).thenReturn( rightStream );
    StepMeta leftStepMeta = mock ( StepMeta.class );
    StepMeta rightStepMeta = mock ( StepMeta.class );
    when( leftStream.getStepMeta() ).thenReturn( leftStepMeta );
    when( rightStream.getStepMeta() ).thenReturn( rightStepMeta );
    RowMetaInterface leftRmi = mock( RowMetaInterface.class );
    RowMetaInterface rightRmi = mock( RowMetaInterface.class );

    when( parentTransMeta.getStepFields(
      eq( leftStepMeta ), any( ProgressMonitorListener.class ) ) ).thenReturn( leftRmi );
    when( parentTransMeta.getStepFields(
      eq( rightStepMeta ), any( ProgressMonitorListener.class ) ) ).thenReturn( rightRmi );
    when( leftStepMeta.getName() ).thenReturn( "left" );
    when( rightStepMeta.getName() ).thenReturn( "right" );

    Map<String, RowMetaInterface> inputFields = analyzer.getInputFields( mergeJoinMeta );
    assertNotNull( inputFields );

    assertEquals( 2, inputFields.size() );
    assertTrue( inputFields.containsKey( "left" ) );
    assertTrue( inputFields.containsKey( "right" ) );

    assertEquals( leftRmi, inputFields.get( "left" ) );
    assertEquals( rightRmi, inputFields.get( "right" ) );

  }

  @Test
  public void testIsPassthrough_leftSideField() throws Exception {
    StepField passthroughField = new StepField( "leftStep", "value" );
    assertTrue( analyzer.isPassthrough( passthroughField ) );
  }

  @Test
  public void testIsPassthrough_rightSideField() throws Exception {
    StepField passthroughField = new StepField( "rightStep", "ST" );
    assertTrue( analyzer.isPassthrough( passthroughField ) );
  }

  @Test
  public void testIsPassthrough_rightSideFieldRenamed() throws Exception {
    // the right side value field gets renamed to value_1, so this one is not a passthrough
    StepField passthroughField = new StepField( "rightStep", "value" );
    assertFalse( analyzer.isPassthrough( passthroughField ) );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    MergeJoinStepAnalyzer analyzer = new MergeJoinStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( MergeJoinMeta.class ) );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new MergeJoinStepAnalyzer();
  }
}
