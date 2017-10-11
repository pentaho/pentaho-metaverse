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
import com.tinkerpop.blueprints.Vertex;

import java.util.Set;

/**
 * A utility class for graphs
 * @author jdixon
 *
 */
public class GraphUtil {

  /**
   * Hides the constructor so that this class cannot be instanced
   */
  protected GraphUtil() {
    throw new UnsupportedOperationException();
  }

  /**
   * Clones a provided vertex into a new graph. The graph should not be the graph that the
   * provided vertex belongs to.
   * @param vertex The vertex to clone
   * @param g The graph to clone the vertex into.
   * @return The vertex in the sub-graph
   */
  public static Vertex cloneVertexIntoGraph( Vertex vertex, Graph g ) {
    Vertex clone = g.getVertex( vertex.getId() );
    if ( clone != null ) {
      return clone;
    }
    clone = g.addVertex( vertex.getId() );
    Set<String> keys = vertex.getPropertyKeys();
    for ( String key : keys ) {
      Object value = vertex.getProperty( key );
      clone.setProperty( key, value );
    }
    return clone;
  }

}
