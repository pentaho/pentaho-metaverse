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
public class StreamFieldNode extends FieldNode {
  public StreamFieldNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<StreamFieldNode> getFieldNodesDerivedFromMe() {
    return wrapAs( vertex.vertices( Direction.OUT, "derives" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<StreamFieldNode> getFieldNodesThatDeriveMe() {
    return wrapAs( vertex.vertices( Direction.IN, "derives" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<StreamFieldNode> getFieldNodesThatJoinToMe() {
    return wrapAs( vertex.vertices( Direction.IN, "joins" ), v -> new StreamFieldNode( v, graph ) );
  }

  public List<StreamFieldNode> getFieldNodesThatIJoinTo() {
    return wrapAs( vertex.vertices( Direction.OUT, "joins" ), v -> new StreamFieldNode( v, graph ) );
  }
}
