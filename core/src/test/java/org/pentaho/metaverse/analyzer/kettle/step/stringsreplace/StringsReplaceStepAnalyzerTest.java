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

package org.pentaho.metaverse.analyzer.kettle.step.stringsreplace;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.replacestring.ReplaceStringMeta;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class StringsReplaceStepAnalyzerTest {

  private StringsReplaceStepAnalyzer analyzer;

  @Mock
  private ReplaceStringMeta stringsReplaceMeta;

  Set<StepField> stepFields;

  @Before
  public void setUp() throws Exception {

    when( stringsReplaceMeta.getFieldInStream() ).thenReturn( new String[]{ "firstName", "middleName", "lastName" } );
    when( stringsReplaceMeta.getFieldOutStream() ).thenReturn( new String[]{ "", "MN", "lastName" } );
    when( stringsReplaceMeta.getFieldReplaceByString() ).thenReturn( new String[]{ "Tom", "Dick", "Harry" } );
    when( stringsReplaceMeta.getReplaceString() ).thenReturn( new String[]{ "Bill", "Steve", "Jeff" } );

    analyzer = spy( new StringsReplaceStepAnalyzer() );

    stepFields = new HashSet<>();
    stepFields.add( new StepField( "prev", "firstName" ) );
    stepFields.add( new StepField( "prev", "middleName" ) );
    stepFields.add( new StepField( "prev", "lastName" ) );
    StepNodes stepNodes = mock( StepNodes.class );
    when( stepNodes.findNodes( anyString() ) ).thenAnswer( new Answer<List<IMetaverseNode>>() {

      @Override
      public List<IMetaverseNode> answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        List<IMetaverseNode> foundNodes = new ArrayList<>();
        String fieldName = (String) args[0];
        if ( fieldName.equals( "firstName" ) || fieldName.equals( "middleName" ) || fieldName.equals( "lastName" ) ) {
          foundNodes.add( mock( IMetaverseNode.class ) );
        }
        return foundNodes;
      }
    } );
    when( analyzer.getInputs() ).thenReturn( stepNodes );
    doReturn( stepFields ).when( analyzer ).createStepFields( anyString(), any( StepNodes.class ) );

    // Call customAnalyze() for coverage, it does nothing
    analyzer.customAnalyze( stringsReplaceMeta, mock( IMetaverseNode.class ) );
  }

  @Test
  public void testGetChangeRecords() throws Exception {

    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( stringsReplaceMeta );
    assertEquals( changeRecords.size(), 3 );
    List<String> inFields = Arrays.asList( stringsReplaceMeta.getFieldInStream() );
    for ( ComponentDerivationRecord change : changeRecords ) {
      assertTrue( inFields.contains( change.getOriginalEntityName() ) );
      assertEquals( 1, change.getOperations( ChangeType.DATA ).size() );
      assertNull( change.getOperations( ChangeType.METADATA ) );
    }
  }

  @Test
  public void testGetUsedFields() {
    Set<StepField> usedFields = analyzer.getUsedFields( stringsReplaceMeta );
    List<String> inFields = Arrays.asList( stringsReplaceMeta.getFieldInStream() );
    // This test class uses all incoming fields
    for ( StepField usedField : usedFields ) {
      assertTrue( inFields.contains( usedField.getFieldName() ) );
    }
  }

  @Test
  public void testIsPassthrough() throws Exception {
    analyzer.setStepMeta( stringsReplaceMeta );
    assertFalse( analyzer.isPassthrough( new StepField( "prev", "firstName" ) ) );
    assertTrue( analyzer.isPassthrough( new StepField( "prev", "middleName" ) ) );
    assertTrue( analyzer.isPassthrough( new StepField( "prev", "lastName" ) ) );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    StringsReplaceStepAnalyzer analyzer = new StringsReplaceStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( ReplaceStringMeta.class ) );
  }
}
