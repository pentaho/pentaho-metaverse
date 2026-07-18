/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.graph;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.pentaho.metaverse.api.model.BaseSynchronizedGraph;

/**
 * A Graph that provides thread-safe modification
 */
public class SynchronizedGraph extends BaseSynchronizedGraph {

  /**
   * Creates a new synchronized graph
   * @param graph The underlying graph
   */
  public SynchronizedGraph( TinkerGraph graph ) {
    super( graph );
  }
}
