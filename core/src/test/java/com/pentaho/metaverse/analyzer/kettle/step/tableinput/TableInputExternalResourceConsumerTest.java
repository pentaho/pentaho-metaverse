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

package com.pentaho.metaverse.analyzer.kettle.step.tableinput;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 5/29/15.
 */
@RunWith(MockitoJUnitRunner.class)
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