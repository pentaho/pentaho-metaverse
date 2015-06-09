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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints;

import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.metaverse.api.ILineageWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionEngine;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.impl.model.ExecutionEngine;

import java.io.IOException;

/**
 * A base class to provide common functionality among runtime extension points
 */
public abstract class BaseRuntimeExtensionPoint implements ExtensionPointInterface {

  public static final String EXECUTION_ENGINE_NAME = "Pentaho Data Integration";
  public static final String EXECUTION_ENGINE_DESCRIPTION =
    "Pentaho data integration prepares and blends data to create a complete picture of your business "
      + "that drives actionable insights.";

  protected ILineageWriter lineageWriter;

  public void writeLineageInfo( LineageHolder holder ) throws IOException {
    if ( lineageWriter != null ) {
      String strategy = lineageWriter.getOutputStrategy();
      if ( !"none".equals( strategy ) ) {
        if ( "latest".equals( strategy ) ) {
          lineageWriter.cleanOutput( holder );
        }
        String id = holder.getExecutionProfile().getName();
        lineageWriter.outputExecutionProfile( holder );
        lineageWriter.outputLineageGraph( holder );
      }
    }
  }

  /**
   * Populates and returns an IExecutionEngine object with the appropriate values
   *
   * @return information about the current execution engine
   */

  public static IExecutionEngine getExecutionEngineInfo() {
    IExecutionEngine executionEngine = new ExecutionEngine();
    executionEngine.setName( EXECUTION_ENGINE_NAME );
    executionEngine.setVersion( BuildVersion.getInstance().getVersion() );
    executionEngine.setDescription( EXECUTION_ENGINE_DESCRIPTION );
    return executionEngine;
  }

  public void addRuntimeLineageInfo( LineageHolder holder ) {
    // TODO
    IMetaverseBuilder builder = holder.getMetaverseBuilder();
    IExecutionProfile profile = holder.getExecutionProfile();
  }

  /**
   * Returns the lineage writer
   *
   * @return the ILineageWriter instance
   */
  public ILineageWriter getLineageWriter() {
    return lineageWriter;
  }

  /**
   * Sets the lineage writer for this object
   *
   * @param lineageWriter the ILineageWriter to set
   */
  public void setLineageWriter( ILineageWriter lineageWriter ) {
    this.lineageWriter = lineageWriter;
  }
}
