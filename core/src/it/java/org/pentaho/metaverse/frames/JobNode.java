/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metaverse.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;

/**
 * User: RFellows Date: 9/4/14
 */
public interface JobNode extends KettleNode {
  @Adjacency( label = "contains", direction = Direction.OUT )
  public Iterable<JobEntryNode> getJobEntryNodes();

  @GremlinGroovy( "it.out('contains').has( 'name', T.eq, name )" )
  public JobEntryNode getJobEntryNode( @GremlinParam( "name" ) String name );

}
