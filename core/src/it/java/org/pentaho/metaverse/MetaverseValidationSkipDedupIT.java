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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Runs the integration test with the {@link MetaverseConfig} mocked to have
 * the {@code deduplicateTransformationFields} graph dedupping turned off.
 */
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( MetaverseConfig.class )
public class MetaverseValidationSkipDedupIT extends MetaverseValidationIT {

  @BeforeClass
  public static void init() throws Exception {

    PowerMockito.mockStatic( MetaverseConfig.class );
    // expecting to deduplicate by default - need to mock to return false
    Mockito.when( MetaverseConfig.adjustExternalResourceFields() ).thenReturn( false );
    Mockito.when( MetaverseConfig.deduplicateTransformationFields() ).thenReturn( false );
    Mockito.when( MetaverseConfig.consolidateSubGraphs() ).thenReturn( true );
    Mockito.when( MetaverseConfig.generateSubGraphs() ).thenReturn( true );

    MetaverseValidationIT.init();
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
