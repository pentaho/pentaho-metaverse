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
import org.pentaho.metaverse.api.model.BaseSynchronizedGraphFactory;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * Thin wrapper around {@link BaseSynchronizedGraphFactory} that constructs {@link SynchronizedGraph} objects.
 *
 * <p>The static methods below are explicit bridge/forwarding methods required for binary compatibility.
 * Static methods are not truly inherited in Java bytecode: callers compiled against
 * {@code SynchronizedGraphFactory.getDefaultGraph()} (etc.) will fail at runtime with
 * {@code NoSuchMethodError} unless the method is physically declared on this class.
 */
public class SynchronizedGraphFactory extends BaseSynchronizedGraphFactory {

  /**
   * Returns the default synchronized graph (a new TinkerGraph).
   *
   * @return a {@link org.pentaho.metaverse.api.model.BaseSynchronizedGraph} wrapping a new {@link TinkerGraph}
   */
  public static Graph getDefaultGraph() {
    return BaseSynchronizedGraphFactory.getDefaultGraph();
  }

  /**
   * Opens a Graph. The configuration is ignored; a new TinkerGraph is always opened.
   *
   * @param configuration The graph configuration (ignored)
   * @return a {@link org.pentaho.metaverse.api.model.BaseSynchronizedGraph} wrapping a new {@link TinkerGraph}
   */
  public static Graph open( final Map<String, String> configuration ) {
    return BaseSynchronizedGraphFactory.open( configuration );
  }

  /**
   * Opens a Graph. The configuration is ignored; a new TinkerGraph is always opened.
   *
   * @param configBundle The graph configuration bundle (ignored)
   * @return a {@link org.pentaho.metaverse.api.model.BaseSynchronizedGraph} wrapping a new {@link TinkerGraph}
   */
  public static Graph open( final ResourceBundle configBundle ) {
    return BaseSynchronizedGraphFactory.open( configBundle );
  }

  /**
   * Wraps a TinkerGraph with a synchronized graph wrapper.
   *
   * @param graph The TinkerGraph to wrap
   * @return a {@link org.pentaho.metaverse.api.model.BaseSynchronizedGraph}
   */
  public static Graph wrapGraph( TinkerGraph graph ) {
    return BaseSynchronizedGraphFactory.wrapGraph( graph );
  }
}
