/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.metaverse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.metaverse.impl.MetaverseConfig;

/**
 * Runs the integration test with the {@link MetaverseConfig} mocked to have
 * the {@code deduplicateTransformationFields} graph dedupping turned off.
 */
public class MetaverseValidationSkipDedupIT extends MetaverseValidationIT {

  private static MockedStatic<MetaverseConfig> metaverseConfigMock;

  @BeforeClass
  public static void init() throws Exception {

    metaverseConfigMock = Mockito.mockStatic( MetaverseConfig.class, Mockito.CALLS_REAL_METHODS );
    // expecting to deduplicate by default - need to mock to return false
    metaverseConfigMock.when( MetaverseConfig::adjustExternalResourceFields ).thenReturn( false );
    metaverseConfigMock.when( MetaverseConfig::deduplicateTransformationFields ).thenReturn( false );
    metaverseConfigMock.when( MetaverseConfig::consolidateSubGraphs ).thenReturn( true );
    metaverseConfigMock.when( MetaverseConfig::generateSubGraphs ).thenReturn( true );

    MetaverseValidationIT.init();
  }

  @AfterClass
  public static void tearDownMock() {
    if ( metaverseConfigMock != null ) {
      metaverseConfigMock.close();
      metaverseConfigMock = null;
    }
  }

  @Test
  public void testSelectValuesStep() throws Exception {
    testSelectValuesStep( 16 );
  }

  @Test
  public void testTextFileInputNode() throws Exception {
    testTextFileInputNodeImpl( 0 );
  }
}
