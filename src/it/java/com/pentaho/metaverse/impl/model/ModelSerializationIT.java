/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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
 */

package com.pentaho.metaverse.impl.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModelSerializationIT {

  ObjectMapper mapper;

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    mapper.enable( SerializationFeature.INDENT_OUTPUT );
    mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    mapper.enable( SerializationFeature.WRAP_EXCEPTIONS );
  }

  @Test
  public void testSerializeDeserialize() throws Exception {

    String server = "localhost";
    String dbName = "test";
    int port = 9999;
    String user = "testUser";
    String password = "password";

    JdbcResourceInfo jdbcResource = new JdbcResourceInfo( server, dbName, port, user, password );
    jdbcResource.setInput( true );

    String json = mapper.writeValueAsString( jdbcResource );
    System.out.println( json );

    JdbcResourceInfo rehydrated = mapper.readValue( json, JdbcResourceInfo.class );

    assertEquals( jdbcResource.getServer(), rehydrated.getServer() );
    assertEquals( jdbcResource.getDatabaseName(), rehydrated.getDatabaseName() );
    assertEquals( jdbcResource.getUsername(), rehydrated.getUsername() );
    assertEquals( jdbcResource.getPassword(), rehydrated.getPassword() );
    assertEquals( jdbcResource.getPort(), rehydrated.getPort() );
    assertEquals( jdbcResource.isInput(), rehydrated.isInput() );

  }
}
