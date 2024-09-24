/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
