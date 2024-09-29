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


package org.pentaho.metaverse.listener;

import com.tinkerpop.blueprints.Graph;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin lifecycle listener for the metaverse plugin. On platfomr shutdown this
 * lifecycle listener calls shutdown on the metaverse graph.
 * @author jdixon
 *
 */
public class MetaversePluginLifecycleListener implements IPluginLifecycleListener {

  private static final Logger LOG = LoggerFactory.getLogger( MetaversePluginLifecycleListener.class );
  private Graph graph;

  /**
   * Returns the metaverse graph
   * @return The graph
   */
  public Graph getGraph() {
    if ( this.graph == null ) {
      // try to get it from PentahoSystem
      this.graph = PentahoSystem.get( Graph.class, "MetaverseGraphImpl", null );
    }
    return graph;
  }

  public void setGraph( Graph graph ) {
    this.graph = graph;
  }

  @Override
  public void init() throws PluginLifecycleException { }

  @Override
  public void loaded() throws PluginLifecycleException { }

  @Override
  public void unLoaded() throws PluginLifecycleException {
    Graph graph = getGraph();
    if ( graph != null ) {
      LOG.info( Messages.getString( "INFO.PluginUnload.ShutdownGraph" ) );
      graph.shutdown();
    }
  }
}
