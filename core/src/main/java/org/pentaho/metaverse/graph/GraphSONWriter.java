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


package org.pentaho.metaverse.graph;

import org.apache.tinkerpop.gremlin.structure.Graph;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The GraphSONWriter class contains methods for writing a metaverse graph model in GraphSON format
 * 
 */
public class GraphSONWriter extends BaseGraphWriter {

  @Override
  public void outputGraphImpl( Graph graph, OutputStream graphSONOutputStream ) throws IOException {
    graph.io( org.apache.tinkerpop.gremlin.structure.io.IoCore.graphson() )
      .writer().create().writeGraph( graphSONOutputStream, graph );
  }

}
