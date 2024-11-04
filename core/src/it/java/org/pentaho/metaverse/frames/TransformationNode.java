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

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;

/**
 * User: RFellows Date: 9/4/14
 */
public interface TransformationNode extends KettleNode {
  @GremlinGroovy( "it.out('contains').hasNot('virtual', true)" )
  Iterable<TransformationStepNode> getStepNodes();

  @GremlinGroovy( "it.out('contains').has('virtual', true)" )
  Iterable<TransformationStepNode> getVirtualStepNodes();

  @GremlinGroovy( "it.out('contains').has( 'name', T.eq, name )" )
  TransformationStepNode getStepNode( @GremlinParam( "name" ) String name );

  @GremlinGroovy( "it.in('contains').has( 'type', T.eq, 'JobEntry' )" )
  Iterable<JobEntryNode> getJobEntriesThatExecuteMe();
}

