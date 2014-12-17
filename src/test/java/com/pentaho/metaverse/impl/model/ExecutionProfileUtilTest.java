package com.pentaho.metaverse.impl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintStream;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ExecutionProfileUtilTest {
  IExecutionProfile executionProfile;

  @Before
  public void setUp() throws Exception {
    executionProfile = new ExecutionProfile();
  }

  @Test
  public void testConstructor() {
    new ExecutionProfileUtil();
  }

  @Test
  public void testDumpExecutionProfile() throws Exception {
    ExecutionProfileUtil.dumpExecutionProfile( System.out, null );
    ExecutionProfileUtil.dumpExecutionProfile( System.out, executionProfile );
  }

  @Test( expected = IOException.class )
  public void testDumpExecutionProfileWithException() throws IOException {
    PrintStream mockStream = mock( PrintStream.class );
    doThrow( JsonProcessingException.class ).when( mockStream ).println( Mockito.anyString() );
    ExecutionProfileUtil.dumpExecutionProfile( mockStream, executionProfile );
  }
}
