/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
