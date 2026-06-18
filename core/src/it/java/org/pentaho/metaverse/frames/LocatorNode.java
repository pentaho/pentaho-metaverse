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
public class LocatorNode extends Concept {
  public LocatorNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public String getLastScan() {
    return getStringValue( "lastScan" );
  }

  public String getUrl() {
    return getStringValue( "url" );
  }

  public List<KettleNode> getDocuments() {
    return wrapAs( vertex.vertices( Direction.OUT, "contains" ), v -> new KettleNode( v, graph ) );
  }
}
