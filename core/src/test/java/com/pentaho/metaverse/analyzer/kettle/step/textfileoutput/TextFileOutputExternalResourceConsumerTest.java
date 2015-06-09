/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 *
 */

package com.pentaho.metaverse.analyzer.kettle.step.textfileoutput;

import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputData;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
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
    when( meta.getExtension() ).thenReturn( "txt" );

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

    when( data.fileName ).thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( tfo, rmi, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( TextFileOutputMeta.class, consumer.getMetaClass() );
  }

}
