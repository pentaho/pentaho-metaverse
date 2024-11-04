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


package org.pentaho.metaverse.api.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BaseInfoTest {

  BaseInfo baseInfo;

  @Before
  public void setUp() throws Exception {
    baseInfo = new BaseInfo();
  }

  @Test
  public void testGetSetName() throws Exception {
    assertNull( baseInfo.getName() );
    baseInfo.setName( "test" );
    assertEquals( "test", baseInfo.getName() );
  }

  @Test
  public void testGetSetDescription() throws Exception {
    assertNull( baseInfo.getDescription() );
    baseInfo.setDescription( "test" );
    assertEquals( "test", baseInfo.getDescription() );
  }
}
