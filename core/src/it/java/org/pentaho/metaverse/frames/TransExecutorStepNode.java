/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.frames;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.metaverse.analyzer.kettle.step.transexecutor.TransExecutorStepAnalyzer;

import java.util.List;

/**
 * Created by rfellows on 4/3/15.
 */
public class TransExecutorStepNode extends TransformationStepNode {
  public TransExecutorStepNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public TransformationNode getTransToExecute() {
    return wrapSingle( vertex.vertices( Direction.OUT, "executes" ), v -> new TransformationNode( v, graph ) );
  }

  public String getExecutionResultsTargetStepName() {
    return getStringValue( TransExecutorStepAnalyzer.EXECUTION_RESULTS_TARGET );
  }

  public String getOutputRowsTargetStepName() {
    return getStringValue( TransExecutorStepAnalyzer.OUTPUT_ROWS_TARGET );
  }

  public String getResultFilesTargetStepName() {
    return getStringValue( TransExecutorStepAnalyzer.RESULT_FILES_TARGET );
  }

  public TransformationStepNode getOutputStepByName( String outputStepName ) {
    List<Vertex> result = graph.traversal().V( vertex.id() ).out( "hops_to" ).has( "name", outputStepName ).toList();
    return result.isEmpty() ? null : new TransformationStepNode( result.get( 0 ), graph );
  }
}
