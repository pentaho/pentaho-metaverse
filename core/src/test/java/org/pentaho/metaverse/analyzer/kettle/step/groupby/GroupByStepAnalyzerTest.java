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

package org.pentaho.metaverse.analyzer.kettle.step.groupby;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class GroupByStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private GroupByStepAnalyzer analyzer;
  @Mock
  private GroupByMeta meta;

  @Mock
  private RowMetaInterface rowMeta1;

  String[] groupFields = new String[] { "country", "state" };
  String[] mockSubjectFields = { "member", "donation" };
  String[] mockAggregateFields = { "memberlist", "totalcity" };
  int[] mockAggregateOperations = { 8, 1 };

  @Before
  public void setUp() throws Exception {

    when( meta.getGroupField() ).thenReturn( groupFields );

    when( meta.getSubjectField() ).thenReturn( mockSubjectFields );
    when( meta.getAggregateField() ).thenReturn( mockAggregateFields );
    when( meta.getAggregateType() ).thenReturn( mockAggregateOperations );

    analyzer = spy( new GroupByStepAnalyzer() );

  }

  @Test
  public void testGetChangeRecords() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( meta );
    assertEquals( 2, changeRecords.size() );
    ComponentDerivationRecord next = changeRecords.iterator().next();
    assertTrue( next.getOriginalEntityName().equals( "member" ) || next.getOriginalEntityName().equals( "donation" ) );
    if ( next.getOriginalEntityName().equals( "member" ) ) {
      assertEquals( "memberlist", next.getChangedEntityName() );
    } else {
      assertEquals( "totalcity", next.getChangedEntityName() );
    }
  }

  @Test
  public void testGetUsedFields() throws Exception {
    Set<StepField> fields = new HashSet<>();
    fields.add( new StepField( "prev", "member" ) );
    fields.add( new StepField( "prev", "donation" ) );
    fields.add( new StepField( "prev", "state" ) );
    fields.add( new StepField( "prev", "country" ) );
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), any() );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    int expectedUsedFieldCount = mockAggregateFields.length + mockSubjectFields.length;
    assertEquals( expectedUsedFieldCount, usedFields.size() );
    verify( analyzer, times( expectedUsedFieldCount ) ).createStepFields( anyString(), any() );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    GroupByStepAnalyzer analyzer = new GroupByStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( GroupByMeta.class ) );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    // only for code coverage
    analyzer.customAnalyze( meta, mock( IMetaverseNode.class ) );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new GroupByStepAnalyzer();
  }
}
