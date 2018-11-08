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
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS;
import static org.pentaho.dictionary.DictionaryConst.LINK_EXECUTES;
import static org.pentaho.dictionary.DictionaryConst.LINK_TYPE_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_TRANS;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_TRANS_FIELD;

// TODO: Ignored for now, remove the @Ignore annotation once https://jira.pentaho.com/browse/ENGOPS-4612 is resolved
@Ignore
public class JMSConsumerStepAnalyzerValidationIT extends StepAnalyzerValidationIT {
  @Test
  public void testOneOIWithMappings() throws Exception {
    final String transNodeName = "parent-jms-consumer";
    final String subTransNodeName = "sub-trans-jms-consumer";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    final TransformationNode subTransNode = verifyTransformationNode( subTransNodeName, true );

    // assert that nodes/edges have expected quantities
    assertEquals( "Unexpected number of nodes", 25, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 64, getIterableSize( framedGraph.getEdges() ) );

    verifyNodesTypes( ImmutableMap.of(
      NODE_TYPE_TRANS, Arrays.asList( transNodeName, subTransNodeName ),
      NODE_TYPE_TRANS_FIELD, Arrays.asList( "message", "message", "message", "message", "message", "message",
        "message_1", "message_1", "message_1", "message_1", "destination", "destination" ) ) );

    // verify individual step nodes in parent
    final Map<String, FramedMetaverseNode> parentStepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "JMS Consumer", "Write to log" }, false );

    // Verify individual step nodes in subtrans
    final Map<String, FramedMetaverseNode> subTransStepNodeMap = verifyTransformationSteps( subTransNode,
      new String[] { "Get records from stream", "Sort rows", "Group by", "Write to log" }, false );

    final TransformationStepNode jmsConsumer = (TransformationStepNode) parentStepNodeMap.get( "JMS Consumer" );
    final TransformationStepNode writeLog = (TransformationStepNode) parentStepNodeMap.get( "Write to log" );

    // verify that JMS Consumer node has proper outputs and correct type nodes associated with it
    verifyNodes( IteratorUtils.toList( jmsConsumer.getNextSteps().iterator() ), testStepNode( writeLog.getName() ) );
    verifyNodes( IteratorUtils.toList( jmsConsumer.getOutputStreamFields().iterator() ),
      testFieldNode( "message", false ), testFieldNode( "message_1", false ) );
    assertEquals( 1, getIterableSize( jmsConsumer.getInNodes( LINK_CONTAINS ) ) );
    assertEquals( 1, getIterableSize( jmsConsumer.getInNodes( LINK_TYPE_CONCEPT ) ) );
    assertEquals( 4, getIterableSize( jmsConsumer.getAllOutNodes() ) );
    assertEquals( 1, getIterableSize( jmsConsumer.getOutNodes( LINK_EXECUTES ) ) );
  }
}