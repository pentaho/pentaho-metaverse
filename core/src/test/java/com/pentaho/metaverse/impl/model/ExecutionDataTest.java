package com.pentaho.metaverse.impl.model;

import org.pentaho.metaverse.api.model.BaseResourceInfo;
import org.pentaho.metaverse.api.model.IArtifactMetadata;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.IParamInfo;
import org.pentaho.metaverse.api.model.IUserMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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
  public void testGetSetAddParameters() {
    List<IParamInfo<String>> parameters = new ArrayList<IParamInfo<String>>();
    parameters.add( new ParamInfo( "Larry", "fine" ) );
    parameters.add( new ParamInfo( "moe", "howard" ) );
    parameters.add( new ParamInfo( "curly", "fine" ) );
    assertEquals( executionData.getParameters().size(), 0 );
    executionData.setParameters( parameters );
    assertEquals( executionData.getParameters().size(), 3 );
    executionData.addParameter( new ParamInfo( "Shemp", "Howard" ) );
  }

  @Test
  public void testGetSetExternalResources() {
    Map<String, List<IExternalResourceInfo>> resourceMap = new HashMap<String, List<IExternalResourceInfo>>();
    List<IExternalResourceInfo> externalResources = new ArrayList<IExternalResourceInfo>();
    externalResources.add( new BaseResourceInfo() );
    assertEquals( executionData.getExternalResources().size(), 0 );
    resourceMap.put( "testStep", externalResources );
    executionData.setExternalResources( resourceMap );
    assertEquals( executionData.getExternalResources().get( "testStep" ).size(), 1 );
  }

  @Test
  public void testAddExternalResource() {
    IExternalResourceInfo externalResource = new BaseResourceInfo();
    assertEquals( executionData.getExternalResources().size(), 0 );
    executionData.addExternalResource( "testStep", externalResource );
    assertEquals( executionData.getExternalResources().get( "testStep" ).get( 0 ), externalResource );

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

  @Test
  public void testGetSetArtifactMetadata() {
    assertNull( executionData.getArtifactMetadata() );
    IArtifactMetadata artifactMetadata = mock( IArtifactMetadata.class );
    executionData.setArtifactMetadata( artifactMetadata );
    assertSame( artifactMetadata, executionData.getArtifactMetadata() );
  }

  @Test
  public void testGetSetUserMetadata() {
    assertNull( executionData.getUserMetadata() );
    IUserMetadata userMetadata = mock( IUserMetadata.class );
    executionData.setUserMetadata( userMetadata );
    assertSame( userMetadata, executionData.getUserMetadata() );
  }
}
