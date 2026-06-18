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
 * Created by rfellows on 5/29/15.
 */
public class DatabaseQueryNode extends Concept {
  public DatabaseQueryNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<TransformationStepNode> getStepNodes() {
    return wrapAs( vertex.vertices( Direction.OUT, "isreadby" ), v -> new TransformationStepNode( v, graph ) );
  }

  public List<DatabaseColumnNode> getDatabaseColumns() {
    return wrapAs( vertex.vertices( Direction.OUT, "contains" ), v -> new DatabaseColumnNode( v, graph ) );
  }

  public String getQuery() {
    return getStringValue( "query" );
  }
}
