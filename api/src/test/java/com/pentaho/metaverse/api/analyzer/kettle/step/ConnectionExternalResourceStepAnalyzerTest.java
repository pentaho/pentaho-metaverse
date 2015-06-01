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

package com.pentaho.metaverse.api.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.MetaverseTransientNode;
import com.pentaho.metaverse.api.IAnalysisContext;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IConnectionAnalyzer;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseObjectFactory;
import com.pentaho.metaverse.api.StepField;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 5/26/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class ConnectionExternalResourceStepAnalyzerTest {

  ConnectionExternalResourceStepAnalyzer analyzer;

  @Mock IComponentDescriptor descriptor;
  @Mock BaseStepMeta meta;
  @Mock IMetaverseNode node;
  @Mock IStepExternalResourceConsumer erc;
  @Mock IMetaverseBuilder builder;
  @Mock IMetaverseNode resourceNode;
  @Mock StepMeta parentStepMeta;
  @Mock TransMeta parentTransMeta;
  @Mock IAnalysisContext context;
  @Mock IMetaverseNode tableNode;
  @Before
  public void setUp() throws Exception {
    analyzer = spy( new ConnectionExternalResourceStepAnalyzer<BaseStepMeta>() {
      @Override protected IMetaverseNode createTableNode( IExternalResourceInfo resource )
        throws MetaverseAnalyzerException {
        return tableNode;
      }

      @Override public String getResourceInputNodeType() {
        return "INPUT_TYPE";
      }
      @Override public String getResourceOutputNodeType() {
        return "OUTPUT_TYPE";
      }
      @Override public boolean isOutput() {
        return false;
      }
      @Override public boolean isInput() {
        return true;
      }
      @Override protected Set<StepField> getUsedFields( BaseStepMeta meta ) {
        Set<StepField> stepFields = new HashSet<>();
        stepFields.add( new StepField( "prevStep", "filename" ) );
        return stepFields;
      }
      @Override public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
        return null;
      }
    } );
    when( analyzer.getMetaverseBuilder() ).thenReturn( builder );
    analyzer.descriptor = descriptor;
    when( descriptor.getContext() ).thenReturn( context );
    analyzer.parentTransMeta = parentTransMeta;
    analyzer.parentStepMeta = parentStepMeta;
    analyzer.setMetaverseObjectFactory( new MetaverseObjectFactory() );
  }

  @Test
  public void testCustomAnalyze_nullERC() throws Exception {
    // fake the super.analyze call
    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    IConnectionAnalyzer connectionAnalyzer = mock( IConnectionAnalyzer.class );
    doReturn( connectionAnalyzer ).when( analyzer ).getConnectionAnalyzer();
    when( connectionAnalyzer.analyze( descriptor, meta ) ).thenReturn( connectionNode );
    analyzer.setExternalResourceConsumer( null );
    analyzer.customAnalyze( meta, node );
    verify( builder, never() ).addNode( resourceNode );
    verify( builder, never() ).addLink( node, DictionaryConst.LINK_READBY, resourceNode );
  }

  @Test
  public void testCustomAnalyze_input() throws Exception {
    // fake the super.analyze call
    doReturn( node ).when( (StepAnalyzer<BaseStepMeta>)analyzer ).analyze( descriptor, meta );
    analyzer.setExternalResourceConsumer( erc );

    List<IExternalResourceInfo> resources = new ArrayList<>();
    IExternalResourceInfo resInfo = mock( IExternalResourceInfo.class );
    resources.add( resInfo );
    when( resInfo.isInput() ).thenReturn( true );

    when( erc.getResourcesFromMeta( meta, context ) ).thenReturn( resources );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();

    doReturn( resourceNode ).when( analyzer ).createResourceNode( any( IExternalResourceInfo.class ) );

    analyzer.customAnalyze( meta, node );

    verify( builder ).addNode( resourceNode );
    verify( builder ).addLink( resourceNode, DictionaryConst.LINK_READBY, node );
    verify( builder ).addNode( connectionNode );
    verify( builder ).addLink( connectionNode, DictionaryConst.LINK_DEPENDENCYOF, node );
  }

  @Test
  public void testCreateResourceNode_inputs() throws Exception {
    IExternalResourceInfo resInfo = mock( IExternalResourceInfo.class );
    StepNodes inputs = mock( StepNodes.class );
    StepNodes outputs = null;
    doReturn( inputs ).when( analyzer ).getInputs();
    doReturn( outputs ).when( analyzer ).getOutputs();
    doReturn( tableNode ).when( analyzer ).createTableNode( resInfo );
    doReturn( true ).when( analyzer ).isInput();

    IMetaverseNode resourceNode = analyzer.createResourceNode( resInfo );
    assertEquals( tableNode, resourceNode );
    verify( analyzer ).linkResourceToFields( inputs );
    verify( analyzer, never() ).linkResourceToFields( outputs );

    assertEquals( tableNode, analyzer.getTableNode() );

  }

  @Test
  public void testCreateResourceNode_outputs() throws Exception {
    IExternalResourceInfo resInfo = mock( IExternalResourceInfo.class );
    StepNodes inputs = null;
    StepNodes outputs = mock( StepNodes.class );
    doReturn( inputs ).when( analyzer ).getInputs();
    doReturn( outputs ).when( analyzer ).getOutputs();
    doReturn( tableNode ).when( analyzer ).createTableNode( resInfo );
    doReturn( false ).when( analyzer ).isInput();

    IMetaverseNode resourceNode = analyzer.createResourceNode( resInfo );
    assertEquals( tableNode, resourceNode );
    verify( analyzer ).linkResourceToFields( outputs );
    verify( analyzer, never() ).linkResourceToFields( inputs );
  }

  @Test
  public void testLinkResourceToFields() throws Exception {
    StepNodes inputs = new StepNodes();
    inputs.addNode( "x", "first name", node );
    inputs.addNode( ExternalResourceStepAnalyzer.RESOURCE, "first name", node );
    inputs.addNode( ExternalResourceStepAnalyzer.RESOURCE, "last name", node );

    doReturn( tableNode ).when( analyzer ).getTableNode();
    when( tableNode.getLogicalId() ).thenReturn( "table logical id" );

    analyzer.linkResourceToFields( inputs );
    int resoruceNodeCount = inputs.getFieldNames( ExternalResourceStepAnalyzer.RESOURCE ).size();
    verify( node, times( resoruceNodeCount ) ).setProperty( DictionaryConst.PROPERTY_NAMESPACE, "table logical id" );
    verify( builder, times( resoruceNodeCount ) ).updateNode( node );
    verify( builder, times( resoruceNodeCount ) ).addLink( tableNode, DictionaryConst.LINK_CONTAINS, node );
  }
}
