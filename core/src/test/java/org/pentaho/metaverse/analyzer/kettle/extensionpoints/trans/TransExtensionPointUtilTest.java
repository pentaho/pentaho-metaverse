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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.pentaho.metaverse.util.MetaverseUtil;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TransExtensionPointUtilTest {

  @Mock
  TransMeta transMeta;

  @Before
  public void setUp() {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    when( transMeta.getFilename() ).thenReturn( "/path/to/file.ktr" );
    when( transMeta.getName() ).thenReturn( "testTrans" );
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull( new TransExtensionPointUtil() );
  }

  @Test( expected = MetaverseException.class )
  public void testAddLineageGraphNullTransMeta() throws Exception {
    TransExtensionPointUtil.addLineageGraph( null );
  }

  @Test
  public void testAddLineageGraphNullFilename() throws Exception {
    IDocumentController mockDoc = mock( IDocumentController.class );
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockDoc.getMetaverseObjectFactory() ).thenReturn( factory );
    MetaverseUtil.setDocumentController( mockDoc );
    when( transMeta.getFilename() ).thenReturn( null );
    when( transMeta.getPathAndName() ).thenReturn( "/Transformation 1" );
    TransExtensionPointUtil.addLineageGraph( transMeta );
  }
}
