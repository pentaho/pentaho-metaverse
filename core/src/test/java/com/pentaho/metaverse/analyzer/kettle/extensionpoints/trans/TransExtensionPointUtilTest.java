package com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.MetaverseException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
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
}
