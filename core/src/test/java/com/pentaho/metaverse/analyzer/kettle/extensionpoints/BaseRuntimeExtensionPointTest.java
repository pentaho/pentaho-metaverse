package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.pentaho.metaverse.api.model.IExecutionEngine;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseRuntimeExtensionPointTest {

  BaseRuntimeExtensionPoint extensionPoint;

  @Before
  public void setUp() throws Exception {
    extensionPoint = new BaseRuntimeExtensionPoint() {
      @Override
      public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
        // Noop
      }
    };
  }

  @Test
  public void testWriteExecutionProfile() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    assertEquals( 0, baos.size() );
    PrintStream stringStream = new PrintStream( baos );
    IExecutionProfile executionProfile = new ExecutionProfile();
    executionProfile.setName( "testName" );
    extensionPoint.writeExecutionProfile( stringStream, executionProfile );
    assertNotEquals( 0, baos.size() );
    assertTrue( baos.toString().contains( "testName" ) );
  }

  @Test
  public void testGetExecutionEngineInfo() {
    IExecutionEngine engineInfo = extensionPoint.getExecutionEngineInfo();
    assertNotNull( engineInfo );
    assertEquals( BaseRuntimeExtensionPoint.EXECUTION_ENGINE_NAME, engineInfo.getName() );
    assertEquals( BaseRuntimeExtensionPoint.EXECUTION_ENGINE_DESCRIPTION, engineInfo.getDescription() );
  }
}
