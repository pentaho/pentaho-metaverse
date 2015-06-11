/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.api.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BaseResourceInfoTest {

  BaseResourceInfo resourceInfo;

  @Before
  public void setUp() throws Exception {
    resourceInfo = new BaseResourceInfo();
  }

  @Test
  public void testGetSetType() throws Exception {
    assertNull( resourceInfo.getType() );
    resourceInfo.setType( "testType" );
    assertEquals( "testType", resourceInfo.getType() );

  }

  @Test
  public void testIsInputOutput() throws Exception {
    assertFalse( resourceInfo.isInput() );
    assertTrue( resourceInfo.isOutput() );
    resourceInfo.setInput( true );
    assertTrue( resourceInfo.isInput() );
    assertFalse( resourceInfo.isOutput() );
  }

  @Test
  public void testGetSetAttributes() throws Exception {
    assertTrue( resourceInfo.getAttributes().isEmpty() );
    resourceInfo.putAttribute( "testKey", "testValue" );
    assertEquals( "testValue", resourceInfo.getAttributes().get( "testKey" ) );
  }
}
