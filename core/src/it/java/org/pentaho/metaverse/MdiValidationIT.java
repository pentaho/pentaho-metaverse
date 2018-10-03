/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.metaverse.frames.FileNode;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.StreamFieldNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith( PowerMockRunner.class )
@PrepareForTest( MetaverseConfig.class )
public class MdiValidationIT extends BaseMetaverseValidationIT {

  private static final String ROOT_FOLDER = "src/it/resources/repo/mdi-validation";
  private static final String OUTPUT_FILE = "target/outputfiles/mdiValidationGraph.graphml";

  @BeforeClass
  public static void init() throws Exception {

    PowerMockito.mockStatic( MetaverseConfig.class );
    Mockito.when( MetaverseConfig.adjustExternalResourceFields() ).thenReturn( true );
    Mockito.when( MetaverseConfig.deduplicateTransformationFields() ).thenReturn( true );

    BaseMetaverseValidationIT.init( ROOT_FOLDER, OUTPUT_FILE );
  }

  private Map<String, TransformationStepNode> verifyStepNodes( final List<TransformationStepNode> stepNodes,
                                                               final String... expectedStepNameArray ) {
    final List<String> expectedStepNames = Arrays.asList(
      expectedStepNameArray == null ? new String[] {} : expectedStepNameArray );
    final Map<String, TransformationStepNode> stepNodeMap = new HashMap();
    for ( final TransformationStepNode stepNode : stepNodes ) {
      assertTrue( expectedStepNames.contains( stepNode.getName() ) );
      stepNodeMap.put( stepNode.getName(), stepNode );
    }
    return stepNodeMap;
  }

  private TransformationNode verifyTransformationNode( final String transformationName, final boolean isSubTrans ) {
    // verify the existence of two transformation nodes - one for the injector and one for the sub-transformation
    final List<TransformationNode> allTransformations = IteratorUtils.toList( root.getTransformations().iterator() );
    final TransformationNode node = isSubTrans ? root.getSubTransformation( transformationName )
      : root.getTransformation( transformationName );
    assertNotNull( node );
    assertTrue( allTransformations.contains( node ) );
    return node;
  }

  private Map<String, TransformationStepNode> verifyTransformationSteps(
    final TransformationNode transNode, final String[] stepNames, final String[] virtualStepNames,
    final boolean isVirtual ) {

    final List<TransformationStepNode> stepNodes = IteratorUtils.toList( transNode.getStepNodes().iterator() );
    assertEquals( stepNames == null ? 0 : stepNames.length, stepNodes.size() );
    // check that all the expected step nodes exist and create a node map on the fly for future use
    final Map<String, TransformationStepNode> stepNodeMap = verifyStepNodes( stepNodes, stepNames );

    final List<TransformationStepNode> virtualStepNodes = IteratorUtils.toList(
      transNode.getVirtualStepNodes().iterator() );
    assertEquals( virtualStepNames == null ? 0 : virtualStepNames.length, virtualStepNodes.size() );
    final Map<String, TransformationStepNode> virtualStepNodeMap = verifyStepNodes(
      virtualStepNodes, virtualStepNames );

    return isVirtual ? virtualStepNodeMap : stepNodeMap;
  }

  private void verifyMdiInputs( final TransformationStepNode mdiNode, final TransformationStepNode inputStepNode,
                                final String subTransStepNodeName,
                                final Map<String, String> fieldToPopulatedPropMap ) {

    // verify that the inputStepNode itself is not null
    assertNotNull( inputStepNode );

    // get the step's output fields and verify that they are all inputs into the mdi node
    final List<StreamFieldNode> inputStepOutputFields = IteratorUtils.toList(
      inputStepNode.getOutputStreamFields().iterator() );
    for ( final StreamFieldNode field : inputStepOutputFields ) {
      assertEquals( mdiNode, field.getStepThatInputsMe() );
      // also verify that the step name belongs to this step
      assertTrue( IteratorUtils.toList( fieldToPopulatedPropMap.keySet().iterator() ).contains( field.getName() ) );
    }

    // get the templateSubTransNode - we can assume that there's exactly one
    final TransformationNode templateSubTrans = (TransformationNode) IteratorUtils.toList(
      mdiNode.getExecutesNodes().iterator() ).get( 0 );
    // get the virtual step node that is contained by this sub-transformation that corresponds to 'subTransStepNodeName'
    final TransformationStepNode templateSubTransStepNode = templateSubTrans.getStepNode( subTransStepNodeName );
    // this node should be virtual
    assertTrue( templateSubTransStepNode.isVirtual() );

    final List<FramedMetaverseNode> containedProperties = IteratorUtils.toList(
      templateSubTransStepNode.getContainedNodes().iterator() );

    // verify that we have populates links from the input step fields to the sub-trans step node properties, as
    // defined within the 'fieldToPopulatedPropMap'
    for ( final StreamFieldNode field : inputStepOutputFields ) {
      // get the property name corresponding to this field name
      final String mdiPropertyName = fieldToPopulatedPropMap.get( field.getName() );
      // verify that it is contained within the containedProperties (only for non-empty properties
      if ( StringUtils.isNotBlank( mdiPropertyName ) ) {
        assertTrue( containsName( containedProperties, mdiPropertyName ) );

        // get the "populates" edges for this field and verify that there is one pointing to this mdi property
        final List<FramedMetaverseNode> nodesPopulatedByMe = IteratorUtils.toList(
          field.getNodesPopulatedByMe().iterator() );
        assertTrue( containsName( nodesPopulatedByMe, mdiPropertyName ) );
      }
    }
  }

