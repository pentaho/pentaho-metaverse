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

package org.pentaho.metaverse.api;

import com.tinkerpop.blueprints.Graph;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The IGraphWriter interface allows for a Graph object to be written to an output stream.
 */
public interface IGraphWriter {

  /**
   * Output the specified graph to the specified output stream
   * 
   * @param graph
   *          the graph
   * @param outputStream
   *          the output stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  void outputGraph( final Graph graph, final OutputStream outputStream ) throws IOException;

}
