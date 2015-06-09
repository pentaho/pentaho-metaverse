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

package org.pentaho.metaverse.analyzer.kettle.step.groupby;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class GroupByStepAnalyzerTest {

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
    doReturn( fields ).when( analyzer ).createStepFields( anyString(), any( StepNodes.class ) );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    int expectedUsedFieldCount = mockAggregateFields.length + mockSubjectFields.length;
    assertEquals( expectedUsedFieldCount, usedFields.size() );
    verify( analyzer, times( expectedUsedFieldCount ) ).createStepFields( anyString(), any( StepNodes.class ) );
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
}
