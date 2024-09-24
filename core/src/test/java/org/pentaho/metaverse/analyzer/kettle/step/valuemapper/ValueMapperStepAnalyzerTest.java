/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.valuemapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class ValueMapperStepAnalyzerTest {

  private ValueMapperStepAnalyzer analyzer = null;

  @Mock
  private ValueMapperMeta meta;
  @Mock
  IMetaverseNode node;

  @Before
  public void setUp() throws Exception {

    when( meta.getFieldToUse() ).thenReturn( "Country" );
    when( meta.getSourceValue() ).thenReturn( new String[]{ "United States", "Canada" } );
    when( meta.getTargetValue() ).thenReturn( new String[]{ "USA", "CA" } );

    analyzer = spy( new ValueMapperStepAnalyzer() );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    analyzer.customAnalyze( meta, node );
    verify( node ).setProperty( eq( "defaultValue" ), any() );
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
  public void testGetChangeRecords() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( meta );
    assertEquals( 1, changeRecords.size() );
    ComponentDerivationRecord changeRecord = changeRecords.iterator().next();
    assertEquals( meta.getFieldToUse(), changeRecord.getOriginalEntityName() );
    assertEquals( ( meta.getTargetField() == null ? meta.getFieldToUse() : meta.getTargetField() ),
      changeRecord.getChangedEntityName() );
    Operations operations = changeRecord.getOperations();
    assertEquals( 1, operations.size() ); // Only data operations
    List<IOperation> dataOperations = operations.get( ChangeType.DATA );
    assertEquals( 2, dataOperations.size() );
  }

  @Test
  public void testGetSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( ValueMapperMeta.class ) );
  }

}
