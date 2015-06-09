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

package com.pentaho.metaverse.analyzer.kettle.step.rest;

import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.rest.Rest;
import org.pentaho.di.trans.steps.rest.RestMeta;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class RestClientExternalResourceConsumerTest {

  RestClientExternalResourceConsumer consumer;

  @Mock RestMeta meta;
  @Mock Rest step;
  @Mock RowMetaInterface rmi;
  Object[] row;

  String[] headerFields = new String[]{ "header" };
  String[] headerNames = new String[]{ "header1" };
  String[] paramFields = new String[]{ "param" };
  String[] paramNames = new String[]{ "param1" };

  @Before
  public void setUp() throws Exception {
    consumer = new RestClientExternalResourceConsumer();

    row = new Object[3];
    row[0] = "http://my.url";
    row[1] = "POST";
    row[2] = "xyz";

    when( rmi.getString( row, "url", null ) ).thenReturn( row[0].toString() );

    when( step.getStepMetaInterface() ).thenReturn( meta );

  }

  @Test
  public void testGetResourcesFromMeta() throws Exception {
    when( meta.getUrl() ).thenReturn( row[ 0 ].toString() );
    Collection<IExternalResourceInfo> resourcesFromMeta = consumer.getResourcesFromMeta( meta );

    assertEquals( 1, resourcesFromMeta.size() );
    assertEquals( row[ 0 ], resourcesFromMeta.toArray( new IExternalResourceInfo[ 1 ] )[ 0 ].getName() );
  }

  @Test
  public void testGetResourcesFromRow() throws Exception {
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getUrlField() ).thenReturn( "url" );
    when( meta.getHeaderField() ).thenReturn( headerFields );
    when( meta.getParameterField() ).thenReturn( paramFields );
    when( meta.getHeaderName() ).thenReturn( headerNames );
    when( meta.getParameterName() ).thenReturn( paramNames );

    when( rmi.getString( row, "header", null ) ).thenReturn( row[ 2 ].toString() );
    when( rmi.getString( row, "param", null ) ).thenReturn( row[ 2 ].toString() );


    Collection<IExternalResourceInfo> resourcesFromMeta = consumer.getResourcesFromRow( step, rmi, row );

    assertEquals( 1, resourcesFromMeta.size() );
    IExternalResourceInfo resourceInfo = resourcesFromMeta.toArray( new IExternalResourceInfo[ 1 ] )[ 0 ];
    assertEquals( row[ 0 ], resourceInfo.getName() );
    assertNotNull( resourceInfo.getAttributes() );
  }

  @Test
  public void testGetResourcesFromRow_fieldsForMethodAndBody() throws Exception {
    when( meta.isUrlInField() ).thenReturn( true );
    when( meta.getUrlField() ).thenReturn( "url" );
    when( meta.getHeaderField() ).thenReturn( null );
    when( meta.getParameterField() ).thenReturn( null );
    when( meta.isDynamicMethod() ).thenReturn( true );
    when( meta.getMethodFieldName() ).thenReturn( "method" );
    when( meta.getBodyField() ).thenReturn( "body" );
    when( rmi.getString( row, "method", null ) ).thenReturn( row[ 2 ].toString() );
    when( rmi.getString( row, "body", null ) ).thenReturn( row[ 2 ].toString() );

    Collection<IExternalResourceInfo> resourcesFromMeta = consumer.getResourcesFromRow( step, rmi, row );

    assertEquals( 1, resourcesFromMeta.size() );
    IExternalResourceInfo resourceInfo = resourcesFromMeta.toArray( new IExternalResourceInfo[ 1 ] )[ 0 ];
    assertEquals( row[ 0 ], resourceInfo.getName() );
    assertNotNull( resourceInfo.getAttributes() );
  }

  @Test
  public void testIsDataDriven() throws Exception {
    assertTrue( consumer.isDataDriven( meta ) );
  }

  @Test
  public void testGetMetaClass() throws Exception {
    assertEquals( RestMeta.class, consumer.getMetaClass() );
  }
}