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


package org.pentaho.metaverse.analyzer.kettle.step.stringoperations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class StringOperationsStepAnalyzerTest {

  private StringOperationsStepAnalyzer analyzer;

  @Mock
  private StringOperationsMeta stringOperationsMeta;

  @Before
  public void setUp() throws Exception {

    when( stringOperationsMeta.getFieldInStream() ).thenReturn( new String[]{ "firstName", "middleName", "lastName" } );
    when( stringOperationsMeta.getFieldOutStream() ).thenReturn( new String[]{ "", "MN", "" } );
    when( stringOperationsMeta.getTrimType() ).thenReturn(
      new String[]{
              getTrimTypeCode( StringOperationsMeta.TRIM_BOTH ),
              getTrimTypeCode( StringOperationsMeta.TRIM_NONE ),
              getTrimTypeCode( StringOperationsMeta.TRIM_NONE)
        });
    when( stringOperationsMeta.getLowerUpper() ).thenReturn(
      new String[]{
              getLowerUpperCode( StringOperationsMeta.LOWER_UPPER_NONE ),
              getLowerUpperCode( StringOperationsMeta.LOWER_UPPER_UPPER ),
              getLowerUpperCode( StringOperationsMeta.LOWER_UPPER_NONE )
        });
    when( stringOperationsMeta.getInitCap() ).thenReturn(
      new String[]{
              getInitCapCode( StringOperationsMeta.INIT_CAP_NO ),
              getInitCapCode( StringOperationsMeta.INIT_CAP_NO ),
              getInitCapCode( StringOperationsMeta.INIT_CAP_YES )
        });
    when( stringOperationsMeta.getDigits() ).thenReturn(
      new String[]{
              getDigitsCode( StringOperationsMeta.DIGITS_NONE ),
              getDigitsCode( StringOperationsMeta.DIGITS_NONE ),
              getDigitsCode( StringOperationsMeta.DIGITS_NONE )
        });
    when( stringOperationsMeta.getMaskXML() ).thenReturn(
      new String[]{
              getMaskXMLCode( StringOperationsMeta.MASK_NONE ),
              getMaskXMLCode( StringOperationsMeta.MASK_NONE ),
              getMaskXMLCode( StringOperationsMeta.MASK_NONE )
        });
    when( stringOperationsMeta.getPaddingType() ).thenReturn(
      new String[]{
              getPaddingCode( StringOperationsMeta.PADDING_NONE ),
              getPaddingCode( StringOperationsMeta.PADDING_NONE ),
              getPaddingCode( StringOperationsMeta.PADDING_NONE )
        });
//    when( stringOperationsMeta.getPadChar() ).thenReturn( new String[]{ "", "", "" } );
    when( stringOperationsMeta.getPadLen() ).thenReturn( new String[]{ "", "", "" } );
    when( stringOperationsMeta.getRemoveSpecialCharacters() ).thenReturn(
      new String[]{
              getRemoveSpecialCharactersCode( StringOperationsMeta.MASK_NONE ),
              getRemoveSpecialCharactersCode( StringOperationsMeta.MASK_NONE ),
              getRemoveSpecialCharactersCode( StringOperationsMeta.MASK_NONE )
        });

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
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), any() );
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

  private String getTrimTypeCode( int i ) {
    return StringOperationsMeta.getTrimTypeCode( i );
  }

  private String getLowerUpperCode( int i ) {
    return StringOperationsMeta.getLowerUpperCode( i );
  }

  private String getInitCapCode( int i ) {
    return StringOperationsMeta.getInitCapCode( i );
  }

  private String getDigitsCode( int i ) {
    return StringOperationsMeta.getDigitsCode( i );
  }

  private String getMaskXMLCode( int i ) {
    return StringOperationsMeta.getMaskXMLCode( i );
  }

  private String getPaddingCode( int i ) {
    return StringOperationsMeta.getPaddingCode( i );
  }

  private static String getRemoveSpecialCharactersCode( int i ) {
    return StringOperationsMeta.getRemoveSpecialCharactersCode( i );
  }

}
