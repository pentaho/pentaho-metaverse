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
