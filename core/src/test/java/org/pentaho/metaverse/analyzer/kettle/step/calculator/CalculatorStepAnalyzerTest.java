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

package org.pentaho.metaverse.analyzer.kettle.step.calculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class CalculatorStepAnalyzerTest extends ClonableStepAnalyzerTest {

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

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new CalculatorStepAnalyzer();
  }
}
