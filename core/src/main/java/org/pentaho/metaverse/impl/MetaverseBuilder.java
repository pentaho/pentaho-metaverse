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


package org.pentaho.metaverse.impl;

import com.tinkerpop.blueprints.Graph;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.pentaho.metaverse.graph.SynchronizedGraphFactory;

/**
 * This is the reference implementation for IMetaverseBuilder, offering the ability to add nodes, links, etc. to an
 * underlying graph
 */
public class MetaverseBuilder extends BaseMetaverseBuilder {
  /**
   * Instantiates a new Metaverse builder.
   *
   * @param graph the Graph to write to
   */
  private static MetaverseBuilder instance;

  public static MetaverseBuilder getInstance() {
    if ( null == instance ) {
      instance = new MetaverseBuilder( SynchronizedGraphFactory.getDefaultGraph() );
    }
    return instance;
  }

  public MetaverseBuilder( Graph graph ) {
    super( graph );
  }

  public MetaverseBuilder() {
    super( SynchronizedGraphFactory.getDefaultGraph() );
  }
}
