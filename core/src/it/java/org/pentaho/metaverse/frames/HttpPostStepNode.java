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
 * User: wseyler Date: 5/11/15
 */
public interface HttpPostStepNode extends TransformationStepNode {
  
  @Adjacency( label = "isreadby", direction = Direction.IN )
  public Iterable<FramedMetaverseNode> getInputUrls();
}
