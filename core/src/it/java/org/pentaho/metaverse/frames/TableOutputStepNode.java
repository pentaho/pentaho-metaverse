/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import org.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputStepAnalyzer;

/**
 * User: RFellows Date: 9/4/14
 */
public interface TableOutputStepNode extends TransformationStepNode {
  @Adjacency( label = "dependencyof", direction = Direction.IN )
  public Iterable<DatasourceNode> getDatasources();

  @GremlinGroovy( "it.in('dependencyof').has( 'name', T.eq, name )" )
  public DatasourceNode getDatasource( @GremlinParam( "name") String name );

  @Adjacency( label = "writesto", direction = Direction.OUT )
  public DatabaseTableNode getDatabaseTable();

  @Property( "schema" )
  public String getSchema();

  @Property( TableOutputStepAnalyzer.TRUNCATE_TABLE )
  public Boolean isTruncateTable();
}
