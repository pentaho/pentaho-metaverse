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
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;

/**
 * Created by rfellows on 5/22/15.
 */
public interface MongoDbInputStepNode extends TransformationStepNode {
  @Adjacency( label = "dependencyof", direction = Direction.IN )
  public Iterable<MongoConnectionNode> getDatasources();

  @GremlinGroovy( "it.in('dependencyof').has( 'name', T.eq, name )" )
  public MongoConnectionNode getDatasource( @GremlinParam( "name") String name );

  @Adjacency( label = "isreadby", direction = Direction.IN )
  public FramedMetaverseNode getCollection();

}
