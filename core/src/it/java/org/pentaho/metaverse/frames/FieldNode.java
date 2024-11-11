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
