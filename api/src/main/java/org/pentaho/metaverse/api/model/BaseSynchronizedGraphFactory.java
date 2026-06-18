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

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * Factory for creating {@link BaseSynchronizedGraph} instances backed by {@link TinkerGraph}.
 */
public class BaseSynchronizedGraphFactory {

  /**
   * Hides the constructor so that this class cannot be instanced
   */
  protected BaseSynchronizedGraphFactory() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the default synchronized graph (a new TinkerGraph).
   *
   * @return a {@link BaseSynchronizedGraph} wrapping a new {@link TinkerGraph}
   */
  public static Graph getDefaultGraph() {
    return wrapGraph( TinkerGraph.open() );
  }

  /**
   * Opens a Graph. The configuration is ignored; a new TinkerGraph is always opened.
   *
   * @param configuration The graph configuration (ignored)
   * @return a {@link BaseSynchronizedGraph} wrapping a new {@link TinkerGraph}
   */
  public static Graph open( final Map<String, String> configuration ) {
    return wrapGraph( TinkerGraph.open() );
  }

  /**
   * Opens a Graph. The configuration is ignored; a new TinkerGraph is always opened.
   *
   * @param configBundle The graph configuration bundle (ignored)
   * @return a {@link BaseSynchronizedGraph} wrapping a new {@link TinkerGraph}
   */
  public static Graph open( final ResourceBundle configBundle ) {
    return wrapGraph( TinkerGraph.open() );
  }

  /**
   * Wraps a TinkerGraph with a synchronized graph wrapper.
   *
   * @param graph The TinkerGraph to wrap
   * @return a {@link BaseSynchronizedGraph}
   */
  public static Graph wrapGraph( TinkerGraph graph ) {
    return new BaseSynchronizedGraph( graph );
  }
}