  private boolean containsName( final List<FramedMetaverseNode> nodes, final String nodeName ) {
    for ( final FramedMetaverseNode node : nodes ) {
      if ( node.getName().equals( nodeName ) ) {
        return true;
      }
    }
    return false;
  }

  private void verifyInjectorTextFileOutputNode( final TransformationStepNode mdiNode, final String outputFileName,
                                                 final String[] expectedOutputFieldNameArray,
                                                 final TransformationStepNode templateReadFromStepNode ) {
    final List<TransformationStepNode> mdiHopsToNodes = IteratorUtils.toList( mdiNode.getNextSteps().iterator() );
    assertEquals( 1, mdiHopsToNodes.size() );
    // verify that the text file output step writes to the correct output file
    final TransformationStepNode textFileOutputNode = mdiHopsToNodes.get( 0 );
    assertEquals( "Text file output", textFileOutputNode.getName() );
    final List<FileNode> writesToNodes = IteratorUtils.toList( textFileOutputNode.getWritesToNodes().iterator() );
    assertEquals( 1, writesToNodes.size() );
    final FileNode fileNode = writesToNodes.get( 0 );
    assertTrue( fileNode.getPath().endsWith( outputFileName ) );

    // verify that the mdi node output fields are inputs into the text file output step
    final List<StreamFieldNode> mdiOutputFields = IteratorUtils.toList(
      mdiNode.getOutputStreamFields().iterator() );
    final List<StreamFieldNode> mdiInputFields = IteratorUtils.toList(
      mdiNode.getInputStreamFields().iterator() );
    final List<StreamFieldNode> inputFields = IteratorUtils.toList(
      textFileOutputNode.getInputStreamFields().iterator() );
    assertEquals( mdiOutputFields.size(), inputFields.size() );
    final List<StreamFieldNode> tempalteReadFromStepOutputFields = templateReadFromStepNode == null
      ? new ArrayList() : IteratorUtils.toList( templateReadFromStepNode.getOutputStreamFields().iterator() );

    for ( final StreamFieldNode field : inputFields ) {
      assertTrue( mdiOutputFields.contains( field ) );
      // verify that the input fields to the text file output node are derived from fields that are outputs from the
      // template node that is being read and that the same field is also an input into the mdi step node
      if ( templateReadFromStepNode != null ) {
        // there should be 1 field that derives this field, and it should be one of the mdi input fields, as well as
        // one of the output fields from the template step that is being read from
        assertEquals( 1, getIterableSize( field.getFieldNodesThatDeriveMe() ) );
        final StreamFieldNode derivingField = (StreamFieldNode) IteratorUtils.toList(
          field.getFieldNodesThatDeriveMe().iterator() ).get( 0 );
        assertTrue( mdiInputFields.contains( derivingField ) );
        assertTrue( tempalteReadFromStepOutputFields.contains( derivingField ) );
        assertEquals( field.getName(), derivingField.getName() );
        // this deriving field is also expected to be virtual
        assertTrue( derivingField.isVirtual() );
      }
    }

    // verify that the text file output step has the expected output fields, and that each field is derived from an
    // mdi output field with the same name
    final List<StreamFieldNode> outputFields = IteratorUtils.toList(
      textFileOutputNode.getOutputStreamFields().iterator() );
    final List<String> expectedOutputFieldNames = Arrays.asList(
      expectedOutputFieldNameArray == null ? new String[] {} : expectedOutputFieldNameArray );
    assertEquals( expectedOutputFieldNames.size(), outputFields.size() );
    for ( final StreamFieldNode field : outputFields ) {
      assertTrue( expectedOutputFieldNames.contains( field.getName() ) );
      // there should be 1 field that derives this field, and it should be one of the mdi output fields
      assertEquals( 1, getIterableSize( field.getFieldNodesThatDeriveMe() ) );
      final StreamFieldNode derivingField = (StreamFieldNode) IteratorUtils.toList(
        field.getFieldNodesThatDeriveMe().iterator() ).get( 0 );
      assertTrue( mdiOutputFields.contains( derivingField ) );
      assertEquals( field.getName(), derivingField.getName() );
    }
  }

