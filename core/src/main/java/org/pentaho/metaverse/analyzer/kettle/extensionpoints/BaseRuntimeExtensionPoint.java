/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints;

import org.apache.commons.lang.ObjectUtils;
import org.pentaho.di.core.KettleClientEnvironment;
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
    "Pentaho Data Integration (PDI) prepares and blends data to create a complete picture of your business "
      + "that drives actionable insights.";

  protected ILineageWriter lineageWriter;

  protected boolean runtimeEnabled;

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

  public void setRuntimeEnabled( String runtimeEnabled ) {
    this.runtimeEnabled = ( runtimeEnabled != null && runtimeEnabled.equalsIgnoreCase( "on" ) );
  }

  public void setRuntimeEnabled( boolean runtimeEnabled ) {
    this.runtimeEnabled = runtimeEnabled;
  }

  public boolean isRuntimeEnabled() {
    return runtimeEnabled;
  }

  /**
   * When executing from Kitchen or Pan there might not enough time to finish all async tasks
   * from job and trans listeners. To prevent jvm to exit use this flag to decide whether to run
   * them async or not.
   */
  public boolean allowedAsync() {
    KettleClientEnvironment.ClientType client = KettleClientEnvironment.getInstance().getClient();
    return !(
          ObjectUtils.equals( client, KettleClientEnvironment.ClientType.KITCHEN )
          || ObjectUtils.equals( client, KettleClientEnvironment.ClientType.PAN ) );
  }
}
