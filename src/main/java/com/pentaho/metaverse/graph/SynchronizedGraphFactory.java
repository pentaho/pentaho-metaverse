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
import org.apache.commons.configuration.Configuration;
import java.util.Map;

/**
 * Thin wrapper around {@link com.tinkerpop.blueprints.GraphFactory}
 * that constructs {@link com.pentaho.metaverse.graph.SynchronizedGraph} objects
 */
public class SynchronizedGraphFactory {

  /**
   * Opens a Graph based on a Configuration
   * @see com.tinkerpop.blueprints.GraphFactory#open(org.apache.commons.configuration.Configuration)
   * @param configuration
   * @return {@link SynchronizedGraph} instance
   */
  public static Graph open( final Configuration configuration ) {
    Graph graph = com.tinkerpop.blueprints.GraphFactory.open( configuration );
    return new SynchronizedGraph( graph );
  }

  /**
   * Opens a Graph based on a Map configuration
   * @see com.tinkerpop.blueprints.GraphFactory#open(java.util.Map)
   * @param configuration
   * @return {@link SynchronizedGraph} instance
   */
  public static Graph open( final Map configuration ) {
    Graph graph = com.tinkerpop.blueprints.GraphFactory.open( configuration );
    return new SynchronizedGraph( graph );
  }

  /**
   * Opens Graph based on configuration defined in a file
   * @see com.tinkerpop.blueprints.GraphFactory#open(String)
   * @param configurationFile
   * @return {@link SynchronizedGraph} instance
   */
  public static Graph open( final String configurationFile ) {
    Graph graph = com.tinkerpop.blueprints.GraphFactory.open( configurationFile );
    return new SynchronizedGraph( graph );
  }

}
