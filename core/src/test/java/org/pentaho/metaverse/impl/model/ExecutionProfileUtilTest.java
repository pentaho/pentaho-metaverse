/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.metaverse.api.model.IExecutionProfile;

import java.io.IOException;
import java.io.PrintStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
  public void testOutputExecutionProfile() throws Exception {
    // Avoid issues with System.out getting closed, see LineageWriterTest.testOutputExecutionProfile()
    try( MockedStatic<IOUtils> mocked = mockStatic( IOUtils.class ) ) {
      ExecutionProfileUtil.outputExecutionProfile( System.out, null );
      ExecutionProfileUtil.outputExecutionProfile( System.out, executionProfile );
    }
  }

  @Test( expected = IOException.class )
  public void testOutputExecutionProfileWithException() throws IOException {
    PrintStream mockStream = mock( PrintStream.class );
    ObjectMapper mapper = mock( ObjectMapper.class );
    when( mapper.writeValueAsString( any() ) ).thenThrow( JsonProcessingException.class );
    ExecutionProfileUtil.outputExecutionProfile( mockStream, executionProfile, mapper );
  }
}
