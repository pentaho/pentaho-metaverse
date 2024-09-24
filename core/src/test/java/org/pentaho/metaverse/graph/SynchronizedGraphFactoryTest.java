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
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import org.pentaho.metaverse.api.model.BaseSynchronizedGraph;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SynchronizedGraphFactoryTest {

  @Test( expected = UnsupportedOperationException.class )
  public void testProtected_Constructor() {
    new SynchronizedGraphFactory() {
    };
  }

  @Test
  public void testOpen_Configuration() throws Exception {
    Configuration config = new PropertiesConfiguration();
    config.addProperty( "blueprints.graph", "com.tinkerpop.blueprints.impls.tg.TinkerGraph" );
    Graph g = SynchronizedGraphFactory.open( config );

    assertTrue( g instanceof BaseSynchronizedGraph );
  }

  @Test
  public void testOpen_Map() throws Exception {
    Map<String, String> config = new HashMap<String, String>();
    config.put( "blueprints.graph", "com.tinkerpop.blueprints.impls.tg.TinkerGraph" );
    Graph g = SynchronizedGraphFactory.open( config );

    assertTrue( g instanceof BaseSynchronizedGraph );
  }

  @Test
  public void testOpen_File() throws Exception {
    String config = "src/test/resources/graph.properties";
    Graph g = SynchronizedGraphFactory.open( config );

    assertTrue( g instanceof BaseSynchronizedGraph );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testWrapGraph_NotAKeyIndexableGraph() throws Exception {
    Graph g = mock( Graph.class );
    SynchronizedGraphFactory.wrapGraph( g );
  }

  @Test
  public void testWrapGraph() throws Exception {
    Graph g = new TinkerGraph();
    BaseSynchronizedGraph wrapped = (BaseSynchronizedGraph) SynchronizedGraphFactory.wrapGraph( g );

    assertNotNull( wrapped );
    assertTrue( wrapped.getGraph() instanceof IdGraph );
    assertTrue( wrapped.getGraph() instanceof KeyIndexableGraph );

  }


}
