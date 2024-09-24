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
