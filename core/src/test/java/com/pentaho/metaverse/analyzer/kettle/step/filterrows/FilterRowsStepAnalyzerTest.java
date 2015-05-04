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

package com.pentaho.metaverse.analyzer.kettle.step.filterrows;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class FilterRowsStepAnalyzerTest {

  private FilterRowsStepAnalyzer analyzer;

  private static final String DEFAULT_STEP_NAME = "testStep";
  @Mock
  private IMetaverseBuilder builder;
  @Mock
  private INamespace namespace;
  @Mock
  private IComponentDescriptor descriptor;
  @Mock
  private StepMeta parentStepMeta;
  @Mock
  private TransMeta parentTransMeta;
  @Mock
  private RowMetaInterface inputRowMeta;
  @Mock
  private RowMetaInterface outputRowMeta;

  @Mock
  private FilterRowsMeta meta;
  @Mock
  private Condition condition;
  private String[] outputFields = new String[]{ "field", "two", "three" };
  private String[] inputFields = new String[]{ "field", "two", "three" };
  private String[] usedFields = new String[]{ "field" };

  @Before
  public void setUp() throws Exception {

    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
    when( descriptor.getParentNamespace() ).thenReturn( namespace );
    when( descriptor.getNamespaceId() ).thenReturn( "namespace" );

    when( builder.getMetaverseObjectFactory() ).thenReturn( MetaverseTestUtils.getMetaverseObjectFactory() );

    when( parentTransMeta.getPrevStepNames( parentStepMeta ) ).thenReturn( new String[]{ "input" } );
    when( inputRowMeta.getFieldNames() ).thenReturn( inputFields );
    when( inputRowMeta.searchValueMeta( any( String.class ) ) ).thenReturn( null );
    when( outputRowMeta.getFieldNames() ).thenReturn( outputFields );
    when( outputRowMeta.searchValueMeta( any( String.class ) ) ).thenReturn( null );

    when( parentTransMeta.getPrevStepFields( eq( parentStepMeta ), any( ProgressMonitorListener.class ) ) ).thenReturn(
      inputRowMeta );
    when( parentTransMeta.getStepFields( eq( parentStepMeta ), any( ProgressMonitorListener.class ) ) ).thenReturn(
      outputRowMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );

    when( meta.getCondition() ).thenReturn( condition );
    when( condition.toString() ).thenReturn( "field=[1]" );
    when( condition.getUsedFields() ).thenReturn( usedFields );

    analyzer = new FilterRowsStepAnalyzer();
    analyzer.setMetaverseBuilder( builder );

    descriptor = new MetaverseComponentDescriptor( DEFAULT_STEP_NAME, DictionaryConst.NODE_TYPE_TRANS, namespace );

  }

  @Test
  public void testCustomAnalyze() throws Exception {
    IMetaverseNode node = analyzer.analyze( descriptor, meta );
    assertNotNull( node );

    verify( builder, times( usedFields.length ) ).addLink(
      any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> supportedSteps = analyzer.getSupportedSteps();
    assertNotNull( supportedSteps );
    assertTrue( supportedSteps.contains( FilterRowsMeta.class ) );
  }
}
