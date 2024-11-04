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


package org.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Graph;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The GraphMLWriter class contains methods for writing a metaverse graph model in GraphML format
 */
public class GraphMLWriter extends BaseGraphWriter {

  @Override
  public void outputGraphImpl( Graph graph, OutputStream graphMLOutputStream ) throws IOException {
    com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter writer =
      new com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter( graph );

    writer.setNormalize( true );
    writer.outputGraph( graphMLOutputStream );
  }
}
