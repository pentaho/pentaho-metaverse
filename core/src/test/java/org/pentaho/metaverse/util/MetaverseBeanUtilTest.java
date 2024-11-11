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


package org.pentaho.metaverse.util;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetaverseBeanUtilTest {

  public static final String BEAN_NAME = "testBean";

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( MetaverseBeanUtil.getInstance() );
  }

  @Test
  public void testGet() throws Exception {
    TestObject testObject = new TestObject();
    PentahoSystem.registerObject( testObject );
    assertEquals( MetaverseBeanUtil.getInstance().get( TestInterface.class ), testObject );
  }

  public interface TestInterface {}

  public class TestObject implements TestInterface {}
}
