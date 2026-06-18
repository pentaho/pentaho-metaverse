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
public class JobNode extends KettleNode {
  public JobNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<JobEntryNode> getJobEntryNodes() {
    return wrapAs( vertex.vertices( Direction.OUT, "contains" ), v -> new JobEntryNode( v, graph ) );
  }

  public JobEntryNode getJobEntryNode( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() ).out( "contains" ).has( "name", name ).toList();
    return result.isEmpty() ? null : new JobEntryNode( result.get( 0 ), graph );
  }
}
