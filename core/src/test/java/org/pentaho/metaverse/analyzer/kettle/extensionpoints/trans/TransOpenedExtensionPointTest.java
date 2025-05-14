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
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.pentaho.metaverse.util.MetaverseUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TransOpenedExtensionPointTest {

  @Mock
  TransMeta transMeta;

  @Before
  public void setUp() throws Exception {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    when( transMeta.getFilename() ).thenReturn( "/path/to/file.ktr" );
    when( transMeta.getName() ).thenReturn( "testTrans" );
    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
  }

  @Test
  public void testCallExtensionWithLineageOnPoint() throws Exception {
    try ( MockedStatic<MetaverseConfig> mockedMetaverseConfig = mockStatic( MetaverseConfig.class );
          MockedConstruction<MetaverseBuilder> mockedMetaverseBuilderConstruction = mockConstruction( MetaverseBuilder.class ) ) {
      mockedMetaverseConfig.when( MetaverseConfig::isLineageExecutionEnabled ).thenReturn( true );
      TransOpenedExtensionPoint extensionPoint = new TransOpenedExtensionPoint();
      extensionPoint.callExtensionPoint( null, transMeta );
      verify( mockedMetaverseBuilderConstruction.constructed().get( 0 ), times( 1 ) ).addNode( any() );
    }
  }

  @Test
  public void testCallExtensionWithLineageOffPoint() throws Exception {
    try ( MockedStatic<MetaverseConfig> mockedMetaverseConfig = mockStatic( MetaverseConfig.class );
          MockedConstruction<MetaverseBuilder> mockedMetaverseBuilderConstruction = mockConstruction( MetaverseBuilder.class ) ) {
      mockedMetaverseConfig.when( MetaverseConfig::isLineageExecutionEnabled ).thenReturn( false );
      TransOpenedExtensionPoint extensionPoint = new TransOpenedExtensionPoint();
      extensionPoint.callExtensionPoint( null, transMeta );

      assertEquals( mockedMetaverseBuilderConstruction.constructed().size(), 0 );
    }
  }
}
