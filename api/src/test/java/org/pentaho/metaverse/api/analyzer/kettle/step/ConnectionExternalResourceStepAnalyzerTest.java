/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IConnectionAnalyzer;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 5/26/15.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
    when( connectionAnalyzer.analyze( eq( descriptor ), any() ) ).thenReturn( connectionNode );
    analyzer.setExternalResourceConsumer( null );
    analyzer.customAnalyze( meta, node );
    verify( builder, never() ).addNode( resourceNode );
    verify( builder, never() ).addLink( node, DictionaryConst.LINK_READBY, resourceNode );
  }

  @Test
  public void testCustomAnalyze_input() throws Exception {
    // fake the super.analyze call
    lenient().doReturn( node ).when( (StepAnalyzer<BaseStepMeta>)analyzer ).analyze( descriptor, meta );
    analyzer.setExternalResourceConsumer( erc );

    List<IExternalResourceInfo> resources = new ArrayList<>();
    IExternalResourceInfo resInfo = mock( IExternalResourceInfo.class );
    resources.add( resInfo );

    when( erc.getResourcesFromMeta( meta, context ) ).thenReturn( resources );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();

    doReturn( resourceNode ).when( analyzer ).createResourceNode( eq(meta), any( IExternalResourceInfo.class ) );

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
    doReturn( inputs ).when( analyzer ).getInputs();
    doReturn( tableNode ).when( analyzer ).createTableNode( resInfo );
    doReturn( true ).when( analyzer ).isInput();

    IMetaverseNode resourceNode = analyzer.createResourceNode( resInfo );
    assertEquals( tableNode, resourceNode );
    verify( analyzer ).linkResourceToFields( inputs );
    verify( analyzer, never() ).linkResourceToFields( null );

    assertEquals( tableNode, analyzer.getTableNode() );

  }

  @Test
  public void testCreateResourceNode_outputs() throws Exception {
    IExternalResourceInfo resInfo = mock( IExternalResourceInfo.class );
    StepNodes outputs = mock( StepNodes.class );
    doReturn( outputs ).when( analyzer ).getOutputs();
    doReturn( tableNode ).when( analyzer ).createTableNode( resInfo );
    doReturn( false ).when( analyzer ).isInput();

    IMetaverseNode resourceNode = analyzer.createResourceNode( resInfo );
    assertEquals( tableNode, resourceNode );
    verify( analyzer ).linkResourceToFields( outputs );
    verify( analyzer, never() ).linkResourceToFields( null );
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
