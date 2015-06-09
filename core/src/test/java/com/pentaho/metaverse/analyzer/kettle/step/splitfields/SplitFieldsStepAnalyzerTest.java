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
 *
 */

package com.pentaho.metaverse.analyzer.kettle.step.splitfields;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operations;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class SplitFieldsStepAnalyzerTest {

  private SplitFieldsStepAnalyzer analyzer;

  @Mock
  private FieldSplitterMeta meta;

  @Mock
  IMetaverseNode node;

  private final String[] outputFields = new String[]{ "one", "two", "three" };

  @Before
  public void setUp() throws Exception {

    when( meta.getDelimiter() ).thenReturn( "," );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.getSplitField() ).thenReturn( "splitField" );
    when( meta.getFieldName() ).thenReturn( outputFields );

    analyzer = spy( new SplitFieldsStepAnalyzer() );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    analyzer.customAnalyze( meta, node );
    verify( node ).setProperty( eq( DictionaryConst.PROPERTY_DELIMITER ), anyString() );
    verify( node ).setProperty( eq( DictionaryConst.PROPERTY_ENCLOSURE ), anyString() );
  }

  @Test
  public void testGetChangeRecords() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( meta );
    assertEquals( 3, changeRecords.size() );
    for ( ComponentDerivationRecord changeRecord : changeRecords ) {
      assertEquals( "splitField", changeRecord.getOriginalEntityName() );
      assertTrue( ArrayUtils.contains( outputFields, changeRecord.getChangedEntityName() ) );
      Operations operations = changeRecord.getOperations();
      assertEquals( 1, operations.size() ); // Only data operations
      List<IOperation> dataOperations = operations.get( ChangeType.DATA );
      assertEquals( 1, dataOperations.size() );
    }
  }

  @Test
  public void testGetUsedFields() throws Exception {
    Set<StepField> fields = new HashSet<>();
    fields.add( new StepField( "prev", "splitField" ) );
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), any( StepNodes.class ) );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    int expectedUsedFieldCount = 1;
    assertEquals( expectedUsedFieldCount, usedFields.size() );
    verify( analyzer, times( expectedUsedFieldCount ) ).createStepFields( anyString(), any( StepNodes.class ) );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> supportedSteps = analyzer.getSupportedSteps();
    assertEquals( 1, supportedSteps.size() );
    assertEquals( FieldSplitterMeta.class, supportedSteps.iterator().next() );
  }
}
