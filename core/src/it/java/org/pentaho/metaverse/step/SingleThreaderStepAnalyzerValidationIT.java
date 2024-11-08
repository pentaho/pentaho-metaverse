/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS;
import static org.pentaho.dictionary.DictionaryConst.LINK_DERIVES;
import static org.pentaho.dictionary.DictionaryConst.LINK_EXECUTES;
import static org.pentaho.dictionary.DictionaryConst.LINK_OUTPUTS;
import static org.pentaho.dictionary.DictionaryConst.LINK_TYPE_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_TRANS;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_TRANS_FIELD;

@Ignore
public class SingleThreaderStepAnalyzerValidationIT extends StepAnalyzerValidationIT {

  @Test
  public void testOneOIWithMappings() throws Exception {

    final String transNodeName = "parent-single-threader";
    final String subTransNodeName = "sub-trans";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( subTransNodeName, true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 30, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 87, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, subTransNodeName } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "strTest", "strTest", "strTest", "strTest", "strTest",
        "strTest", "strTest", "strTest", "counter", "counter", "counter", "counter", "counter", "counter",
        "counter" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "Add constants", "Single threader", "Write to log", "Write to log 2" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Mapping input specification", "String operations", "Mapping output specification", }, false );

    final TransformationStepNode singleThreader = (TransformationStepNode) parentStepNodeMap.get(
      "Single threader" );
    final TransformationStepNode writeToLog = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log" );
    final TransformationStepNode writeToLog2 = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log 2" );

    final TransformationStepNode inputSpec = (TransformationStepNode) subTransStepNodeMap.get(
      "Mapping input specification" );
    final TransformationStepNode stringOperations = (TransformationStepNode) subTransStepNodeMap.get(
      "String operations" );
    final TransformationStepNode outputSpec = (TransformationStepNode) subTransStepNodeMap.get(
      "Mapping output specification" );

    // ---------- Generate Random Int
    verifyNodes( IteratorUtils.toList( singleThreader.getPreviousSteps().iterator() ), testStepNode( "Add constants" ) );
    verifyNodes( IteratorUtils.toList( singleThreader.getNextSteps().iterator() ),
      testStepNode( writeToLog.getName() ), testStepNode( writeToLog2.getName() ) );
    verifyNodes( IteratorUtils.toList( singleThreader.getInputStreamFields().iterator() ),
      testFieldNode( "counter", false ), testFieldNode( "strTest", false ) );
    verifyNodes( IteratorUtils.toList( singleThreader.getOutputStreamFields().iterator() ),
      testFieldNode( "counter", false ), testFieldNode( "strTest", false ) );
    assertEquals( 5, getIterableSize( singleThreader.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( singleThreader.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( singleThreader.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 5, getIterableSize( singleThreader.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( singleThreader.getOutNodes( LINK_EXECUTES ) ) );

    // verify the following link chain:
    // Single threader > outputs > counter > derives > Mapping input specification:counter > derives >
    // String operations:counter > Mapping output specification:counter > derives > Single threader:counter
    final FramedMetaverseNode singleThreader_output_counter =
      verifyLinkedNode( singleThreader, LINK_OUTPUTS, "counter" );
    final FramedMetaverseNode inputSpec_output_counter = verifyLinkedNode( inputSpec, LINK_OUTPUTS, "counter" );
    final FramedMetaverseNode stringOperations_output_counter = verifyLinkedNode( stringOperations, LINK_OUTPUTS,
      "counter" );
    final FramedMetaverseNode outputSpec_output_counter = verifyLinkedNode( outputSpec, LINK_OUTPUTS, "counter" );
    assertEquals( inputSpec_output_counter,
      verifyLinkedNode( singleThreader_output_counter, LINK_DERIVES, "counter" ) );
    assertEquals( stringOperations_output_counter,
      verifyLinkedNode( inputSpec_output_counter, LINK_DERIVES, "counter" ) );
    assertEquals( outputSpec_output_counter,
      verifyLinkedNode( stringOperations_output_counter, LINK_DERIVES, "counter" ) );
    assertEquals( singleThreader_output_counter,
      verifyLinkedNode( outputSpec_output_counter, LINK_DERIVES, "counter" ) );
  }
}
