/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.filterrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.Condition;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.Set;

import static org.junit.Assert.*;
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
