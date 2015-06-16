/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.service;

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
