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

/**
 * User: RFellows Date: 9/4/14
 */
public interface FileInputStepNode extends TransformationStepNode {
  @Adjacency( label = "isreadby", direction = Direction.IN )
  Iterable<FramedMetaverseNode> getInputFiles();

  @Adjacency( label = "uses", direction = Direction.OUT )
  Iterable<FileFieldNode> getFileFieldNodesUses();
}
