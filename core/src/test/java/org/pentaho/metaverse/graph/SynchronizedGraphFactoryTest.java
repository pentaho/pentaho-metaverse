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
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;
import org.pentaho.metaverse.api.model.BaseSynchronizedGraph;

import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SynchronizedGraphFactoryTest {

  @Test( expected = UnsupportedOperationException.class )
  public void testProtected_Constructor() {
    new SynchronizedGraphFactory() {
    };
  }

  @Test
  public void testGetDefaultGraph() {
    Graph graph = SynchronizedGraphFactory.getDefaultGraph();
    assertTrue( graph instanceof BaseSynchronizedGraph );
  }

  @Test
  public void testOpen_Map() {
    Map<String, String> config = new HashMap<String, String>();
    config.put( "blueprints.graph", "ignored" );
    Graph graph = SynchronizedGraphFactory.open( config );
    assertTrue( graph instanceof BaseSynchronizedGraph );
  }

  @Test
  public void testOpen_ResourceBundle() {
    ResourceBundle bundle = new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][] { { "blueprints.graph", "ignored" } };
      }
    };
    Graph graph = SynchronizedGraphFactory.open( bundle );
    assertTrue( graph instanceof BaseSynchronizedGraph );
  }

  @Test
  public void testWrapGraph() {
    TinkerGraph graph = TinkerGraph.open();
    BaseSynchronizedGraph wrapped = (BaseSynchronizedGraph) SynchronizedGraphFactory.wrapGraph( graph );
    assertNotNull( wrapped );
    assertTrue( wrapped.getGraph() instanceof TinkerGraph );
  }
}
