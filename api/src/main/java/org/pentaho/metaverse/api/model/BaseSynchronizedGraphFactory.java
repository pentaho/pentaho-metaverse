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


package org.pentaho.metaverse.api.model;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.apache.commons.configuration.Configuration;
import org.pentaho.metaverse.api.messages.Messages;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * <p> Thin wrapper around {@link com.tinkerpop.blueprints.GraphFactory} that constructs {@link BaseSynchronizedGraph}
 * objects. </p> <p> <strong>NOTE:</strong> The backing graph configured <em>must</em> implement {@link
 * com.tinkerpop.blueprints.KeyIndexableGraph} </p>
 */
public class BaseSynchronizedGraphFactory {
  private static final Map<String, String> configMap = new HashMap<>();

  static {
    configMap.put( "blueprints.graph", "com.tinkerpop.blueprints.impls.tg.TinkerGraph" );
  }

  /**
   * Hides the constructor so that this class cannot be instanced
   */
  protected BaseSynchronizedGraphFactory() {
    throw new UnsupportedOperationException();
  }

  public static Graph getDefaultGraph() {
    return open( configMap );
  }

  /**
   * Opens a Graph based on a Configuration
   *
   * @param configuration The graph configuration
   * @return {@link BaseSynchronizedGraph} instance {@link com.tinkerpop.blueprints.KeyIndexableGraph}
   * @see com.tinkerpop.blueprints.GraphFactory#open(org.apache.commons.configuration.Configuration)
   */
  public static Graph open( final Configuration configuration ) {
    Graph graph = com.tinkerpop.blueprints.GraphFactory.open( configuration );
    return wrapGraph( graph );
  }

  /**
   * Opens a Graph based on a Map configuration
   *
   * @param configuration The graph configuration
   * @return {@link BaseSynchronizedGraph} instance {@link com.tinkerpop.blueprints.KeyIndexableGraph}
   * @see com.tinkerpop.blueprints.GraphFactory#open(java.util.Map)
   */
  public static Graph open( final Map<String, String> configuration ) {
    Graph graph = com.tinkerpop.blueprints.GraphFactory.open( configuration );
    return wrapGraph( graph );
  }

  /**
   * Opens Graph based on configuration defined in a file
   *
   * @param configurationFile The graph configuration file
   * @return {@link BaseSynchronizedGraph} instance {@link com.tinkerpop.blueprints.KeyIndexableGraph}
   * @see com.tinkerpop.blueprints.GraphFactory#open(String)
   */
  public static Graph open( final String configurationFile ) {
    Graph graph = com.tinkerpop.blueprints.GraphFactory.open( configurationFile );
    return wrapGraph( graph );
  }

  /**
   * Opens Graph based on configuration defined in a {@link ResourceBundle}.
   *
   * @param configBundle The {@link ResourceBundle} containing the graph configuration
   * @return {@link BaseSynchronizedGraph} instance {@link com.tinkerpop.blueprints.KeyIndexableGraph}
   * @see com.tinkerpop.blueprints.GraphFactory#open(String)
   */
  public static Graph open( final ResourceBundle configBundle ) {
    final Map<String, String> graphProps = new HashMap<>();
    final Enumeration<String> keys = configBundle.getKeys();
    while ( keys.hasMoreElements() ) {
      final String key = keys.nextElement();
      final String value = configBundle.getString( key );
      graphProps.put( key, value );
    }
    return open( graphProps );
  }

  /**
   * Wraps the underlying graph with a synchronized one
   *
   * @param graph The graph to wrap
   * @return The synchronized graph
   */
  public static Graph wrapGraph( Graph graph ) {
    if ( graph instanceof KeyIndexableGraph ) {
      KeyIndexableGraph keyIndexableGraph = (KeyIndexableGraph) graph;
      IdGraph<KeyIndexableGraph> idGraph = new IdGraph<>( keyIndexableGraph );
      return new BaseSynchronizedGraph( idGraph );
    } else {
      throw new IllegalArgumentException( Messages.getString( "ERROR.BackingGraph.MustImplement.KeyIndexableGraph" ) );
    }
  }
}
