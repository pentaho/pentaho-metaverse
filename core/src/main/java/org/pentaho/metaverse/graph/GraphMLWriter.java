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
 * The GraphMLWriter class contains methods for writing a metaverse graph model in GraphML format
 */
public class GraphMLWriter extends BaseGraphWriter {

  @Override
  public void outputGraphImpl( Graph graph, OutputStream graphMLOutputStream ) throws IOException {
    graph.io( org.apache.tinkerpop.gremlin.structure.io.IoCore.graphml() )
      .writer().normalize( true ).create().writeGraph( graphMLOutputStream, graph );
  }
}
