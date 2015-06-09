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

package org.pentaho.metaverse.analyzer.kettle.step.streamlookup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.streamlookup.StreamLookupMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


@RunWith( MockitoJUnitRunner.class )
public class StreamLookupStepAnalyzerTest {

  private StreamLookupStepAnalyzer analyzer;
  private List<StreamInterface> streams;
  private List<ValueMetaInterface> outValueMetas;

  String[] mockKeylookup = { "code" };
  String[] mockKeystream = { "country_code" };
  String[] mockValues = { "territory" };
  String[] mockValueNames = { "country_ref" };

  @Mock
  private IMetaverseNode node;
  @Mock
  private IMetaverseNode mockFieldNode;
  @Mock
  private IMetaverseBuilder builder;
  @Mock
  private StreamLookupMeta streamLookupMeta;
  @Mock
  private TransMeta parentTransMeta;
  @Mock
  private RowMetaInterface prevRowMeta;
  @Mock
  private RowMetaInterface stepRowMeta;
  @Mock
  private StepIOMetaInterface stepIoMeta;
  @Mock
  private StreamInterface stream1;
  @Mock
  private StreamInterface stream2;
  @Mock
  private StepMeta parentStepMeta;
  @Mock
  private StepMeta stepMeta1;
  @Mock
  private StepMeta stepMeta2;
  @Mock
  private RowMetaInterface rowMeta1;
  @Mock
  private RowMetaInterface rowMeta2;
  @Mock
  private ValueMetaInterface leftField1;
  @Mock
  private ValueMetaInterface leftField2;
  @Mock
  private ValueMetaInterface rightField1;
  @Mock
  private ValueMetaInterface rightField2;

  @Mock
  private RowMetaInterface step1RowMeta;
  @Mock
  private RowMetaInterface step2RowMeta;
  @Mock
  private RowMetaInterface outputRowMeta;

  StepNodes inputs;

  @Before
  public void setUp() throws Exception {

    streams = new ArrayList<>( 2 );
    streams.add( stream1 );
    streams.add( stream2 );

    outValueMetas = new ArrayList<>( 4 );
    outValueMetas.add( leftField1 );
    outValueMetas.add( leftField2 );
    outValueMetas.add( rightField1 );
    outValueMetas.add( rightField2 );

    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIoMeta );
    when( streamLookupMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( streamLookupMeta.getKeylookup() ).thenReturn( mockKeylookup );
    when( streamLookupMeta.getKeystream() ).thenReturn( mockKeystream );
    when( streamLookupMeta.getValue() ).thenReturn( mockValues );
    when( streamLookupMeta.getValueName() ).thenReturn( mockValueNames );

    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    when( stepIoMeta.getInfoStreams() ).thenReturn( streams );
    when( stepMeta1.getName() ).thenReturn( "step1" );
    when( stepMeta2.getName() ).thenReturn( "step2" );
    when( stream1.getStepMeta() ).thenReturn( stepMeta1 );
    when( stream1.getStepname() ).thenReturn( "step1" );
    when( stream2.getStepMeta() ).thenReturn( stepMeta2 );
    when( stream2.getStepname() ).thenReturn( "step2" );
    when( parentTransMeta.getStepFields( stepMeta1 ) ).thenReturn( rowMeta1 );
    when( parentTransMeta.getStepFields( stepMeta2 ) ).thenReturn( rowMeta2 );
    when( parentTransMeta.getStepFields( parentStepMeta ) ).thenReturn( stepRowMeta );
    when( parentTransMeta.getStepFields( "step1" ) ).thenReturn( step1RowMeta );
    when( parentTransMeta.getStepFields( "step2" ) ).thenReturn( step2RowMeta );
    when( step1RowMeta.searchValueMeta( anyString() ) ).thenReturn( mock( ValueMetaInterface.class ) );
    String[] stepNames = { "step1", "step2" };
    when( parentTransMeta.getPrevStepNames( any( StepMeta.class ) ) ).thenReturn( stepNames );
    when( parentTransMeta.getPrevStepNames( anyString() ) ).thenReturn( stepNames );

    analyzer = spy( new StreamLookupStepAnalyzer() );
    analyzer.setParentStepMeta( parentStepMeta );
    analyzer.setParentTransMeta( parentTransMeta );
    analyzer.setMetaverseBuilder( builder );

    inputs = new StepNodes();
    inputs.addNode( "step1", "Country", mockFieldNode );
    inputs.addNode( "step2", "State", mockFieldNode );

    when( analyzer.getInputs() ).thenReturn( inputs );

    when( analyzer.getOutputFields( any( StreamLookupMeta.class ) ) ).thenReturn( outputRowMeta );
    ValueMetaInterface searchFieldResult = mock( ValueMetaInterface.class );
    when( outputRowMeta.searchValueMeta( anyString() ) ).thenReturn( searchFieldResult );
    when( searchFieldResult.getOrigin() ).thenReturn( "step1" );
    when( searchFieldResult.getName() ).thenReturn( "country_code" );
  }

  @Test
  public void testCustomAnalyze() throws Exception {

    analyzer.customAnalyze( streamLookupMeta, node );

    verify( builder, times( 2 * mockKeylookup.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_JOINS ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetChangeRecords() throws Exception {

    analyzer.setStepMeta( streamLookupMeta );
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( streamLookupMeta );
    assertNotNull( changeRecords );

    assertEquals( 1, changeRecords.size() );
    ComponentDerivationRecord cr = changeRecords.iterator().next();
    assertEquals( ChangeType.METADATA, cr.getChangeType() );
    assertEquals( 1, cr.getOperations().size() );
  }

  @Test
  public void testGetUsedFields() throws Exception {

    Set<StepField> usedFields = analyzer.getUsedFields( streamLookupMeta );
    assertNotNull( usedFields );

    assertEquals(
      streamLookupMeta.getKeystream().length
        + streamLookupMeta.getKeylookup().length
        + streamLookupMeta.getValue().length,
      usedFields.size() );
  }


  @Test
  public void testGetInputFields() throws Exception {
    analyzer.setParentTransMeta( parentTransMeta );
    analyzer.setParentStepMeta( parentStepMeta );
    Map<String, RowMetaInterface> inputRowMeta = analyzer.getInputFields( streamLookupMeta );
    assertNotNull( inputRowMeta );
    assertEquals( 2, inputRowMeta.size() );
  }

  @Test
  public void testNewFieldNameExistsInMainInputStream_nullParentTransMeta() {
    analyzer.setParentTransMeta( null );
    assertFalse( analyzer.newFieldNameExistsInMainInputStream( "" ) );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    StreamLookupStepAnalyzer analyzer = new StreamLookupStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( StreamLookupMeta.class ) );
  }
}
