/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
