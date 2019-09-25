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

package org.pentaho.metaverse.step;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.pentaho.metaverse.BaseMetaverseValidationIT;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith( PowerMockRunner.class )
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
