/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.api;

import com.pentaho.metaverse.api.model.LineageHolder;

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
