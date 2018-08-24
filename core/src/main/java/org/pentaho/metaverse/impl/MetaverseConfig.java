/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.impl;

import org.pentaho.metaverse.api.IMetaverseConfig;

/**
 * A single point of access for all metaverse osgi configuration properties.
 */
public class MetaverseConfig implements IMetaverseConfig {

  private String executionRuntime = "off";
  private String extecutionOutputFolder = "./pentaho-lineage-output";
  private String executionGenerationStrategy = "latest";
  private boolean resolveExternalResources = false;
  // Used for testing ONLY, to verify that any listeners waiting for lineage to be written aren't invoked until
  // graphml has been written
  private int lineageDelay = 0;

  public void setExecutionRuntime( final String executionRuntime ) {
    this.executionRuntime = executionRuntime;
  }

  public String getExecutionRuntime() {
    return this.executionRuntime;
  }

  public void setExecutionOutputFolder( final String extecutionOutputFolder ) {
    this.extecutionOutputFolder = extecutionOutputFolder;
  }

  public String getExecutionOutputFolder() {
    return this.extecutionOutputFolder;
  }

  public void setExecutionGenerationStrategy( final String executionGenerationStrategy ) {
    this.executionGenerationStrategy = executionGenerationStrategy;
  }

  public String getExecutionGenerationStrategy() {
    return this.executionGenerationStrategy;
  }

  public void setResolveExternalResources( final boolean resolveExternalResources ) {
    this.resolveExternalResources = resolveExternalResources;
  }

  public boolean getResolveExternalResources() {
    return this.resolveExternalResources;
  }

  public void setLineageDelay( final int lineageDelay ) {
    this.lineageDelay = lineageDelay;
  }

  public int getLineageDelay() {
    return this.lineageDelay;
  }
}
