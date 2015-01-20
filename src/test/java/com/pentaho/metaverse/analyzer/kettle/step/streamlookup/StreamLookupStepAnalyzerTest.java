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

package com.pentaho.metaverse.analyzer.kettle.step.streamlookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class StreamLookupStepAnalyzerTest {

  private StreamLookupStepAnalyzer analyzer;
  private static final String DEFAULT_STEP_NAME = "testStep";
  private List<StreamInterface> streams;
  private List<ValueMetaInterface> outValueMetas;

  String[] mockKeylookup = {"code"};
  String[] mockKeystream = {"country_code"};
  String[] mockValues = {"territory"};
  String[] mockValueNames = {"country_ref"};

  @Mock private IMetaverseBuilder builder;
  @Mock private StreamLookupMeta streamLookupMeta;
  @Mock private TransMeta parentTransMeta;
  @Mock private RowMetaInterface prevRowMeta;
  @Mock private RowMetaInterface stepRowMeta;
  @Mock private INamespace namespace;
  @Mock private IComponentDescriptor descriptor;
  @Mock private StepIOMetaInterface stepIoMeta;
  @Mock private StreamInterface stream1;
  @Mock private StreamInterface stream2;
  @Mock private StepMeta parentStepMeta;
  @Mock private StepMeta stepMeta1;
  @Mock private StepMeta stepMeta2;
  @Mock private RowMetaInterface rowMeta1;
  @Mock private RowMetaInterface rowMeta2;
  @Mock private ValueMetaInterface leftField1;
  @Mock private ValueMetaInterface leftField2;
  @Mock private ValueMetaInterface rightField1;
  @Mock private ValueMetaInterface rightField2;


  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
    when( descriptor.getParentNamespace() ).thenReturn( namespace );
    when( descriptor.getNamespaceId() ).thenReturn( "namespace" );

    streams = new ArrayList<StreamInterface>( 2 );
    streams.add( stream1 );
    streams.add( stream2 );

    outValueMetas = new ArrayList<ValueMetaInterface>( 4 );
    outValueMetas.add( leftField1 );
    outValueMetas.add( leftField2 );
    outValueMetas.add( rightField1 );
    outValueMetas.add( rightField2 );

    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIoMeta );
    when( streamLookupMeta.getParentStepMeta() ).thenReturn( parentStepMeta );

    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    when( stepIoMeta.getInfoStreams() ).thenReturn( streams );
    when(stepMeta1.getName()).thenReturn("step1");
    when(stepMeta2.getName()).thenReturn( "step2" );
    when( stream1.getStepMeta() ).thenReturn( stepMeta1 );
    when( stream2.getStepMeta() ).thenReturn( stepMeta2 );
    when( parentTransMeta.getStepFields( stepMeta1 ) ).thenReturn( rowMeta1 );
    when( parentTransMeta.getStepFields( stepMeta2 ) ).thenReturn( rowMeta2 );
    when( parentTransMeta.getStepFields( parentStepMeta ) ).thenReturn( stepRowMeta );
    String[] stepNames = {"step1", "step2"};
    when( parentTransMeta.getStepNames()).thenReturn( stepNames );

    analyzer = new StreamLookupStepAnalyzer();
    analyzer.setMetaverseBuilder( builder );

    descriptor = new MetaverseComponentDescriptor( DEFAULT_STEP_NAME, DictionaryConst.NODE_TYPE_TRANS, namespace );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyze() {
    try {
      analyzer.analyze( descriptor, streamLookupMeta );
      verify( builder, times( 2 ) ).addLink( any( IMetaverseNode.class ),
          eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );
  
      verify( builder, times( 1 ) ).addLink( any( IMetaverseNode.class ),
          eq( DictionaryConst.LINK_JOINS ), any( IMetaverseNode.class ) );
  
      verify( builder, times( 1 ) ).addLink( any( IMetaverseNode.class ),
          eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );
    } catch (MetaverseAnalyzerException e) {
      // noop
    }
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
  public void testFieldNameExistsInInput() {
    String[] keyLookups = {"EAST", "WEST"};
    String[] keyStreams = {"SALES", "MARKETING"};
    String[] values     = {"TERRITORY", "COUNTRY"};
    String[] valueNames = {"LOCATION", "NATIONALITY"};
    analyzer.setKeyLookups(keyLookups);
    analyzer.setKeyStreams( keyStreams );
    analyzer.setValues( values );
    analyzer.setValueNames( valueNames );
    
    assert( analyzer.fieldNameExistsInInput( "TERRITORY" ) );
    assert( !analyzer.fieldNameExistsInInput( "AFRICA" ) );
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