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

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class SplitFieldsStepAnalyzerTest {

  private SplitFieldsStepAnalyzer analyzer;

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
  private FieldSplitterMeta fieldSplitterMeta;

  @Mock
  private ValueMetaInterface inField1;

  private String[] outputFields = new String[] { "one", "two", "three" };
  public static final String SPLIT_FIELD = "SPLIT_ME";

  private List<ValueMetaInterface> inFields;

  @Before
  public void setUp() throws Exception {

    when( inField1.getName() ).thenReturn( SPLIT_FIELD );

    inFields = new ArrayList<ValueMetaInterface>();
    inFields.add( inField1 );

    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
    when( descriptor.getParentNamespace() ).thenReturn( namespace );
    when( descriptor.getNamespaceId() ).thenReturn( "namespace" );

    when( builder.getMetaverseObjectFactory() ).thenReturn( MetaverseTestUtils.getMetaverseObjectFactory() );

    when( parentTransMeta.getPrevStepNames( parentStepMeta ) ).thenReturn( new String[] { "input" } );
    when( inputRowMeta.getFieldNames() ).thenReturn( new String[] { SPLIT_FIELD } );
    when( inputRowMeta.searchValueMeta( any( String.class ) ) ).thenReturn( null );
    when( inputRowMeta.getValueMetaList() ).thenReturn( inFields );
    when( outputRowMeta.getFieldNames() ).thenReturn( outputFields );
    when( outputRowMeta.searchValueMeta( any( String.class ) ) ).thenReturn( null );

    when( parentTransMeta.getPrevStepFields( parentStepMeta ) ).thenReturn( inputRowMeta );
    when( parentTransMeta.getStepFields( parentStepMeta ) ).thenReturn( outputRowMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    when( fieldSplitterMeta.getParentStepMeta() ).thenReturn( parentStepMeta );

    when( fieldSplitterMeta.getDelimiter() ).thenReturn( "," );
    when( fieldSplitterMeta.getEnclosure() ).thenReturn( "\"" );
    when( fieldSplitterMeta.getSplitField() ).thenReturn( SPLIT_FIELD );

    when( fieldSplitterMeta.getFieldName() ).thenReturn( outputFields );

    analyzer = new SplitFieldsStepAnalyzer();
    analyzer.setMetaverseBuilder( builder );

    descriptor = new MetaverseComponentDescriptor( DEFAULT_STEP_NAME, DictionaryConst.NODE_TYPE_TRANS, namespace );
  }

  @Test
  public void testAnalyze() throws Exception {
    IMetaverseNode node = analyzer.analyze( descriptor, fieldSplitterMeta );
    assertNotNull( node );
    assertEquals( ",", node.getProperty( DictionaryConst.PROPERTY_DELIMITER ) );
    assertEquals( "\"", node.getProperty( DictionaryConst.PROPERTY_ENCLOSURE ) );

    // one uses link between the step node and the "split field" stream node
    verify( builder, times( 1 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );

    // one node created for each output field as well as one for the step itself
    verify( builder, times( outputFields.length + 1 ) ).addNode( any( IMetaverseNode.class ) );

    // one deletes link of the original "split field" stream field
    verify( builder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DELETES ),
      any( IMetaverseNode.class ) );

    // one derives link for each output field
    verify( builder, times( outputFields.length ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ),
      any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_reuseSplitFieldNameInOutputField() throws Exception {
    when( fieldSplitterMeta.getSplitField() ).thenReturn( "one" );
    when( inputRowMeta.getFieldNames() ).thenReturn( new String[] { "one" } );

    IMetaverseNode node = analyzer.analyze( descriptor, fieldSplitterMeta );
    assertNotNull( node );
    assertEquals( ",", node.getProperty( DictionaryConst.PROPERTY_DELIMITER ) );
    assertEquals( "\"", node.getProperty( DictionaryConst.PROPERTY_ENCLOSURE ) );

    // one uses link between the step node and the "split field" stream node
    verify( builder, times( 1 ) ).addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ),
      any( IMetaverseNode.class ) );

    // one node created for each output field as well as one for the step itself
    verify( builder, times( outputFields.length + 1 ) ).addNode( any( IMetaverseNode.class ) );

    // one deletes link of the original "split field" stream field
    verify( builder, atLeast( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DELETES ),
      any( IMetaverseNode.class ) );

    // one derives link for each output field
    verify( builder, times( outputFields.length ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ),
      any( IMetaverseNode.class ) );
  }


  @Test
  public void testGetFieldMappings() throws Exception {
    Set<IFieldMapping> fieldMappings = analyzer.getFieldMappings( fieldSplitterMeta );
    assertEquals( outputFields.length, fieldMappings.size() );
    int i = 0;
    for ( IFieldMapping fieldMapping : fieldMappings ) {
      assertEquals( SPLIT_FIELD, fieldMapping.getSourceFieldName() );
      assertEquals( outputFields[i], fieldMapping.getTargetFieldName() );
      i++;
    }
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> supportedSteps = analyzer.getSupportedSteps();
    assertEquals( 1, supportedSteps.size() );
    assertEquals( FieldSplitterMeta.class, supportedSteps.iterator().next() );
  }
}