  private TransformationStepNode verifyMdiNode( final Map<String, TransformationStepNode> stepNodeMap,
                                                final TransformationNode templateSubTransNode,
                                                final String[] expectedOutputFieldNameArray ) {
    // verify that the Text Output and Text Output - Fields steps hot into the mdi step and that their output fields
    // input into the mdi step
    final TransformationStepNode mdiNode = stepNodeMap.get( "ETL Metadata Injection" );
    assertNotNull( mdiNode );

    // the MDI node should "execute" the template sub-transformation node
    assertEquals( 1, getIterableSize( mdiNode.getExecutesNodes() ) );
    final TransformationNode executedTransNode = (TransformationNode) IteratorUtils.toList(
      mdiNode.getExecutesNodes().iterator() ).get( 0 );
    assertEquals( templateSubTransNode, executedTransNode );

    final List<StreamFieldNode> mdiOutputFields = IteratorUtils.toList( mdiNode.getOutputStreamFields().iterator() );
    final List<String> expectedOutputFieldNames = Arrays.asList(
      expectedOutputFieldNameArray == null ? new String[] {} : expectedOutputFieldNameArray );
    assertEquals( expectedOutputFieldNames.size(), mdiOutputFields.size() );
    for ( final StreamFieldNode field : mdiOutputFields ) {
      assertTrue( expectedOutputFieldNames.contains( field.getName() ) );
    }

    return mdiNode;
  }

  @Test
  public void testMdiInjectorStreamReadingAndStreamingSameStep() throws Exception {

    final TransformationNode injectorTransNode = verifyTransformationNode( "injector_stream_same_as_mapping", false );
    final TransformationNode templateSubTransNode = verifyTransformationNode(
      "template_stream_same_as_mapping", true );

    // The injector should "contain" 4 step nodes and no virtual nodes
    final Map<String, TransformationStepNode> stepNodeMap = verifyTransformationSteps( injectorTransNode,
      new String[] { "Text Output - Fields", "Text Output", "ETL Metadata Injection", "Text file output" },
      null, false );

    // the template subTransformation should "contain" no step nodes (non-virtual) and one virtual nodes
    final Map<String, TransformationStepNode> virtualTemplteStepNodeMap = verifyTransformationSteps(
      templateSubTransNode, null, new String[] { "My Text file output" }, true );

    // verify that the MDI node has no output fields, we don't expect any when not reading directly from a template step
    final TransformationStepNode mdiNode = verifyMdiNode( stepNodeMap, templateSubTransNode,
      new String[] { "First Name", "Last Name" } );

    verifyInjectorTextFileOutputNode( mdiNode, "stream_same_as_mapping_injector.txt",
      new String[] { "First Name", "Last Name" }, virtualTemplteStepNodeMap.get( "My Text file output" ) );

    // verify all the expected mdi properties and links from fields to properties exist
    final TransformationStepNode textOutputNode = stepNodeMap.get( "Text Output" );
    verifyMdiInputs( mdiNode, textOutputNode, "My Text file output", ImmutableMap.of( "File Name", "FILENAME",
      "Separator", "SEPARATOR" ) );

    final TransformationStepNode textOutputFieldsNode = stepNodeMap.get( "Text Output - Fields" );
    verifyMdiInputs( mdiNode, textOutputFieldsNode, "My Text file output", ImmutableMap.of( "Field Name",
      "", "Trim Type", "", "Type", "", "Dummy", "" ) );

    // we are streaming directly from the 'Text Output - Fields' step to the template's 'My Text file output' step -
    // verify that all the expected 'derives' links exist between the 'Text Output - Fields' output fields and the
    // 'My Text file output'  output fields
    final TransformationStepNode templateStreamToStepNode = virtualTemplteStepNodeMap.get( "My Text file output" );
    final List<StreamFieldNode> templateStreamToStepOutputFields = IteratorUtils.toList(
      templateStreamToStepNode.getOutputStreamFields().iterator() );
    final List<StreamFieldNode> textOutputFieldsOutputFields = IteratorUtils.toList(
      textOutputFieldsNode.getOutputStreamFields().iterator() );
    for ( final StreamFieldNode field : textOutputFieldsOutputFields ) {
      // get the derived field
      assertEquals( 1, getIterableSize( field.getFieldNodesDerivedFromMe() ) );
      final StreamFieldNode derivedField = (StreamFieldNode) IteratorUtils.toList(
        field.getFieldNodesDerivedFromMe().iterator() ).get( 0 );
      assertTrue( templateStreamToStepOutputFields.contains( derivedField ) );
    }
  }

