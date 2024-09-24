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
