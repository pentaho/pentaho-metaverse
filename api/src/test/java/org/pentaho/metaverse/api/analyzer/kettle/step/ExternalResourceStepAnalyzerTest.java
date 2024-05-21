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

package org.pentaho.metaverse.api.analyzer.kettle.step;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 5/14/15.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class ExternalResourceStepAnalyzerTest {

  ExternalResourceStepAnalyzer analyzer;

  @Mock IComponentDescriptor descriptor;
  @Mock BaseStepMeta meta;
  @Mock BaseFileInputMeta fileMeta;
  @Mock IMetaverseNode node;
  @Mock IStepExternalResourceConsumer erc;
  @Mock IMetaverseBuilder builder;
  @Mock IMetaverseNode resourceNode;
  @Mock StepMeta parentStepMeta;
  @Mock TransMeta parentTransMeta;

  @Before
  public void setUp() throws Exception {
    analyzer = spy( new ExternalResourceStepAnalyzer<BaseStepMeta>() {
      @Override public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
        return null;
      }
      @Override protected Set<StepField> getUsedFields( BaseStepMeta meta ) {
        Set<StepField> stepFields = new HashSet<>();
        stepFields.add( new StepField( "prevStep", "filename" ) );
        return stepFields;
      }
      @Override public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
        return resourceNode;
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
    } );

    when( analyzer.getMetaverseBuilder() ).thenReturn( builder );
    analyzer.descriptor = descriptor;
    analyzer.parentTransMeta = parentTransMeta;
    analyzer.parentStepMeta = parentStepMeta;
    analyzer.setMetaverseObjectFactory( new MetaverseObjectFactory() );
  }

  @Test
  public void testAnalyze_nullERC() throws Exception {
    // fake the super.analyze call
    analyzer.setExternalResourceConsumer( null );
    analyzer.customAnalyze( meta, node );
    verify( builder, never() ).addNode( resourceNode );
    verify( builder, never() ).addLink( node, DictionaryConst.LINK_READBY, resourceNode );
  }

  @Test
  public void testAnalyze_input() throws Exception {
    // fake the super.analyze call
    lenient().doReturn( node ).when( (StepAnalyzer<BaseStepMeta>)analyzer ).analyze( descriptor, meta );
    analyzer.setExternalResourceConsumer( erc );

    List<IExternalResourceInfo> resources = new ArrayList<>();
    IExternalResourceInfo resInfo = mock( IExternalResourceInfo.class );
    resources.add( resInfo );
    lenient().when( resInfo.isInput() ).thenReturn( true );
    lenient().when( resInfo.isOutput() ).thenReturn( false );
    when( analyzer.isInput() ).thenReturn( true );
    when( analyzer.isOutput() ).thenReturn( false );

    when( erc.getResourcesFromMeta( eq( meta ), any() ) ).thenReturn( resources );

    analyzer.customAnalyze( meta, node );

    verify( builder ).addNode( resourceNode );
    verify( builder ).addLink( resourceNode, DictionaryConst.LINK_READBY, node );
  }

  @Test
  public void testAnalyze_output() throws Exception {
    // fake the super.analyze call
    lenient().doReturn( node ).when( (StepAnalyzer<BaseStepMeta>)analyzer ).analyze( descriptor, meta );
    analyzer.setExternalResourceConsumer( erc );

    List<IExternalResourceInfo> resources = new ArrayList<>();
    IExternalResourceInfo resInfo = mock( IExternalResourceInfo.class );
    resources.add( resInfo );
    lenient().when( resInfo.isInput() ).thenReturn( false );
    lenient().when( resInfo.isOutput() ).thenReturn( true );

    when( analyzer.isInput() ).thenReturn( false );
    when( analyzer.isOutput() ).thenReturn( true );

    when( erc.getResourcesFromMeta( eq( meta ), any() ) ).thenReturn( resources );

    analyzer.customAnalyze( meta, node );

    verify( builder ).addNode( resourceNode );
    verify( builder ).addLink( node, DictionaryConst.LINK_WRITESTO, resourceNode );
  }

  @Test
  public void testGetInputRowMetaInterfaces_isInput() throws Exception {
    when( parentTransMeta.getPrevStepNames( Mockito.<StepMeta>any() ) ).thenReturn( null );

    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    doReturn( rowMetaInterface ).when( analyzer ).getOutputFields( meta );
    doReturn( true ).when( analyzer ).isInput();

    Map<String, RowMetaInterface> rowMetaInterfaces = analyzer.getInputRowMetaInterfaces( meta );
    assertNotNull( rowMetaInterfaces );
  }

  @Test
  public void testGetInputRowMetaInterfaces_isInputNullOutputFields() throws Exception {
    when( parentTransMeta.getPrevStepNames( Mockito.<StepMeta>any() ) ).thenReturn( null );

    doReturn( null ).when( analyzer ).getOutputFields( meta );
    doReturn( true ).when( analyzer ).isInput();

    Map<String, RowMetaInterface> rowMetaInterfaces = analyzer.getInputRowMetaInterfaces( meta );
    assertNotNull( rowMetaInterfaces );
  }

  @Test
  public void testGetInputRowMetaInterfaces_isInputAndIncomingNodes() throws Exception {
    Map<String, RowMetaInterface> inputs = new HashMap<>();
    RowMetaInterface inputRmi = mock( RowMetaInterface.class );

    List<ValueMetaInterface> vmis = new ArrayList<>();
    ValueMetaInterface vmi = new ValueMeta( "filename" );
    vmis.add( vmi );

    when( inputRmi.getValueMetaList() ).thenReturn( vmis );
    inputs.put( "test", inputRmi );
    doReturn( inputs ).when( analyzer ).getInputFields( meta );
    lenient().when( parentTransMeta.getPrevStepNames( parentStepMeta ) ).thenReturn( null );

    RowMetaInterface rowMetaInterface = new RowMeta();
    rowMetaInterface.addValueMeta( vmi );
    ValueMetaInterface vmi2 = new ValueMeta( "otherField" );
    rowMetaInterface.addValueMeta( vmi2 );

    doReturn( rowMetaInterface ).when( analyzer ).getOutputFields( meta );
    doReturn( true ).when( analyzer ).isInput();

    Map<String, RowMetaInterface> rowMetaInterfaces = analyzer.getInputRowMetaInterfaces( meta );
    assertNotNull( rowMetaInterfaces );
    assertEquals( 2, rowMetaInterfaces.size() );
    RowMetaInterface metaInterface = rowMetaInterfaces.get( ExternalResourceStepAnalyzer.RESOURCE );
    // the row meta interface should only have 1 value meta in it, and it should NOT be filename
    assertEquals( 1, metaInterface.size() );
    assertEquals( "otherField", metaInterface.getFieldNames()[0] );
  }

  @Test
  public void testGetOutputRowMetaInterfaces() throws Exception {
    String[] nextStepNames = new String[] { "nextStep1" };
    when( parentTransMeta.getNextStepNames( parentStepMeta ) ).thenReturn( nextStepNames );

    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    doReturn( rowMetaInterface ).when( analyzer ).getOutputFields( meta );

    Map<String, RowMetaInterface> rowMetaInterfaces = analyzer.getOutputRowMetaInterfaces( meta );
    assertNotNull( rowMetaInterfaces );
    assertEquals( nextStepNames.length, rowMetaInterfaces.size() );
    assertEquals( rowMetaInterface, rowMetaInterfaces.get( nextStepNames[ 0 ] ) );
  }

  @Test
  public void testGetOutputRowMetaInterfaces_isOutput() throws Exception {
    String[] nextStepNames = new String[] { "nextStep1" };
    when( parentTransMeta.getNextStepNames( parentStepMeta ) ).thenReturn( nextStepNames );

    List<ValueMetaInterface> valueMetas = new ArrayList<>();
    valueMetas.add( new ValueMeta( "field1" ) );
    valueMetas.add( new ValueMeta( "field2" ) );

    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    RowMetaInterface clone = mock( RowMetaInterface.class );
    when( rowMetaInterface.getValueMetaList() ).thenReturn( valueMetas );
    when( rowMetaInterface.clone() ).thenReturn( clone );
    doReturn( rowMetaInterface ).when( analyzer ).getOutputFields( meta );
    doReturn( true ).when( analyzer ).isOutput();

    Set<String> resourceFields = new HashSet<>();
    resourceFields.add( "field1" );
    doReturn( resourceFields ).when( analyzer ).getOutputResourceFields( meta );

    Map<String, RowMetaInterface> rowMetaInterfaces = analyzer.getOutputRowMetaInterfaces( meta );
    assertNotNull( rowMetaInterfaces );
    // should have the normal rmi as well as the resource ones
    assertEquals( nextStepNames.length * 2, rowMetaInterfaces.size() );
    assertEquals( rowMetaInterface, rowMetaInterfaces.get( nextStepNames[ 0 ] ) );

    // field 2 isn't one of the fields written to the resource, it should be removed from the cloned RowMetaInterface
    verify( clone ).removeValueMeta( "field2" );
    // field 1 is one of the fields written to the resource, it should not be removed from the cloned RowMetaInterface
    verify( clone, never() ).removeValueMeta( "field1" );

  }

  @Test
  public void testGetOutputResourceFields() throws Exception {
    assertNull( analyzer.getOutputResourceFields( meta ) );
  }

  @Test
  public void testCreateInputFieldNode_resource() throws Exception {
    IAnalysisContext context = mock( IAnalysisContext.class );
    doReturn( "thisStepName" ).when( analyzer ).getStepName();
    analyzer.rootNode = node;
    lenient().when( node.getLogicalId() ).thenReturn( "logical id" );
    ValueMetaInterface vmi = new ValueMeta( "name", 1 );

    IMetaverseNode inputFieldNode = analyzer.createInputFieldNode(
      context,
      vmi,
      ExternalResourceStepAnalyzer.RESOURCE,
      DictionaryConst.NODE_TYPE_TRANS_FIELD );

    assertNotNull( inputFieldNode );

    assertNotNull( inputFieldNode.getProperty( DictionaryConst.PROPERTY_KETTLE_TYPE ) );
    assertEquals( "thisStepName", inputFieldNode.getProperty( DictionaryConst.PROPERTY_TARGET_STEP ) );
    assertEquals( "INPUT_TYPE", inputFieldNode.getType() );

    // the input node should be added by this step
    verify( builder ).addNode( inputFieldNode );

  }

  @Test
  public void testCreateOutputFieldNode_resource() throws Exception {
    IAnalysisContext context = mock( IAnalysisContext.class );
    lenient().doReturn( "thisStepName" ).when( analyzer ).getStepName();
    analyzer.rootNode = node;
    when( node.getLogicalId() ).thenReturn( "logical id" );
    ValueMetaInterface vmi = new ValueMeta( "name", 1 );

    IMetaverseNode outputFieldNode = analyzer.createOutputFieldNode(
      context,
      vmi,
      ExternalResourceStepAnalyzer.RESOURCE,
      DictionaryConst.NODE_TYPE_TRANS_FIELD );

    assertNotNull( outputFieldNode );

    assertNotNull( outputFieldNode.getProperty( DictionaryConst.PROPERTY_KETTLE_TYPE ) );
    assertEquals( ExternalResourceStepAnalyzer.RESOURCE,
      outputFieldNode.getProperty( DictionaryConst.PROPERTY_TARGET_STEP ) );
    assertEquals( "OUTPUT_TYPE", outputFieldNode.getType() );

    // the input node should be added by this step
    verify( builder ).addNode( outputFieldNode );

  }

  @Test
  public void testCreateOutputFieldNode() throws Exception {
    IAnalysisContext context = mock( IAnalysisContext.class );
    lenient().doReturn( "thisStepName" ).when( analyzer ).getStepName();
    analyzer.rootNode = node;
    when( node.getLogicalId() ).thenReturn( "logical id" );
    ValueMetaInterface vmi = new ValueMeta( "name", 1 );

    IMetaverseNode outputFieldNode = analyzer.createOutputFieldNode(
      context,
      vmi,
      "targetStep",
      DictionaryConst.NODE_TYPE_TRANS_FIELD );

    assertNotNull( outputFieldNode );

    assertNotNull( outputFieldNode.getProperty( DictionaryConst.PROPERTY_KETTLE_TYPE ) );
    assertEquals( "targetStep", outputFieldNode.getProperty( DictionaryConst.PROPERTY_TARGET_STEP ) );
    assertEquals( DictionaryConst.NODE_TYPE_TRANS_FIELD, outputFieldNode.getType() );

    // the input node should be added by this step
    verify( builder ).addNode( outputFieldNode );

  }

  @Test
  public void testLinkChangeNodes_populates() throws Exception {
    IMetaverseNode inputNode = mock( IMetaverseNode.class );
    IMetaverseNode outputNode = mock( IMetaverseNode.class );

    when( inputNode.getType() ).thenReturn( "A" );
    when( outputNode.getType() ).thenReturn( "B" );

    doReturn( false ).when( analyzer ).isInput();
    doReturn( true ).when( analyzer ).isOutput();

    analyzer.linkChangeNodes( inputNode, outputNode );
    verify( builder ).addLink( inputNode, DictionaryConst.LINK_POPULATES, outputNode );
    verify( builder, never() ).addLink( inputNode, analyzer.getInputToOutputLinkLabel(), outputNode );
  }

  @Test
  public void testLinkChangeNodes_populates2() throws Exception {
    IMetaverseNode inputNode = mock( IMetaverseNode.class );
    IMetaverseNode outputNode = mock( IMetaverseNode.class );

    when( inputNode.getType() ).thenReturn( "A" );
    when( outputNode.getType() ).thenReturn( "B" );

    lenient().doReturn( true ).when( analyzer ).isInput();
    lenient().doReturn( false ).when( analyzer ).isOutput();

    analyzer.linkChangeNodes( inputNode, outputNode );
    verify( builder ).addLink( inputNode, DictionaryConst.LINK_POPULATES, outputNode );
    verify( builder, never() ).addLink( inputNode, analyzer.getInputToOutputLinkLabel(), outputNode );
  }

  @Test
  public void testLinkChangeNodes() throws Exception {
    IMetaverseNode inputNode = mock( IMetaverseNode.class );
    IMetaverseNode outputNode = mock( IMetaverseNode.class );

    when( inputNode.getType() ).thenReturn( "A" );
    when( outputNode.getType() ).thenReturn( "A" );

    lenient().doReturn( true ).when( analyzer ).isInput();
    lenient().doReturn( false ).when( analyzer ).isOutput();

    analyzer.linkChangeNodes( inputNode, outputNode );
    verify( builder, never() ).addLink( inputNode, DictionaryConst.LINK_POPULATES, outputNode );
    verify( builder ).addLink( inputNode, analyzer.getInputToOutputLinkLabel(), outputNode );
  }

  @Test
  public void testGetInputFieldsToIgnore() {

    lenient().doReturn( true ).when( analyzer ).isInput();

    // setup input fields
    RowMetaInterface inputFieldRowMeta = mock( RowMetaInterface.class );
    List<ValueMetaInterface> inputFields = new ArrayList<>();
    inputFields.add( new ValueMeta( "in_field_1" ) );
    Map<String, RowMetaInterface> inputFieldRowMetaMap = new HashMap<>();
    inputFieldRowMetaMap.put( ExternalResourceStepAnalyzer.RESOURCE, inputFieldRowMeta );
    when( inputFieldRowMeta.getValueMetaList() ).thenReturn( inputFields );

    // setup output fields
    List<ValueMetaInterface> outputFields = new ArrayList<>();
    outputFields.addAll( inputFields );
    outputFields.add( new ValueMeta( "file_field_1" ) );
    outputFields.add( new ValueMeta( "additional_field" ) );
    outputFields.add( new ValueMeta( "file_field_2" ) );
    RowMetaInterface outputFieldsRowMeta = mock( RowMetaInterface.class );
    when( outputFieldsRowMeta.getValueMetaList() ).thenReturn( outputFields );

    // setup step "resource" fields
    final BaseFileField[] resourceFields = new BaseFileField[ 2 ];
    resourceFields[ 0 ] = new BaseFileField( "file_field_1", 0, 0 );
    resourceFields[ 1 ] = new BaseFileField( "file_field_2", 0, 0 );
    doReturn( resourceFields ).when( fileMeta ).getInputFields();

    Set<String> fieldsToIgnore = analyzer.getInputFieldsToIgnore( fileMeta, inputFieldRowMetaMap, outputFieldsRowMeta );
    assertEquals( 2, fieldsToIgnore.size() );
    assertTrue( fieldsToIgnore.contains( "in_field_1" ) );
    assertTrue( fieldsToIgnore.contains( "additional_field" ) );
  }
}
