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


package org.pentaho.metaverse.api;

import java.io.OutputStream;

/**
 * Created by mburgess on 3/27/15.
 */
public interface ILineageWriterFactory<T> {

  /**
   * Creates an output stream using the specified object of the parameterized type for configuration. For example a
   * LoggerLineageWriterFactory could pass in a Logger, a FileLineageWriterFactory could pass in a File or String.
   * @param configuration An object containing the configuration information to tell the factory how and what to create
   *                      for an OutputStream
   * @return a configured OutputStream object for use by an ILineageWriter instance
   */
  OutputStream createOutputStream( T configuration );
}
