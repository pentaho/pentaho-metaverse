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

/**
 * User: RFellows Date: 9/4/14
 */
public interface DatasourceNode extends Concept {
  @Property( "port" )
  public String getPort();
  @Property( "host" )
  public String getHost();
  @Property( "userName" )
  public String getUserName();
  @Property( "password" )
  public String getPassword();
  @Property( "accessType" )
  public Integer getAccessType();
  @Property( "accessTypeDesc" )
  public String getAccessTypeDesc();
  @Property( "databaseName" )
  public String getDatabaseName();

  @Adjacency( label = "dependencyof", direction = Direction.OUT )
  public Iterable<TransformationStepNode> getTransformationStepNodes();

}
