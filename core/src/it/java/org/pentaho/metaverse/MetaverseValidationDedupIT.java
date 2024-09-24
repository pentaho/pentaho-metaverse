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

package org.pentaho.metaverse;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import java.util.ArrayList;
import java.util.List;


/**
 * Runs the integration test with the {@link MetaverseConfig} mocked to have
 * the {@code deduplicateTransformationFields} graph dedupping turned on.
 */
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( MetaverseConfig.class )
public class MetaverseValidationDedupIT extends MetaverseValidationIT {

  @BeforeClass
  public static void init() throws Exception {

    PowerMockito.mockStatic( MetaverseConfig.class );
    Mockito.when( MetaverseConfig.adjustExternalResourceFields() ).thenReturn( true );
    Mockito.when( MetaverseConfig.deduplicateTransformationFields() ).thenReturn( true );
    Mockito.when( MetaverseConfig.consolidateSubGraphs() ).thenReturn( true );
    Mockito.when( MetaverseConfig.generateSubGraphs() ).thenReturn( true );

    MetaverseValidationIT.init();
  }

  @Test
  public void testSelectValuesStep() throws Exception {
    testSelectValuesStep( 8 );
  }

  @Test
  public void testTextFileInputNode() throws Exception {
    List<FramedMetaverseNode> containedNodes = testTextFileInputNodeImpl( 3 );
    // get field names
    List<String> nodeNames = new ArrayList();
    for ( final FramedMetaverseNode node : containedNodes ) {
      nodeNames.add( node.getName() );
    }
    Assert.assertTrue( nodeNames.contains( "longitude" ) );
    Assert.assertTrue( nodeNames.contains( "latitude" ) );
    Assert.assertTrue( nodeNames.contains( "address" ) );
  }

  @Test
  @Override
  public void testTransformationStepNodes() throws Exception {
    super.testTransformationStepNodes();
  }
}
