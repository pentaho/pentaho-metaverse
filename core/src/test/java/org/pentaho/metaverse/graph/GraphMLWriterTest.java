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
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class GraphMLWriterTest {

  @Test
  public void testOutputGraph() throws IOException {
    Graph g = TinkerGraph.open();
    g.addVertex( T.id, "v1" );
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    GraphMLWriter writer = new GraphMLWriter();
    assertNotNull( writer );
    writer.outputGraph( g, outStream );
    assertNotNull( outStream.toString() );
  }
}
