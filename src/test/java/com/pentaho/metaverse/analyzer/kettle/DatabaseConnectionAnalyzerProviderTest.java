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

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DatabaseConnectionAnalyzerProviderTest {

  DatabaseConnectionAnalyzerProvider provider = null;

  @Before
  public void setUp() {
    provider = new DatabaseConnectionAnalyzerProvider();
  }

  @Test
  public void testGetAnalyzers() throws Exception {
    Set<IDatabaseConnectionAnalyzer> analyzers = provider.getAnalyzers();
    assertNotNull( analyzers );
    assertEquals( 1, analyzers.size() );
    provider.analyzers = new HashSet<IDatabaseConnectionAnalyzer>( 1 ) {
      {
        add( new DatabaseConnectionAnalyzer() );
      }
    };
    analyzers = provider.getAnalyzers();
    assertNotNull( analyzers );
    assertEquals( 1, analyzers.size() );
  }

  @Test
  public void testGetAnalyzersOfType() throws Exception {
    provider.analyzers = new HashSet<IDatabaseConnectionAnalyzer>( 1 ) {
      {
        add( new DatabaseConnectionAnalyzer() );
      }
    };
    Set<Class<?>> types = new HashSet<Class<?>>();
    Set<IDatabaseConnectionAnalyzer> analyzers = provider.getAnalyzers( types );
    assertNull( analyzers );

    types = new HashSet<Class<?>>( 1 ) {{
      add( IDatabaseConnectionAnalyzer.class );
    }};
    analyzers = provider.getAnalyzers( types );
    assertNotNull( analyzers );
    assertEquals( 1, analyzers.size() );

    types = new HashSet<Class<?>>( 1 ) {{
      add( Class.class );
    }};
    analyzers = provider.getAnalyzers( types );
    assertNull( analyzers );
  }

  @Test
  public void testSetDatabaseConnectionAnalyzers() throws Exception {
    provider.setDatabaseConnectionAnalyzers( new HashSet<IDatabaseConnectionAnalyzer>( 1 ) {
      {
        add( new DatabaseConnectionAnalyzer() );
      }
    } );
    Set<IDatabaseConnectionAnalyzer> analyzers = provider.getAnalyzers();
    assertNotNull( analyzers );
    assertEquals( 1, analyzers.size() );
  }
}
