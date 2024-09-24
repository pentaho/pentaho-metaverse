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
import com.tinkerpop.frames.Property;

/**
 * User: RFellows Date: 9/4/14
 */
public interface LocatorNode extends FramedMetaverseNode {
  @Property( "lastScan" )
  public String getLastScan();

  @Property( "url" )
  public String getUrl();

  @Adjacency( label = "contains", direction = Direction.OUT )
  public Iterable<KettleNode> getDocuments();
}
