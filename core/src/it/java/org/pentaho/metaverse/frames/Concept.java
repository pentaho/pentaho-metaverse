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

/**
 * User: RFellows Date: 9/4/14
 */
public interface Concept extends FramedMetaverseNode {
  @GremlinGroovy( "it.in.has( 'type', T.eq, 'Entity' )" )
  FramedMetaverseNode getEntity();
}
