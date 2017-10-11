/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
