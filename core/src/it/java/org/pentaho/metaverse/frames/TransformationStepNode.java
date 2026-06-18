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

import java.util.List;

/**
 * User: RFellows Date: 9/4/14
 */
public class TransformationStepNode extends Concept {
  public TransformationStepNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public String getStepType() {
    return getStringValue( "stepType" );
  }

  public TransformationNode getTransNode() {
    return wrapSingle( vertex.vertices( Direction.IN, "contains" ), v -> new TransformationNode( v, graph ) );
  }

  public List<StreamFieldNode> getStreamFieldNodesDeletes() {
    return wrapAs( vertex.vertices( Direction.OUT, "deletes" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<StreamFieldNode> getStreamFieldNodesCreates() {
    return wrapAs( vertex.vertices( Direction.OUT, "creates" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<StreamFieldNode> getStreamFieldNodesUses() {
    return wrapAs( vertex.vertices( Direction.OUT, "uses" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<TransformationStepNode> getNextSteps() {
    return wrapAs( vertex.vertices( Direction.OUT, "hops_to" ), v -> new TransformationStepNode( v, graph ) );
  }

  public List<TransformationStepNode> getPreviousSteps() {
    return wrapAs( vertex.vertices( Direction.IN, "hops_to" ), v -> new TransformationStepNode( v, graph ) );
  }

  public List<FieldNode> getInputFields() {
    return wrapAs( vertex.vertices( Direction.IN, "inputs" ), v -> new FieldNode( v, graph ) );
  }

  public List<StreamFieldNode> getInputStreamFields() {
    return wrapAs( vertex.vertices( Direction.IN, "inputs" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<StreamFieldNode> getOutputStreamFields() {
    return wrapAs( vertex.vertices( Direction.OUT, "outputs" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<Concept> getWritesToNodes() {
    return wrapAsConcept( vertex.vertices( Direction.OUT, "writesto" ) );
  }

  public List<FileNode> getWritesToFileNodes() {
    return wrapAs( vertex.vertices( Direction.OUT, "writesto" ), v -> new FileNode( v, graph ) );
  }

  public List<Concept> getReadByNodes() {
    return wrapAsConcept( vertex.vertices( Direction.IN, "isreadby" ) );
  }
}
