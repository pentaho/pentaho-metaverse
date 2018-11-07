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

package org.pentaho.metaverse.step;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.metaverse.frames.FileNode;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.StreamFieldNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.pentaho.dictionary.DictionaryConst.*;

@RunWith( PowerMockRunner.class )
@PrepareForTest( MetaverseConfig.class )
// TODO: Ignored for now, remove the @Ignore annotation once https://jira.pentaho.com/browse/ENGOPS-4612 is resolved
@Ignore
public class MdiValidationIT extends StepAnalyzerValidationIT {

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
    final List<FileNode> writesToNodes = IteratorUtils.toList( textFileOutputNode.getWritesToFileNodes().iterator() );
    assertEquals( 1, writesToNodes.size() );
    final FileNode fileNode = writesToNodes.get( 0 );
    assertTrue( String.format( "Path expected to end with '%s': %s", outputFileName, fileNode.getPath() ), fileNode
      .getPath().endsWith( outputFileName ) );

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

  private TransformationStepNode verifyMdiNode( final Map<String, FramedMetaverseNode> stepNodeMap,
                                                final TransformationNode templateSubTransNode,
                                                final String[] expectedOutputFieldNameArray ) {
    // verify that the Text Output and Text Output - Fields steps hot into the mdi step and that their output fields
    // input into the mdi step
    final TransformationStepNode mdiNode = (TransformationStepNode) stepNodeMap.get( "ETL Metadata Injection" );
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

    final String transNodeName = "injector_stream_same_as_mapping";
    final String subTransNodeName = "template_stream_same_as_mapping";
    initTest( transNodeName );

    final TransformationNode injectorTransNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( subTransNodeName, true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 35, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 96, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, subTransNodeName } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "File Name", "Separator", "Field Name", "Type",
        "Trim Type", "Dummy", "First Name", "First Name", "First Name", "First Name", "Last Name", "Last Name",
        "Last Name", "Last Name", "Age", "Age" } ),
      NODE_TYPE_STEP_PROPERTY, Arrays.asList( new String[] { "FILENAME", "SEPARATOR" } ) ) );

    // The injector should "contain" 4 step nodes and no virtual nodes
    final Map<String, FramedMetaverseNode> stepNodeMap = verifyTransformationSteps( injectorTransNode,
      new String[] { "Text Output - Fields", "Text Output", "ETL Metadata Injection", "Text file output" },
      false );

    // the template subTransformation should "contain" no step nodes (non-virtual) and one virtual nodes
    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps(
      subTransNode, new String[] { "My Text file output", "My Generate Rows" }, false );

    final TransformationStepNode textOutputFieldsNode = (TransformationStepNode) stepNodeMap.get(
      "Text Output - Fields" );
    final TransformationStepNode textOutputNode = (TransformationStepNode) stepNodeMap.get( "Text Output" );
    final TransformationStepNode metaInject = (TransformationStepNode) stepNodeMap.get( "ETL Metadata Injection" );
    final TransformationStepNode textFileOutput = (TransformationStepNode) stepNodeMap.get( "Text file output" );

    final TransformationStepNode myTextFileOutputNode = (TransformationStepNode) subTransStepNodeMap.get(
      "My Text file output" );

    // Text Output
    verifyNodes( IteratorUtils.toList( textOutputNode.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getNextSteps().iterator() ),
      testStepNode( metaInject.getName() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getOutputStreamFields().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ) );
    assertEquals( 2, getIterableSize( textOutputNode.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( textOutputNode.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( textOutputNode.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( textOutputNode.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( textOutputNode.getOutNodes( LINK_HOPSTO ) ) );

    // ETL Metadata injection
    // verify the mdi node and connections to the node
    assertEquals( metaInject, verifyMdiNode( stepNodeMap, subTransNode, new String[] { "First Name", "Last Name" } ) );

    verifyInjectorTextFileOutputNode( metaInject, "stream_same_as_mapping_injector.txt",
      new String[] { "First Name", "Last Name" }, myTextFileOutputNode );
    // verify all the expected mdi properties and links from fields to properties exist
    verifyMdiInputs( metaInject, textOutputNode, myTextFileOutputNode.getName(), ImmutableMap.of( "File Name",
      "FILENAME", "Separator", "SEPARATOR" ) );
    verifyMdiInputs( metaInject, textOutputFieldsNode, myTextFileOutputNode.getName(), ImmutableMap.of( "Field Name",
      "", "Trim Type", "", "Type", "", "Dummy", "" ) );

    verifyNodes( IteratorUtils.toList( metaInject.getPreviousSteps().iterator() ),
      testStepNode( textOutputNode.getName() ), testStepNode( textOutputFieldsNode.getName() ) );
    verifyNodes( IteratorUtils.toList( metaInject.getNextSteps().iterator() ),
      testStepNode( textFileOutput.getName() ) );
    verifyNodes( IteratorUtils.toList( metaInject.getInputStreamFields().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ), testFieldNode( "Field Name" ),
      testFieldNode( "Type" ), testFieldNode( "Trim Type" ), testFieldNode( "Dummy" ), testFieldNode( "First Name" ),
      testFieldNode( "Last Name" ), testFieldNode( "Age" ) );
    verifyNodes( IteratorUtils.toList( metaInject.getStreamFieldNodesUses().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ), testFieldNode( "Field Name" ),
      testFieldNode( "Trim Type" ) );
    verifyNodes( IteratorUtils.toList( metaInject.getOutputStreamFields().iterator() ),
      testFieldNode( "First Name" ), testFieldNode( "Last Name" ) );
    assertEquals( 13, getIterableSize( metaInject.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( metaInject.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( metaInject.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 8, getIterableSize( metaInject.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( metaInject.getOutNodes( LINK_HOPSTO ) ) );
    assertEquals( 1, getIterableSize( metaInject.getOutNodes( LINK_EXECUTES ) ) );

    // Verify the following chains:
    // Chain 1: Text Output > outputs First Name > populates > FILENAME
    // Chain 2: My Text file output > contains > FILENAME
    final FramedMetaverseNode textOutput_output_fileName = verifyLinkedNode(
      textOutputNode, LINK_OUTPUTS, "File Name" );
    final FramedMetaverseNode textOutput_property_fileName = verifyLinkedNode( textOutput_output_fileName,
      LINK_POPULATES, "FILENAME" );
    assertEquals( textOutput_property_fileName, verifyLinkedNode( myTextFileOutputNode, LINK_CONTAINS, "FILENAME" ) );

    // Verify the following chains:
    // - Text Output - Fields > outputs > Field Name > derives > My Text file output:First Name > inputs > ETL Metadata
    //   injection
    // - Text Output - Fields > outputs > Type > derives > My Text file output:Last Name > inputs > ETL Metadata
    //   injection
    // - Text Output - Fields > outputs > Trim Type > derives > My Text file output:Age > inputs > ETL Metadata
    //   injection
    final FramedMetaverseNode textOutputFields_output_fieldName = verifyLinkedNode(
      textOutputFieldsNode, LINK_OUTPUTS, "Field Name" );
    final FramedMetaverseNode myTextFileOutput_output_firstName = verifyLinkedNode(
      myTextFileOutputNode, LINK_OUTPUTS, "First Name" );
    assertEquals( myTextFileOutput_output_firstName, verifyLinkedNode( textOutputFields_output_fieldName, LINK_DERIVES,
      "First Name" ) );
    assertEquals( metaInject, verifyLinkedNode( myTextFileOutput_output_firstName,
      LINK_INPUTS, metaInject.getName() ) );

    final FramedMetaverseNode textOutputFields_output_type = verifyLinkedNode(
      textOutputFieldsNode, LINK_OUTPUTS, "Type" );
    final FramedMetaverseNode myTextFileOutput_output_lastName = verifyLinkedNode(
      myTextFileOutputNode, LINK_OUTPUTS, "Last Name" );
    assertEquals( myTextFileOutput_output_lastName, verifyLinkedNode( textOutputFields_output_type, LINK_DERIVES,
      "Last Name" ) );
    assertEquals( metaInject, verifyLinkedNode( myTextFileOutput_output_lastName,
      LINK_INPUTS, metaInject.getName() ) );

    final FramedMetaverseNode textOutputFields_output_trimType = verifyLinkedNode(
      textOutputFieldsNode, LINK_OUTPUTS, "Trim Type" );
    final FramedMetaverseNode myTextFileOutput_output_age = verifyLinkedNode(
      myTextFileOutputNode, LINK_OUTPUTS, "Age" );
    assertEquals( myTextFileOutput_output_age, verifyLinkedNode( textOutputFields_output_trimType, LINK_DERIVES,
      "Age" ) );
    assertEquals( metaInject, verifyLinkedNode( myTextFileOutput_output_age,
      LINK_INPUTS, metaInject.getName() ) );

    // Verify the following chains:
    // - My Text file output:First Name > derives > ETL Metadata injection:First Name > inputs > Text file output >
    // outputs First Name
    final FramedMetaverseNode metaInject_output_firstName = verifyLinkedNode(
      metaInject, LINK_OUTPUTS, "First Name" );
    assertEquals( metaInject_output_firstName, verifyLinkedNode( myTextFileOutput_output_firstName, LINK_DERIVES,
      "First Name" ) );
    assertEquals( textFileOutput, verifyLinkedNode( metaInject_output_firstName, LINK_INPUTS,
      textFileOutput.getName() ) );

    // validate properties
    verifyNodeProperties( metaInject, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP ).put( "runResultingTransformation", "true" )
      .put( "streamTargetStepname", SKIP ).put( "streamSourceStepname", SKIP ).put( "targetFile", SKIP )
      .put( "sourceStepName", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "mapping [1],ignored mapping [1],mapping [2],ignored mapping [2]" )
      .put( "mapping [1]", "Text Output: Separator > [template_stream_same_as_mapping] My Text file output: SEPARATOR" )
      .put( "mapping [2]", "Text Output: File Name > [template_stream_same_as_mapping] My Text file output: FILENAME" )
      .put( "ignored mapping [1]", "Text Output - Fields: Trim Type > [template_stream_same_as_mapping] My Text file "
        + "output: OUTPUT_TRIM" )
      .put( "ignored mapping [2]", "Text Output - Fields: Field Name > [template_stream_same_as_mapping] My Text file"
        + " output: OUTPUT_FIELDNAME" ).build() );
  }

  @Test
  public void testMdiInjectorStreamReadingAndStreamingDifferentSteps() throws Exception {

    final String transNodeName = "injector_stream_different_than_mapping";
    final String subTransNodeName = "template_stream_different_than_mapping";
    initTest( transNodeName );

    final TransformationNode injectorTransNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( subTransNodeName, true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 40, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 113, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, subTransNodeName } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "File Name", "Separator", "Field Name", "Type",
        "Trim Type", "Dummy", "First Name", "First Name", "First Name", "First Name", "First Name", "Last Name",
        "Last Name", "Last Name", "Last Name", "Last Name", "Age", "Age", "Age" } ),
      NODE_TYPE_STEP_PROPERTY, Arrays.asList( new String[] { "FILENAME", "SEPARATOR" } ) ) );

    // The injector should "contain" 4 step nodes and no virtual nodes
    final Map<String, FramedMetaverseNode> stepNodeMap = verifyTransformationSteps( injectorTransNode,
      new String[] { "Text Output - Fields", "Text Output", "ETL Metadata Injection", "Text file output" },
      false );

    // the template subTransformation should "contain" no step nodes (non-virtual) and one virtual nodes
    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps(
      subTransNode, new String[] { "My Text file output", "My Text file output [2]", "My Generate Rows" }, false );

    final TransformationStepNode textOutputFieldsNode = (TransformationStepNode) stepNodeMap.get(
      "Text Output - Fields" );
    final TransformationStepNode textOutputNode = (TransformationStepNode) stepNodeMap.get( "Text Output" );
    final TransformationStepNode metaInject = (TransformationStepNode) stepNodeMap.get( "ETL Metadata Injection" );
    final TransformationStepNode textFileOutput = (TransformationStepNode) stepNodeMap.get( "Text file output" );

    final TransformationStepNode myTextFileOutputNode = (TransformationStepNode) subTransStepNodeMap.get(
      "My Text file output" );
    final TransformationStepNode myTextFileOutputNode2 = (TransformationStepNode) subTransStepNodeMap.get(
      "My Text file output [2]" );

    // Text Output
    verifyNodes( IteratorUtils.toList( textOutputNode.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getNextSteps().iterator() ),
      testStepNode( metaInject.getName() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getOutputStreamFields().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ) );
    assertEquals( 2, getIterableSize( textOutputNode.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( textOutputNode.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( textOutputNode.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( textOutputNode.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( textOutputNode.getOutNodes( LINK_HOPSTO ) ) );

    // ETL Metadata injection
    // verify the mdi node and connections to the node
    assertEquals( metaInject, verifyMdiNode( stepNodeMap, subTransNode, new String[] { "First Name", "Last Name" } ) );

    verifyInjectorTextFileOutputNode( metaInject, "stream_different_than_mapping_injector.txt",
      new String[] { "First Name", "Last Name" }, myTextFileOutputNode );
    // verify all the expected mdi properties and links from fields to properties exist
    verifyMdiInputs( metaInject, textOutputNode, myTextFileOutputNode.getName(),
      ImmutableMap.of( "File Name", "FILENAME",
        "Separator", "SEPARATOR" ) );
    verifyMdiInputs( metaInject, textOutputFieldsNode, myTextFileOutputNode.getName(), ImmutableMap.of( "Field Name",
      "", "Trim Type", "", "Type", "", "Dummy", "" ) );

    verifyNodes( IteratorUtils.toList( metaInject.getPreviousSteps().iterator() ),
      testStepNode( textOutputNode.getName() ), testStepNode( textOutputFieldsNode.getName() ) );
    verifyNodes( IteratorUtils.toList( metaInject.getNextSteps().iterator() ),
      testStepNode( textFileOutput.getName() ) );
    verifyNodes( IteratorUtils.toList( metaInject.getInputStreamFields().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ), testFieldNode( "Field Name" ),
      testFieldNode( "Type" ), testFieldNode( "Trim Type" ), testFieldNode( "Dummy" ), testFieldNode( "First Name" ),
      testFieldNode( "Last Name" ), testFieldNode( "Age" ) );
    verifyNodes( IteratorUtils.toList( metaInject.getStreamFieldNodesUses().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ), testFieldNode( "Field Name" ),
      testFieldNode( "Trim Type" ) );
    verifyNodes( IteratorUtils.toList( metaInject.getOutputStreamFields().iterator() ),
      testFieldNode( "First Name" ), testFieldNode( "Last Name" ) );
    assertEquals( 13, getIterableSize( metaInject.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( metaInject.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( metaInject.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 8, getIterableSize( metaInject.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( metaInject.getOutNodes( LINK_HOPSTO ) ) );
    assertEquals( 1, getIterableSize( metaInject.getOutNodes( LINK_EXECUTES ) ) );

    // Verify the following chains:
    // Chain 1: Text Output > outputs First Name > populates > FILENAME
    // Chain 2: My Text file output > contains > FILENAME
    final FramedMetaverseNode textOutput_output_fileName = verifyLinkedNode(
      textOutputNode, LINK_OUTPUTS, "File Name" );
    final FramedMetaverseNode textOutput_property_fileName = verifyLinkedNode( textOutput_output_fileName,
      LINK_POPULATES, "FILENAME" );
    assertEquals( textOutput_property_fileName, verifyLinkedNode( myTextFileOutputNode, LINK_CONTAINS, "FILENAME" ) );

    // Verify the following chains:
    // - Text Output - Fields > outputs > Field Name > derives My Text file output [2]: First Name
    // - My Text file output: First Name > inputs > ETL Metadata injection
    // - My Text file output: First Name > derives > ETL Metadata injection:First Name
    final FramedMetaverseNode textOutputFields_output_fieldName = verifyLinkedNode(
      textOutputFieldsNode, LINK_OUTPUTS, "Field Name" );
    final FramedMetaverseNode myTextFileOutput_output_firstName = verifyLinkedNode(
      myTextFileOutputNode, LINK_OUTPUTS, "First Name" );
    final FramedMetaverseNode myTextFileOutput2_output_firstName = verifyLinkedNode(
      myTextFileOutputNode2, LINK_OUTPUTS, "First Name" );
    final FramedMetaverseNode metaInject_output_firstName = verifyLinkedNode( metaInject, LINK_OUTPUTS, "First Name" );
    assertEquals( myTextFileOutput2_output_firstName, verifyLinkedNode(
      textOutputFields_output_fieldName, LINK_DERIVES, "First Name" ) );
    assertEquals( metaInject, verifyLinkedNode( myTextFileOutput_output_firstName,
      LINK_INPUTS, metaInject.getName() ) );
    assertEquals( metaInject_output_firstName, verifyLinkedNode( myTextFileOutput_output_firstName,
      LINK_DERIVES, "First Name" ) );

    // validate properties
    verifyNodeProperties( metaInject, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP ).put( "runResultingTransformation", "true" )
      .put( "streamTargetStepname", SKIP ).put( "streamSourceStepname", SKIP ).put( "targetFile", SKIP )
      .put( "sourceStepName", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "mapping [1],ignored mapping [1],mapping [2],ignored mapping [2]" )
      .put( "mapping [1]", "Text Output: Separator > [template_stream_different_than_mapping] My Text file output: SEPARATOR" )
      .put( "mapping [2]", "Text Output: File Name > [template_stream_different_than_mapping] My Text file output: FILENAME" )
      .put( "ignored mapping [1]", "Text Output - Fields: Trim Type > [template_stream_different_than_mapping]"
        + " My Text file output: OUTPUT_TRIM" )
      .put( "ignored mapping [2]", "Text Output - Fields: Field Name > [template_stream_different_than_mapping]"
        + " My Text file output: OUTPUT_FIELDNAME" ).build() );
  }

  @Test
  public void testMdiInjectorNoStreaMapTwoSteps() throws Exception {

    final String transNodeName = "injector_no_stream_map_2_steps";
    final String subTransNodeName = "template_no_stream_map_2_steps";
    initTest( transNodeName );

    final TransformationNode injectorTransNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( subTransNodeName, true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 38, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 99, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, subTransNodeName } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "File Name", "Separator", "Field Name", "Trim Type",
        "First Name", "First Name", "First Name", "Last Name", "Last Name", "Last Name", "Age", "Age", "Age" } ),
      NODE_TYPE_STEP_PROPERTY, Arrays.asList( new String[] { "FILENAME", "SEPARATOR", "SEPARATOR", "OUTPUT_FIELDNAME",
        "OUTPUT_FIELDNAME", "OUTPUT_TRIM" } ) ) );

    // The injector should "contain" 4 step nodes and no virtual nodes
    final Map<String, FramedMetaverseNode> stepNodeMap = verifyTransformationSteps( injectorTransNode,
      new String[] { "Text Output - Fields", "Text Output", "ETL Metadata Injection", "Text file output" },
      false );

    // the template subTransformation should "contain" no step nodes (non-virtual) and one virtual nodes
    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps(
      subTransNode, new String[] { "My Text file output", "My Text file output [2]", "My Generate Rows" }, false );

    final TransformationStepNode textOutputFieldsNode = (TransformationStepNode) stepNodeMap.get(
      "Text Output - Fields" );
    final TransformationStepNode textOutputNode = (TransformationStepNode) stepNodeMap.get( "Text Output" );
    final TransformationStepNode metaInject = (TransformationStepNode) stepNodeMap.get( "ETL Metadata Injection" );
    final TransformationStepNode textFileOutput = (TransformationStepNode) stepNodeMap.get( "Text file output" );

    final TransformationStepNode myTextFileOutputNode = (TransformationStepNode) subTransStepNodeMap.get(
      "My Text file output" );
    final TransformationStepNode myTextFileOutputNode2 = (TransformationStepNode) subTransStepNodeMap.get(
      "My Text file output [2]" );

    // Text Output
    verifyNodes( IteratorUtils.toList( textOutputNode.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getNextSteps().iterator() ),
      testStepNode( metaInject.getName() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( textOutputNode.getOutputStreamFields().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ) );
    assertEquals( 2, getIterableSize( textOutputNode.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( textOutputNode.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( textOutputNode.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( textOutputNode.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( textOutputNode.getOutNodes( LINK_HOPSTO ) ) );

    // ETL Metadata injection
    // verify the mdi node and connections to the node - since we are not streaming, the mdi node should have no
    // output fields
    assertEquals( metaInject, verifyMdiNode( stepNodeMap, subTransNode, new String[] {} ) );

    verifyInjectorTextFileOutputNode( metaInject, "injector_no_stream_map_2_steps.txt", new String[] {},
      myTextFileOutputNode );
    // verify all the expected mdi properties and links from fields to properties exist
    verifyMdiInputs( metaInject, textOutputNode, myTextFileOutputNode.getName(),
      ImmutableMap.of( "File Name", "FILENAME",
        "Separator", "SEPARATOR" ) );
    verifyMdiInputs( metaInject, textOutputFieldsNode, myTextFileOutputNode.getName(), ImmutableMap.of( "Field Name",
      "OUTPUT_FIELDNAME", "Trim Type", "OUTPUT_TRIM" ) );

    verifyNodes( IteratorUtils.toList( metaInject.getPreviousSteps().iterator() ),
      testStepNode( textOutputNode.getName() ), testStepNode( textOutputFieldsNode.getName() ) );
    verifyNodes( IteratorUtils.toList( metaInject.getNextSteps().iterator() ),
      testStepNode( textFileOutput.getName() ) );
    verifyNodes( IteratorUtils.toList( metaInject.getInputStreamFields().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ), testFieldNode( "Field Name" ),
      testFieldNode( "Trim Type" ) );
    verifyNodes( IteratorUtils.toList( metaInject.getStreamFieldNodesUses().iterator() ),
      testFieldNode( "File Name" ), testFieldNode( "Separator" ), testFieldNode( "Field Name" ),
      testFieldNode( "Trim Type" ) );
    verifyNodes( IteratorUtils.toList( metaInject.getOutputStreamFields().iterator() ) );
    assertEquals( 8, getIterableSize( metaInject.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( metaInject.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( metaInject.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 6, getIterableSize( metaInject.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( metaInject.getOutNodes( LINK_HOPSTO ) ) );
    assertEquals( 1, getIterableSize( metaInject.getOutNodes( LINK_EXECUTES ) ) );

    // Verify the following chains:
    // Chain 1: Text Output > outputs > First Name > populates > My Text file output:FILENAME
    // Chain 2: My Text file output > contains > FILENAME
    // Chain 3: Text Output > outputs > Separator > populates > My Text file output:SEPARATOR (x2)
    final FramedMetaverseNode textOutput_output_fileName = verifyLinkedNode(
      textOutputNode, LINK_OUTPUTS, "File Name" );
    final FramedMetaverseNode myTextFileOutput_property_fileName = verifyLinkedNode( myTextFileOutputNode,
      LINK_CONTAINS, "FILENAME" );
    assertEquals( myTextFileOutput_property_fileName, verifyLinkedNode( textOutput_output_fileName,
      LINK_POPULATES, "FILENAME" ) );
    final FramedMetaverseNode textOutput_output_separator = verifyLinkedNode(
      textOutputNode, LINK_OUTPUTS, "Separator" );
    final FramedMetaverseNode myTextFileOutput_property_separator = verifyLinkedNode( myTextFileOutputNode,
      LINK_CONTAINS, "SEPARATOR" );
    final List<FramedMetaverseNode> textOutput_property_separators = verifyLinkedNodes( textOutput_output_separator,
      LINK_POPULATES, "SEPARATOR" );
    assertEquals( 2, textOutput_property_separators.size() );
    assertTrue( textOutput_property_separators.contains( myTextFileOutput_property_separator ) );

    // Verify the following chains:
    // Chain 1: Text Output - Fields > outputs > First Name > populates > My Text file output [2]:OUTPUT_FIELDNAME (x2)
    // Chain 2: My Text file output [2] > contains > OUTPUT_FIELDNAME
    final FramedMetaverseNode textOutputFields_output_fieldName = verifyLinkedNode(
      textOutputFieldsNode, LINK_OUTPUTS, "Field Name" );
    final FramedMetaverseNode myTextFileOutput_property_fieldName = verifyLinkedNode( myTextFileOutputNode,
      LINK_CONTAINS, "OUTPUT_FIELDNAME" );
    final List<FramedMetaverseNode> textOutputFields_property_fieldNames = verifyLinkedNodes(
      textOutputFields_output_fieldName, LINK_POPULATES, "OUTPUT_FIELDNAME" );
    assertEquals( 2, textOutputFields_property_fieldNames.size() );
    assertTrue( textOutputFields_property_fieldNames.contains( myTextFileOutput_property_fieldName ) );

    // validate properties
    verifyNodeProperties( metaInject, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP ).put( "runResultingTransformation", "true" )
      .put( "targetFile", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "mapping [1],mapping [2],mapping [3],mapping [4],mapping [5],mapping [6]" )
      .put( "mapping [1]", "Text Output: Separator > [template_no_stream_map_2_steps] My Text file output: SEPARATOR" )
      .put( "mapping [2]", "Text Output: Separator > [template_no_stream_map_2_steps] My Text file output [2]: "
        + "SEPARATOR" )
      .put( "mapping [3]", "Text Output - Fields: Trim Type > [template_no_stream_map_2_steps] My Text file output: "
        + "OUTPUT_TRIM" )
      .put( "mapping [4]", "Text Output - Fields: Field Name > [template_no_stream_map_2_steps] My Text file output: "
        + "OUTPUT_FIELDNAME" )
      .put( "mapping [5]", "Text Output: File Name > [template_no_stream_map_2_steps] My Text file output: FILENAME" )
      .put( "mapping [6]", "Text Output - Fields: Field Name > [template_no_stream_map_2_steps] My Text file output "
        + "[2]: OUTPUT_FIELDNAME" ).build() );
  }
}
