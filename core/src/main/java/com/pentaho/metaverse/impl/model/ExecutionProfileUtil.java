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
package com.pentaho.metaverse.impl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pentaho.metaverse.api.model.IExecutionProfile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A collection of utilities for working with Execution Profile documents
 */
public class ExecutionProfileUtil {

  protected ExecutionProfileUtil() {
    // Protected per Singleton pattern (but available for testing)
  }

  public static void outputExecutionProfile( OutputStream outputStream, IExecutionProfile executionProfile )
    throws IOException {

    PrintStream out;
    if ( outputStream instanceof PrintStream ) {
      out = (PrintStream) outputStream;
    } else {
      out = new PrintStream( outputStream );
    }
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
