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

/**
 * User: RFellows Date: 9/4/14
 */
public interface TransformationStepNode extends Concept {
  @Property( "stepType" )
  String getStepType();

  @Adjacency( label = "contains", direction = Direction.IN )
  TransformationNode getTransNode();

  @Adjacency( label = "deletes", direction = Direction.OUT )
  Iterable<StreamFieldNode> getStreamFieldNodesDeletes();

  @Adjacency( label = "creates", direction = Direction.OUT )
  Iterable<StreamFieldNode> getStreamFieldNodesCreates();

  @Adjacency( label = "uses", direction = Direction.OUT )
  Iterable<StreamFieldNode> getStreamFieldNodesUses();

  @Adjacency( label = "hops_to", direction = Direction.OUT )
  Iterable<TransformationStepNode> getNextSteps();

  @Adjacency( label = "hops_to", direction = Direction.IN )
  Iterable<TransformationStepNode> getPreviousSteps();

  @Adjacency( label = "inputs", direction = Direction.IN )
  Iterable<FieldNode> getInputFields();

  @Adjacency( label = "inputs", direction = Direction.IN )
  Iterable<StreamFieldNode> getInputStreamFields();

  @Adjacency( label = "outputs", direction = Direction.OUT )
  Iterable<StreamFieldNode> getOutputStreamFields();

  @Adjacency( label = "writesto", direction = Direction.OUT )
  Iterable<Concept> getWritesToNodes();

  @Adjacency( label = "writesto", direction = Direction.OUT )
  Iterable<FileNode> getWritesToFileNodes();

  @Adjacency( label = "isreadby", direction = Direction.IN )
  Iterable<Concept> getReadByNodes();

}
