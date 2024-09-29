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


package org.pentaho.metaverse.analyzer.kettle.step.selectvalues;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class SelectValuesStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private static final String DEFAULT_STEP_NAME = "testStep";

  private SelectValuesStepAnalyzer analyzer;

  @Mock SelectValuesMeta selectValuesMeta;
  @Mock RowMetaInterface rmi;
  @Mock TransMeta parentTransMeta;
  @Mock IMetaverseNode node;

  Map<String, RowMetaInterface> prevFields;

  StepNodes inputs;
  SelectMetadataChange testChange1;
  SelectMetadataChange testChange2;

  @Before
  public void setUp() throws Exception {
    analyzer = spy( new SelectValuesStepAnalyzer() );
    analyzer.setParentTransMeta( parentTransMeta );
    analyzer.setBaseStepMeta( selectValuesMeta );
    doNothing().when( analyzer).validateState( null, selectValuesMeta );

    prevFields = new HashMap<>();

    prevFields.put( "previousStep", rmi );

    doReturn( prevFields ).when( analyzer ).getInputFields( selectValuesMeta );
    doReturn( "select values" ).when( analyzer ).getStepName();
    when( parentTransMeta.getPrevStepNames( analyzer.getStepName() ) ).thenReturn( new String[] { "previousStep" } );

    inputs = new StepNodes();
    inputs.addNode( "previousStep", "first", node );
    inputs.addNode( "previousStep", "last", node );
    inputs.addNode( "previousStep", "age", node );
    inputs.addNode( "previousStep", "birthday", node );

    doReturn( inputs ).when( analyzer ).getInputs();

    testChange1 = new SelectMetadataChange( selectValuesMeta );
    testChange1.setName( "first" );
    testChange1.setCurrencySymbol( "~" );
    testChange1.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    testChange1.setDateFormatLocale( "en_UK" );
    testChange1.setGroupingSymbol( "..." );

    testChange2 = new SelectMetadataChange( selectValuesMeta );
    testChange2.setName( "last" );
    testChange2.setRename( "last name" );
    testChange2.setDateFormatLenient( true );
    testChange2.setConversionMask( "##.#" );
    testChange2.setDateFormatTimeZone( "YYYY-MM-DD" );
    testChange2.setDecimalSymbol( "," );
  }

  @Test
  public void testGetChangeRecords_select() throws Exception {

    // intentionally select some of the fields using upper case
    String[] selectedNames = new String[] { "first", "LAST", "age" };
    String[] selectedRenames = new String[] { null, null, "years on earth" };
    int[] fieldLengths = new int[] { 60, SelectValuesStepAnalyzer.NOT_CHANGED, SelectValuesStepAnalyzer.NOT_CHANGED };
    int[] fieldPrecisions = new int[] { SelectValuesStepAnalyzer.NOT_CHANGED, SelectValuesStepAnalyzer.NOT_CHANGED, 1 };

    when( selectValuesMeta.getSelectName() ).thenReturn( selectedNames );
    when( selectValuesMeta.getSelectRename() ).thenReturn( selectedRenames );
    when( selectValuesMeta.getSelectLength() ).thenReturn( fieldLengths );
    when( selectValuesMeta.getSelectPrecision() ).thenReturn( fieldPrecisions );

    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( selectValuesMeta );
    assertNotNull( changeRecords );

    // all selected fields should have a change record
    assertEquals( selectedNames.length, changeRecords.size() );

  }

  @Test
  public void testGetChangeRecords_meta() throws Exception {
    SelectMetadataChange[] metadataChanges = new SelectMetadataChange[] { testChange1, testChange2 };

    when( selectValuesMeta.getMeta() ).thenReturn( metadataChanges );

    ValueMetaInterface vmiFirst = mock( ValueMetaInterface.class );
    ValueMetaInterface vmiLast = mock( ValueMetaInterface.class );
    when( rmi.searchValueMeta( "first" ) ).thenReturn( vmiFirst );
    when( rmi.searchValueMeta( "last" ) ).thenReturn( vmiLast );

    lenient().when( vmiFirst.getName() ).thenReturn( "first" );
    lenient().when( vmiFirst.getCurrencySymbol() ).thenReturn( "$" );
    lenient().when( vmiFirst.getStorageType() ).thenReturn( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    lenient().when( vmiLast.getDateFormatLocale() ).thenReturn( Locale.US );
    lenient().when( vmiLast.getGroupingSymbol() ).thenReturn( "," );

    lenient().when( vmiLast.getName() ).thenReturn( "last" );
    when( vmiLast.getConversionMask() ).thenReturn( "000.##" );
    when( vmiLast.getDateFormatTimeZone() ).thenReturn( TimeZone.getDefault() );
    when( vmiLast.getDecimalSymbol() ).thenReturn( "." );
    when( vmiLast.isDateFormatLenient() ).thenReturn( false );

    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( selectValuesMeta );
    assertNotNull( changeRecords );
    assertEquals( metadataChanges.length, changeRecords.size() );
  }

  @Test
  public void testGetUsedFields_selectOnly() throws Exception {
    // intentionally select some of the fields using upper case
    String[] selectedNames = new String[] { "FIRST", "LAST", "age" };
    when( selectValuesMeta.getSelectName() ).thenReturn( selectedNames );
    when( selectValuesMeta.getMeta() ).thenReturn( new SelectMetadataChange[0] );
    Set<StepField> usedFields = analyzer.getUsedFields( selectValuesMeta );
    assertNotNull( usedFields );
    assertEquals( selectedNames.length, usedFields.size() );
  }

  @Test
  public void testGetUsedFields_metaOnly() throws Exception {
    String[] selectedNames = new String[0];
    SelectMetadataChange[] changes = new SelectMetadataChange[] { testChange1, testChange2 };

    when( selectValuesMeta.getSelectName() ).thenReturn( selectedNames );
    when( selectValuesMeta.getMeta() ).thenReturn( changes );
    Set<StepField> usedFields = analyzer.getUsedFields( selectValuesMeta );
    assertNotNull( usedFields );
    assertEquals( changes.length, usedFields.size() );
  }

  @Test
  public void testGetUsedFields_selectAndMetaOverlap() throws Exception {
    // intentionally select some of the fields using upper case
    String[] selectedNames = new String[] { "first", "AGE" };
    SelectMetadataChange[] changes = new SelectMetadataChange[] { testChange1, testChange2 };

    when( selectValuesMeta.getSelectName() ).thenReturn( selectedNames );
    when( selectValuesMeta.getMeta() ).thenReturn( changes );
    Set<StepField> usedFields = analyzer.getUsedFields( selectValuesMeta );
    assertNotNull( usedFields );
    // both select and meta identify first as being used, select also uses age, meta also uses last
    assertEquals( 3, usedFields.size() );
  }

  @Test
  public void testGetSupportedSteps() {
    SelectValuesStepAnalyzer analyzer = new SelectValuesStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( SelectValuesMeta.class ) );
  }

  @Test
  public void testIsPassthrough() throws Exception {
    // intentionally select some of the fields using upper case
    String[] deleted = new String[] { "FIRST, last" };
    String[] selected = new String[] { "AGE" };
    SelectMetadataChange[] changed = new SelectMetadataChange[] { testChange1, testChange2 };

    lenient().when( selectValuesMeta.getDeleteName() ).thenReturn( deleted );
    lenient().when( selectValuesMeta.getSelectName() ).thenReturn( selected );
    lenient().when( selectValuesMeta.getMeta() ).thenReturn( changed );

    StepField stepField = new StepField( "previousStep", "first" );
    assertFalse( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "last" );
    assertFalse( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "age" );
    assertFalse( analyzer.isPassthrough( stepField ) );

    // birthday should not be a passthrough, there are fields "selected"
    stepField.setFieldName( "birthday" );
    assertFalse( analyzer.isPassthrough( stepField ) );
  }

  @Test
  public void testIsPassthrough_onlyDelete() throws Exception {
    // intentionally select some of the fields using upper case
    String[] deleted = new String[] { "first", "LAST" };
    String[] selected = new String[0];
    SelectMetadataChange[] changed = new SelectMetadataChange[0];

    when( selectValuesMeta.getDeleteName() ).thenReturn( deleted );
    when( selectValuesMeta.getSelectName() ).thenReturn( selected );
    when( selectValuesMeta.getMeta() ).thenReturn( changed );

    StepField stepField = new StepField( "previousStep", "first" );
    // first was deleted, not a passthrough
    assertFalse( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "last" );
    // last was deleted, not a passthrough
    assertFalse( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "age" );
    // age was NOT deleted, it is a passthrough
    assertTrue( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "birthday" );
    // birthday was NOT deleted, it is a passthrough
    assertTrue( analyzer.isPassthrough( stepField ) );
  }

  @Test
  public void testIsPassthrough_deleteAndMeta() throws Exception {
    // intentionally select some of the fields using upper case
    String[] deleted = new String[] { "FIRST", "age" };
    String[] selected = new String[0];
    SelectMetadataChange[] changed = new SelectMetadataChange[] { testChange1, testChange2 };

    when( selectValuesMeta.getDeleteName() ).thenReturn( deleted );
    when( selectValuesMeta.getSelectName() ).thenReturn( selected );
    when( selectValuesMeta.getMeta() ).thenReturn( changed );

    StepField stepField = new StepField( "previousStep", "first" );
    // first was deleted, not a passthrough
    assertFalse( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "last" );
    // last not deleted but it is meta-modified, not a passthrough
    assertFalse( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "age" );
    // age was deleted, it is NOT passthrough
    assertFalse( analyzer.isPassthrough( stepField ) );

    stepField.setFieldName( "birthday" );
    // birthday was NOT deleted and not meta-modified, it is a passthrough
    assertTrue( analyzer.isPassthrough( stepField ) );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new SelectValuesStepAnalyzer();
  }
}
