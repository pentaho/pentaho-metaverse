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

package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.api.model.IExecutionEngine;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.impl.model.ExecutionEngine;
import com.pentaho.metaverse.impl.model.ExecutionProfileUtil;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;

import java.io.IOException;
import java.io.PrintStream;

/**
 * A base class to provide common functionality among runtime extension points
 */
public abstract class BaseRuntimeExtensionPoint implements ExtensionPointInterface {

  public static final String EXECUTION_ENGINE_NAME = "Pentaho Data Integration";
  public static final String EXECUTION_ENGINE_DESCRIPTION =
    "Pentaho data integration prepares and blends data to create a complete picture of your business "
      + "that drives actionable insights.";

  /**
   * Write the given execution profile to the output stream
   *
   * @param out              the output stream to which we write the profile
   * @param executionProfile the execution profile to be output
   * @throws IOException
   */
  public void writeExecutionProfile( PrintStream out, IExecutionProfile executionProfile ) throws IOException {
    // TODO where to persist the execution profile?
    ExecutionProfileUtil.dumpExecutionProfile( out, executionProfile );
  }

  public void writeLineageInfo( PrintStream out, LineageHolder holder) throws IOException {
    writeExecutionProfile( out, holder.getExecutionProfile() );
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
}
