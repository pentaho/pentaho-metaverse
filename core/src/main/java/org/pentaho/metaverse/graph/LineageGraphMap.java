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

import com.tinkerpop.blueprints.Graph;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * LineageGraphMap is a singleton that maintains a map from document content objects to a Future task that will return
 * a Graph object. The graph is a representation of the lineage analysis performed on the document content object.
 */
public class LineageGraphMap {
  public static final Map<Object, Future<Graph>> lineageGraphMap
    = new ConcurrentHashMap<Object, Future<Graph>>();


  public static Map<Object, Future<Graph>> getInstance() {
    return lineageGraphMap;
  }
}
