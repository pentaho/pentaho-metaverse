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
 * This class uses Tinkerpop Frames to access the GetXMLData nodes in a graph
 */
public interface GetXMLDataStepNode extends TransformationStepNode {
  @Adjacency( label = "isreadby", direction = Direction.IN )
  public Iterable<FramedMetaverseNode> getInputFiles();

  @Adjacency( label = "uses", direction = Direction.OUT )
  public Iterable<FileFieldNode> getFileFieldNodesUses();
}
