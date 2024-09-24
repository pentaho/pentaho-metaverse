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

package org.pentaho.metaverse.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import org.pentaho.metaverse.analyzer.kettle.step.transexecutor.TransExecutorStepAnalyzer;

/**
 * Created by rfellows on 4/3/15.
 */
public interface TransExecutorStepNode extends TransformationStepNode {

  @Adjacency( label = "executes", direction = Direction.OUT )
  public TransformationNode getTransToExecute();

  @Property( TransExecutorStepAnalyzer.EXECUTION_RESULTS_TARGET )
  public String getExecutionResultsTargetStepName();

  @Property( TransExecutorStepAnalyzer.OUTPUT_ROWS_TARGET )
  public String getOutputRowsTargetStepName();

  @Property( TransExecutorStepAnalyzer.RESULT_FILES_TARGET )
  public String getResultFilesTargetStepName();

  @GremlinGroovy( "it.out( 'hops_to' ).has( 'name', T.eq, outputStepName )" )
  public TransformationStepNode getOutputStepByName( @GremlinParam( "outputStepName" ) String outputStepName );

}
