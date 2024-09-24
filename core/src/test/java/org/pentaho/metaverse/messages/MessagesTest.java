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

package org.pentaho.metaverse.messages;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MessagesTest {

  @Test
  public void testConstructor() {
    Messages msg = new Messages();
    assertNotNull( msg );
  }

  @Test
  public void testGetString() throws Exception {
    assertEquals( "Test message", Messages.getString( "test.noParams" ) );
  }

  @Test
  public void testGetString_NoKey() throws Exception {
    assertEquals( "!noKeyMatch!", Messages.getString( "noKeyMatch" ) );
  }

  @Test
  public void testGetString_1param() throws Exception {
    assertEquals( "Test HELLO param", Messages.getString( "test.oneParam", "HELLO" ) );
  }

  @Test
  public void testGetString_2params() throws Exception {
    assertEquals( "Test ONE, TWO params",
      Messages.getString( "test.twoParams", "ONE", "TWO" ) );
  }

  @Test
  public void testGetString_3params() throws Exception {
    assertEquals( "Test ONE, TWO, THREE params",
      Messages.getString( "test.threeParams", "ONE", "TWO", "THREE" ) );
  }

  @Test
  public void testGetString_4params() throws Exception {
    assertEquals( "Test ONE, TWO, THREE, FOUR params",
      Messages.getString( "test.fourParams", "ONE", "TWO", "THREE", "FOUR" ) );
  }

  @Test
  public void testGetErrorString() throws Exception {
    assertEquals( "test.noParams - Test message", Messages.getErrorString( "test.noParams" ) );
  }

  @Test
  public void testGetErrorString_1param() throws Exception {
    assertEquals( "test.oneParam - Test HELLO param", Messages.getErrorString( "test.oneParam", "HELLO" ) );
  }

  @Test
  public void testGetErrorString_2params() throws Exception {
    assertEquals( "test.twoParams - Test ONE, TWO params",
      Messages.getErrorString( "test.twoParams", "ONE", "TWO" ) );
  }

  @Test
  public void testGetErrorString_3params() throws Exception {
    assertEquals( "test.threeParams - Test ONE, TWO, THREE params",
      Messages.getErrorString( "test.threeParams", "ONE", "TWO", "THREE" ) );
  }

}
