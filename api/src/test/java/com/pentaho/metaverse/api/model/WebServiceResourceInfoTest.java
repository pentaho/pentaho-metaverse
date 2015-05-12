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

package com.pentaho.metaverse.api.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 5/11/15.
 */
public class WebServiceResourceInfoTest {

  WebServiceResourceInfo resourceInfo;

  @Before
  public void setUp() throws Exception {
    resourceInfo = new WebServiceResourceInfo();
  }

  @Test
  public void testSetMethod() throws Exception {
    resourceInfo.setMethod( "myMethod" );
    assertEquals( resourceInfo.getAttributes().get( "method" ), "myMethod" );
  }

  @Test
  public void testSetBody() throws Exception {
    resourceInfo.setBody( "myBody" );
    assertEquals( resourceInfo.getAttributes().get( "body" ), "myBody" );
  }

  @Test
  public void testSetApplicationType() throws Exception {
    resourceInfo.setApplicationType( "JSON" );
    assertEquals( resourceInfo.getAttributes().get( "applicationType" ), "JSON" );
  }

  @Test
  public void testAddParameter() throws Exception {
    resourceInfo.addParameter( "param1", "value1" );
    Map parameters = (Map) resourceInfo.getAttributes().get( "parameters" );
    assertNotNull( parameters );
    assertEquals( parameters.get( "param1" ), "value1" );

    resourceInfo.addParameter( "param2", "value2" );
    parameters = (Map) resourceInfo.getAttributes().get( "parameters" );
    assertNotNull( parameters );
    assertEquals( parameters.get( "param2" ), "value2" );
  }

  @Test
  public void testAddHeader() throws Exception {
    resourceInfo.addHeader( "header1", "value1" );
    Map headers = (Map) resourceInfo.getAttributes().get( "headers" );
    assertNotNull( headers );
    assertEquals( headers.get( "header1" ), "value1" );

    resourceInfo.addHeader( "header2", "value2" );
    headers = (Map) resourceInfo.getAttributes().get( "headers" );
    assertNotNull( headers );
    assertEquals( headers.get( "header2" ), "value2" );
  }
}