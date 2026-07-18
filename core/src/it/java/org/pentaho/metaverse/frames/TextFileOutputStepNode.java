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
 * User: RFellows Date: 9/23/14
 */
public class TextFileOutputStepNode extends TransformationStepNode {
  public TextFileOutputStepNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<FramedMetaverseNode> getOutputFiles() {
    return wrapAsNodes( vertex.vertices( Direction.OUT, "writesto" ) );
  }
}
