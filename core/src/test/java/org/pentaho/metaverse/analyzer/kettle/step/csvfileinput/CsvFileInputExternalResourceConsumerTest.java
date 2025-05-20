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


package org.pentaho.metaverse.analyzer.kettle.step.csvfileinput;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInput;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( DefaultBowl.getInstance(), meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );

    resources = consumer.getResourcesFromRow( input, rmi, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( CsvInputMeta.class, consumer.getMetaClass() );
  }
}
