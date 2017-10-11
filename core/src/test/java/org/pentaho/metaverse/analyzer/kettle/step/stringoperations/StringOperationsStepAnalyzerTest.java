/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.stringoperations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class StringOperationsStepAnalyzerTest {

  private StringOperationsStepAnalyzer analyzer;

  @Mock
  private StringOperationsMeta stringOperationsMeta;

  @Before
  public void setUp() throws Exception {

    when( stringOperationsMeta.getFieldInStream() ).thenReturn( new String[]{ "firstName", "middleName", "lastName" } );
    when( stringOperationsMeta.getFieldOutStream() ).thenReturn( new String[]{ "", "MN", "" } );
    when( stringOperationsMeta.getTrimType() ).thenReturn(
      new int[]{ StringOperationsMeta.TRIM_BOTH, StringOperationsMeta.TRIM_NONE, StringOperationsMeta.TRIM_NONE } );
    when( stringOperationsMeta.getLowerUpper() ).thenReturn(
      new int[]{ StringOperationsMeta.LOWER_UPPER_NONE, StringOperationsMeta.LOWER_UPPER_UPPER,
        StringOperationsMeta.LOWER_UPPER_NONE } );
    when( stringOperationsMeta.getInitCap() ).thenReturn(
      new int[]{ StringOperationsMeta.INIT_CAP_NO, StringOperationsMeta.INIT_CAP_NO,
        StringOperationsMeta.INIT_CAP_YES } );
    when( stringOperationsMeta.getDigits() ).thenReturn(
      new int[]{ StringOperationsMeta.DIGITS_NONE, StringOperationsMeta.DIGITS_NONE,
        StringOperationsMeta.DIGITS_NONE } );
    when( stringOperationsMeta.getMaskXML() ).thenReturn(
      new int[]{ StringOperationsMeta.MASK_NONE, StringOperationsMeta.MASK_NONE, StringOperationsMeta.MASK_NONE } );
    when( stringOperationsMeta.getPaddingType() ).thenReturn(
      new int[]{ StringOperationsMeta.PADDING_NONE, StringOperationsMeta.PADDING_NONE,
        StringOperationsMeta.PADDING_NONE } );
    when( stringOperationsMeta.getPadChar() ).thenReturn( new String[]{ "", "", "" } );
    when( stringOperationsMeta.getPadLen() ).thenReturn( new String[]{ "", "", "" } );
    when( stringOperationsMeta.getRemoveSpecialCharacters() ).thenReturn(
      new int[]{ StringOperationsMeta.MASK_NONE, StringOperationsMeta.MASK_NONE, StringOperationsMeta.MASK_NONE } );

    analyzer = spy( new StringOperationsStepAnalyzer() );

    // Call customAnalyze() for coverage, it does nothing
    analyzer.customAnalyze( stringOperationsMeta, mock( IMetaverseNode.class ) );
  }

  @Test
  public void testGetChangeRecords() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( stringOperationsMeta );
    assertEquals( changeRecords.size(), 3 );
    List<String> inFields = Arrays.asList( stringOperationsMeta.getFieldInStream() );
    for ( ComponentDerivationRecord change : changeRecords ) {
      assertTrue( inFields.contains( change.getOriginalEntityName() ) );
      assertEquals( 1, change.getOperations( ChangeType.DATA ).size() );
      assertNull( change.getOperations( ChangeType.METADATA ) );
    }
  }

  @Test
  public void testGetUsedFields() {
    Set<StepField> fields = new HashSet<>();
    fields.add( new StepField( "prev", "firstName" ) );
    fields.add( new StepField( "prev", "middleName" ) );
    fields.add( new StepField( "prev", "lastName" ) );
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), any( StepNodes.class ) );
    Set<StepField> usedFields = analyzer.getUsedFields( stringOperationsMeta );
    List<String> inFields = Arrays.asList( stringOperationsMeta.getFieldInStream() );
    // This test class uses all incoming fields
    for ( StepField usedField : usedFields ) {
      assertTrue( inFields.contains( usedField.getFieldName() ) );
    }
  }

  @Test
  public void testIsPassthrough() throws Exception {
    analyzer.setStepMeta( stringOperationsMeta );
    assertFalse( analyzer.isPassthrough( new StepField( "prev", "firstName" ) ) );
    assertTrue( analyzer.isPassthrough( new StepField( "prev", "middleName" ) ) );
    assertFalse( analyzer.isPassthrough( new StepField( "prev", "lastName" ) ) );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    StringOperationsStepAnalyzer analyzer = new StringOperationsStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( StringOperationsMeta.class ) );
  }
}
