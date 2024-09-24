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

package org.pentaho.metaverse.api.messages;

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
