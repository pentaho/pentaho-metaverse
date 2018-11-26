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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.pentaho.dictionary.DictionaryConst.*;

@RunWith( PowerMockRunner.class )
@PrepareForTest( MetaverseConfig.class )
public class MappingAnalyzerValidationIT extends StepAnalyzerValidationIT {

  private static final String VALUE = "value";
  private static final String RANDOM_INT = "randomInt";
  private static final String RANDOM_VALUE = "randomValue";
  private static final String CHECKSUM = "checksum";
  private static final String NEW_CHECKSUM = "newChecksum";
  private static final String PARITY = "parity";
  private static final String NEW_PARITY = "newParity";

  @Test
  public void testOneOIWithMappingsExtraFields() throws Exception {

    final String transNodeName = "oneOIWithMappingsExtraField";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 29, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 85, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_INT, RANDOM_VALUE, RANDOM_VALUE, VALUE, VALUE, VALUE,
        VALUE, VALUE, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM, NEW_CHECKSUM, "value_value" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "calc checksum", "Write to log Checksum" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "calc checksum", "Calculator", "Select values", "output checksum", }, false );

    final TransformationStepNode dataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Data grid" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksumSubTrans = (TransformationStepNode) subTransStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode calculator = (TransformationStepNode) subTransStepNodeMap.get(
      "Calculator" );
    final TransformationStepNode selectValues = (TransformationStepNode) subTransStepNodeMap.get(
      "Select values" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );

    // ---------- Data grid
    verifyNodes( IteratorUtils.toList( dataGrid.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getNextSteps().iterator() ), testLineageNode( calcChecksum ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getOutputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE, false ), testFieldNode( RANDOM_INT, false ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( dataGrid.getAllOutNodes() ) );

    // ---------- calc checksum (Mapping step)
    verifyNodes( IteratorUtils.toList( calcChecksum.getPreviousSteps().iterator() ),
      testLineageNode( dataGrid ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getInputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE, false ), testFieldNode( RANDOM_INT, false ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 5, getIterableSize( calcChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( calcChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( calcChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] update field names,input [1] rename [1],output [1],"
        + "output [1] update field names,output [1] rename [1]" )
      .put( "input [1]", "Data grid > [sub] Input checksum" )
      .put( "input [1] update field names", "true" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "output [1]", "[sub] output checksum > Write to log Checksum" )
      .put( "output [1] rename [1]", "checksum > newChecksum" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- Write to log Checksum
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName(), false ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( NEW_CHECKSUM, false ), testFieldNode( RANDOM_VALUE, false ) );
    verifyStepIOLinks( writeToLogChecksum,
      testLineageLink( testFieldNode( CHECKSUM, false ), LINK_DERIVES, testFieldNode( NEW_CHECKSUM, false ) ),
      testLineageLink( testFieldNode( VALUE, false ), LINK_DERIVES, testFieldNode( RANDOM_VALUE, false ) ) );
    assertEquals( 5, getIterableSize( writeToLogChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( writeToLogChecksum.getAllOutNodes() ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ), testStepNode(
      selectValues.getName(), false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Data drid > outputs > randomValue >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > Calculator > outputs > checksum > input > Select values >
    // outputs > checksum > outputs > value > inputs > Write to log Checksum > outputs > randomValue
    // - chain 2: Generate Random Int > outputs > randomValue > derives > Input Checksum:value > derives
    //   > calc checksum:value > derives > output checksum:value > derives > Write to log Checksum:randomValue
    final FramedMetaverseNode dataGrid_output_randomValue =
      verifyLinkedNode( dataGrid, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calculator_output_value = verifyLinkedNode( calculator, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calculator_output_checksum = verifyLinkedNode( calculator, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode calculator_output_valueValue = verifyLinkedNode( calculator, LINK_OUTPUTS,
      "value_value" );
    final FramedMetaverseNode selectValues_output_value = verifyLinkedNode( selectValues, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode selectValues_output_checksum = verifyLinkedNode( selectValues, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_randomValue =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, RANDOM_VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( dataGrid_output_randomValue, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksumSubTrans,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksumSubTrans.getName() ) );
    assertEquals( calculator,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, calculator.getName() ) );
    assertEquals( selectValues,
      verifyLinkedNode( calculator_output_value, LINK_INPUTS, selectValues.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( selectValues_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertEquals( inputChecksum_output_value,
      verifyLinkedNode( dataGrid_output_randomValue, LINK_DERIVES, VALUE ) );
    assertEquals( calcChecksum_output_value, verifyLinkedNode( inputChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( calculator_output_value, verifyLinkedNode( calcChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( calculator_output_valueValue, verifyLinkedNode( calcChecksum_output_value, LINK_DERIVES,
      "value_value" ) );
    assertEquals( outputChecksum_output_value, verifyLinkedNode( selectValues_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( writeToLogChecksum_output_randomValue,
      verifyLinkedNode( outputChecksum_output_value, LINK_DERIVES, RANDOM_VALUE ) );

    // Verify the following link chains
    // - chain 1: calc checksum > outputs > checksum > inputs > calculator > outputs > checksum > inputs > Select
    //   values > output > checksum > inputs > Write to log Checksum > outputs > newChecksum
    // - chain 2: calc checksum:checksum > derives > output checksum:checksum > derives > Write to log
    //   Checksum:newChecksum
    final FramedMetaverseNode calcChecksum_output_checksum =
      verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_checksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode writeToLogChecksum_output_newChecksum =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, NEW_CHECKSUM );

    assertEquals( calculator,
      verifyLinkedNode( calcChecksum_output_checksum, LINK_INPUTS, calculator.getName() ) );
    assertEquals( selectValues,
      verifyLinkedNode( calculator_output_checksum, LINK_INPUTS, selectValues.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_checksum, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertEquals( calculator_output_checksum,
      verifyLinkedNode( calcChecksum_output_checksum, LINK_DERIVES, CHECKSUM ) );
    assertEquals( selectValues_output_checksum,
      verifyLinkedNode( calculator_output_checksum, LINK_DERIVES, CHECKSUM ) );
    assertEquals( outputChecksum_output_checksum,
      verifyLinkedNode( selectValues_output_checksum, LINK_DERIVES, CHECKSUM ) );
    assertEquals( writeToLogChecksum_output_newChecksum, verifyLinkedNode( outputChecksum_output_checksum,
      LINK_DERIVES, NEW_CHECKSUM ) );
  }

  @Test
  public void testOneOIFieldNoMappings() throws Exception {

    final String transNodeName = "oneOINoFieldMappings";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 28, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 82, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { VALUE, VALUE, VALUE, VALUE, VALUE,
        VALUE, VALUE, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM, "value_value" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "calc checksum", "Write to log Checksum" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "calc checksum", "Calculator", "Select values", "output checksum", }, false );

    final TransformationStepNode dataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Data grid" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksumSubTrans = (TransformationStepNode) subTransStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode calculator = (TransformationStepNode) subTransStepNodeMap.get(
      "Calculator" );
    final TransformationStepNode selectValues = (TransformationStepNode) subTransStepNodeMap.get(
      "Select values" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );

    // ---------- Data grid
    verifyNodes( IteratorUtils.toList( dataGrid.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getNextSteps().iterator() ), testLineageNode( calcChecksum ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getOutputStreamFields().iterator() ),
      testFieldNode( VALUE, false ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllOutNodes() ) );

    // ---------- calc checksum (Mapping step)
    verifyNodes( IteratorUtils.toList( calcChecksum.getPreviousSteps().iterator() ),
      testLineageNode( dataGrid ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getInputStreamFields().iterator() ),
      testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 4, getIterableSize( calcChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( calcChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( calcChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] update field names,output [1],output [1] update field names" )
      .put( "input [1]", "Data grid > [sub] Input checksum" )
      .put( "input [1] update field names", "false" )
      .put( "output [1]", "[sub] output checksum > Write to log Checksum" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- Write to log Checksum
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName(), false ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyStepIOLinks( writeToLogChecksum,
      testLineageLink( testFieldNode( CHECKSUM, false ), LINK_DERIVES, testFieldNode( CHECKSUM, false ) ),
      testLineageLink( testFieldNode( VALUE, false ), LINK_DERIVES, testFieldNode( VALUE, false ) ) );
    assertEquals( 5, getIterableSize( writeToLogChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( writeToLogChecksum.getAllOutNodes() ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ), testStepNode(
      selectValues.getName(), false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Data drid > outputs > value >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > Calculator > outputs > checksum > input > Select values >
    // outputs > checksum > outputs > value > inputs > Write to log Checksum > outputs > value
    // - chain 2: Generate Random Int > outputs > value > derives > Input Checksum:value > derives
    //   > calc checksum:value > derives > output checksum:value > derives > Write to log Checksum:value
    final FramedMetaverseNode dataGrid_output_value = verifyLinkedNode( dataGrid, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calculator_output_value = verifyLinkedNode( calculator, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calculator_output_checksum = verifyLinkedNode( calculator, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode calculator_output_valueValue = verifyLinkedNode( calculator, LINK_OUTPUTS,
      "value_value" );
    final FramedMetaverseNode selectValues_output_value = verifyLinkedNode( selectValues, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode selectValues_output_checksum = verifyLinkedNode( selectValues, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_value =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( dataGrid_output_value, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksumSubTrans,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksumSubTrans.getName() ) );
    assertEquals( calculator,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, calculator.getName() ) );
    assertEquals( selectValues,
      verifyLinkedNode( calculator_output_value, LINK_INPUTS, selectValues.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( selectValues_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertEquals( inputChecksum_output_value,
      verifyLinkedNode( dataGrid_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( calcChecksum_output_value, verifyLinkedNode( inputChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( calculator_output_value, verifyLinkedNode( calcChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( calculator_output_valueValue, verifyLinkedNode( calcChecksum_output_value, LINK_DERIVES,
      "value_value" ) );
    assertEquals( outputChecksum_output_value, verifyLinkedNode( selectValues_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( writeToLogChecksum_output_value,
      verifyLinkedNode( outputChecksum_output_value, LINK_DERIVES, VALUE ) );

    // Verify the following link chains
    // - chain 1: calc checksum > outputs > checksum > inputs > calculator > outputs > checksum > inputs > Select
    //   values > output > checksum > inputs > Write to log Checksum > outputs > checksum
    // - chain 2: calc checksum:checksum > derives > output checksum:checksum > derives > Write to log
    //   Checksum:checksum
    final FramedMetaverseNode calcChecksum_output_checksum =
      verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_checksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode writeToLogChecksum_output_checksum =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, CHECKSUM );

    assertEquals( calculator,
      verifyLinkedNode( calcChecksum_output_checksum, LINK_INPUTS, calculator.getName() ) );
    assertEquals( selectValues,
      verifyLinkedNode( calculator_output_checksum, LINK_INPUTS, selectValues.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_checksum, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertEquals( calculator_output_checksum,
      verifyLinkedNode( calcChecksum_output_checksum, LINK_DERIVES, CHECKSUM ) );
    assertEquals( selectValues_output_checksum,
      verifyLinkedNode( calculator_output_checksum, LINK_DERIVES, CHECKSUM ) );
    assertEquals( outputChecksum_output_checksum,
      verifyLinkedNode( selectValues_output_checksum, LINK_DERIVES, CHECKSUM ) );
    assertEquals( writeToLogChecksum_output_checksum, verifyLinkedNode( outputChecksum_output_checksum,
      LINK_DERIVES, CHECKSUM ) );
  }

  @Test
  public void testOneOIWithMappings() throws Exception {

    final String transNodeName = "oneOIWithMappings";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 21, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 53, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_VALUE, RANDOM_VALUE, VALUE, VALUE, VALUE, CHECKSUM,
        CHECKSUM, NEW_CHECKSUM } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate random integer", "calc checksum", "Write to log Checksum" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "calc checksum", "output checksum", }, false );

    final TransformationStepNode generateRandomInt = (TransformationStepNode) parentStepNodeMap.get(
      "Generate random integer" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );

    final TransformationStepNode calcChecksumSubTrans = (TransformationStepNode) subTransStepNodeMap.get(
      "calc checksum" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );

    // ---------- Generate Random Int
    verifyNodes( IteratorUtils.toList( generateRandomInt.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getNextSteps().iterator() ), testLineageNode( calcChecksum ) );
    verifyNodes( IteratorUtils.toList( generateRandomInt.getOutputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE, false ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllOutNodes() ) );

    // ---------- calc checksum (Mapping step)
    verifyNodes( IteratorUtils.toList( calcChecksum.getPreviousSteps().iterator() ),
      testLineageNode( generateRandomInt ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getInputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE, false ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 4, getIterableSize( calcChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( calcChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( calcChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] update field names,input [1] rename [1],output [1],"
        + "output [1] update field names,output [1] rename [1]" )
      .put( "input [1]", "Generate random integer > [sub] Input checksum" )
      .put( "input [1] update field names", "true" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "output [1]", "[sub] output checksum > Write to log Checksum" )
      .put( "output [1] rename [1]", "checksum > newChecksum" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- Write to log Checksum
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName(), false ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( NEW_CHECKSUM, false ), testFieldNode( RANDOM_VALUE, false ) );
    verifyStepIOLinks( writeToLogChecksum,
      testLineageLink( testFieldNode( CHECKSUM, false ), LINK_DERIVES, testFieldNode( NEW_CHECKSUM, false ) ),
      testLineageLink( testFieldNode( VALUE, false ), LINK_DERIVES, testFieldNode( RANDOM_VALUE, false ) ) );
    assertEquals( 5, getIterableSize( writeToLogChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( writeToLogChecksum.getAllOutNodes() ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ), testStepNode(
      calcChecksumSubTrans.getName(), false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Generate Random Int > outputs > randomValue >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > output checksum > outputs > value > inputs
    //   > Write to log Checksum > outputs > randomValue
    // - chain 2: Generate Random Int > outputs > randomValue > derives > Input Checksum:value > derives
    //   > calc checksum:value > derives > output checksum:value > derives > Write to log Checksum:randomValue
    final FramedMetaverseNode generateRandomInt_output_randomValue =
      verifyLinkedNode( generateRandomInt, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_randomValue =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, RANDOM_VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksumSubTrans,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksum.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertEquals( inputChecksum_output_value,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_DERIVES, VALUE ) );
    assertEquals( calcChecksum_output_value, verifyLinkedNode( inputChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( outputChecksum_output_value, verifyLinkedNode( calcChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( writeToLogChecksum_output_randomValue,
      verifyLinkedNode( outputChecksum_output_value, LINK_DERIVES, RANDOM_VALUE ) );

    // Verify the following link chains
    // - chain 1: calc checksum > outputs > checksum > inputs > output checksum > outputs > checksum > inputs
    //   > Write to log Checksum > outputs > newChecksum
    // - chain 2: calc checksum:checksum > derives > output checksum:checksum > derives > Write to log
    //   Checksum:newChecksum
    final FramedMetaverseNode calcChecksum_output_checksum =
      verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_checksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode writeToLogChecksum_output_newChecksum =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, NEW_CHECKSUM );

    assertEquals( outputChecksum,
      verifyLinkedNode( calcChecksum_output_checksum, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_checksum, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertEquals( outputChecksum_output_checksum,
      verifyLinkedNode( calcChecksum_output_checksum, LINK_DERIVES, CHECKSUM ) );
    assertEquals( writeToLogChecksum_output_newChecksum, verifyLinkedNode( outputChecksum_output_checksum,
      LINK_DERIVES, NEW_CHECKSUM ) );
  }

  @Test
  public void testMoreThanOneIOstepsMappingOut() throws Exception {

    final String transNodeName = "moreThanOneIOstepsMappingOut";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 43, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 123, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_VALUE, RANDOM_VALUE, RANDOM_VALUE,
        RANDOM_VALUE, RANDOM_VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, CHECKSUM, CHECKSUM,
        NEW_CHECKSUM, NEW_CHECKSUM, PARITY, PARITY, NEW_PARITY, NEW_PARITY } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate Rows", "Generate random integer", "calc parity & checksum", "Write to file Checksum",
        "Write to log Checksum", "Write to file Parity", "Write to log Parity", "Write to log Dummy" }, false );

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
    final TransformationStepNode writeToLogDummy = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Dummy" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) subTransStepNodeMap.get( "calc checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );
    final TransformationStepNode calcParity = (TransformationStepNode) subTransStepNodeMap.get( "calc parity" );
    final TransformationStepNode outputParity = (TransformationStepNode) subTransStepNodeMap.get( "output parity" );

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
      testFieldNode( RANDOM_VALUE, false ) );
    assertEquals( 3, getIterableSize( generateRandomInt.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( generateRandomInt.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( generateRandomInt.getAllOutNodes() ) );

    // ---------- calc parity & checksum
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getPreviousSteps().iterator() ),
      testLineageNode( generateRandomInt ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToFileChecksum ), testLineageNode( writeToFileParity ),
      testLineageNode( writeToLogDummy ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getInputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE, false ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( calcParityAndChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS,
        "input [1],input [1] description,input [1] update field names,input [1] rename [1],"
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
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( NEW_CHECKSUM, false ), testFieldNode( RANDOM_VALUE, false ) );
    verifyStepIOLinks( writeToFileChecksum,
      testLineageLink( testFieldNode( CHECKSUM, false ), LINK_DERIVES, testFieldNode( NEW_CHECKSUM, false ) ),
      testLineageLink( testFieldNode( VALUE, false ), LINK_DERIVES, testFieldNode( RANDOM_VALUE, false ) ) );
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
      testFieldNode( PARITY, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getOutputStreamFields().iterator() ),
      testFieldNode( NEW_PARITY, false ), testFieldNode( RANDOM_VALUE, false ) );
    verifyStepIOLinks( writeToFileParity,
      testLineageLink( testFieldNode( PARITY, false ), LINK_DERIVES, testFieldNode( NEW_PARITY, false ) ),
      testLineageLink( testFieldNode( VALUE, false ), LINK_DERIVES, testFieldNode( RANDOM_VALUE, false ) ) );
    assertEquals( 5, getIterableSize( writeToFileParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileParity.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName(), false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllOutNodes() ) );

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
    // - chain 1: Generate Random Int > outputs > randomValue >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > output checksum > outputs > value > inputs
    //   > Write to file Checksum > outputs > randomValue
    // - chain 2: Generate Random Int:randomValue > derives > Input Checksum:value > derives > calc checksum:value >
    //   derives > output checksum:value > derives > Write to log Checksum:randomValue
    final FramedMetaverseNode generateRandomInt_output_randomValue =
      verifyLinkedNode( generateRandomInt, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToFileChecksum_output_randomValue =
      verifyLinkedNode( writeToFileChecksum, LINK_OUTPUTS, RANDOM_VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksum,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksum.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToFileChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToFileChecksum.getName() ) );
    // there are more than one value nodes derived from generateRandomInt_output_randomValue - make sure one if them
    // is the inputChecksum_output_value node
    final List<FramedMetaverseNode> derivedValueNodes =
      verifyLinkedNodes( generateRandomInt_output_randomValue, LINK_DERIVES, VALUE );
    assertTrue( derivedValueNodes.contains( inputChecksum_output_value ) );

    assertEquals( inputChecksum_output_value,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_DERIVES, VALUE ) );
    assertEquals( calcChecksum_output_value,
      verifyLinkedNode( inputChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( outputChecksum_output_value,
      verifyLinkedNode( calcChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( writeToFileChecksum_output_randomValue,
      verifyLinkedNode( outputChecksum_output_value, LINK_DERIVES, RANDOM_VALUE ) );

    // Verify the following link chains
    // - chain 1: calc parity > outputs parity > inputs > output parity > outputs > parity > inputs > Write to file
    //   Parity > outputs > newParity
    // - chain 2: calc parity:parity > derives > output parity:parity > derives > Write to file Checksum:parity
    final FramedMetaverseNode calcParity_output_parity = verifyLinkedNode( calcParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode outputParity_output_parity = verifyLinkedNode( outputParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode writeToFileParity_output_newParity =
      verifyLinkedNode( writeToFileParity, LINK_OUTPUTS, NEW_PARITY );

    assertEquals( outputParity,
      verifyLinkedNode( calcParity_output_parity, LINK_INPUTS, outputParity.getName() ) );
    assertEquals( writeToFileParity,
      verifyLinkedNode( outputParity_output_parity, LINK_INPUTS, writeToFileParity.getName() ) );

    assertEquals( outputParity_output_parity,
      verifyLinkedNode( calcParity_output_parity, LINK_DERIVES, PARITY ) );
    assertEquals( writeToFileParity_output_newParity,
      verifyLinkedNode( outputParity_output_parity, LINK_DERIVES, NEW_PARITY ) );
  }

  @Test
  public void testMoreThanOneIOstepsNoUpdtMappDownstream() throws Exception {

    final String transNodeName = "moreThanOneIOstepsNoUpdtMappDownstream";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 42, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 120, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_VALUE, VALUE, VALUE, VALUE, VALUE,
        VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM,
        PARITY, PARITY, PARITY, PARITY } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate Rows", "Generate random integer", "calc parity & checksum", "Write to file Checksum",
        "Write to log Checksum", "Write to file Parity", "Write to log Parity" }, false );

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

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) subTransStepNodeMap.get( "calc checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );
    final TransformationStepNode calcParity = (TransformationStepNode) subTransStepNodeMap.get( "calc parity" );
    final TransformationStepNode outputParity = (TransformationStepNode) subTransStepNodeMap.get( "output parity" );

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
      testFieldNode( RANDOM_VALUE, false ) );
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
      testFieldNode( RANDOM_VALUE, false ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 4, getIterableSize( calcParityAndChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( calcParityAndChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcParityAndChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // verify properties
    verifyNodeProperties( calcParityAndChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] description,input [1] update field names,input [1] rename "
        + "[1],input [2],input [2] description,input [2] update field names,input [2] rename [1],output [1],"
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
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( writeToFileChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyStepIOLinks( writeToFileChecksum,
      testLineageLink( testFieldNode( CHECKSUM, true ), LINK_DERIVES, testFieldNode( CHECKSUM, false ) ),
      testLineageLink( testFieldNode( VALUE, true ), LINK_DERIVES, testFieldNode( VALUE, false ) ) );
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
      testFieldNode( PARITY, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( writeToFileParity.getOutputStreamFields().iterator() ),
      testFieldNode( PARITY, false ), testFieldNode( VALUE, false ) );
    verifyStepIOLinks( writeToFileParity,
      testLineageLink( testFieldNode( PARITY, true ), LINK_DERIVES, testFieldNode( PARITY, false ) ),
      testLineageLink( testFieldNode( VALUE, true ), LINK_DERIVES, testFieldNode( VALUE, false ) ) );
    assertEquals( 5, getIterableSize( writeToFileParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( writeToFileParity.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( writeToFileParity.getOutNodes( LINK_WRITESTO ) ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName(), false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM, false ), testFieldNode( VALUE, false ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( outputChecksum.getAllOutNodes() ) );

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
    // - chain 1: Generate Random Int > outputs > randomValue >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > output checksum > outputs > value > inputs
    //   > Write to file Checksum > outputs > randomValue
    // - chain 2: Generate Random Int:randomValue > derives > Input Checksum:value > derives > calc checksum:value >
    //   derives > output checksum:value > derives > Write to log Checksum:randomValue
    final FramedMetaverseNode generateRandomInt_output_randomValue =
      verifyLinkedNode( generateRandomInt, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToFileChecksum_output_value =
      verifyLinkedNode( writeToFileChecksum, LINK_OUTPUTS, VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksum,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksum.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToFileChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToFileChecksum.getName() ) );
    // there are more than one value nodes derived from generateRandomInt_output_randomValue - make sure one if them
    // is the inputChecksum_output_value node
    final List<FramedMetaverseNode> derivedValueNodes =
      verifyLinkedNodes( generateRandomInt_output_randomValue, LINK_DERIVES, VALUE );
    assertTrue( derivedValueNodes.contains( inputChecksum_output_value ) );

    assertEquals( inputChecksum_output_value,
      verifyLinkedNode( generateRandomInt_output_randomValue, LINK_DERIVES, VALUE ) );
    assertEquals( calcChecksum_output_value,
      verifyLinkedNode( inputChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( outputChecksum_output_value,
      verifyLinkedNode( calcChecksum_output_value, LINK_DERIVES, VALUE ) );
    assertEquals( writeToFileChecksum_output_value,
      verifyLinkedNode( outputChecksum_output_value, LINK_DERIVES, VALUE ) );

    // Verify the following link chains
    // - chain 1: calc parity > outputs parity > inputs > output parity > outputs > parity > inputs > Write to file
    //   Parity > outputs > newParity
    // - chain 2: calc parity:parity > derives > output parity:parity > derives > Write to file Checksum:parity
    final FramedMetaverseNode calcParity_output_parity = verifyLinkedNode( calcParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode outputParity_output_parity = verifyLinkedNode( outputParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode writeToFileParity_output_parity =
      verifyLinkedNode( writeToFileParity, LINK_OUTPUTS, PARITY );

    assertEquals( outputParity,
      verifyLinkedNode( calcParity_output_parity, LINK_INPUTS, outputParity.getName() ) );
    assertEquals( writeToFileParity,
      verifyLinkedNode( outputParity_output_parity, LINK_INPUTS, writeToFileParity.getName() ) );

    assertEquals( outputParity_output_parity,
      verifyLinkedNode( calcParity_output_parity, LINK_DERIVES, PARITY ) );
    assertEquals( writeToFileParity_output_parity,
      verifyLinkedNode( outputParity_output_parity, LINK_DERIVES, PARITY ) );
  }
}
