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
import org.pentaho.dictionary.DictionaryConst;

import java.util.List;

/**
 * User: RFellows Date: 9/4/14
 */
public class FieldNode extends Concept {
  public FieldNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public String getKettleType() {
    return getStringValue( DictionaryConst.PROPERTY_KETTLE_TYPE );
  }

  public String getOperations() {
    return getStringValue( DictionaryConst.PROPERTY_OPERATIONS );
  }

  public List<TransformationStepNode> getStepsThatUseMe() {
    return wrapAs( vertex.vertices( Direction.IN, "uses" ), v -> new TransformationStepNode( v, graph ) );
  }

  public TransformationStepNode getStepThatDeletesMe() {
    return wrapSingle( vertex.vertices( Direction.IN, "deletes" ), v -> new TransformationStepNode( v, graph ) );
  }

  public TransformationStepNode getStepThatCreatesMe() {
    return wrapSingle( vertex.vertices( Direction.IN, "creates" ), v -> new TransformationStepNode( v, graph ) );
  }

  public TransformationStepNode getStepThatOutputsMe() {
    return wrapSingle( vertex.vertices( Direction.IN, "outputs" ), v -> new TransformationStepNode( v, graph ) );
  }

  public TransformationStepNode getStepThatInputsMe() {
    return wrapSingle( vertex.vertices( Direction.OUT, "inputs" ), v -> new TransformationStepNode( v, graph ) );
  }

  public FieldNode getFieldPopulatesMe() {
    return wrapSingle( vertex.vertices( Direction.IN, "populates" ), v -> new FieldNode( v, graph ) );
  }
}
