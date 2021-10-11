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
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest( { MetaverseConfig.class, TransExtensionPointUtil.class } )
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class TransOpenedExtensionPointTest {

  @Mock
  TransMeta transMeta;
  @Mock
  MetaverseBuilder metaverseBuilder;

  @Before
  public void setUp() throws Exception {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    when( transMeta.getFilename() ).thenReturn( "/path/to/file.ktr" );
    when( transMeta.getName() ).thenReturn( "testTrans" );
    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
    PowerMockito.mockStatic( MetaverseConfig.class );
    PowerMockito.whenNew( MetaverseBuilder.class ).withAnyArguments().thenReturn( metaverseBuilder );
  }

  @Test
  public void testCallExtensionWithLineageOnPoint() throws Exception {
    when( MetaverseConfig.isLineageExecutionEnabled() ).thenReturn( true );
    TransOpenedExtensionPoint extensionPoint = new TransOpenedExtensionPoint();
    extensionPoint.callExtensionPoint( null, transMeta );
    verify( metaverseBuilder, times( 1 ) ).addNode( anyObject() );
  }

  @Test
  public void testCallExtensionWithLineageOffPoint() throws Exception {
    when( MetaverseConfig.isLineageExecutionEnabled() ).thenReturn( false );
    TransOpenedExtensionPoint extensionPoint = new TransOpenedExtensionPoint();
    extensionPoint.callExtensionPoint( null, transMeta );
    verify( metaverseBuilder, times( 0 ) ).addNode( anyObject() );
  }
}