  @Test
  public void testMdiInjectorStreamReadingAndStreamingDifferentStep() throws Exception {

    final TransformationNode injectorTransNode =
      verifyTransformationNode( "injector_stream_different_than_mapping", false );
    final TransformationNode templateSubTransNode = verifyTransformationNode(
      "template_stream_different_than_mapping", true );

    // The injector should "contain" 4 step nodes and no virtual nodes
    final Map<String, TransformationStepNode> stepNodeMap = verifyTransformationSteps( injectorTransNode,
      new String[] { "Text Output - Fields", "Text Output", "ETL Metadata Injection", "Text file output" },
      null, false );

    // the template subTransformation should "contain" no step nodes (non-virtual) and one virtual nodes
    final Map<String, TransformationStepNode> virtualTemplteStepNodeMap = verifyTransformationSteps(
      templateSubTransNode, null, new String[] { "My Text file output", "My Text file output [2]" }, true );

    // verify that the MDI node has no output fields, we don't expect any when not reading directly from a template step
    final TransformationStepNode mdiNode = verifyMdiNode( stepNodeMap, templateSubTransNode,
      new String[] { "First Name", "Last Name" } );

    verifyInjectorTextFileOutputNode( mdiNode, "stream_different_than_mapping_injector.txt",
      new String[] { "First Name", "Last Name" }, virtualTemplteStepNodeMap.get( "My Text file output" ) );

    // verify all the expected mdi properties and links from fields to properties exist
    final TransformationStepNode textOutputNode = stepNodeMap.get( "Text Output" );
    verifyMdiInputs( mdiNode, textOutputNode, "My Text file output", ImmutableMap.of( "File Name", "FILENAME",
      "Separator", "SEPARATOR" ) );

    final TransformationStepNode textOutputFieldsNode = stepNodeMap.get( "Text Output - Fields" );
    verifyMdiInputs( mdiNode, textOutputFieldsNode, "My Text file output", ImmutableMap.of( "Field Name",
      "", "Trim Type", "", "Type", "", "Dummy", "" ) );

    // we are streaming directly from the 'Text Output - Fields' step to the template's 'My Text file output [2]' step -
    // verify that all the expected 'derives' links exist between the 'Text Output - Fields' output fields and the
    // 'My Text file output [2]'  output fields
    final TransformationStepNode templateStreamToStepNode = virtualTemplteStepNodeMap.get( "My Text file output [2]" );
    final List<StreamFieldNode> templateStreamToStepOutputFields = IteratorUtils.toList(
      templateStreamToStepNode.getOutputStreamFields().iterator() );
    final List<StreamFieldNode> textOutputFieldsOutputFields = IteratorUtils.toList(
      textOutputFieldsNode.getOutputStreamFields().iterator() );
    for ( final StreamFieldNode field : textOutputFieldsOutputFields ) {
      // get the derived field
      assertEquals( 1, getIterableSize( field.getFieldNodesDerivedFromMe() ) );
      final StreamFieldNode derivedField = (StreamFieldNode) IteratorUtils.toList(
        field.getFieldNodesDerivedFromMe().iterator() ).get( 0 );
      assertTrue( templateStreamToStepOutputFields.contains( derivedField ) );
    }
  }

