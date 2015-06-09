/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

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
  public String getKettleType();

  @Property( DictionaryConst.PROPERTY_OPERATIONS )
  public String getOperations();

  @Adjacency( label = "uses", direction = Direction.IN )
  public Iterable<TransformationStepNode> getStepsThatUseMe();

  @Adjacency( label = "deletes", direction = Direction.IN )
  public TransformationStepNode getStepThatDeletesMe();

  @Adjacency( label = "creates", direction = Direction.IN )
  public TransformationStepNode getStepThatCreatesMe();

  @Adjacency( label = "outputs", direction = Direction.IN )
  public TransformationStepNode getStepThatOutputsMe();

  @Adjacency( label = "inputs", direction = Direction.OUT )
  public TransformationStepNode getStepThatInputsMe();

  @Adjacency( label = "populates", direction = Direction.OUT )
  public FieldNode getFieldPopulatedByMe();

  @Adjacency( label = "populates", direction = Direction.IN )
  public FieldNode getFieldPopulatesMe();
}
