/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;


/**
 * Runs the integration test with the {@link MetaverseConfig} mocked to have
 * the {@code deduplicateTransformationFields} graph dedupping turned on.
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest( MetaverseConfig.class )
public class MetaverseValidationDedupIT extends MetaverseValidationIT {

  @BeforeClass
  public static void init() throws Exception {

    PowerMockito.mockStatic( MetaverseConfig.class );
    // expecting to deduplicate by default - need to mock to return false
    Mockito.when( MetaverseConfig.adjustExternalResourceFields() ).thenReturn( true );
    Mockito.when( MetaverseConfig.deduplicateTransformationFields() ).thenReturn( true );

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
}
