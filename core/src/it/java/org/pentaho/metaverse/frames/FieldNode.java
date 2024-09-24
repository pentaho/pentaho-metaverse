/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.dictionary.DictionaryConst;

/**
 * User: RFellows Date: 9/4/14
 */
public interface FieldNode extends Concept {
  @Property( DictionaryConst.PROPERTY_KETTLE_TYPE )
  String getKettleType();

  @Property( DictionaryConst.PROPERTY_OPERATIONS )
  String getOperations();

  @Adjacency( label = "uses", direction = Direction.IN )
  Iterable<TransformationStepNode> getStepsThatUseMe();

  @Adjacency( label = "deletes", direction = Direction.IN )
  TransformationStepNode getStepThatDeletesMe();

  @Adjacency( label = "creates", direction = Direction.IN )
  TransformationStepNode getStepThatCreatesMe();

  @Adjacency( label = "outputs", direction = Direction.IN )
  TransformationStepNode getStepThatOutputsMe();

  @Adjacency( label = "inputs", direction = Direction.OUT )
  TransformationStepNode getStepThatInputsMe();

  @Adjacency( label = "populates", direction = Direction.IN )
  FieldNode getFieldPopulatesMe();
}
