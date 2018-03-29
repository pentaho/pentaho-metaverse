/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
    when( erc.getResources( eq( meta ), any( IAnalysisContext.class ) ) ).thenReturn( resources );

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
