package com.pentaho.metaverse.impl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pentaho.metaverse.api.model.IExecutionProfile;

import java.io.IOException;
import java.io.PrintStream;

/**
 * A collection of utilities for working with Execution Profile documents
 */
public class ExecutionProfileUtil {

  public static void dumpExecutionProfile( PrintStream out, IExecutionProfile executionProfile ) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable( SerializationFeature.INDENT_OUTPUT );
    mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    mapper.enable( SerializationFeature.WRAP_EXCEPTIONS );
    try {
      out.println( mapper.writeValueAsString( executionProfile ) );
    } catch ( JsonProcessingException jpe ) {
      throw new IOException( jpe );
    }
  }
}
