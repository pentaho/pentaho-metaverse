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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;

/**
 * Created by rfellows on 5/29/15.
 */
public interface TableInputStepNode extends TransformationStepNode {
  @Adjacency( label = "dependencyof", direction = Direction.IN )
  public Iterable<DatasourceNode> getDatasources();

  @GremlinGroovy( "it.in('dependencyof').has( 'name', T.eq, name )" )
  public DatasourceNode getDatasource( @GremlinParam( "name") String name );

  @Adjacency( label = "isreadby", direction = Direction.IN )
  public DatabaseQueryNode getDatabaseQueryNode();

}
