/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( MetaverseConfig.class )
public class TransChangedExtensionPointTest {

  @Mock
  TransMeta transMeta;

  @Before
  public void setUp() throws Exception {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    when( transMeta.getFilename() ).thenReturn( "/path/to/file.ktr" );
    when( transMeta.getName() ).thenReturn( "testTrans" );
    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
    PowerMockito.mockStatic( MetaverseConfig.class );
  }

  @Test
  public void testCallExtensionWithLineageOnPoint() throws Exception {
    when( MetaverseConfig.isLineageExecutionEnabled() ).thenReturn( true );
    TransChangedExtensionPoint extensionPoint = new TransChangedExtensionPoint();
    extensionPoint.callExtensionPoint( null, null );
    verify( transMeta, times( 0 ) ).addContentChangedListener( any() );
    extensionPoint.callExtensionPoint( null, transMeta );
    verify( transMeta, times( 1 ) ).addContentChangedListener( any() );
  }

  @Test
  public void testCallExtensionWithLineageOffPoint() throws Exception {
    when( MetaverseConfig.isLineageExecutionEnabled() ).thenReturn( false );
    TransChangedExtensionPoint extensionPoint = new TransChangedExtensionPoint();
    extensionPoint.callExtensionPoint( null, null );
    verify( transMeta, times( 0 ) ).addContentChangedListener( any() );
    extensionPoint.callExtensionPoint( null, transMeta );
    verify( transMeta, times( 0 ) ).addContentChangedListener( any() );
  }
}
