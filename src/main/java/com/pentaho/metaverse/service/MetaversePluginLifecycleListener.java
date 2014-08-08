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

package com.pentaho.metaverse.service;

import com.pentaho.metaverse.messages.Messages;
import com.tinkerpop.blueprints.Graph;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaversePluginLifecycleListener implements IPluginLifecycleListener {

  private Graph graph;
  private static final Logger log = LoggerFactory.getLogger( MetaversePluginLifecycleListener.class );

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
      log.info( Messages.getString( "INFO.PluginUnload.ShutdownGraph" ) );
      graph.shutdown();
    }
  }
}
