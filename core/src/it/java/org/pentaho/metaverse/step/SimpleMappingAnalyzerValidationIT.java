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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.pentaho.dictionary.DictionaryConst.*;

@RunWith( PowerMockRunner.class )
@PrepareForTest( MetaverseConfig.class )
public class SimpleMappingAnalyzerValidationIT extends StepAnalyzerValidationIT {


  @Test
  public void testMoreThanOneIOstepsMappingOut() throws Exception {

    final String transNodeName = "simple_moreThanOneIOstepsMappingOut";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode mainSubTransNode = verifyTransformationNode( "simple_sub", true );
    final TransformationNode subTransNode = verifyTransformationNode( "simple_sub", false );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 44, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 125, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "simple_sub", "simple_sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "randomValue", "randomValue", "randomValue", "randomValue",
        "randomValue", "value", "value", "value", "value", "value", "parity", "parity", "parity", "parity",
        "newParity", "newParity", "newParity", "newParity", "ParityFoo", "ParityFoo", "ParityFoo", "ParityFoo",
        "ParityFoo", "ParityFoo" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate Rows", "Generate random integer", "Simple mapping (sub-transformation)",
       "Write to log Checksum", "Write to log Parity", "Write to log Dummy" }, false );

    final Map<String, FramedMetaverseNode> parentVirtualStepNodeMap = verifyTransformationSteps( mainSubTransNode,
      new String[] { "<mapping_output_specification>", "<mapping_input_specification>" }, true );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input parity", "output parity", "calc parity", "Add constants" }, false );

    final TransformationStepNode generateRows = (TransformationStepNode) parentStepNodeMap.get( "Generate Rows" );
    final TransformationStepNode generateRandomInt = (TransformationStepNode) parentStepNodeMap.get(
      "Generate random integer" );
    final TransformationStepNode simpleMapping = (TransformationStepNode) parentStepNodeMap.get(
      "Simple mapping (sub-transformation)" );
    final TransformationStepNode writeToLogParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Parity" );
    final TransformationStepNode writeToLogDummy = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Dummy" );

    // virtual sub-trans nodes within the parent graph
    final TransformationStepNode inputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "<mapping_input_specification>" );
    final TransformationStepNode outputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "<mapping_output_specification>" );

    // ---------- Generate Rows
    verifyNodes( IteratorUtils.toList( generateRows.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( generateRows.getNextSteps().iterator() ),
      testStepNode( generateRandomInt.getName(), false ) );
    verifyNodes( IteratorUtils.toList( generateRows.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( generateRows.getOutputStreamFields().iterator() ) );
    assertEquals( 2, getIterableSize( generateRows.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRows.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRows.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 1, getIterableSize( generateRows.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( generateRows.getOutNodes( LINK_HOPSTO ) ) );

    // ---------- Generate Random Int
    verifyNodes( IteratorUtils.toList( generateRandomInt.getPreviousSteps().iterator() ),
      testLineageNode( generateRows ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getNextSteps().iterator() ),
      testLineageNode( simpleMapping ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getOutputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    assertEquals( 3, getIterableSize( generateRandomInt.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllOutNodes() ) );

    // ---------- "Simple mapping (sub-transformation)
    verifyNodes( IteratorUtils.toList( simpleMapping.getPreviousSteps().iterator() ),
      testLineageNode( generateRandomInt ) );
    verifyNodes( IteratorUtils.toList( simpleMapping.getInputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    verifyNodes( IteratorUtils.toList( simpleMapping.getOutputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ), testFieldNode( "ParityFoo", false ), testFieldNode(
        "newParity", false ) );
    assertEquals( 4, getIterableSize( simpleMapping.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( simpleMapping.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( simpleMapping.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 7, getIterableSize( simpleMapping.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( simpleMapping.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( simpleMapping, new ImmutableMap.Builder<String, Object>()
      .put( "stepType", SKIP ).put( "color", SKIP ).put( "pluginId", SKIP ).put( "type", SKIP ).put( "_analyzer", SKIP )
      .put( "category", SKIP ).put( "copies", SKIP ).put( "logicalId", SKIP ).put( "name", SKIP )
      .put( "namespace", SKIP ).put( "virtual", SKIP )
      .put( "verboseDetails", "input [1],input [1] update field names,input [1] rename [1],output [1],output [1] "
        + "update field names,output [1] rename [1]" )
      .put( "input [1]", "Generate random integer > [simple_sub] <mapping_input_specification>" )
      .put( "input [1] update field names", "true" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "output [1]", "[simple_sub] <mapping_output_specification> > Write to log Parity" )
      .put( "output [1] rename [1]", "parity > newParity" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- output parity
    verifyNodes( IteratorUtils.toList( outputParity.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getOutputStreamFields().iterator() ),
      testFieldNode( "parity", true ) );
    assertEquals( 2, getIterableSize( outputParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 1, getIterableSize( outputParity.getAllOutNodes() ) );

    // Verify the following chain of links
    // chain 1: Generate Random Int > outputs > randomValue >  inputs > Input checksum > outputs > value > derives >
    // calc checksum & parity:randomValue
    // chain 2: Generate Random Int > outputs > randomValue >  derives > Input checksum:value
    final FramedMetaverseNode generateRandomIntOutRandomValue =
      verifyLinkedNode( generateRandomInt, LINK_OUTPUTS, "randomValue" );
    final FramedMetaverseNode inputChecksumOutValue =
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_DERIVES, "value" );
    final FramedMetaverseNode calcChecksumAndParityOutRandomValue =
      verifyLinkedNode( simpleMapping, LINK_OUTPUTS, "randomValue" );
    assertEquals( calcChecksumAndParityOutRandomValue, verifyLinkedNode(
      inputChecksumOutValue, LINK_DERIVES, "randomValue" ) );

    // Verify the following chain of links
    // chain 1: Generate Random Int > outputs > randomValue >  inputs > Input parity > outputs > value > derives >
    // calc checksum & parity:randomValue
    // chain 2: Generate Random Int > outputs > randomValue >  derives > Input parity:value
    assertEquals( inputParity,
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_INPUTS, inputParity.getName() ) );
    final FramedMetaverseNode inputParityOutValue =
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_DERIVES, "value" );
    assertEquals( calcChecksumAndParityOutRandomValue, verifyLinkedNode(
      inputParityOutValue, LINK_DERIVES, "randomValue" ) );

    // Verify the following chain of links
    // chain 1 : [sub] output parity > outputs > parity > derives > calc parity & checksum:newChecksum > inputs >
    // Write to file Parity
    // chain 1 : [sub] output parity > outputs > parity > derives > calc parity & checksum:newChecksum > derives >
    // Write to file Parity:newParity
    final FramedMetaverseNode outputParityOutParity =
      verifyLinkedNode( outputParity, LINK_OUTPUTS, "parity" );
    final FramedMetaverseNode calcChecksumAndParityOutNewParity =
      verifyLinkedNode( simpleMapping, LINK_OUTPUTS, "newParity" );
    assertEquals( calcChecksumAndParityOutNewParity, verifyLinkedNode( outputParityOutParity, LINK_DERIVES,
      "newParity" ) );
  }
}
