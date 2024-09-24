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

package org.pentaho.metaverse.api;

public interface IMetaverseConfig {

  String KETTLE_LINEAGE_EXECUTION_RUNTIME = "KETTLE_LINEAGE_EXECUTION_RUNTIME";
  String KETTLE_LINEAGE_EXECUTION_OUTPUT_FOLDER = "KETTLE_LINEAGE_EXECUTION_OUTPUT_FOLDER";
  String KETTLE_LINEAGE_EXECUTION_GENERATION_STRATEGY = "KETTLE_LINEAGE_EXECUTION_GENERATION_STRATEGY";
  String KETTLE_LINEAGE_RESOLVE_EXTERNAL_RESOURCES = "KETTLE_LINEAGE_RESOLVE_EXTERNAL_RESOURCES";
  String KETTLE_LINEAGE_DEDUPLICATE_TRANSFORMATION_FIELDS = "KETTLE_LINEAGE_DEDUPLICATE_TRANSFORMATION_FIELDS";
  String KETTLE_LINEAGE_ADJUST_EXTERNAL_RESOURCE_FIELDS = "KETTLE_LINEAGE_ADJUST_EXTERNAL_RESOURCE_FIELDS";
  String KETTLE_LINEAGE_GENERATE_SUBGRAPHS = "KETTLE_LINEAGE_GENERATE_SUBGRAPHS";
  String KETTLE_LINEAGE_CONSOLIDATE_SUBGRAPHS = "KETTLE_LINEAGE_CONSOLIDATE_SUBGRAPHS";
  String KETTLE_LINEAGE_EXTERNAL_RESOURCE_CACHE_EXPIRE_TIME = "KETTLE_LINEAGE_EXTERNAL_RESOURCE_CACHE_EXPIRE_TIME";


  void setExecutionRuntime( final String executionRuntime );

  String getExecutionOutputFolder();

  void setExecutionOutputFolder( final String executionOutputFolder );

  String getExecutionRuntime();

  void setExecutionGenerationStrategy( final String executionGenerationStrategy );

  String getExecutionGenerationStrategy();

  void setResolveExternalResources( final boolean resolveExternalResources );

  boolean getResolveExternalResources();

  void setGenerateSubGraphs( final boolean generateSubGraphs );

  boolean getGenerateSubGraphs();

  void setConsolidateSubGraphs( final boolean consolidateGraphs );

  boolean getConsolidateSubGraphs();

  void setExternalResourceCacheExpireTime( final String cacheExpire );

  String getExternalResourceCacheExpireTime();
}
