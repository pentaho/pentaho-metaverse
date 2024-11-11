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


package org.pentaho.metaverse.client;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.graph.LineageGraphMap;


public class LineageClientTest {

  private LineageClient lineageClient;
  private Graph g;

  @Before
  public void setUp() throws Exception {
    lineageClient = new LineageClient();
    g = new TinkerGraph();
    LineageGraphMap.getInstance().clear();
  }

  @Test
  public void testGetInstance( ) {
  }
}
