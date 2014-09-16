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

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

public class BaseKettleMetaverseComponentWithDatabasesTest {

  BaseKettleMetaverseComponentWithDatabases componentWithDatabases = null;

  @Before
  public void setUp() throws Exception {
    componentWithDatabases = new BaseKettleMetaverseComponentWithDatabases();
  }

  @Test
  public void testGetDatabaseConnectionAnalyzers() {
    // Should be a default DatabaseConnectionAnalyzer (as we are not using PentahoSystem in unit tests)
    componentWithDatabases.setDatabaseConnectionAnalyzerProvider( new DatabaseConnectionAnalyzerProvider() );
    Set<IDatabaseConnectionAnalyzer> dbas = componentWithDatabases.getDatabaseConnectionAnalyzers();
    assertNotNull( dbas );
    assertEquals( 1, dbas.size() );
    Iterator<IDatabaseConnectionAnalyzer> dbasIterator = dbas.iterator();
    assertNotNull( dbasIterator );
    while ( dbasIterator.hasNext() ) {
      IDatabaseConnectionAnalyzer dba = dbasIterator.next();
      assertTrue( dba instanceof DatabaseConnectionAnalyzer );
    }
  }
  @Test
  public void testGetDatabaseConnectionAnalyzersNullProvider() {
    componentWithDatabases.getDatabaseConnectionAnalyzers();
  }
}
