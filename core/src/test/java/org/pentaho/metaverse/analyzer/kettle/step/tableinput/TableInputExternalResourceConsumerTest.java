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

package org.pentaho.metaverse.analyzer.kettle.step.tableinput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 5/29/15.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TableInputExternalResourceConsumerTest {

  TableInputExternalResourceConsumer consumer;

  @Mock
  TableInputMeta meta;
  @Mock
  IAnalysisContext context;
  @Mock
  DatabaseMeta dbMeta;

  @Before
  public void setUp() throws Exception {
    consumer = spy( new TableInputExternalResourceConsumer() );

    DatabaseInterface dbi = mock( DatabaseInterface.class );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbi );
    when( dbi.getPluginId() ).thenReturn( "JNDI" );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "JNDI" );
  }

  @Test
  public void testGetMetaClass() throws Exception {
    assertEquals( TableInputMeta.class, consumer.getMetaClass() );
  }

  @Test
  public void testGetResourcesFromMeta() throws Exception {
    when( meta.getSQL() ).thenReturn( "select * from table" );
    when( meta.getDatabaseMeta() ).thenReturn( dbMeta );
    Collection<IExternalResourceInfo> resourceInfos = consumer.getResourcesFromMeta( meta, context );
    assertNotNull( resourceInfos );

    assertEquals( 1, resourceInfos.size() );
    IExternalResourceInfo info = resourceInfos.iterator().next();
    assertEquals( "select * from table", info.getAttributes().get( DictionaryConst.PROPERTY_QUERY ) );

  }
  @Test
  public void testGetResourcesFromMeta_nullDbMeta() throws Exception {
    when( meta.getDatabaseMeta() ).thenReturn( null );
    Collection<IExternalResourceInfo> resourceInfos = consumer.getResourcesFromMeta( meta, context );
    assertNotNull( resourceInfos );

    assertEquals( 0, resourceInfos.size() );

  }
}
