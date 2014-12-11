/*!
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

package com.pentaho.metaverse.analyzer.kettle.step.numberrange;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
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
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class NumberRangeStepAnalyzerTest {

  NumberRangeStepAnalyzer analyzer = null;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private NumberRangeMeta numberRangeMeta;

  @Mock
  private StepMeta parentStepMeta;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockInRowMetaInterface;

  @Mock
  private RowMetaInterface mockOutRowMetaInterface;

  @Mock
  private INamespace mockNamespace;

  IMetaverseComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );
//    when( mockNamespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( mockNamespace );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    when( numberRangeMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    when( numberRangeMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( parentStepMeta.getName() ).thenReturn( "test" );
    when( parentStepMeta.getStepID() ).thenReturn( "Number range" );

    analyzer = new NumberRangeStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS, mockNamespace );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {

    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {

    IMetaverseNode result = analyzer.analyze( descriptor, numberRangeMeta );
    assertNotNull( result );
    assertEquals( parentStepMeta.getName(), result.getName() );

    verify( numberRangeMeta, times( 1 ) ).getInputField();
    verify( numberRangeMeta, times( 1 ) ).getOutputField();

    // make sure only the step node is added
    verify( mockBuilder, times( 1 ) ).addNode( any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_Fields() throws Exception {

    when( mockTransMeta.getPrevStepFields( parentStepMeta ) ).thenReturn( mockInRowMetaInterface );
    when( numberRangeMeta.getInputField() ).thenReturn( "inField" );
    when( numberRangeMeta.getOutputField() ).thenReturn( "outField" );
    when( mockTransMeta.getStepFields( parentStepMeta ) ).thenReturn( mockOutRowMetaInterface );

    final ValueMetaInterface vmiInField = new ValueMetaNumber( "inField" );
    vmiInField.setOrigin( "originStep" );
    final ValueMetaInterface vmiOutField = new ValueMetaString( "outField" );
    vmiOutField.setOrigin( "test" );
    final String[] inFields = new String[] { "inField" };
    final String[] outFields = new String[] { "inField,outField" };
    List<ValueMetaInterface> prevStepFieldsValueMetaList = Arrays.asList( vmiInField );
    List<ValueMetaInterface> stepFieldsValueMetaList = Arrays.asList( vmiInField, vmiOutField );

    when( mockInRowMetaInterface.getFieldNames() ).thenReturn( inFields );
    when( mockInRowMetaInterface.getValueMetaList() ).thenReturn( prevStepFieldsValueMetaList );
    when( mockInRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer(
        new Answer<ValueMetaInterface>() {

          @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
            Object[] args = invocation.getArguments();
            if ( args[0] == "inField" ) {
              return vmiInField;
            }
            return null;
          }
        }
    );

    when( mockOutRowMetaInterface.getFieldNames() ).thenReturn( outFields );
    when( mockOutRowMetaInterface.getValueMetaList() ).thenReturn( stepFieldsValueMetaList );
    when( mockOutRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer(
        new Answer<ValueMetaInterface>() {

          @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
            Object[] args = invocation.getArguments();
            if ( args[0] == "inField" ) {

              return vmiInField;
            }
            if ( args[0] == "outField" ) {
              return vmiOutField;
            }
            return null;
          }
        }
    );

    IMetaverseNode result = analyzer.analyze( descriptor, numberRangeMeta );
    assertNotNull( result );
    assertEquals( parentStepMeta.getName(), result.getName() );

    verify( numberRangeMeta, times( 1 ) ).getInputField();
    verify( numberRangeMeta, times( 1 ) ).getOutputField();

    // make sure the step node and the new field node are created
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there is a "uses" link added
    verify( mockBuilder, times( 1 ) ).addLink(
        any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

    // make sure there is a "uses" link added
    verify( mockBuilder, times( 1 ) ).addLink(
        any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetSupportedSteps() {
    NumberRangeStepAnalyzer analyzer = new NumberRangeStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( NumberRangeMeta.class ) );
  }
}
