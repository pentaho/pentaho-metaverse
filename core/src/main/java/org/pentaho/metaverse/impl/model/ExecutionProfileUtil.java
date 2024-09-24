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

import com.cronutils.utils.VisibleForTesting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.pentaho.metaverse.api.model.IExecutionProfile;

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
    outputExecutionProfile( outputStream, executionProfile, new ObjectMapper() );
  }

  @VisibleForTesting
  protected static void outputExecutionProfile( OutputStream outputStream, IExecutionProfile executionProfile, ObjectMapper mapper )
    throws IOException {
    PrintStream out = null;
    try {
      if ( outputStream instanceof PrintStream ) {
        out = (PrintStream) outputStream;
      } else {
        out = new PrintStream( outputStream );
      }
      mapper.enable( SerializationFeature.INDENT_OUTPUT );
      mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
      mapper.enable( SerializationFeature.WRAP_EXCEPTIONS );
      try {
        out.println( mapper.writeValueAsString( executionProfile ) );
      } catch ( JsonProcessingException jpe ) {
        throw new IOException( jpe );
      }
    } finally {
      IOUtils.closeQuietly( out );
    }
  }
}
