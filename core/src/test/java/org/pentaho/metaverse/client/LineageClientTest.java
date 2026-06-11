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


package org.pentaho.metaverse.client;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.graph.LineageGraphMap;


public class LineageClientTest {

  private LineageClient lineageClient;
  private Graph g;

  @Before
  public void setUp() throws Exception {
    lineageClient = new LineageClient();
    g = TinkerGraph.open();
    LineageGraphMap.getInstance().clear();
  }

  @Test
  public void testGetInstance( ) {
  }
}
