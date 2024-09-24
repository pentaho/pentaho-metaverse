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
