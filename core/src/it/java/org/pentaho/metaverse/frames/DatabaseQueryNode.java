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
 * Created by rfellows on 5/29/15.
 */
public interface DatabaseQueryNode extends FramedMetaverseNode {
  @Adjacency( label = "isreadby", direction = Direction.OUT )
  public Iterable<TransformationStepNode> getStepNodes();

  @Adjacency( label = "contains", direction = Direction.OUT )
  public Iterable<DatabaseColumnNode> getDatabaseColumns();

  @Property( "query" )
  public String getQuery();

}
