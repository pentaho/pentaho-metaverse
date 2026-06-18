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

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * User: RFellows Date: 9/4/14
 */
public class TransformationNode extends KettleNode {
  public TransformationNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<TransformationStepNode> getStepNodes() {
    List<Vertex> result = graph.traversal().V( vertex.id() ).out( "contains" )
      .filter( tv -> !Boolean.TRUE.equals(
        tv.get().property( "virtual" ).isPresent() ? tv.get().<Boolean>value( "virtual" ) : null ) )
      .toList();
    return wrapAs( result.iterator(), v -> new TransformationStepNode( v, graph ) );
  }

  public List<TransformationStepNode> getVirtualStepNodes() {
    return wrapAs( graph.traversal().V( vertex.id() ).out( "contains" ).has( "virtual", true ).toList().iterator(),
      v -> new TransformationStepNode( v, graph ) );
  }

  public TransformationStepNode getStepNode( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() ).out( "contains" ).has( "name", name ).toList();
    return result.isEmpty() ? null : new TransformationStepNode( result.get( 0 ), graph );
  }

  public List<JobEntryNode> getJobEntriesThatExecuteMe() {
    List<Vertex> result = graph.traversal().V( vertex.id() ).in( "contains" ).has( "type", "JobEntry" ).toList();
    return wrapAs( result.iterator(), v -> new JobEntryNode( v, graph ) );
  }
}
