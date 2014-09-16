/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle;

import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Set;

/**
 * BaseKettleMetaverseComponentWithDatabases extends BaseKettleMetaverseComponent with methods for getting/setting
 * database connection analyzers
 */
public class BaseKettleMetaverseComponentWithDatabases extends BaseKettleMetaverseComponent {

  protected IDatabaseConnectionAnalyzerProvider dbConnectionAnalyzerProvider = null;

  /**
   * Returns an object capable of analyzing database connections (DatabaseMetas)
   *
   * @return a database connection Analyzer
   */
  public Set<IDatabaseConnectionAnalyzer> getDatabaseConnectionAnalyzers() {

    if ( dbConnectionAnalyzerProvider == null ) {
      try {
        dbConnectionAnalyzerProvider = PentahoSystem.get( IDatabaseConnectionAnalyzerProvider.class );
      } catch ( Throwable t ) {
        // Don't fail because of PentahoSystem, instead let the caller handle null
        dbConnectionAnalyzerProvider = null;
      }
    }
    // Default to the built-in database connection analyzer
    if ( dbConnectionAnalyzerProvider == null ) {
      dbConnectionAnalyzerProvider = new DatabaseConnectionAnalyzerProvider();
    }

    return dbConnectionAnalyzerProvider.getAnalyzers();
  }

  /**
   * Sets the database connection analyzer provider for this component
   *
   * @param provider the database connection analyzer provider
   */
  public void setDatabaseConnectionAnalyzerProvider( IDatabaseConnectionAnalyzerProvider provider ) {
    dbConnectionAnalyzerProvider = provider;
  }
}
