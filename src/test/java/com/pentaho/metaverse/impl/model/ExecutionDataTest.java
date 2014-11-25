package com.pentaho.metaverse.impl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.model.IParamInfo;

public class ExecutionDataTest {
  ExecutionData executionData;

  @Before
  public void setUp() throws Exception {
    executionData = new ExecutionData();
  }

  @Test
  public void testGetSetStartTime() {
    assertNull( executionData.getStartTime() );
    Date startTime = new Date();
    executionData.setStartTime( startTime );
    assertEquals( executionData.getStartTime(), startTime );
  }

  @Test
  public void testGetSetEndTime() {
    assertNull( executionData.getEndTime() );
    Date endTime = new Date();
    executionData.setEndTime( endTime );
    assertEquals( executionData.getEndTime(), endTime );
  }

  @Test
  public void testGetSetFailureCount() {
    long failureCount = 10;
    assertNotEquals( executionData.getFailureCount(), failureCount );
    executionData.setFailureCount( failureCount );
    assertEquals( executionData.getFailureCount(), failureCount );
  }

  @Test
  public void testGetSetExecutorServer() {
    String executorServer = "executor server";
    assertNull( executionData.getExecutorServer() );
    executionData.setExecutorServer( executorServer );
    assertEquals( executionData.getExecutorServer(), executorServer );
  }

  @Test
  public void testGetSetExecutorUser() {
    String executorUser = "executor user";
    assertNull( executionData.getExecutorUser() );
    executionData.setExecutorUser( executorUser );
    assertEquals( executionData.getExecutorUser(), executorUser );
  }

  @Test
  public void testGetSetClientExecutor() {
    String clientExecutor = "client executor";
    assertNull( executionData.getClientExecutor() );
    executionData.setClientExecutor( clientExecutor );
    assertEquals( executionData.getClientExecutor(), clientExecutor );
  }

  @Test
  public void testGetSetLoggingChannelId() {
    String loggingChannelId = "debug";
    assertNull( executionData.getLoggingChannelId() );
    executionData.setLoggingChannelId( loggingChannelId );
    assertEquals( executionData.getLoggingChannelId(), loggingChannelId );
  }

  @Test
  public void testGetSetParameters() {
    List<IParamInfo<String>> parameters = new ArrayList<IParamInfo<String>>();
    parameters.add( new ParamInfo( "Larry", "fine" ) );
    parameters.add( new ParamInfo( "moe", "howard" ) );
    parameters.add( new ParamInfo( "curly", "fine") );
    assertEquals( executionData.getParameters().size(), 0 );
    executionData.setParameters( parameters );
    assertEquals( executionData.getParameters().size(), 3 );
  }

  @Test
  public void testGetSetExternalResources() {
    List<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    externalResources.add( new BaseResourceInfo() );
    assertEquals( executionData.getExternalResources().size(), 0 );
    executionData.setExternalResources( externalResources );
    assertEquals( executionData.getExternalResources().size(), 1 );
  }

  @Test
  public void testAddExternalResource() {
    IExternalResourceInfo externalResource = new BaseResourceInfo();
    assertEquals( executionData.getExternalResources().size(), 0 );
    executionData.addExternalResource( externalResource );
    assertEquals( executionData.getExternalResources().get( 0 ), externalResource );

  }

  @Test
  public void testPutGetArgument() {
    assertEquals( executionData.getArguments().size(), 0 );
    executionData.putArgument( 0, "testValue" );
    assertEquals( executionData.getArguments().size(), 1 );
  }

  @Test
  public void testAddGetVariables() {
    assertEquals( executionData.getVariables().size(), 0 );
    executionData.addVariable( "Captain", "Kangaroo" );
    assertEquals( executionData.getVariables().size(), 1 );
  }
}
