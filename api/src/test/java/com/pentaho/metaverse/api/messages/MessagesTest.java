/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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
 */

package com.pentaho.metaverse.api.messages;

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
