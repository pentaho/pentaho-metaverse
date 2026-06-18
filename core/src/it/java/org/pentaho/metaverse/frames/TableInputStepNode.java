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
public class TableInputStepNode extends TransformationStepNode {
  public TableInputStepNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<DatasourceNode> getDatasources() {
    return wrapAs( vertex.vertices( Direction.IN, "dependencyof" ), v -> new DatasourceNode( v, graph ) );
  }

  public DatasourceNode getDatasource( String name ) {
    List<Vertex> result = graph.traversal().V( vertex.id() ).in( "dependencyof" ).has( "name", name ).toList();
    return result.isEmpty() ? null : new DatasourceNode( result.get( 0 ), graph );
  }

  public DatabaseQueryNode getDatabaseQueryNode() {
    return wrapSingle( vertex.vertices( Direction.IN, "isreadby" ), v -> new DatabaseQueryNode( v, graph ) );
  }
}
