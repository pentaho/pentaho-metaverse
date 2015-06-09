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

package com.pentaho.metaverse.analyzer.kettle.step.filterrows;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.Condition;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class FilterRowsStepAnalyzerTest {

  private FilterRowsStepAnalyzer analyzer;

  @Mock
  private IComponentDescriptor descriptor;
  @Mock
  private IMetaverseNode node;
  @Mock
  private FilterRowsMeta meta;
  @Mock
  private Condition condition;

  private String[] conditionFields = new String[]{ "height", "width" };

  @Before
  public void setUp() throws Exception {
    when( meta.getCondition() ).thenReturn( condition );
    when( condition.getUsedFields() ).thenReturn( conditionFields );

    analyzer = spy( new FilterRowsStepAnalyzer() );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    StepNodes inputNodes = new StepNodes();
    inputNodes.addNode( "input", "height", node );
    inputNodes.addNode( "input", "width", node );
    inputNodes.addNode( "input", "radius", node );
    inputNodes.addNode( "input", "pi", node );
    inputNodes.addNode( "input", "two", node );

    when( analyzer.getInputs() ).thenReturn( inputNodes );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( conditionFields.length, usedFields.size() );
  }

  @Test
  public void testGetUsedFields_nullCondition() throws Exception {
    when( meta.getCondition() ).thenReturn( null );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    analyzer.customAnalyze( meta, node );
    verify( node ).setProperty( eq( DictionaryConst.PROPERTY_OPERATIONS ), anyString() );
  }

  @Test
  public void testCustomAnalyze_nullCondition() throws Exception {
    when( meta.getCondition() ).thenReturn( null );
    analyzer.customAnalyze( meta, node );
    verify( node, never() ).setProperty( eq( DictionaryConst.PROPERTY_OPERATIONS ), anyString() );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> supportedSteps = analyzer.getSupportedSteps();
    assertNotNull( supportedSteps );
    assertTrue( supportedSteps.contains( FilterRowsMeta.class ) );
  }
}
