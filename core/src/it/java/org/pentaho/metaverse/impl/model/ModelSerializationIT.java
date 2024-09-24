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

package org.pentaho.metaverse.impl.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.metaverse.api.model.BaseResourceInfo;
import org.pentaho.metaverse.api.model.JdbcResourceInfo;

import java.sql.Timestamp;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModelSerializationIT {

  ObjectMapper mapper;

  @BeforeClass
  public static void init() throws Exception {
    // Register the default Kettle encoder (needed for password stuff)
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    Encr.init( "Kettle" );
  }

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
//    System.out.println( json );

    JdbcResourceInfo rehydrated = mapper.readValue( json, JdbcResourceInfo.class );

    assertEquals( jdbcResource.getServer(), rehydrated.getServer() );
    assertEquals( jdbcResource.getDatabaseName(), rehydrated.getDatabaseName() );
    assertEquals( jdbcResource.getUsername(), rehydrated.getUsername() );
    assertEquals( jdbcResource.getPassword(), rehydrated.getPassword() );
    assertEquals( jdbcResource.getPort(), rehydrated.getPort() );
    assertEquals( jdbcResource.isInput(), rehydrated.isInput() );

    ExecutionProfile executionProfile = new ExecutionProfile( "run1", "some/path/to/a.ktl", "tranformation", "A test profile" );

    long currentMillis = System.currentTimeMillis();
    long futureMillis = currentMillis + 10000;
    Timestamp startTime = new Timestamp( currentMillis );
    Timestamp endTime = new Timestamp( futureMillis );
    executionProfile.getExecutionData().setStartTime( startTime );
    executionProfile.getExecutionData().setEndTime( endTime );
    executionProfile.getExecutionData().setClientExecutor( "client.executer" );
    executionProfile.getExecutionData().setExecutorServer( "www.pentaho.com" );
    executionProfile.getExecutionData().setExecutorUser( "wseyler" );
    executionProfile.getExecutionData().setLoggingChannelId( "kettle.debug" );
    executionProfile.getExecutionData().addParameter( new ParamInfo( "testParam1", "Larry", "Fine", "A Test Parameter" ) );
    executionProfile.getExecutionData().addParameter( new ParamInfo( "testParam2", "Howard", "Moe", "Another Parameter" ) );
    executionProfile.getExecutionData().addParameter( new ParamInfo( "testParam3", "Fine", "Curly", "A Third Parameter" ) );

    String externalResourceName = "prices.csv";
    String externalResourceDescription = "A test csv file";
    String externalResourceType = "csv";
    String attributeName = "hair";
    String attributeValue = "red";
    BaseResourceInfo externalResourceInfo = new BaseResourceInfo();
    externalResourceInfo.setName( externalResourceName );
    externalResourceInfo.setDescription( externalResourceDescription );
    externalResourceInfo.setInput( true );
    externalResourceInfo.setType( externalResourceType );
    externalResourceInfo.putAttribute( attributeName, attributeValue );
    executionProfile.getExecutionData().addExternalResource( "testStep", externalResourceInfo );
    String variable1Name = "area";
    String variable1Value = "West";
    String variable2Name = "dept";
    String variable2Value = "Sales";
    executionProfile.getExecutionData().addVariable( variable1Name, variable1Value );
    executionProfile.getExecutionData().addVariable( variable2Name, variable2Value );
    String arg1 = "You're stupid";
    String arg2 = "You're ugly";
    String arg3 = "You're lazy";
    executionProfile.getExecutionData().putArgument( 0, arg1 );
    executionProfile.getExecutionData().putArgument( 1, arg2 );
    executionProfile.getExecutionData().putArgument( 2, arg3 );

    json = mapper.writeValueAsString( executionProfile );
//    System.out.println( json );

    ExecutionProfile rehydratedProfile = mapper.readValue( json, ExecutionProfile.class );
    assertEquals( executionProfile.getName(), rehydratedProfile.getName() );
    assertEquals( executionProfile.getPath(), rehydratedProfile.getPath() );
    assertEquals( executionProfile.getType(), rehydratedProfile.getType() );
    assertEquals( executionProfile.getDescription(), rehydratedProfile.getDescription() );
    assertEquals( executionProfile.getExecutionData().getStartTime().compareTo( rehydratedProfile.getExecutionData().getStartTime() ), 0 );
    assertEquals( executionProfile.getExecutionData().getEndTime().compareTo( rehydratedProfile.getExecutionData().getEndTime() ), 0 );
    assertEquals( executionProfile.getExecutionData().getFailureCount(), 0 );
    assertEquals( executionProfile.getExecutionData().getClientExecutor(), rehydratedProfile.getExecutionData().getClientExecutor() );
    assertEquals( executionProfile.getExecutionData().getExecutorServer(), rehydratedProfile.getExecutionData().getExecutorServer() );
    assertEquals( executionProfile.getExecutionData().getExecutorUser(), rehydratedProfile.getExecutionData().getExecutorUser() );
    assertEquals( executionProfile.getExecutionData().getLoggingChannelId(), rehydratedProfile.getExecutionData().getLoggingChannelId() );
    assertEquals( rehydratedProfile.getExecutionData().getParameters().size(), 3 );
    assertEquals( rehydratedProfile.getExecutionData().getExternalResources().size(), 1 );
    Map<Object, Object> attributes = rehydratedProfile.getExecutionData().getExternalResources().get( "testStep" ).get( 0 ).getAttributes();
    assertEquals( attributes.get( attributeName ), attributeValue );
    assertEquals( executionProfile.getExecutionData().getVariables().size(), 2 );
    assertTrue( executionProfile.getExecutionData().getVariables().containsKey( variable1Name ) );
    assertTrue( executionProfile.getExecutionData().getVariables().containsKey( variable2Name ) );
    assertTrue( executionProfile.getExecutionData().getVariables().containsValue( variable1Value ) );
    assertTrue( executionProfile.getExecutionData().getVariables().containsValue( variable2Value ) );
    assertEquals( executionProfile.getExecutionData().getArguments().get( 0 ), arg1 );
    assertEquals( executionProfile.getExecutionData().getArguments().get( 1 ), arg2 );
    assertEquals( executionProfile.getExecutionData().getArguments().get( 2 ), arg3 );
  }
}
