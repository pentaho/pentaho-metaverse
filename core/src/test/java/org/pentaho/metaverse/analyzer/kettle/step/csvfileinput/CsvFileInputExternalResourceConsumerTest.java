/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.csvfileinput;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInput;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class CsvFileInputExternalResourceConsumerTest {

  @Mock CsvInputMeta meta;
  @Mock CsvInput input;
  @Mock TransMeta transMeta;
  @Mock RowMetaInterface rmi;

  @Test
  public void testCsvInputExternalResourceConsumer() throws Exception {
    CsvFileInputExternalResourceConsumer consumer = new CsvFileInputExternalResourceConsumer();

    StepMeta stepMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( stepMeta );
    when( stepMeta.getParentTransMeta() ).thenReturn( transMeta );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( meta.getFilePaths( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );

    when( rmi.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( input, rmi, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( CsvInputMeta.class, consumer.getMetaClass() );
  }
}