  @Test
  public void testMdiInjectorNoStream() throws Exception {

    final TransformationNode injectorTransNode = verifyTransformationNode( "injector_no_stream", false );
    final TransformationNode templateSubTransNode = verifyTransformationNode( "template_no_stream", true );

    // The injector should "contain" 4 step nodes and no virtual nodes
    final Map<String, TransformationStepNode> stepNodeMap = verifyTransformationSteps( injectorTransNode,
      new String[] { "Text Output - Fields", "Text Output", "ETL Metadata Injection", "Text file output" },
      null, false );

    // the template subTransformation should "contain" no step nodes (non-virtual) and one virtual nodes
    verifyTransformationSteps( templateSubTransNode, null, new String[] { "My Text file output" }, true );

    // verify that the MDI node has no output fields, we don't expect any when not reading directly from a template step
    final TransformationStepNode mdiNode = verifyMdiNode( stepNodeMap, templateSubTransNode, null );

    verifyInjectorTextFileOutputNode( mdiNode, "injector_no_stream.txt", null, null );

    // verify all the expected mdi properties and links from fields to properties exist
    final TransformationStepNode textOutputNode = stepNodeMap.get( "Text Output" );
    verifyMdiInputs( mdiNode, textOutputNode, "My Text file output", ImmutableMap.of( "File Name", "FILENAME",
      "Separator", "SEPARATOR" ) );

    final TransformationStepNode textOutputFieldsNode = stepNodeMap.get( "Text Output - Fields" );
    verifyMdiInputs( mdiNode, textOutputFieldsNode, "My Text file output", ImmutableMap.of( "Field Name",
      "OUTPUT_FIELDNAME", "Trim Type", "OUTPUT_TRIM", "Type", "", "Dummy", "" ) );
  }

  //@Test
  public void testMdiTemplateNoStream() throws Exception {
    final TransformationNode templateTransNode = verifyTransformationNode( "template_no_stream", false );

    // The template should "contain" 3 step nodes and no virtual nodes
    final Map<String, TransformationStepNode> stepNodeMap = verifyTransformationSteps( templateTransNode,
      new String[] { "My Generate Rows", "My Text file output", "My Text file output [2]" }, null, false );

    final TransformationStepNode textOutputNode1 = stepNodeMap.get( "My Text file output" );
    // verify that the output file name for the first file output node comes from the injector
    final List<FileNode> writesToNodes1 = IteratorUtils.toList( textOutputNode1.getWritesToNodes().iterator() );
    assertEquals( 1, writesToNodes1.size() );
    assertTrue( writesToNodes1.get( 0 ).getPath().endsWith( "no_stream_tempalte.txt" ) );
    // should have two output fields

    final TransformationStepNode textOutputNode2 = stepNodeMap.get( "My Text file output [2]" );
    // verify that the output file name for the second file output node remains unchanged
    final List<FileNode> writesToNodes2 = IteratorUtils.toList( textOutputNode1.getWritesToNodes().iterator() );
    assertEquals( 1, writesToNodes2.size() );
    assertTrue( writesToNodes2.get( 0 ).getPath().endsWith( "orig_no_stream_tempalte2.txt" ) );
    // should have three output fields
  }

  @Test
  public void testMdiInjectorNoStreamMap2Steps() throws Exception {

    final TransformationNode injectorTransNode = verifyTransformationNode( "injector_no_stream_map_2_steps", false );
    final TransformationNode templateSubTransNode = verifyTransformationNode( "template_no_stream_map_2_steps", true );

    // The injector should "contain" 4 step nodes and no virtual nodes
    final Map<String, TransformationStepNode> stepNodeMap = verifyTransformationSteps( injectorTransNode,
      new String[] { "Text Output - Fields", "Text Output", "ETL Metadata Injection", "Text file output" },
      null, false );

    // the template subTransformation should "contain" no step nodes (non-virtual) and one virtual nodes
    verifyTransformationSteps( templateSubTransNode, null, new String[] { "My Text file output",
      "My Text file output [2]" }, true );

    // verify that the MDI node has no output fields, we don't expect any when not reading directly from a template step
    final TransformationStepNode mdiNode = verifyMdiNode( stepNodeMap, templateSubTransNode, null );

    verifyInjectorTextFileOutputNode( mdiNode, "injector_no_stream_map_2_steps.txt", null, null );

    // verify all the expected mdi properties and links from fields to properties exist
    final TransformationStepNode textOutputNode = stepNodeMap.get( "Text Output" );
    verifyMdiInputs( mdiNode, textOutputNode, "My Text file output", ImmutableMap.of( "File Name", "FILENAME",
      "Separator", "SEPARATOR" ) );
    verifyMdiInputs( mdiNode, textOutputNode, "My Text file output [2]", ImmutableMap.of( "File Name", "",
      "Separator", "SEPARATOR" ) );

    final TransformationStepNode textOutputFieldsNode = stepNodeMap.get( "Text Output - Fields" );
    verifyMdiInputs( mdiNode, textOutputFieldsNode, "My Text file output", ImmutableMap.of( "Field Name",
      "OUTPUT_FIELDNAME", "Trim Type", "OUTPUT_TRIM" ) );
    verifyMdiInputs( mdiNode, textOutputFieldsNode, "My Text file output [2]", ImmutableMap.of( "Field Name",
      "OUTPUT_FIELDNAME", "Trim Type", "" ) );
  }
}
