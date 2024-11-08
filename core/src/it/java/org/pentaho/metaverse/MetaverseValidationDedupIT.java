/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.impl.MetaverseConfig;

import java.util.ArrayList;
import java.util.List;


/**
 * Runs the integration test with the {@link MetaverseConfig} mocked to have
 * the {@code deduplicateTransformationFields} graph dedupping turned on.
 */
@RunWith( MockitoJUnitRunner.class )
@Ignore
public class MetaverseValidationDedupIT extends MetaverseValidationIT {

  @BeforeClass
  public static void init() throws Exception {
    MetaverseValidationIT.init();
  }

  @Test
  public void testSelectValuesStep() throws Exception {
    try ( MockedStatic<MetaverseConfig> metaverseConfigMockedStatic = Mockito.mockStatic( MetaverseConfig.class ) ) {
      metaverseConfigMockedStatic.when( MetaverseConfig::adjustExternalResourceFields ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::deduplicateTransformationFields ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::consolidateSubGraphs ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::generateSubGraphs ).thenReturn( true );
      testSelectValuesStep( 8 );
    }
  }

  @Test
  public void testTextFileInputNode() throws Exception {
    try ( MockedStatic<MetaverseConfig> metaverseConfigMockedStatic = Mockito.mockStatic( MetaverseConfig.class ) ) {
      metaverseConfigMockedStatic.when( MetaverseConfig::adjustExternalResourceFields ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::deduplicateTransformationFields ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::consolidateSubGraphs ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::generateSubGraphs ).thenReturn( true );
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
  }

  @Test
  @Override
  public void testTransformationStepNodes() throws Exception {
    try ( MockedStatic<MetaverseConfig> metaverseConfigMockedStatic = Mockito.mockStatic( MetaverseConfig.class ) ) {
      metaverseConfigMockedStatic.when( MetaverseConfig::adjustExternalResourceFields ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::deduplicateTransformationFields ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::consolidateSubGraphs ).thenReturn( true );
      metaverseConfigMockedStatic.when( MetaverseConfig::generateSubGraphs ).thenReturn( true );
      super.testTransformationStepNodes();
    }
  }
}
