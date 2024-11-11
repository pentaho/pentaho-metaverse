/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.pentaho.metaverse.util.MetaverseUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TransChangedExtensionPointTest {

  @Mock
  TransMeta transMeta;

  @Before
  public void setUp() throws Exception {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    lenient().when( transMeta.getFilename() ).thenReturn( "/path/to/file.ktr" );
    lenient().when( transMeta.getName() ).thenReturn( "testTrans" );
    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
  }

  @Test
  public void testCallExtensionWithLineageOnPoint() throws Exception {
    try ( MockedStatic<MetaverseConfig> mockedMetaverseConfig = mockStatic( MetaverseConfig.class ) ) {
      mockedMetaverseConfig.when( MetaverseConfig::isLineageExecutionEnabled ).thenReturn( true );
      TransChangedExtensionPoint extensionPoint = new TransChangedExtensionPoint();
      extensionPoint.callExtensionPoint( null, null );
      verify( transMeta, times( 0 ) ).addContentChangedListener( any() );
      extensionPoint.callExtensionPoint( null, transMeta );
      verify( transMeta, times( 1 ) ).addContentChangedListener( any() );
    }
  }

  @Test
  public void testCallExtensionWithLineageOffPoint() throws Exception {
    try ( MockedStatic<MetaverseConfig> mockedMetaverseConfig = mockStatic( MetaverseConfig.class ) ) {
      mockedMetaverseConfig.when( MetaverseConfig::isLineageExecutionEnabled ).thenReturn( false );
      TransChangedExtensionPoint extensionPoint = new TransChangedExtensionPoint();
      extensionPoint.callExtensionPoint( null, null );
      verify( transMeta, times( 0 ) ).addContentChangedListener( any() );
      extensionPoint.callExtensionPoint( null, transMeta );
      verify( transMeta, times( 0 ) ).addContentChangedListener( any() );
    }
  }
}
