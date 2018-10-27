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
public class TableOutputValidationIT extends StepAnalyzerValidationIT {

  @Override
  protected String getRootFolder() {
    return "src/it/resources/repo/table-output-validation/";
  }

  @Test
  public void testTableIoBogusDbFieldUnchecked() throws Exception {

    final String transNodeName = "table_io_bogus_db_fields_unchecked";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( 26, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( 62, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName } ),
      NODE_TYPE_DATASOURCE, Arrays.asList( new String[] { "Postgres - localhost" } ),
      NODE_TYPE_DATA_TABLE, Arrays.asList( new String[] { "people" } ),
      NODE_TYPE_DATA_COLUMN, Arrays.asList( new String[] { "first_name", "first_name", "last_name", "last_name" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "first_name", "first_name", "first_name", "last_name",
        "last_name", "last_name" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> stepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Table input", "Select values", "Table output" }, false );

    // ---------- Table input
    final TransformationStepNode tableInput = (TransformationStepNode) stepNodeMap.get( "Table input" );
    verifyNodes( IteratorUtils.toList( tableInput.getReadByNodes().iterator() ), testSqlQueryNode( "SQL", false ) );
    verifyNodes( IteratorUtils.toList( tableInput.getNextSteps().iterator() ), testStepNode( "Select values", false ) );
    verifyNodes( IteratorUtils.toList( tableInput.getInputFields().iterator() ),
      testColumnNode( "first_name", false ), testColumnNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( tableInput.getOutputStreamFields().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyStepIOLinks( tableInput,
      testLineageLink( testColumnNode( "first_name", false ), LINK_POPULATES, testFieldNode( "first_name", false ) ),
      testLineageLink( testColumnNode( "last_name", false ), LINK_POPULATES, testFieldNode( "last_name", false ) ) );
    final FramedMetaverseNode sqlNode = (FramedMetaverseNode) IteratorUtils.toList(
      tableInput.getReadByNodes().iterator() ).get( 0 );
    verifyNodes( IteratorUtils.toList( sqlNode.getContainedNodes().iterator() ),
      testColumnNode( "first_name", false ), testColumnNode( "last_name", false ) );

    // ---------- Select values
    final TransformationStepNode selectValues = (TransformationStepNode) stepNodeMap.get( "Select values" );
    verifyNodes( IteratorUtils.toList( selectValues.getPreviousSteps().iterator() ),
      testStepNode( "Table input", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getNextSteps().iterator() ),
      testStepNode( "Table output", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getInputStreamFields().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getOutputStreamFields().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getStreamFieldNodesUses().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyStepIOLinks( selectValues,
      testLineageLink( testFieldNode( "first_name", false ), LINK_DERIVES, testFieldNode( "first_name", false ) ),
      testLineageLink( testFieldNode( "last_name", false ), LINK_DERIVES, testFieldNode( "last_name", false ) ) );

    // ---------- Table output
    final TransformationStepNode tableOutput = (TransformationStepNode) stepNodeMap.get( "Table output" );
    verifyNodes( IteratorUtils.toList( tableOutput.getPreviousSteps().iterator() ),
      testStepNode( "Select values", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getWritesToNodes().iterator() ), testTableNode( "people", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getInputStreamFields().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getOutputStreamFields().iterator() ),
      testFieldNode( "first_name", false ), testColumnNode( "first_name", false ),
      testFieldNode( "last_name", false ), testColumnNode( "last_name", false ) );
    verifyStepIOLinks( tableOutput,
      testLineageLink( testFieldNode( "first_name", false ), LINK_DERIVES, testFieldNode( "first_name", false ) ),
      testLineageLink( testFieldNode( "first_name", false ), LINK_POPULATES, testColumnNode( "first_name", false ) ),
      testLineageLink( testFieldNode( "last_name", false ), LINK_DERIVES, testFieldNode( "last_name", false ) ),
      testLineageLink( testFieldNode( "last_name", false ), LINK_POPULATES, testColumnNode( "last_name", false ) ) );
    final FramedMetaverseNode people = (FramedMetaverseNode) IteratorUtils.toList(
      tableOutput.getWritesToNodes().iterator() ).get( 0 );
    verifyNodes( IteratorUtils.toList( people.getContainedNodes().iterator() ),
      testColumnNode( "first_name", false ), testColumnNode( "last_name", false ) );
  }

  @Test
  public void testTableIoDbFieldRenamedAndListTrimmed() throws Exception {

    final String transNodeName = "table_io_db_field_renamed_and_list_trimmed";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );

    // smoke test - verify that the right number of nodes and edges exist in the graph and that the expected top
    // level nodes of expected types exist
    assertEquals( 28, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( 69, getIterableSize( framedGraph.getEdges() ) );
    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( new String[] { transNodeName } ),
      NODE_TYPE_DATASOURCE, Arrays.asList( new String[] { "Postgres - localhost" } ),
      NODE_TYPE_DATA_TABLE, Arrays.asList( new String[] { "people" } ),
      NODE_TYPE_DATA_COLUMN, Arrays.asList( new String[] { "first_name", "first_name", "last_name" } ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( new String[] { "first_name", "first_name_renamed", "first_name_renamed",
        "first_name_renamed", "last_name", "last_name", "last_name", "last_name" } ) ) );

    // verify individual step nodes
    final Map<String, FramedMetaverseNode> stepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Table input", "Select values", "Table output", "Dummy (do nothing)" }, false );

    // ---------- Table input
    final TransformationStepNode tableInput = (TransformationStepNode) stepNodeMap.get( "Table input" );
    verifyNodes( IteratorUtils.toList( tableInput.getReadByNodes().iterator() ), testSqlQueryNode( "SQL", false ) );
    verifyNodes( IteratorUtils.toList( tableInput.getNextSteps().iterator() ), testStepNode( "Select values", false ) );
    verifyNodes( IteratorUtils.toList( tableInput.getInputFields().iterator() ),
      testColumnNode( "first_name", false ), testColumnNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( tableInput.getOutputStreamFields().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyStepIOLinks( tableInput,
      testLineageLink( testColumnNode( "first_name", false ), LINK_POPULATES, testFieldNode( "first_name", false ) ),
      testLineageLink( testColumnNode( "last_name", false ), LINK_POPULATES, testFieldNode( "last_name", false ) ) );
    final FramedMetaverseNode sqlNode = (FramedMetaverseNode) IteratorUtils.toList(
      tableInput.getReadByNodes().iterator() ).get( 0 );
    verifyNodes( IteratorUtils.toList( sqlNode.getContainedNodes().iterator() ),
      testColumnNode( "first_name", false ), testColumnNode( "last_name", false ) );

    // ---------- Select values
    final TransformationStepNode selectValues = (TransformationStepNode) stepNodeMap.get( "Select values" );
    verifyNodes( IteratorUtils.toList( selectValues.getPreviousSteps().iterator() ),
      testStepNode( "Table input", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getNextSteps().iterator() ),
      testStepNode( "Table output", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getInputStreamFields().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getOutputStreamFields().iterator() ),
      testFieldNode( "first_name_renamed", false ), testFieldNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( selectValues.getStreamFieldNodesUses().iterator() ),
      testFieldNode( "first_name", false ), testFieldNode( "last_name", false ) );
    verifyStepIOLinks( selectValues, Arrays.asList( new TestLineageLink[] {
      testLineageLink( testFieldNode( "first_name", false ), LINK_DERIVES,
        testFieldNode( "first_name_renamed", false ) ),
      testLineageLink( testFieldNode( "last_name", false ), LINK_DERIVES, testFieldNode( "last_name", false ) )
    } ) );

    // ---------- Table output
    final TransformationStepNode tableOutput = (TransformationStepNode) stepNodeMap.get( "Table output" );
    verifyNodes( IteratorUtils.toList( tableOutput.getPreviousSteps().iterator() ),
      testStepNode( "Select values", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getNextSteps().iterator() ),
      testStepNode( "Dummy (do nothing)", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getWritesToNodes().iterator() ), testTableNode( "people", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getInputStreamFields().iterator() ),
      testFieldNode( "first_name_renamed", false ), testFieldNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getOutputStreamFields().iterator() ),
      testFieldNode( "first_name_renamed", false ), testColumnNode( "first_name", false ),
      testFieldNode( "last_name", false ) );
    verifyStepIOLinks( tableOutput,
      testLineageLink( testFieldNode( "first_name_renamed", false ),
        LINK_DERIVES, testFieldNode( "first_name_renamed", false ) ),
      testLineageLink( testFieldNode( "first_name_renamed", false ),
        LINK_POPULATES, testColumnNode( "first_name", false ) ),
      testLineageLink( testFieldNode( "last_name", false ), LINK_DERIVES, testFieldNode( "last_name", false ) ) );
    final FramedMetaverseNode people = (FramedMetaverseNode) IteratorUtils.toList(
      tableOutput.getWritesToNodes().iterator() ).get( 0 );
    verifyNodes( IteratorUtils.toList( people.getContainedNodes().iterator() ), testColumnNode( "first_name", false ) );

    // ---------- Table output
    final TransformationStepNode dummy = (TransformationStepNode) stepNodeMap.get( "Dummy (do nothing)" );
    verifyNodes( IteratorUtils.toList( dummy.getPreviousSteps().iterator() ), testStepNode( "Table output", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getInputStreamFields().iterator() ),
      testFieldNode( "first_name_renamed", false ), testFieldNode( "last_name", false ) );
    verifyNodes( IteratorUtils.toList( tableOutput.getOutputStreamFields().iterator() ),
      testFieldNode( "first_name_renamed", false ), testColumnNode( "first_name", false ),
      testFieldNode( "last_name", false ) );
    verifyStepIOLinks( tableOutput,
      testLineageLink( testFieldNode( "first_name_renamed", false ),
        LINK_DERIVES, testFieldNode( "first_name_renamed", false ) ),
      testLineageLink( testFieldNode( "last_name", false ), LINK_DERIVES, testFieldNode( "last_name", false ) ) );
  }
}
