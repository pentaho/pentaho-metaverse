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

package org.pentaho.metaverse.analyzer.kettle.step.stringscut;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.stringcut.StringCutMeta;
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
public class StringsCutStepAnalyzerTest {

  private StringsCutStepAnalyzer analyzer;

  @Mock
  private StringCutMeta stringsCutMeta;

  @Before
  public void setUp() throws Exception {

    when( stringsCutMeta.getFieldInStream() ).thenReturn( new String[]{ "firstName", "middleName", "lastName" } );
    when( stringsCutMeta.getFieldOutStream() ).thenReturn( new String[]{ "", "MN", "" } );
    when( stringsCutMeta.getCutFrom() ).thenReturn( new String[]{ "1", "2", "3" } );
    when( stringsCutMeta.getCutTo() ).thenReturn( new String[]{ "4", "5", "6 " } );

    analyzer = spy( new StringsCutStepAnalyzer() );

    // Call customAnalyze() for coverage, it does nothing
    analyzer.customAnalyze( stringsCutMeta, mock( IMetaverseNode.class ) );

  }

  @Test
  public void testGetChangeRecords() throws Exception {
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( stringsCutMeta );
    assertEquals( changeRecords.size(), 3 );
    List<String> inFields = Arrays.asList( stringsCutMeta.getFieldInStream() );
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
    Set<StepField> usedFields = analyzer.getUsedFields( stringsCutMeta );
    List<String> inFields = Arrays.asList( stringsCutMeta.getFieldInStream() );
    // This test class uses all incoming fields
    for ( StepField usedField : usedFields ) {
      assertTrue( inFields.contains( usedField.getFieldName() ) );
    }
  }

  @Test
  public void testIsPassthrough() throws Exception {
    analyzer.setStepMeta( stringsCutMeta );
    assertFalse( analyzer.isPassthrough( new StepField( "prev", "firstName" ) ) );
    assertTrue( analyzer.isPassthrough( new StepField( "prev", "middleName" ) ) );
    assertFalse( analyzer.isPassthrough( new StepField( "prev", "lastName" ) ) );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    StringsCutStepAnalyzer analyzer = new StringsCutStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( StringCutMeta.class ) );
  }
}
