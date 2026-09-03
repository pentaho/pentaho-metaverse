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


package org.pentaho.metaverse.step;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.pentaho.metaverse.BaseMetaverseValidationIT;
import org.pentaho.metaverse.impl.MetaverseConfig;

public abstract class StepAnalyzerValidationIT extends BaseMetaverseValidationIT {

  private MockedStatic<MetaverseConfig> metaverseConfigMock;

  @Before
  public void init() throws Exception {

    metaverseConfigMock = Mockito.mockStatic( MetaverseConfig.class, Mockito.CALLS_REAL_METHODS );
    metaverseConfigMock.when( MetaverseConfig::adjustExternalResourceFields ).thenReturn( true );
    metaverseConfigMock.when( MetaverseConfig::deduplicateTransformationFields ).thenReturn( true );
    metaverseConfigMock.when( MetaverseConfig::consolidateSubGraphs ).thenReturn( true );
    metaverseConfigMock.when( MetaverseConfig::generateSubGraphs ).thenReturn( true );

  }

  @After
  public void tearDownMock() {
    if ( metaverseConfigMock != null ) {
      metaverseConfigMock.close();
      metaverseConfigMock = null;
    }
  }

  @Override
  protected boolean shouldCleanupInstance() {
    return true;
  }

  protected void initTest( final String transNodeName ) throws Exception {
    BaseMetaverseValidationIT.init( getRootFolder() + "/" + transNodeName,
      getOutputFileRoot() + "/" + transNodeName + ".graphml" );
  }

  protected String getRootFolder() {
    return "src/it/resources/repo/" + getClass().getSimpleName();
  }

  protected String getOutputFileRoot() {
    return "target/outputfiles/" + getClass().getSimpleName();
  }
}
