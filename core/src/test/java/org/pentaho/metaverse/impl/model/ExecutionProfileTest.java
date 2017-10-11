/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionEngine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecutionProfileTest {

  private ExecutionProfile executionProfile;

  @Before
  public void setUp() throws Exception {
    executionProfile = new ExecutionProfile();
  }

  @Test
  public void testNonDefaultConstructor() {
    executionProfile = new ExecutionProfile( "myKTR", "/my/path", "myType", "My description" );
    assertEquals( "myKTR", executionProfile.getName() );
    assertEquals( "/my/path", executionProfile.getPath() );
    assertEquals( "myType", executionProfile.getType() );
    assertEquals( "My description", executionProfile.getDescription() );
  }

  @Test
  public void testGetSetPath() throws Exception {
    assertNull( executionProfile.getPath() );
    executionProfile.setPath( "/this/is/a/test" );
    assertEquals( "/this/is/a/test", executionProfile.getPath() );
  }

  @Test
  public void testGetSetType() throws Exception {
    assertNull( executionProfile.getType() );
    executionProfile.setType( "test" );
    assertEquals( "test", executionProfile.getType() );
  }

  @Test
  public void testGetSetExecutionEngine() throws Exception {
    IExecutionEngine mockEngine = mock( IExecutionEngine.class );
    when( mockEngine.getName() ).thenReturn( "engineName" );
    assertNotNull( executionProfile.getExecutionEngine() );
    assertNull( executionProfile.getExecutionEngine().getName() );
    executionProfile.setExecutionEngine( mockEngine );
    assertNotNull( executionProfile.getExecutionEngine() );
    assertEquals( "engineName", executionProfile.getExecutionEngine().getName() );
  }

  @Test
  public void testGetSetExecutionData() throws Exception {
    IExecutionData mockData = mock( IExecutionData.class );
    when( mockData.getExecutorUser() ).thenReturn( "myUser" );
    assertNotNull( executionProfile.getExecutionData() );
    assertNull( executionProfile.getExecutionData().getExecutorUser() );
    executionProfile.setExecutionData( mockData );
    assertNotNull( executionProfile.getExecutionData() );
    assertEquals( "myUser", executionProfile.getExecutionData().getExecutorUser() );
  }

  @Test
  public void testGetSetName() throws Exception {
    assertNull( executionProfile.getName() );
    executionProfile.setName( "test" );
    assertEquals( "test", executionProfile.getName() );
  }

  @Test
  public void testGetSetDescription() throws Exception {
    assertNull( executionProfile.getDescription() );
    executionProfile.setDescription( "test" );
    assertEquals( "test", executionProfile.getDescription() );
  }
}
