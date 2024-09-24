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

package org.pentaho.metaverse.analyzer.kettle.step.numberrange;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeRule;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class NumberRangeStepAnalyzerTest {

  NumberRangeStepAnalyzer analyzer = null;

  @Mock
  private NumberRangeMeta meta;


  @Before
  public void setUp() throws Exception {
    when( meta.getInputField() ).thenReturn( "inField" );
    when( meta.getOutputField() ).thenReturn( "outField" );
    when( meta.getRules() ).thenReturn( Arrays.asList(
      new NumberRangeRule( 0.0, 1.0, "ONE" ), new NumberRangeRule( 1.0, 2.0, "TWO" ) ) );

    analyzer = spy( new NumberRangeStepAnalyzer() );
  }

  @Test
  public void testGetChangeRecords() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( meta );
    assertEquals( 1, changeRecords.size() );
    ComponentDerivationRecord changeRecord = changeRecords.iterator().next();
    assertEquals( "inField", changeRecord.getOriginalEntityName() );
    assertEquals( "outField", changeRecord.getChangedEntityName() );
    Operations operations = changeRecord.getOperations();
    assertEquals( 1, operations.size() ); // Only data operations
    List<IOperation> dataOperations = operations.get( ChangeType.DATA );
    assertEquals( 2, dataOperations.size() );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    Set<StepField> fields = new HashSet<>();
    fields.add( new StepField( "prev", "inField" ) );
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), any() );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    int expectedUsedFieldCount = 1;
    assertEquals( expectedUsedFieldCount, usedFields.size() );
    verify( analyzer, times( expectedUsedFieldCount ) ).createStepFields( anyString(), any() );
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
