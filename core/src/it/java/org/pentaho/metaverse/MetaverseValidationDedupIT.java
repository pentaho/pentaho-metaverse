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
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.impl.MetaverseConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Runs the integration test with the {@link MetaverseConfig} mocked to have
 * the {@code deduplicateTransformationFields} graph dedupping turned on.
 */
public class MetaverseValidationDedupIT extends MetaverseValidationIT {

  private static MockedStatic<MetaverseConfig> metaverseConfigMock;

  @BeforeClass
  public static void init() throws Exception {

    metaverseConfigMock = Mockito.mockStatic( MetaverseConfig.class, Mockito.CALLS_REAL_METHODS );
    metaverseConfigMock.when( MetaverseConfig::adjustExternalResourceFields ).thenReturn( true );
    metaverseConfigMock.when( MetaverseConfig::deduplicateTransformationFields ).thenReturn( true );
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
    testSelectValuesStep( 8 );
  }

  @Test
  public void testTextFileInputNode() throws Exception {
    List<FramedMetaverseNode> containedNodes = testTextFileInputNodeImpl( 3 );
    // get field names
    List<String> nodeNames = new ArrayList<>();
    for ( final FramedMetaverseNode node : containedNodes ) {
      nodeNames.add( node.getName() );
    }
    assertTrue( nodeNames.contains( "longitude" ) );
    assertTrue( nodeNames.contains( "latitude" ) );
    assertTrue( nodeNames.contains( "address" ) );
  }

  @Test
  @Override
  public void testTransformationStepNodes() throws Exception {
    super.testTransformationStepNodes();
  }
}
