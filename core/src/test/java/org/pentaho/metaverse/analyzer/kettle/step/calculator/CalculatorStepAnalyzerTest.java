/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.step.calculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
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
