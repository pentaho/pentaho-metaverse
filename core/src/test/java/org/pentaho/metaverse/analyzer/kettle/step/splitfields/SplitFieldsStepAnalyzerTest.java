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

package org.pentaho.metaverse.analyzer.kettle.step.splitfields;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operations;

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
