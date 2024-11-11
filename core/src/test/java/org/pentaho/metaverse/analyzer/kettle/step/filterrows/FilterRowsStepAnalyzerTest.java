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


package org.pentaho.metaverse.analyzer.kettle.step.filterrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Condition;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class FilterRowsStepAnalyzerTest extends ClonableStepAnalyzerTest {

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

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new FilterRowsStepAnalyzer();
  }
}
