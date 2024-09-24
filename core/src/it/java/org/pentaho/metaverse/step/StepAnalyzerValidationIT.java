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

package org.pentaho.metaverse.step;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.pentaho.metaverse.BaseMetaverseValidationIT;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( MetaverseConfig.class )
public abstract class StepAnalyzerValidationIT extends BaseMetaverseValidationIT {

  @Before
  public void init() throws Exception {

    PowerMockito.mockStatic( MetaverseConfig.class );
    Mockito.when( MetaverseConfig.adjustExternalResourceFields() ).thenReturn( true );
    Mockito.when( MetaverseConfig.deduplicateTransformationFields() ).thenReturn( true );
    Mockito.when( MetaverseConfig.consolidateSubGraphs() ).thenReturn( true );
    Mockito.when( MetaverseConfig.generateSubGraphs() ).thenReturn( true );

  }

  @Override
  protected boolean shouldCleanupInstance() {
    return true;
  }

  protected void initTest( final String transNodeName ) throws Exception {
    BaseMetaverseValidationIT.init( getRootFolder() + "/" + transNodeName,
      getOutputFileRoot() + "/" + transNodeName + ".graphml");
  }

  protected String getRootFolder() {
    return  "src/it/resources/repo/" + getClass().getSimpleName();
  }

  protected String getOutputFileRoot() {
    return "target/outputfiles/" + getClass().getSimpleName();
  }
}
