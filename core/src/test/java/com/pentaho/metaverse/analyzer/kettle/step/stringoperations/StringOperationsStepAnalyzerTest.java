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

package com.pentaho.metaverse.analyzer.kettle.step.stringoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;

import org.pentaho.metaverse.api.IMetaverseNode;

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
