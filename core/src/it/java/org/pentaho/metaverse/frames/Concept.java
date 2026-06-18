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
public class Concept extends FramedMetaverseNode {
  public Concept( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public FramedMetaverseNode getEntity() {
    List<Vertex> result = graph.traversal().V( vertex.id() ).in().has( "type", "Entity" ).toList();
    return result.isEmpty() ? null : wrapNode( result.get( 0 ) );
  }
}
