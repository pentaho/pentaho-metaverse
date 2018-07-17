/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.impl.model;

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

    PrintStream out = null;
    try {
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
    } finally {
      IOUtils.closeQuietly( out );
    }
  }
}
