/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.frames;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * User: RFellows Date: 9/4/14
 */
public class FileInputStepNode extends TransformationStepNode {
  public FileInputStepNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<FramedMetaverseNode> getInputFiles() {
    return wrapAsNodes( vertex.vertices( Direction.IN, "isreadby" ) );
  }

  public List<FileFieldNode> getFileFieldNodesUses() {
    return wrapAs( vertex.vertices( Direction.OUT, "uses" ), v -> new FileFieldNode( v, graph ) );
  }
}
