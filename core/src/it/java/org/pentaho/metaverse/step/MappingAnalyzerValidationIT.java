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
import org.pentaho.metaverse.step.StepAnalyzerValidationIT;
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
public class MappingAnalyzerValidationIT extends StepAnalyzerValidationIT {

  @Test
  public void testMoreThanOneIOstepsMappingOut() throws Exception {

    final String transNodeName = "moreThanOneIOstepsMappingOut";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode mainSubTransNode = verifyTransformationNode( "sub", true );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", false );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 54, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 147, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub", "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "randomValue", "randomValue", "randomValue",
        "randomValue", "randomValue", "randomValue", "value", "value", "value", "value", "value", "value", "value",
        "value", "checksum", "checksum", "checksum", "newChecksum", "newChecksum", "newChecksum", "parity", "parity",
        "parity", "newParity", "newParity", "newParity" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate Rows", "Generate random integer", "calc parity & checksum", "Write to file Checksum",
        "Write to log Checksum", "Write to file Parity", "Write to log Parity" }, false );

    final Map<String, FramedMetaverseNode> parentVirtualStepNodeMap = verifyTransformationSteps( mainSubTransNode,
      new String[] { "Input checksum", "Input parity", "output checksum", "output parity" }, true );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "Input parity", "output checksum", "output parity",
        "calc parity", "calc checksum" }, false );

    final TransformationStepNode generateRows = (TransformationStepNode) parentStepNodeMap.get( "Generate Rows" );
    final TransformationStepNode generateRandomInt = (TransformationStepNode) parentStepNodeMap.get(
      "Generate random integer" );
    final TransformationStepNode calcParityAndChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "calc parity & checksum" );
    final TransformationStepNode writeToFileChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to file Checksum" );
    final TransformationStepNode writeToFileParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to file Parity" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );
    final TransformationStepNode writeToLogParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Parity" );

    // virtual sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "Input checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "output checksum" );
    final TransformationStepNode inputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "Input parity" );
    final TransformationStepNode outputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "output parity" );

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
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getOutputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    assertEquals( 3, getIterableSize( generateRandomInt.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllOutNodes() ) );

    // ---------- calc parity & checksum
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getPreviousSteps().iterator() ),
      testLineageNode( generateRandomInt ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToFileChecksum ), testLineageNode( writeToFileParity ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getInputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( "randomValue", true ), testFieldNode( "newChecksum", true ), testFieldNode(
        "newParity", true ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 6, getIterableSize( calcParityAndChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( calcParityAndChecksum, new ImmutableMap.Builder<String, Object>()
      .put( "stepType", SKIP ).put( "color", SKIP ).put( "pluginId", SKIP ).put( "type", SKIP ).put( "_analyzer", SKIP )
      .put( "category", SKIP ).put( "copies", SKIP ).put( "logicalId", SKIP ).put( "name", SKIP )
      .put( "namespace", SKIP ).put( "virtual", SKIP )
      .put( "verboseDetails", "input [1],input [1] description,input [1] update field names,input [1] rename [1],"
        + "input [2],input [2] description,input [2] update field names,input [2] rename [1],output [1],"
        + "output [1] description,output [1] update field names,output [1] rename [1],output [2],"
        + "output [2] description,output [2] update field names,output [2] rename [1]" )
      .put( "input [1]", "Generate random integer > [sub] Input checksum" )
      .put( "input [1] update field names", "true" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "input [2]", "Generate random integer > [sub] Input parity" )
      .put( "input [2] update field names", "true" )
      .put( "input [2] rename [1]", "randomValue > value" )
      .put( "output [1]", "[sub] output parity > Write to file Parity" )
      .put( "output [1] rename [1]", "parity > newParity" )
      .put( "output [1] update field names", "false" )
      .put( "output [2]", "[sub] output checksum > Write to file Checksum" )
      .put( "output [2] rename [1]", "checksum > newChecksum" )
      .put( "output [2] update field names", "false" ).build() );

    // ---------- Write to file Checksum
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getPreviousSteps().iterator() ),
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getInputStreamFields().iterator() ),
      testFieldNode( "newChecksum", true ), testFieldNode( "randomValue", true ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( "newChecksum", false ), testFieldNode( "randomValue", false ) );
    verifyStepIOLinks( writeToFileChecksum,
      testLineageLink( testFieldNode( "newChecksum", true ), LINK_DERIVES, testFieldNode( "newChecksum", false ) ),
      testLineageLink( testFieldNode( "randomValue", true ), LINK_DERIVES, testFieldNode( "randomValue", false ) ) );
    assertEquals( 5, getIterableSize( writeToFileChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- Write to file Parity
    verifyNodes( IteratorUtils.toList( writeToFileParity.getPreviousSteps().iterator() ),
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getNextSteps().iterator() ),
      testLineageNode( writeToLogParity ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getInputStreamFields().iterator() ),
      testFieldNode( "newParity", true ), testFieldNode( "randomValue", true ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getOutputStreamFields().iterator() ),
      testFieldNode( "newParity", false ), testFieldNode( "randomValue", false ) );
    verifyStepIOLinks( writeToFileParity,
      testLineageLink( testFieldNode( "newParity", true ), LINK_DERIVES, testFieldNode( "newParity", false ) ),
      testLineageLink( testFieldNode( "randomValue", true ), LINK_DERIVES, testFieldNode( "randomValue", false ) ) );
    assertEquals( 5, getIterableSize( writeToFileParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileParity.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( "checksum", true ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getAllOutNodes() ) );

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
    assertEquals( inputChecksum,
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_INPUTS, inputChecksum.getName() ) );
    final FramedMetaverseNode inputChecksumOutValue =
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_DERIVES, "value" );
    final FramedMetaverseNode calcChecksumAndParityOutRandomValue =
      verifyLinkedNode( calcParityAndChecksum, LINK_OUTPUTS, "randomValue" );
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
    // chain 1 : [sub] output checksum > outputs > checksum > derives > calc parity & checksum:newChecksum > inputs >
    // Write to file Checksum
    // chain 1 : [sub] output checksum > outputs > checksum > derives > calc parity & checksum:newChecksum > derives >
    // Write to file Checksum:newChecksum
    final FramedMetaverseNode outputChecksumOutChecksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, "checksum" );
    final FramedMetaverseNode calcChecksumAndParityOutNewChecksum =
      verifyLinkedNode( calcParityAndChecksum, LINK_OUTPUTS, "newChecksum" );
    assertEquals( calcChecksumAndParityOutNewChecksum, verifyLinkedNode( outputChecksumOutChecksum, LINK_DERIVES,
      "newChecksum" ) );
    assertEquals( writeToFileChecksum, verifyLinkedNode( calcChecksumAndParityOutNewChecksum,
      LINK_INPUTS, writeToFileChecksum.getName() ) );
    final FramedMetaverseNode writeToFileChecksumOutNewChecksum =
      verifyLinkedNode( writeToFileChecksum, LINK_OUTPUTS, "newChecksum" );
    assertEquals( writeToFileChecksumOutNewChecksum, verifyLinkedNode( calcChecksumAndParityOutNewChecksum,
      LINK_DERIVES, "newChecksum" ) );

    // Verify the following chain of links
    // chain 1 : [sub] output parity > outputs > parity > derives > calc parity & checksum:newChecksum > inputs >
    // Write to file Parity
    // chain 1 : [sub] output parity > outputs > parity > derives > calc parity & checksum:newChecksum > derives >
    // Write to file Parity:newParity
    final FramedMetaverseNode outputParityOutParity =
      verifyLinkedNode( outputParity, LINK_OUTPUTS, "parity" );
    final FramedMetaverseNode calcChecksumAndParityOutNewParity =
      verifyLinkedNode( calcParityAndChecksum, LINK_OUTPUTS, "newParity" );
    assertEquals( calcChecksumAndParityOutNewParity, verifyLinkedNode( outputParityOutParity, LINK_DERIVES,
      "newParity" ) );
    assertEquals( writeToFileParity, verifyLinkedNode( calcChecksumAndParityOutNewParity,
      LINK_INPUTS, writeToFileParity.getName() ) );
    final FramedMetaverseNode writeToFileChecksumOutNewParity =
      verifyLinkedNode( writeToFileParity, LINK_OUTPUTS, "newParity" );
    assertEquals( writeToFileChecksumOutNewParity, verifyLinkedNode( calcChecksumAndParityOutNewParity,
      LINK_DERIVES, "newParity" ) );
  }

  @Test
  public void testMoreThanOneIOsteps() throws Exception {

    final String transNodeName = "moreThanOneIOsteps";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode mainSubTransNode = verifyTransformationNode( "sub", true );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", false );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 52, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 139, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub", "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "randomValue", "randomValue", "randomValue",
        "randomValue", "randomValue", "randomValue", "value", "value", "value", "value", "value", "value", "value",
        "value", "checksum", "checksum", "checksum", "checksum", "checksum", "parity", "parity",
        "parity", "parity", "parity" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate Rows", "Generate random integer", "calc parity & checksum", "Write to file Checksum",
        "Write to log Checksum", "Write to file Parity", "Write to log Parity" }, false );

    final Map<String, FramedMetaverseNode> parentVirtualStepNodeMap = verifyTransformationSteps( mainSubTransNode,
      new String[] { "Input checksum", "Input parity", "output checksum", "output parity" }, true );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "Input parity", "output checksum", "output parity",
        "calc parity", "calc checksum" }, false );

    final TransformationStepNode generateRows = (TransformationStepNode) parentStepNodeMap.get( "Generate Rows" );
    final TransformationStepNode generateRandomInt = (TransformationStepNode) parentStepNodeMap.get(
      "Generate random integer" );
    final TransformationStepNode calcParityAndChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "calc parity & checksum" );
    final TransformationStepNode writeToFileChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to file Checksum" );
    final TransformationStepNode writeToFileParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to file Parity" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );
    final TransformationStepNode writeToLogParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Parity" );

    // virtual sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "Input checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "output checksum" );
    final TransformationStepNode inputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "Input parity" );
    final TransformationStepNode outputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "output parity" );

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
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getOutputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    assertEquals( 3, getIterableSize( generateRandomInt.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllOutNodes() ) );

    // ---------- calc parity & checksum
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getPreviousSteps().iterator() ),
      testLineageNode( generateRandomInt ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToFileChecksum ), testLineageNode( writeToFileParity ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getInputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( "randomValue", true ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // ---------- Write to file Checksum
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getPreviousSteps().iterator() ),
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getInputStreamFields().iterator() ),
      testFieldNode( "checksum", true ), testFieldNode( "randomValue", true ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( "checksum", false ), testFieldNode( "randomValue", false ) );
    verifyStepIOLinks( writeToFileChecksum,
      testLineageLink( testFieldNode( "checksum", true ), LINK_DERIVES, testFieldNode( "checksum", false ) ),
      testLineageLink( testFieldNode( "randomValue", true ), LINK_DERIVES, testFieldNode( "randomValue", false ) ) );
    assertEquals( 5, getIterableSize( writeToFileChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- Write to file Parity
    verifyNodes( IteratorUtils.toList( writeToFileParity.getPreviousSteps().iterator() ),
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getNextSteps().iterator() ),
      testLineageNode( writeToLogParity ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getInputStreamFields().iterator() ),
      testFieldNode( "parity", true ), testFieldNode( "randomValue", true ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getOutputStreamFields().iterator() ),
      testFieldNode( "parity", false ), testFieldNode( "randomValue", false ) );
    verifyStepIOLinks( writeToFileParity,
      testLineageLink( testFieldNode( "parity", true ), LINK_DERIVES, testFieldNode( "parity", false ) ),
      testLineageLink( testFieldNode( "randomValue", true ), LINK_DERIVES, testFieldNode( "randomValue", false ) ) );
    assertEquals( 5, getIterableSize( writeToFileParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileParity.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 0, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // ---------- output parity
    verifyNodes( IteratorUtils.toList( outputParity.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getOutputStreamFields().iterator() ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 0, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following chain of links
    // chain 1: Generate Random Int > outputs > randomValue >  inputs > Input checksum > outputs > value > derives >
    // calc checksum & parity:randomValue
    // chain 2: Generate Random Int > outputs > randomValue >  derives > Input checksum:value
    final FramedMetaverseNode generateRandomIntOutRandomValue =
      verifyLinkedNode( generateRandomInt, LINK_OUTPUTS, "randomValue" );
    assertEquals( inputChecksum,
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_INPUTS, inputChecksum.getName() ) );
    final FramedMetaverseNode inputChecksumOutValue =
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_DERIVES, "value" );
    final FramedMetaverseNode calcChecksumAndParityOutRandomValue =
      verifyLinkedNode( calcParityAndChecksum, LINK_OUTPUTS, "randomValue" );
    assertEquals( calcChecksumAndParityOutRandomValue, verifyLinkedNode(
      inputChecksumOutValue, LINK_DERIVES, "randomValue" ) );
  }

  @Test
  public void testMoreThanOneIOstepsNoUpdtMappDownstream() throws Exception {

    final String transNodeName = "moreThanOneIOstepsNoUpdtMappDownstream";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode mainSubTransNode = verifyTransformationNode( "sub", true );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", false );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 52, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 139, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub", "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "randomValue", "value", "value",
        "value", "value", "value", "value", "value", "value", "value", "value", "value", "value",
        "value", "checksum", "checksum", "checksum", "checksum", "checksum", "parity", "parity",
        "parity", "parity", "parity" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate Rows", "Generate random integer", "calc parity & checksum", "Write to file Checksum",
        "Write to log Checksum", "Write to file Parity", "Write to log Parity" }, false );

    final Map<String, FramedMetaverseNode> parentVirtualStepNodeMap = verifyTransformationSteps( mainSubTransNode,
      new String[] { "Input checksum", "Input parity", "output checksum", "output parity" }, true );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "Input parity", "output checksum", "output parity",
        "calc parity", "calc checksum" }, false );

    final TransformationStepNode generateRows = (TransformationStepNode) parentStepNodeMap.get( "Generate Rows" );
    final TransformationStepNode generateRandomInt = (TransformationStepNode) parentStepNodeMap.get(
      "Generate random integer" );
    final TransformationStepNode calcParityAndChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "calc parity & checksum" );
    final TransformationStepNode writeToFileChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to file Checksum" );
    final TransformationStepNode writeToFileParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to file Parity" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );
    final TransformationStepNode writeToLogParity = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Parity" );

    // virtual sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "Input checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "output checksum" );
    final TransformationStepNode inputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "Input parity" );
    final TransformationStepNode outputParity = (TransformationStepNode) parentVirtualStepNodeMap.get(
      "output parity" );

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
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getOutputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    assertEquals( 3, getIterableSize( generateRandomInt.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllOutNodes() ) );

    // ---------- calc parity & checksum
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getPreviousSteps().iterator() ),
      testLineageNode( generateRandomInt ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToFileChecksum ), testLineageNode( writeToFileParity ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getInputStreamFields().iterator() ),
      testFieldNode( "randomValue", false ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( "value", true ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getOutNodes( LINK_EXECUTES ) ) );
    // verify properties
    verifyNodeProperties( calcParityAndChecksum, new ImmutableMap.Builder<String, Object>()
      .put( "stepType", SKIP ).put( "color", SKIP ).put( "pluginId", SKIP ).put( "type", SKIP ).put( "_analyzer", SKIP )
      .put( "category", SKIP ).put( "copies", SKIP ).put( "logicalId", SKIP ).put( "name", SKIP )
      .put( "namespace", SKIP ).put( "virtual", SKIP )
      .put( "verboseDetails", "input [1],input [1] description,input [1] update field names,input [1] rename [1],"
        + "input [2],input [2] description,input [2] update field names,input [2] rename [1],output [1],"
        + "output [1] description,output [1] update field names,output [2],"
        + "output [2] description,output [2] update field names" )
      .put( "input [1]", "Generate random integer > [sub] Input checksum" )
      .put( "input [1] update field names", "false" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "input [2]", "Generate random integer > [sub] Input parity" )
      .put( "input [2] update field names", "false" )
      .put( "input [2] rename [1]", "randomValue > value" )
      .put( "output [1]", "[sub] output parity > Write to file Parity" )
      .put( "output [1] update field names", "false" )
      .put( "output [2]", "[sub] output checksum > Write to file Checksum" )
      .put( "output [2] update field names", "false" ).build() );

    // ---------- Write to file Checksum
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getPreviousSteps().iterator() ),
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getInputStreamFields().iterator() ),
      testFieldNode( "checksum", true ), testFieldNode( "value", true ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( "checksum", false ), testFieldNode( "value", false ) );
    verifyStepIOLinks( writeToFileChecksum,
      testLineageLink( testFieldNode( "checksum", true ), LINK_DERIVES, testFieldNode( "checksum", false ) ),
      testLineageLink( testFieldNode( "value", true ), LINK_DERIVES, testFieldNode( "value", false ) ) );
    assertEquals( 5, getIterableSize( writeToFileChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileChecksum.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- Write to file Parity
    verifyNodes( IteratorUtils.toList( writeToFileParity.getPreviousSteps().iterator() ),
      testLineageNode( calcParityAndChecksum ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getNextSteps().iterator() ),
      testLineageNode( writeToLogParity ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getInputStreamFields().iterator() ),
      testFieldNode( "parity", true ), testFieldNode( "value", true ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getOutputStreamFields().iterator() ),
      testFieldNode( "parity", false ), testFieldNode( "value", false ) );
    verifyStepIOLinks( writeToFileParity,
      testLineageLink( testFieldNode( "parity", true ), LINK_DERIVES, testFieldNode( "parity", false ) ),
      testLineageLink( testFieldNode( "value", true ), LINK_DERIVES, testFieldNode( "value", false ) ) );
    assertEquals( 5, getIterableSize( writeToFileParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileParity.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 0, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // ---------- output parity
    verifyNodes( IteratorUtils.toList( outputParity.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getOutputStreamFields().iterator() ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 0, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following chain of links
    // chain 1: Generate Random Int > outputs > randomValue >  inputs > Input checksum > outputs > value > derives >
    // calc checksum & parity:value
    // chain 2: Generate Random Int > outputs > randomValue >  derives > Input checksum:value
    final FramedMetaverseNode generateRandomIntOutRandomValue =
      verifyLinkedNode( generateRandomInt, LINK_OUTPUTS, "randomValue" );
    assertEquals( inputChecksum,
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_INPUTS, inputChecksum.getName() ) );
    final FramedMetaverseNode inputChecksumOutValue =
      verifyLinkedNode( generateRandomIntOutRandomValue, LINK_DERIVES, "value" );
    final FramedMetaverseNode calcChecksumAndParityOutValue =
      verifyLinkedNode( calcParityAndChecksum, LINK_OUTPUTS, "value" );
    assertEquals( calcChecksumAndParityOutValue, verifyLinkedNode(
      inputChecksumOutValue, LINK_DERIVES, "value" ) );
  }
}
