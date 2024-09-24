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
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.pentaho.dictionary.DictionaryConst.*;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
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
  public void test_oneIOMainPathWithMappingsAndExtraField() throws Exception {

    final String transNodeName = "oneIOMainPathWithMappingsAndExtraField";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 38, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 112, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_INT, RANDOM_INT, RANDOM_INT, RANDOM_INT,
        RANDOM_VALUE, RANDOM_VALUE, RANDOM_VALUE, RANDOM_VALUE, VALUE, VALUE, VALUE, VALUE,
        VALUE, VALUE, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM, NEW_CHECKSUM, NEW_CHECKSUM } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "Write to log - Data grid", "mapping - calc checksum",
        "Write to log - mapping - calc checksum", "Write to log Checksum" },  false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "calc checksum", "output checksum", "Write to log - Input checksum",
        "Write to log - calc checksum", "Write to log - output checksum" }, false );

    final TransformationStepNode dataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Data grid" );
    final TransformationStepNode writeToLogDataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - Data grid" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "mapping - calc checksum" );
    final TransformationStepNode writeToLogCalcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - mapping - calc checksum" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksumSubTrans = (TransformationStepNode) subTransStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );
    final TransformationStepNode writeToLogOutputChecksum = (TransformationStepNode) subTransStepNodeMap.get(
      "Write to log - output checksum" );

    // ---------- Data grid
    verifyNodes( IteratorUtils.toList( dataGrid.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getNextSteps().iterator() ), testLineageNode( calcChecksum ),
      testLineageNode( writeToLogDataGrid ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getOutputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE ), testFieldNode( RANDOM_INT ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( dataGrid.getAllOutNodes() ) );

    // ---------- mapping - calc checksum
    verifyNodes( IteratorUtils.toList( calcChecksum.getPreviousSteps().iterator() ),
      testLineageNode( dataGrid ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ), testLineageNode( writeToLogCalcChecksum ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getInputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE ), testFieldNode( RANDOM_INT ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 5, getIterableSize( calcChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( calcChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // ---------- Write to log - mapping - calc checksum
    verifyNodes( IteratorUtils.toList( writeToLogCalcChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( RANDOM_INT ), testFieldNode( RANDOM_VALUE ), testFieldNode( NEW_CHECKSUM ) );

    // verify properties
    verifyNodeProperties( calcChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( PROPERTY_PATH, SKIP ).put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
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
      testStepNode( calcChecksum.getName() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( NEW_CHECKSUM ), testFieldNode( RANDOM_VALUE ), testFieldNode( RANDOM_INT ) );
    verifyStepIOLinks( writeToLogChecksum,
      testLineageLink( testFieldNode( CHECKSUM ), LINK_DERIVES, testFieldNode( NEW_CHECKSUM ) ),
      testLineageLink( testFieldNode( VALUE ), LINK_DERIVES, testFieldNode( RANDOM_VALUE ) ) );
    assertEquals( 5, getIterableSize( writeToLogChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( writeToLogChecksum.getAllOutNodes() ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ), testStepNode(
      calcChecksumSubTrans.getName() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ),
      testStepNode(  writeToLogOutputChecksum.getName() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Data grid > outputs > randomValue >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > calc checksum > outputs > value > inputs > Write to log Checksum >
    //   outputs > randomValue
    // - chain 2: Data grid  > outputs > randomValue > derives > Input Checksum:value > derives
    //   > calc checksum:value > derives > output checksum:value > derives > Write to log Checksum:randomValue
    final FramedMetaverseNode dataGrid_output_randomValue =
      verifyLinkedNode( dataGrid, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_randomValue =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, RANDOM_VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( dataGrid_output_randomValue, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksumSubTrans,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksumSubTrans.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertTrue( IteratorUtils.toList( dataGrid_output_randomValue.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( inputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( inputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( calcChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( calcChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( outputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( outputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogChecksum_output_randomValue ) );

    // Verify the following link chains
    // - chain 1: calc checksum > outputs > checksum > inputs > output checksum > output > checksum > inputs > Write to
    //   log Checksum > outputs > newChecksum
    // - chain 2: calc checksum:checksum > derives > output checksum:checksum > derives > Write to log
    //   Checksum:newChecksum
    final FramedMetaverseNode calcChecksum_output_checksum =
      verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_checksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode writeToLogChecksum_output_newChecksum =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, NEW_CHECKSUM );

    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_checksum, LINK_INPUTS, writeToLogChecksum.getName() ) );
    assertEquals( writeToLogChecksum_output_newChecksum, verifyLinkedNode( outputChecksum_output_checksum,
      LINK_DERIVES, NEW_CHECKSUM ) );
  }

  @Test
  public void test_oneIONonMainPathWithMappingsAndExtraField() throws Exception {

    final String transNodeName = "oneIONonMainPathWithMappingsAndExtraField";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 35, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 102, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_INT, RANDOM_INT, RANDOM_INT, RANDOM_VALUE,
        RANDOM_VALUE, RANDOM_VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, CHECKSUM, CHECKSUM, CHECKSUM,
        CHECKSUM, NEW_CHECKSUM } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "Write to log - Data grid", "mapping - calc checksum",
        "Write to log - mapping - calc checksum", "Write to log Checksum" },  false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "calc checksum", "output checksum", "Write to log - Input checksum",
        "Write to log - calc checksum", "Write to log - output checksum" }, false );

    final TransformationStepNode dataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Data grid" );
    final TransformationStepNode writeToLogDataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - Data grid" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "mapping - calc checksum" );
    final TransformationStepNode writeToLogCalcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - mapping - calc checksum" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksumSubTrans = (TransformationStepNode) subTransStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );
    final TransformationStepNode writeToLogOutputChecksum = (TransformationStepNode) subTransStepNodeMap.get(
      "Write to log - output checksum" );

    // ---------- Data grid
    verifyNodes( IteratorUtils.toList( dataGrid.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getNextSteps().iterator() ), testLineageNode( calcChecksum ),
      testLineageNode( writeToLogDataGrid ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getOutputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE ), testFieldNode( RANDOM_INT ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( dataGrid.getAllOutNodes() ) );

    // ---------- mapping - calc checksum
    verifyNodes( IteratorUtils.toList( calcChecksum.getPreviousSteps().iterator() ),
      testLineageNode( dataGrid ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ), testLineageNode( writeToLogCalcChecksum ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getInputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE ), testFieldNode( RANDOM_INT ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 5, getIterableSize( calcChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( calcChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // ---------- Write to log - mapping - calc checksum
    verifyNodes( IteratorUtils.toList( writeToLogCalcChecksum.getOutputStreamFields().iterator() ) );

    // verify properties
    verifyNodeProperties( calcChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( PROPERTY_PATH, SKIP ).put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] description,input [1] update field names,input [1] rename "
        + "[1],output [1],output [1] description,output [1] update field names,output [1] rename [1]" )
      .put( "input [1]", "Data grid > [sub] Input checksum" )
      .put( "input [1] update field names", "true" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "output [1]", "[sub] output checksum > Write to log Checksum" )
      .put( "output [1] rename [1]", "checksum > newChecksum" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- Write to log Checksum
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( NEW_CHECKSUM ), testFieldNode( RANDOM_VALUE ), testFieldNode( RANDOM_INT ) );
    verifyStepIOLinks( writeToLogChecksum,
      testLineageLink( testFieldNode( CHECKSUM ), LINK_DERIVES, testFieldNode( NEW_CHECKSUM ) ),
      testLineageLink( testFieldNode( VALUE ), LINK_DERIVES, testFieldNode( RANDOM_VALUE ) ) );
    assertEquals( 5, getIterableSize( writeToLogChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( writeToLogChecksum.getAllOutNodes() ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ), testStepNode(
      calcChecksumSubTrans.getName() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ),
      testStepNode(  writeToLogOutputChecksum.getName() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Data grid > outputs > randomValue >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > calc checksum > outputs > value > inputs > Write to log Checksum >
    //   outputs > randomValue
    // - chain 2: Data grid  > outputs > randomValue > derives > Input Checksum:value > derives
    //   > calc checksum:value > derives > output checksum:value > derives > Write to log Checksum:randomValue
    final FramedMetaverseNode dataGrid_output_randomValue =
      verifyLinkedNode( dataGrid, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_randomValue =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, RANDOM_VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( dataGrid_output_randomValue, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksumSubTrans,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksumSubTrans.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertTrue( IteratorUtils.toList( dataGrid_output_randomValue.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( inputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( inputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( calcChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( calcChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( outputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( outputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogChecksum_output_randomValue ) );

    // Verify the following link chains
    // - chain 1: calc checksum > outputs > checksum > inputs > output checksum > output > checksum > inputs > Write to
    //   log Checksum > outputs > newChecksum
    // - chain 2: calc checksum:checksum > derives > output checksum:checksum > derives > Write to log
    //   Checksum:newChecksum
    final FramedMetaverseNode calcChecksum_output_checksum =
      verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_checksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode writeToLogChecksum_output_newChecksum =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, NEW_CHECKSUM );

    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_checksum, LINK_INPUTS, writeToLogChecksum.getName() ) );
    assertEquals( writeToLogChecksum_output_newChecksum, verifyLinkedNode( outputChecksum_output_checksum,
      LINK_DERIVES, NEW_CHECKSUM ) );
  }

  @Test
  public void test_oneIOMainPathNoFieldMappings() throws Exception {

    final String transNodeName = "oneIOMainPathNoFieldMappings";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 34, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 100, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { VALUE, VALUE, VALUE, VALUE, VALUE,
        VALUE, VALUE, VALUE, VALUE, VALUE, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "mapping - calc checksum", "Write to log Checksum", "Write to log - Data grid",
        "Write to log - mapping - calc checksum" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "calc checksum", "output checksum" , "Write to log - Input checksum",
        "Write to log - calc checksum", "Write to log - output checksum"}, false );

    final TransformationStepNode dataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Data grid" );
    final TransformationStepNode writeToLogDataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - Data grid" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "mapping - calc checksum" );
    final TransformationStepNode writeToLogCalcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - mapping - calc checksum" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksumSubTrans = (TransformationStepNode) subTransStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );
    final TransformationStepNode writeToLogOutputChecksum = (TransformationStepNode) subTransStepNodeMap.get(
      "Write to log - output checksum" );

    // ---------- Data grid
    verifyNodes( IteratorUtils.toList( dataGrid.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getNextSteps().iterator() ), testLineageNode( calcChecksum ),
      testLineageNode( writeToLogDataGrid ));
    verifyNodes( IteratorUtils.toList( dataGrid.getOutputStreamFields().iterator() ),
      testFieldNode( VALUE ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( dataGrid.getAllOutNodes() ) );

    // ---------- mapping - calc checksum
    verifyNodes( IteratorUtils.toList( calcChecksum.getPreviousSteps().iterator() ),
      testLineageNode( dataGrid ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ), testLineageNode( writeToLogCalcChecksum ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getInputStreamFields().iterator() ),
      testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 4, getIterableSize( calcChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( calcChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // ---------- Write to log - mapping - calc checksum
    verifyNodes( IteratorUtils.toList( writeToLogCalcChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( VALUE ), testFieldNode( CHECKSUM ) );

    // verify properties
    verifyNodeProperties( calcChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( PROPERTY_PATH, SKIP ).put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] update field names,output [1],output [1] update field names" )
      .put( "input [1]", "Data grid > [sub] Input checksum" )
      .put( "input [1] update field names", "false" )
      .put( "output [1]", "[sub] output checksum > Write to log Checksum" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- Write to log Checksum
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyStepIOLinks( writeToLogChecksum,
      testLineageLink( testFieldNode( CHECKSUM ), LINK_DERIVES, testFieldNode( CHECKSUM ) ),
      testLineageLink( testFieldNode( VALUE ), LINK_DERIVES, testFieldNode( VALUE ) ) );
    assertEquals( 5, getIterableSize( writeToLogChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( writeToLogChecksum.getAllOutNodes() ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksumSubTrans.getName() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ),
      testStepNode( writeToLogOutputChecksum.getName() ));
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Data grid > outputs > value >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > input > output checksum > outputs > checksum > outputs > value > inputs >
    //   Write to log Checksum > outputs > value
    // - chain 2: Data grid > outputs > value > derives > Input Checksum:value > derives
    //   > calc checksum:value > derives > output checksum:value > derives > Write to log Checksum:value
    final FramedMetaverseNode dataGrid_output_value = verifyLinkedNode( dataGrid, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_value =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( dataGrid_output_value, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksumSubTrans,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksumSubTrans.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertTrue( IteratorUtils.toList( dataGrid_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( inputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( inputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( calcChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( calcChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( outputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( outputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogChecksum_output_value ) );

    // Verify the following link chains
    // - chain 1: calc checksum > outputs > checksum > inputs > output > output checksum > inputs > Write to log
    //   Checksum > outputs > checksum
    // - chain 2: calc checksum:checksum > derives > output checksum:checksum > derives > Write to log
    //   Checksum:checksum
    final FramedMetaverseNode calcChecksum_output_checksum =
      verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_checksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode writeToLogChecksum_output_checksum =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, CHECKSUM );

    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_checksum, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertTrue( IteratorUtils.toList( outputChecksum_output_checksum.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogChecksum_output_checksum ) );
  }

  @Test
  public void test_oneIONonMainPathNoFieldMappings() throws Exception {

    final String transNodeName = "oneIONonMainPathNoFieldMappings";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 32, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 92, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { VALUE, VALUE, VALUE, VALUE, VALUE,
        VALUE, VALUE, VALUE, VALUE, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM, CHECKSUM } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "mapping - calc checksum", "Write to log Checksum", "Write to log - Data grid",
        "Write to log - mapping - calc checksum" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "calc checksum", "output checksum" , "Write to log - Input checksum",
        "Write to log - calc checksum", "Write to log - output checksum"}, false );

    final TransformationStepNode dataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Data grid" );
    final TransformationStepNode writeToLogDataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - Data grid" );
    final TransformationStepNode calcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "mapping - calc checksum" );
    final TransformationStepNode writeToLogCalcChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - mapping - calc checksum" );
    final TransformationStepNode writeToLogChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log Checksum" );

    // sub-trans nodes within the parent graph
    final TransformationStepNode inputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "Input checksum" );
    final TransformationStepNode calcChecksumSubTrans = (TransformationStepNode) subTransStepNodeMap.get(
      "calc checksum" );
    final TransformationStepNode outputChecksum = (TransformationStepNode) subTransStepNodeMap.get( "output checksum" );
    final TransformationStepNode writeToLogOutputChecksum = (TransformationStepNode) subTransStepNodeMap.get(
      "Write to log - output checksum" );

    // ---------- Data grid
    verifyNodes( IteratorUtils.toList( dataGrid.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getNextSteps().iterator() ), testLineageNode( calcChecksum ),
      testLineageNode( writeToLogDataGrid ));
    verifyNodes( IteratorUtils.toList( dataGrid.getOutputStreamFields().iterator() ),
      testFieldNode( VALUE ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( dataGrid.getAllOutNodes() ) );

    // ---------- mapping - calc checksum
    verifyNodes( IteratorUtils.toList( calcChecksum.getPreviousSteps().iterator() ),
      testLineageNode( dataGrid ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ), testLineageNode( writeToLogCalcChecksum ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getInputStreamFields().iterator() ),
      testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( calcChecksum.getOutputStreamFields().iterator() ) );
    assertEquals( 4, getIterableSize( calcChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( calcChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( calcChecksum.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( calcChecksum.getOutNodes( LINK_EXECUTES ) ) );

    // ---------- Write to log - mapping - calc checksum
    verifyNodes( IteratorUtils.toList( writeToLogCalcChecksum.getOutputStreamFields().iterator() ) );

    // verify properties
    verifyNodeProperties( calcChecksum, new ImmutableMap.Builder<String, Object>()
      .put( PROPERTY_STEP_TYPE, SKIP ).put( "color", SKIP ).put( PROPERTY_PLUGIN_ID, SKIP ).put( PROPERTY_TYPE, SKIP )
      .put( PROPERTY_ANALYZER, SKIP ).put( PROPERTY_CATEGORY, SKIP ).put( PROPERTY_COPIES, SKIP )
      .put( PROPERTY_LOGICAL_ID, SKIP ).put( PROPERTY_NAME, SKIP ).put( PROPERTY_NAMESPACE, SKIP )
      .put( PROPERTY_PATH, SKIP ).put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS, "input [1],input [1] description,input [1] update field names,output [1],output"
        + " [1] description,output [1] update field names" )
      .put( "input [1]", "Data grid > [sub] Input checksum" )
      .put( "input [1] update field names", "false" )
      .put( "output [1]", "[sub] output checksum > Write to log Checksum" )
      .put( "output [1] update field names", "false" ).build() );

    // ---------- Write to log Checksum
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getNextSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( writeToLogChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyStepIOLinks( writeToLogChecksum,
      testLineageLink( testFieldNode( CHECKSUM ), LINK_DERIVES, testFieldNode( CHECKSUM ) ),
      testLineageLink( testFieldNode( VALUE ), LINK_DERIVES, testFieldNode( VALUE ) ) );
    assertEquals( 5, getIterableSize( writeToLogChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( writeToLogChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 2, getIterableSize( writeToLogChecksum.getAllOutNodes() ) );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksumSubTrans.getName() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ),
      testStepNode( writeToLogOutputChecksum.getName() ));
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Data grid > outputs > value >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > input > output checksum > outputs > checksum > outputs > value > inputs >
    //   Write to log Checksum > outputs > value
    // - chain 2: Data grid > outputs > value > derives > Input Checksum:value > derives
    //   > calc checksum:value > derives > output checksum:value > derives > Write to log Checksum:value
    final FramedMetaverseNode dataGrid_output_value = verifyLinkedNode( dataGrid, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_value =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( dataGrid_output_value, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksumSubTrans,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksumSubTrans.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertTrue( IteratorUtils.toList( dataGrid_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( inputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( inputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( calcChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( calcChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( outputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( outputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogChecksum_output_value ) );

    // Verify the following link chains
    // - chain 1: calc checksum > outputs > checksum > inputs > output > output checksum > inputs > Write to log
    //   Checksum > outputs > checksum
    // - chain 2: calc checksum:checksum > derives > output checksum:checksum > derives > Write to log
    //   Checksum:checksum
    final FramedMetaverseNode calcChecksum_output_checksum =
      verifyLinkedNode( calcChecksumSubTrans, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode outputChecksum_output_checksum =
      verifyLinkedNode( outputChecksum, LINK_OUTPUTS, CHECKSUM );
    final FramedMetaverseNode writeToLogChecksum_output_checksum =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, CHECKSUM );

    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_checksum, LINK_INPUTS, writeToLogChecksum.getName() ) );

    assertTrue( IteratorUtils.toList( outputChecksum_output_checksum.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogChecksum_output_checksum ) );
  }

  @Test
  public void test_moreThanOneIOstepsMappingOut() throws Exception {

    final String transNodeName = "moreThanOneIOstepsMappingOut";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( "sub", true );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( "Unexpected number of nodes", 51, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 158, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName, "sub" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { RANDOM_VALUE, RANDOM_VALUE, RANDOM_VALUE,
        RANDOM_VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, CHECKSUM,
        CHECKSUM, CHECKSUM, CHECKSUM, NEW_CHECKSUM, PARITY, PARITY, PARITY, PARITY, NEW_PARITY } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Data grid", "calc parity & checksum",  "Write to log Checksum", "Write to log Parity",
        "Write to log Dummy", "Write to log - Data grid" }, false );

    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Input checksum", "Input parity", "output checksum", "output parity",
        "calc parity", "calc checksum", "Write to log - Input checksum", "Write to log - Input parity",
        "Write to log - output checksum", "Write to log - output parity", "Write to log - calc parity",
        "Write to log - calc checksum" }, false );

    final TransformationStepNode dataGrid = (TransformationStepNode) parentStepNodeMap.get( "Data grid" );
    final TransformationStepNode writeToLogDataGrid = (TransformationStepNode) parentStepNodeMap.get(
      "Write to log - Data grid" );
    final TransformationStepNode calcParityAndChecksum = (TransformationStepNode) parentStepNodeMap.get(
      "calc parity & checksum" );
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
    final TransformationStepNode writeToLogOutputChecksum = (TransformationStepNode) subTransStepNodeMap.get(
      "Write to log - output checksum" );
    final TransformationStepNode calcParity = (TransformationStepNode) subTransStepNodeMap.get( "calc parity" );
    final TransformationStepNode outputParity = (TransformationStepNode) subTransStepNodeMap.get( "output parity" );
    final TransformationStepNode writeToLogOutputParity = (TransformationStepNode) subTransStepNodeMap.get(
      "Write to log - output parity" );

    // ---------- Data grid
    verifyNodes( IteratorUtils.toList( dataGrid.getPreviousSteps().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getNextSteps().iterator() ),
      testStepNode( calcParityAndChecksum.getName() ), testStepNode( writeToLogDataGrid.getName() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getInputStreamFields().iterator() ) );
    verifyNodes( IteratorUtils.toList( dataGrid.getOutputStreamFields().iterator() ), testFieldNode( RANDOM_VALUE ) );
    assertEquals( 2, getIterableSize( dataGrid.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( dataGrid.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( dataGrid.getAllOutNodes() ) );
    assertEquals( 2, getIterableSize( dataGrid.getOutNodes( LINK_HOPSTO ) ) );

    // ---------- calc parity & checksum
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getPreviousSteps().iterator() ),
      testLineageNode( dataGrid ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getNextSteps().iterator() ),
      testLineageNode( writeToLogChecksum ), testLineageNode( writeToLogParity ),
      testLineageNode( writeToLogDummy ) );
    verifyNodes( IteratorUtils.toList( calcParityAndChecksum.getInputStreamFields().iterator() ),
      testFieldNode( RANDOM_VALUE ) );
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
      .put( PROPERTY_PATH, SKIP ).put( NODE_VIRTUAL, SKIP ).put( "subTransformation", SKIP )
      .put( PROPERTY_VERBOSE_DETAILS,
        "input [1],input [1] description,input [1] update field names,input [1] rename [1],"
          + "input [2],input [2] description,input [2] update field names,input [2] rename [1],output [1],"
          + "output [1] description,output [1] update field names,output [1] rename [1],output [2],"
          + "output [2] description,output [2] update field names,output [2] rename [1]" )
      .put( "input [1]", "Data grid > [sub] Input checksum" )
      .put( "input [1] update field names", "true" )
      .put( "input [1] rename [1]", "randomValue > value" )
      .put( "input [2]", "Data grid > [sub] Input parity" )
      .put( "input [2] update field names", "true" )
      .put( "input [2] rename [1]", "randomValue > value" )
      .put( "output [1]", "[sub] output parity > Write to log Parity" )
      .put( "output [1] rename [1]", "parity > newParity" )
      .put( "output [1] update field names", "false" )
      .put( "output [2]", "[sub] output checksum > Write to log Checksum" )
      .put( "output [2] rename [1]", "checksum > newChecksum" )
      .put( "output [2] update field names", "false" ).build() );

    // ---------- output checksum
    verifyNodes( IteratorUtils.toList( outputChecksum.getPreviousSteps().iterator() ),
      testStepNode( calcChecksum.getName() ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getNextSteps().iterator() ),
      testStepNode( writeToLogOutputChecksum.getName()  ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getInputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( outputChecksum.getOutputStreamFields().iterator() ),
      testFieldNode( CHECKSUM ), testFieldNode( VALUE ) );
    assertEquals( 5, getIterableSize( outputChecksum.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputChecksum.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( outputChecksum.getAllOutNodes() ) );

    // ---------- output parity
    verifyNodes( IteratorUtils.toList( outputParity.getPreviousSteps().iterator() ),
      testStepNode( calcParity.getName() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getNextSteps().iterator() ),
      testStepNode( writeToLogOutputParity.getName() ) );
    verifyNodes( IteratorUtils.toList( outputParity.getInputStreamFields().iterator() ),
      testFieldNode( PARITY ), testFieldNode( VALUE ) );
    verifyNodes( IteratorUtils.toList( outputParity.getOutputStreamFields().iterator() ),
      testFieldNode( PARITY ), testFieldNode( VALUE ) );
    assertEquals( 5, getIterableSize( outputParity.getAllInNodes() ) );
    assertEquals( 1, getIterableSize( outputParity.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( outputParity.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 3, getIterableSize( outputParity.getAllOutNodes() ) );

    // Verify the following link chains
    // - chain 1: Data grid > outputs > randomValue >  inputs > Input checksum > outputs > value > inputs >
    //   calc checksum > outputs > value > inputs > output checksum > outputs > value > inputs
    //   > Write to file Checksum > outputs > randomValue
    // - chain 2: Data grid:randomValue > derives > Input Checksum:value > derives > calc checksum:value >
    //   derives > output checksum:value > derives > Write to log Checksum:randomValue
    final FramedMetaverseNode dataGrid_output_randomValue =
      verifyLinkedNode( dataGrid, LINK_OUTPUTS, RANDOM_VALUE );
    final FramedMetaverseNode inputChecksum_output_value = verifyLinkedNode( inputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode calcChecksum_output_value = verifyLinkedNode( calcChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode outputChecksum_output_value = verifyLinkedNode( outputChecksum, LINK_OUTPUTS, VALUE );
    final FramedMetaverseNode writeToLogChecksum_output_randomValue =
      verifyLinkedNode( writeToLogChecksum, LINK_OUTPUTS, RANDOM_VALUE );

    assertEquals( inputChecksum,
      verifyLinkedNode( dataGrid_output_randomValue, LINK_INPUTS, inputChecksum.getName() ) );
    assertEquals( calcChecksum,
      verifyLinkedNode( inputChecksum_output_value, LINK_INPUTS, calcChecksum.getName() ) );
    assertEquals( outputChecksum,
      verifyLinkedNode( calcChecksum_output_value, LINK_INPUTS, outputChecksum.getName() ) );
    assertEquals( writeToLogChecksum,
      verifyLinkedNode( outputChecksum_output_value, LINK_INPUTS, writeToLogChecksum.getName() ) );
    // there are more than one value nodes derived from dataGrid_output_randomValue - make sure one if them
    // is the inputChecksum_output_value node
    final List<FramedMetaverseNode> derivedValueNodes =
      verifyLinkedNodes( dataGrid_output_randomValue, LINK_DERIVES, VALUE );
    assertTrue( derivedValueNodes.contains( inputChecksum_output_value ) );

    assertTrue( IteratorUtils.toList( dataGrid_output_randomValue.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( inputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( inputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( calcChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( calcChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( outputChecksum_output_value ) );
    assertTrue( IteratorUtils.toList( outputChecksum_output_value.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogChecksum_output_randomValue ) );

    // Verify the following link chains
    // - chain 1: calc parity > outputs parity > inputs > output parity > outputs > parity > inputs > Write to file
    //   Parity > outputs > newParity
    // - chain 2: calc parity:parity > derives > output parity:parity > derives > Write to file Checksum:parity
    final FramedMetaverseNode calcParity_output_parity = verifyLinkedNode( calcParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode outputParity_output_parity = verifyLinkedNode( outputParity, LINK_OUTPUTS, PARITY );
    final FramedMetaverseNode writeToLogParity_output_newParity =
      verifyLinkedNode( writeToLogParity, LINK_OUTPUTS, NEW_PARITY );

    assertEquals( outputParity,
      verifyLinkedNode( calcParity_output_parity, LINK_INPUTS, outputParity.getName() ) );
    assertEquals( writeToLogParity,
      verifyLinkedNode( outputParity_output_parity, LINK_INPUTS, writeToLogParity.getName() ) );

    assertTrue( IteratorUtils.toList( calcParity_output_parity.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( outputParity_output_parity ) );
    assertTrue( IteratorUtils.toList( outputParity_output_parity.getOutNodes( LINK_DERIVES ).iterator() )
      .contains( writeToLogParity_output_newParity ) );
  }
}
