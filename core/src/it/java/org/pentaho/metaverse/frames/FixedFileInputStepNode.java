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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * User: wseyler Date: 4/22/15
 */
public class FixedFileInputStepNode extends TransformationStepNode {
  public FixedFileInputStepNode( Vertex vertex, Graph graph ) {
    super( vertex, graph );
  }

  public List<FramedMetaverseNode> getInputFiles() {
    return wrapAsNodes( vertex.vertices( Direction.IN, "isreadby" ) );
  }

  public List<FileFieldNode> getFileFieldNodesUses() {
    return wrapAs( vertex.vertices( Direction.OUT, "uses" ), v -> new FileFieldNode( v, graph ) );
  }
}
