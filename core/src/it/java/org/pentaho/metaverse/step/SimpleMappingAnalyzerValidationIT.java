/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.pentaho.dictionary.DictionaryConst.*;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( MetaverseConfig.class )
public class SimpleMappingAnalyzerValidationIT extends StepAnalyzerValidationIT {

  private static final String VALUE = "value";
  private static final String RANDOM_VALUE = "randomValue";
  private static final String PARITY = "parity";
  private static final String NEW_PARITY = "newParity";

  @Test
  public void testMoreThanOneIOstepsMappingOut() throws Exception {

    final String transNodeName = "simple_moreThanOneIOstepsMappingOut";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "simple_sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 21, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 53, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "simple_sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_VALUE, RANDOM_VALUE, VALUE, VALUE, VALUE,
        PARITY, PARITY, NEW_PARITY } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate random integer", "Simple mapping (sub-transformation)", "Write to log Parity" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input parity", "output parity", "calc parity" }, false );

    final TransformationStepNode generateRandomInt = (TransformationStepNode) parentStepNodeMap.get(
      "Generate random integer" );
    final TransformationStepNode simpleMapping = (TransformationStepNode) parentStepNodeMap.get(
      "Simple mapping (sub-transformation)" );
    final TransformationStepNode writeToLogParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Parity" );

    // virtual sub-trans nodes within the parent graph
    final TransformationStepNode inputParity = (TransformationStepNode) subTransStepNodeMap.get(
      "Input parity" );
    final TransformationStepNode calcParity = (TransformationStepNode) subTransStepNodeMap.get(
      "calc parity" );
    final TransformationStepNode outputParity = (TransformationStepNode) subTransStepNodeMap.get(
      "output parity" );

    // ---------- Generate Random Int
    verifyNodes( IteratorUtils.toList( generateRandomInt.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getNextSteps().iterator() ),
      testLineageNode( simpleMapping ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getOutputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE, false ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllOutNodes() ) );

    // ---------- "Simple mapping (sub-transformation)
    verifyNodes( IteratorUtils.toList( simpleMapping.getPreviousSteps().iterator() ),
      testLineageNode( generateRandomInt ) );
    verifyNodes( IteratorUtils.toList( simpleMapping.getInputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE, false ) );
    verifyNodes( IteratorUtils.toList( simpleMapping.getOutputStreamFields().iterator() ) );
    assertEquals( 4, getIterableSize( simpleMapping.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( simpleMapping.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( simpleMapping.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( simpleMapping.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( simpleMapping.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( simpleMapping, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( PROPERTY_PATH, SKIP ).put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] update field names,input [1] rename [1],output [1],output [1] "
        + "update field names,output [1] rename [1]" )
      .put( "input [1]", "Generate random integer > [simple_sub] Input parity" )
      .put( "input [1] update field names", "true" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "output [1]", "[simple_sub] output parity > Write to log Parity" )
      .put( "output [1] rename [1]", "parity > newParity" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- output parity
    verifyNodes( IteratorUtils.toList( outputParity.getPreviousSteps().iterator() ),
      testStepNode( calcParity.getName(), false ) );
    verifyNodes( IteratorUtils.toList( outputParity.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getInputStreamFields().iterator() ),
      testFieldNode( PARITY, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( outputParity.getOutputStreamFields().iterator() ),
      testFieldNode( PARITY, false ), testFieldNode( VALUE, false ) );
    assertEquals( 5, getIterableSize( outputParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( outputParity.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Generate Random Int > outputs > randomValue >  inputs > Input parity > outputs > value > inputs >
    //   calc parity > outputs > value > inputs > output parity > outputs > value > inputs
    //   > Write to log Parity > outputs > randomValue
    // - chain 2: Generate Random Int > outputs > randomValue > derives > Input Parity:value > derives
    //   > calc parity:value > derives > output parity:value > derives > Write to log Parity:randomValue
    final FramedMetaverseNode generateRandomInt_output_randomValue =
      verifyLinkedNode( generateRandomInt, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputParity_output_value = verifyLinkedNode( inputParity, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcParity_output_value = verifyLinkedNode( calcParity, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputParity_output_value = verifyLinkedNode( outputParity, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogParity_output_randomValue =
      verifyLinkedNode( writeToLogParity, LINK_OUTPUTS, RANDOM_VALUE );

    assertEquals( inputParity,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_INPUTS, inputParity.getName() ) );
    assertEquals( calcParity,
      verifyLinkedNode( inputParity_output_value, LINK_INPUTS, calcParity.getName() ) );
    assertEquals( outputParity,
      verifyLinkedNode( calcParity_output_value, LINK_INPUTS, outputParity.getName() ) );
    assertEquals( writeToLogParity,
      verifyLinkedNode( outputParity_output_value, LINK_INPUTS, writeToLogParity.getName() ) );

    assertEquals( inputParity_output_value,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_DERIVES, VALUE ) );
    assertEquals( calcParity_output_value, verifyLinkedNode( inputParity_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( outputParity_output_value, verifyLinkedNode( calcParity_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( writeToLogParity_output_randomValue,
      verifyLinkedNode( outputParity_output_value, LINK_DERIVES, RANDOM_VALUE ) );

    // Verify the following link chains
    // - chain 1: calc parity > outputs > parity > inputs > output parity > outputs > parity > inputs
    //   > Write to log Parity > outputs > newParity
    // - chain 2: calc parity:parity > derives > output parity:parity > derives > Write to log
    //   Parity:newParity
    final FramedMetaverseNode calcParity_output_parity =
      verifyLinkedNode( calcParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode outputParity_output_parity =
      verifyLinkedNode( outputParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode writeToLogParity_output_newParity =
      verifyLinkedNode( writeToLogParity, LINK_OUTPUTS, NEW_PARITY );

    assertEquals( outputParity,
      verifyLinkedNode( calcParity_output_parity, LINK_INPUTS, outputParity.getName() ) );
    assertEquals( writeToLogParity,
      verifyLinkedNode( outputParity_output_parity, LINK_INPUTS, writeToLogParity.getName() ) );

    assertEquals( outputParity_output_parity,
      verifyLinkedNode( calcParity_output_parity, LINK_DERIVES, PARITY ) );
    assertEquals( writeToLogParity_output_newParity, verifyLinkedNode( outputParity_output_parity,
      LINK_DERIVES, NEW_PARITY ) );
  }
}
