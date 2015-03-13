/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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

    assertTrue( g instanceof SynchronizedGraph );
  }

  @Test
  public void testOpen_Map() throws Exception {
    Map<String, String> config = new HashMap<String, String>();
    config.put( "blueprints.graph", "com.tinkerpop.blueprints.impls.tg.TinkerGraph" );
    Graph g = SynchronizedGraphFactory.open( config );

    assertTrue( g instanceof SynchronizedGraph );
  }

  @Test
  public void testOpen_File() throws Exception {
    String config = "src/test/resources/graph.properties";
    Graph g = SynchronizedGraphFactory.open( config );

    assertTrue( g instanceof SynchronizedGraph );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testWrapGraph_NotAKeyIndexableGraph() throws Exception {
    Graph g = mock( Graph.class );
    SynchronizedGraphFactory.wrapGraph( g );
  }

  @Test
  public void testWrapGraph() throws Exception {
    Graph g = new TinkerGraph();
    SynchronizedGraph wrapped = (SynchronizedGraph) SynchronizedGraphFactory.wrapGraph( g );

    assertTrue( wrapped instanceof SynchronizedGraph );
    assertTrue( wrapped.graph instanceof IdGraph );
    assertTrue( wrapped.graph instanceof KeyIndexableGraph );

  }


}
