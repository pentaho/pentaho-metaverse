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

package com.pentaho.metaverse.analyzer.kettle.step.calculator;

import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class CalculatorStepAnalyzerTest {

  private CalculatorStepAnalyzer analyzer;
  @Mock
  private CalculatorMetaFunction function1;
  @Mock
  private CalculatorMetaFunction function2;
  @Mock
  private CalculatorMeta calculatorMeta;
  @Mock
  private IComponentDescriptor descriptor;
  @Mock
  private IMetaverseNode node;

  @Before
  public void setUp() throws Exception {
    analyzer = spy( new CalculatorStepAnalyzer() );

    CalculatorMetaFunction[] calcFunctions = new CalculatorMetaFunction[] { function1, function2 };
    when( function1.getFieldA() ).thenReturn( "height" );
    when( function1.getFieldB() ).thenReturn( "width" );
    when( function1.getFieldName() ).thenReturn( "area" );
    when( function1.getCalcTypeDesc() ).thenReturn( "height * width = area" );

    when( function2.getFieldA() ).thenReturn( "radius" );
    when( function2.getFieldB() ).thenReturn( "pi" );
    when( function2.getFieldC() ).thenReturn( "two" );
    when( function2.getFieldName() ).thenReturn( "circumference" );
    when( function2.getCalcTypeDesc() ).thenReturn( "two * pi * radius = circumference" );

    when( calculatorMeta.getCalculation() ).thenReturn( calcFunctions );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    // no-op method, just call it to get the method covered
    analyzer.customAnalyze( calculatorMeta, node );
  }

  @Test
  public void testGetChangeRecords() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( calculatorMeta );

    assertNotNull( changeRecords );
    assertEquals( 5, changeRecords.size() );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    StepNodes inputNodes = new StepNodes();
    inputNodes.addNode( "input", "height", node );
    inputNodes.addNode( "input", "width", node );
    inputNodes.addNode( "input", "radius", node );
    inputNodes.addNode( "input", "pi", node );
    inputNodes.addNode( "input", "two", node );
    inputNodes.addNode( "input", "NOT USED", node );

    when( analyzer.getInputs() ).thenReturn( inputNodes );

    Set<StepField> usedFields = analyzer.getUsedFields( calculatorMeta );

    assertEquals( 5, usedFields.size() );

  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    CalculatorStepAnalyzer analyzer = new CalculatorStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( CalculatorMeta.class ) );
  }
}
