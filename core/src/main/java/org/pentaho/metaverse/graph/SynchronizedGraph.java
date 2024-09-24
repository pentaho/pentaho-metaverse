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

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.pentaho.metaverse.api.model.BaseSynchronizedGraph;

/**
 * A Graph that provides thread-safe modification
 */
public class SynchronizedGraph extends BaseSynchronizedGraph {

  /**
   * Creates a new synchronized graph
   * @param graph The underlying graph
   */
  public SynchronizedGraph( IdGraph<KeyIndexableGraph> graph ) {
    super( graph );
  }
}
