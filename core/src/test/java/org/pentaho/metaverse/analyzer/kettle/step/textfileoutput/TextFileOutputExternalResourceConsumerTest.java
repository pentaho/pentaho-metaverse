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


package org.pentaho.metaverse.analyzer.kettle.step.textfileoutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputData;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TextFileOutputExternalResourceConsumerTest {

  TextFileOutputExternalResourceConsumer consumer;

  @Mock TextFileOutputMeta meta;
  @Mock TextFileOutputData data;
  @Mock TextFileOutput tfo;
  @Mock TransMeta transMeta;
  @Mock RowMetaInterface rmi;

  @Before
  public void setUp() throws Exception {
    consumer = new TextFileOutputExternalResourceConsumer();
  }

  @Test
  public void testTextFileOutputExternalResourceConsumer() throws Exception {

    StepMeta stepMeta = new StepMeta( "test", meta );
    StepMeta spyMeta = spy( stepMeta );

    when( tfo.getStepMeta() ).thenReturn( spyMeta );
    when( tfo.getStepDataInterface() ).thenReturn( data );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.getFileName() ).thenReturn( null );
    when( meta.isFileNameInField() ).thenReturn( false );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( meta.getFiles( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );


    when( meta.isFileNameInField() ).thenReturn( true );
    lenient().when( meta.getExtension() ).thenReturn( "txt" );

    assertTrue( consumer.isDataDriven( meta ) );
    assertTrue( consumer.getResourcesFromMeta( meta ).isEmpty() );

    data.fileName = "/path/to/row/file";
    when( tfo.buildFilename( Mockito.anyString(), Mockito.anyBoolean() ) )
      .thenAnswer( new Answer<String>() {
        @Override
        public String answer( InvocationOnMock invocation ) throws Throwable {
          Object[] args = invocation.getArguments();
          return ( args[ 0 ].toString() + ".txt" );
        }
      } );

    resources = consumer.getResourcesFromRow( tfo, rmi, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    try( MockedStatic<KettleVFS> mocked = mockStatic( KettleVFS.class ) ) {
      mocked.when( () -> KettleVFS.getFileObject( any() ) ).thenThrow( new KettleFileException() );
      resources = consumer.getResourcesFromRow( tfo, rmi, new String[]{ "id", "name" } );
      assertTrue( resources.isEmpty() );
    }

    assertEquals( TextFileOutputMeta.class, consumer.getMetaClass() );
  }

}
