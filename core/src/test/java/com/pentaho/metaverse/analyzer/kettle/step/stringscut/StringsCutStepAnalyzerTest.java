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

package com.pentaho.metaverse.analyzer.kettle.step.stringscut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.stringcut.StringCutMeta;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;

@RunWith( MockitoJUnitRunner.class )
public class StringsCutStepAnalyzerTest {

  private StringsCutStepAnalyzer analyzer;
  private static final String DEFAULT_STEP_NAME = "testStep";

  @Mock
  private IMetaverseBuilder builder;
  @Mock
  private StringCutMeta stringsCutMeta;
  @Mock
  private INamespace namespace;
  @Mock
  private IComponentDescriptor descriptor;
  @Mock
  private StepMeta parentStepMeta;
  @Mock
  private TransMeta parentTransMeta;
  @Mock
  private RowMetaInterface rowMeta1;

  @Before
  public void setUp() throws Exception {

    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
    when( descriptor.getParentNamespace() ).thenReturn( namespace );
    when( descriptor.getNamespaceId() ).thenReturn( "namespace" );

    when( parentTransMeta.getPrevStepNames( parentStepMeta ) ).thenReturn( new String[] { "prevStep" } );
    when( rowMeta1.getFieldNames() ).thenReturn( new String[] { "firstName", "middleName", "lastName" } );
    when( rowMeta1.searchValueMeta( any( String.class ) ) ).thenReturn( null );
    when( parentTransMeta.getPrevStepFields( parentStepMeta ) ).thenReturn( rowMeta1 );
    when( parentTransMeta.getStepFields( parentStepMeta ) ).thenReturn( rowMeta1 );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    when( stringsCutMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( stringsCutMeta.getFieldInStream() ).thenReturn( new String[] { "firstName", "middleName", "lastName" } );
    when( stringsCutMeta.getFieldOutStream() ).thenReturn( new String[] { "", "MN", "" } );
    when( stringsCutMeta.getCutFrom() ).thenReturn( new String[] { "1", "2", "3" } );
    when( stringsCutMeta.getCutTo() ).thenReturn(  new String[] { "4", "5", "6 "} );
    

    analyzer = new StringsCutStepAnalyzer();
    when( builder.getMetaverseObjectFactory() ).thenReturn( MetaverseTestUtils.getMetaverseObjectFactory() );
    analyzer.setMetaverseBuilder( builder );

    descriptor = new MetaverseComponentDescriptor( DEFAULT_STEP_NAME, DictionaryConst.NODE_TYPE_TRANS, namespace );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyze() {
    try {
      analyzer.analyze( descriptor, stringsCutMeta );
      Set<IFieldMapping> mappings = analyzer.getFieldMappings( stringsCutMeta );
      assertEquals( 4, mappings.size() );
      boolean foundNonPassthrough = false;
      for ( IFieldMapping mapping : mappings ) {
        if ( !mapping.getSourceFieldName().equals( mapping.getTargetFieldName() ) ) {
          foundNonPassthrough = true;
        }
      }
      assertTrue( foundNonPassthrough );
    } catch ( MetaverseAnalyzerException e ) {
      e.printStackTrace();
    }
    verify( builder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    verify( builder, times( 3 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
        any( IMetaverseNode.class ) );

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
