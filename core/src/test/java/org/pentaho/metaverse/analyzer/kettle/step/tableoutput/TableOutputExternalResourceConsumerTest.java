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


package org.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
    lenient().when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    lenient().when( parentTransMeta.environmentSubstitute( "tableName" ) ).thenReturn( "tableName" );
    lenient().when( parentTransMeta.environmentSubstitute( "schemaName" ) ).thenReturn( "schemaName" );

    when( dbMeta.getAccessTypeDesc() ).thenReturn( "JNDI" );
    when( dbMeta.getName() ).thenReturn( "TestConnection" );
    when( dbMeta.getDescription() ).thenReturn( "my conn description" );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbi );

    when( dbi.getPluginId() ).thenReturn( "POSTGRESQL" );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( DefaultBowl.getInstance(), meta,
      new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
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

    lenient().when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    lenient().when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    lenient().when( parentTransMeta.environmentSubstitute( "tableName" ) ).thenReturn( "tableName" );
    lenient().when( parentTransMeta.environmentSubstitute( "schemaName" ) ).thenReturn( "schemaName" );

    when( dbMeta.getAccessTypeDesc() ).thenReturn( "JNDI" );
    when( dbMeta.getName() ).thenReturn( "TestConnection" );
    when( dbMeta.getDescription() ).thenReturn( "my conn description" );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbi );

    when( dbi.getPluginId() ).thenReturn( "POSTGRESQL" );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( DefaultBowl.getInstance(), meta,
      new AnalysisContext( DictionaryConst.CONTEXT_STATIC ) );
    assertEquals( 1, resources.size() );
    IExternalResourceInfo res = resources.iterator().next();
    assertEquals( "TestConnection", res.getName() );

    assertEquals( "tableName", res.getAttributes().get( DictionaryConst.PROPERTY_TABLE ) );
    assertEquals( "schemaName", res.getAttributes().get( DictionaryConst.PROPERTY_SCHEMA ) );

  }
}
