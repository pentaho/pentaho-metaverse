/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

  private static final String EXECUTION_RUNTIME_OFF = "off";
  private String executionRuntime = EXECUTION_RUNTIME_OFF;
  private String executionOutputFolder = "./pentaho-lineage-output";
  private String executionGenerationStrategy = "latest";
  private String externalResourceCacheExpireTime = "21600";
  private boolean resolveExternalResources = true;
  private boolean deduplicateTransformationFields = true;
  private boolean adjustExternalResourceFields = true;
  private boolean generateSubGraphs = true;
  private boolean consolidateSubGraphs = true;

  private static MetaverseConfig instance;

  // Used for testing ONLY, to verify that any listeners waiting for lineage to be written aren't invoked until
  // graphml has been written
  private int lineageDelay = 0;

  public static MetaverseConfig getInstance() {
    if ( null == instance ) {
      instance = new MetaverseConfig();
    }
    return instance;
  }

  MetaverseConfig() {
    executionRuntime = System.getProperty( KETTLE_LINEAGE_EXECUTION_RUNTIME, executionRuntime );
    executionOutputFolder = System.getProperty( KETTLE_LINEAGE_EXECUTION_OUTPUT_FOLDER, executionOutputFolder );
    executionGenerationStrategy = System.getProperty( KETTLE_LINEAGE_EXECUTION_GENERATION_STRATEGY, executionGenerationStrategy );
    externalResourceCacheExpireTime = System.getProperty( KETTLE_LINEAGE_EXTERNAL_RESOURCE_CACHE_EXPIRE_TIME, externalResourceCacheExpireTime );
    resolveExternalResources = "true".equalsIgnoreCase( System.getProperty( KETTLE_LINEAGE_RESOLVE_EXTERNAL_RESOURCES, Boolean.toString( resolveExternalResources ) ) );
    deduplicateTransformationFields = "true".equalsIgnoreCase( System.getProperty( KETTLE_LINEAGE_DEDUPLICATE_TRANSFORMATION_FIELDS, Boolean.toString( deduplicateTransformationFields ) ) );
    adjustExternalResourceFields = "true".equalsIgnoreCase( System.getProperty( KETTLE_LINEAGE_ADJUST_EXTERNAL_RESOURCE_FIELDS, Boolean.toString( adjustExternalResourceFields ) ) );
    generateSubGraphs = "true".equalsIgnoreCase( System.getProperty( KETTLE_LINEAGE_GENERATE_SUBGRAPHS, Boolean.toString( generateSubGraphs ) ) );
    consolidateSubGraphs = "true".equalsIgnoreCase( System.getProperty( KETTLE_LINEAGE_CONSOLIDATE_SUBGRAPHS, Boolean.toString( consolidateSubGraphs ) ) );
  }

  public void setExecutionRuntime( final String executionRuntime ) {
    this.executionRuntime = executionRuntime;
  }

  public String getExecutionRuntime() {
    return this.executionRuntime;
  }

  public void setExecutionOutputFolder( final String extecutionOutputFolder ) {
    this.executionOutputFolder = extecutionOutputFolder;
  }

  public String getExecutionOutputFolder() {
    return this.executionOutputFolder;
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

  public void setDeduplicateTransformationFields( final boolean deduplicateTransformationFields ) {
    this.deduplicateTransformationFields = deduplicateTransformationFields;
  }

  public boolean getDeduplicateTransformationFields() {
    return this.deduplicateTransformationFields;
  }

  public static boolean deduplicateTransformationFields() {
    final MetaverseConfig config = getInstance();
    return config != null && config.getDeduplicateTransformationFields();
  }

  public void setAdjustExternalResourceFields( final boolean adjustExternalResourceFields ) {
    this.adjustExternalResourceFields = adjustExternalResourceFields;
  }

  public boolean getAdjustExternalResourceFields() {
    return this.adjustExternalResourceFields;
  }

  public static boolean adjustExternalResourceFields() {
    final MetaverseConfig config = getInstance();
    return config != null && config.getAdjustExternalResourceFields();
  }

  public void setLineageDelay( final int lineageDelay ) {
    this.lineageDelay = lineageDelay;
  }

  public int getLineageDelay() {
    return this.lineageDelay;
  }

  public void setGenerateSubGraphs( final boolean generateSubGraphs ) {
    this.generateSubGraphs = generateSubGraphs;
  }

  public boolean getGenerateSubGraphs() {
    return this.generateSubGraphs;
  }

  public static boolean generateSubGraphs() {
    final MetaverseConfig config = getInstance();
    return config != null && config.getGenerateSubGraphs();
  }

  public void setConsolidateSubGraphs( final boolean consolidateSubGraphs ) {
    this.consolidateSubGraphs = consolidateSubGraphs;
  }

  public boolean getConsolidateSubGraphs() {
    return this.consolidateSubGraphs;
  }

  public void setExternalResourceCacheExpireTime( final String externalResourceCacheExpireTime ) {
    this.externalResourceCacheExpireTime = externalResourceCacheExpireTime;
  }

  public String getExternalResourceCacheExpireTime() {
    return this.externalResourceCacheExpireTime;
  }

  public static boolean consolidateSubGraphs() {
    final MetaverseConfig config = getInstance();
    return config != null && config.getConsolidateSubGraphs();
  }

  public static boolean isLineageExecutionEnabled() {
    final MetaverseConfig instance = getInstance();
    return instance != null && !EXECUTION_RUNTIME_OFF.equalsIgnoreCase( instance.getExecutionRuntime() );
  }
}
