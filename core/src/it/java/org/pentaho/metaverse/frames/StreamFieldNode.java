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

/**
 * User: RFellows Date: 9/4/14
 */
public interface StreamFieldNode extends FieldNode {
  @Adjacency( label = "derives", direction = Direction.OUT )
  public Iterable<StreamFieldNode> getFieldNodesDerivedFromMe();

  @Adjacency( label = "derives", direction = Direction.IN )
  public Iterable<StreamFieldNode> getFieldNodesThatDeriveMe();

  @Adjacency( label = "joins", direction = Direction.IN )
  public Iterable<StreamFieldNode> getFieldNodesThatJoinToMe();

  @Adjacency( label = "joins", direction = Direction.OUT )
  public Iterable<StreamFieldNode> getFieldNodesThatIJoinTo();
}
