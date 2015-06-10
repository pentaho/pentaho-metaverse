/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class TableOutputExternalResourceConsumerTest {

  TableOutputExternalResourceConsumer consumer;

  @Mock StepMeta parentStepMeta;
  @Mock TransMeta parentTransMeta;


  @Before
  public void setUp() throws Exception {
    consumer = new TableOutputExternalResourceConsumer();
  }

  @Test
  public void testGetStepMetaClass() throws Exception {
    assertEquals( TableOutputMeta.class, consumer.getMetaClass() );
  }

  @Test
  public void testGetResourcesFromMeta_runtime() throws Exception {
    TableOutputMeta meta = mock( TableOutputMeta.class );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    DatabaseInterface dbi = mock( DatabaseInterface.class );

    when( meta.getDatabaseMeta() ).thenReturn( dbMeta );
    when( meta.getTableName() ).thenReturn( "tableName" );
    when( meta.getSchemaName() ).thenReturn( "schemaName" );

    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    when( parentTransMeta.environmentSubstitute( "tableName" ) ).thenReturn( "tableName" );
    when( parentTransMeta.environmentSubstitute( "schemaName" ) ).thenReturn( "schemaName" );

    when( dbMeta.getAccessTypeDesc() ).thenReturn( "JNDI" );
    when( dbMeta.getName() ).thenReturn( "TestConnection" );
    when( dbMeta.getDescription() ).thenReturn( "my conn description" );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbi );

    when( dbi.getPluginId() ).thenReturn( "POSTGRESQL" );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta, new AnalysisContext(
      DictionaryConst.CONTEXT_RUNTIME ) );
    assertEquals( 1, resources.size() );
    IExternalResourceInfo res = resources.iterator().next();
    assertEquals( "TestConnection", res.getName() );

    assertEquals( "tableName", res.getAttributes().get( DictionaryConst.PROPERTY_TABLE ) );
    assertEquals( "schemaName", res.getAttributes().get( DictionaryConst.PROPERTY_SCHEMA ) );

  }

  @Test
  public void testGetResourcesFromMeta_static() throws Exception {
    TableOutputMeta meta = mock( TableOutputMeta.class );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    DatabaseInterface dbi = mock( DatabaseInterface.class );

    when( meta.getDatabaseMeta() ).thenReturn( dbMeta );
    when( meta.getTableName() ).thenReturn( "tableName" );
    when( meta.getSchemaName() ).thenReturn( "schemaName" );

    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    when( parentTransMeta.environmentSubstitute( "tableName" ) ).thenReturn( "tableName" );
    when( parentTransMeta.environmentSubstitute( "schemaName" ) ).thenReturn( "schemaName" );

    when( dbMeta.getAccessTypeDesc() ).thenReturn( "JNDI" );
    when( dbMeta.getName() ).thenReturn( "TestConnection" );
    when( dbMeta.getDescription() ).thenReturn( "my conn description" );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbi );

    when( dbi.getPluginId() ).thenReturn( "POSTGRESQL" );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta, new AnalysisContext(
      DictionaryConst.CONTEXT_STATIC ) );
    assertEquals( 1, resources.size() );
    IExternalResourceInfo res = resources.iterator().next();
    assertEquals( "TestConnection", res.getName() );

    assertEquals( "tableName", res.getAttributes().get( DictionaryConst.PROPERTY_TABLE ) );
    assertEquals( "schemaName", res.getAttributes().get( DictionaryConst.PROPERTY_SCHEMA ) );

  }
}
