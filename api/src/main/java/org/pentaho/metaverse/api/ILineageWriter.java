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

package org.pentaho.metaverse.api;

import org.pentaho.metaverse.api.model.LineageHolder;

import java.io.IOException;

/**
 * ILineageWriter is a composite interface that offers methods for writing lineage objects such as graphs,
 * execution profiles, etc.
 */
public interface ILineageWriter {

  String DEFAULT_OUTPUT_STRATEGY = "none";

  /**
   * Outputs an IExecutionProfile
   *
   * @param holder Context of the lineage related info
   * @throws IOException
   */
  void outputExecutionProfile( LineageHolder holder ) throws IOException;

  /**
   * Outputs a relationship graph that backs an IMetaverseBuilder
   *
   * @param holder Context of the lineage related info
   * @throws IOException
   */
  void outputLineageGraph( LineageHolder holder ) throws IOException;

  /**
   * Returns the output strategy (all, latest, none, etc.) as a string
   *
   * @return The String name of the output strategy
   */
  String getOutputStrategy();

  /**
   * Sets the output strategy (all, latest, none) for this writer
   *
   * @param strategy The strategy to use when outputting lineage information
   */
  void setOutputStrategy( String strategy );

  /**
   * Method called on the writer to do any cleanup of the output artifacts, folders, etc.
   *
   * @param holder Context of the lineage related info
   */
  void cleanOutput( LineageHolder holder );
}
