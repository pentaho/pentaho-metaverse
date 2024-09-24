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

package org.pentaho.metaverse.impl.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ParamInfoTest {

  private static final String TEST_NAME = "testName";
  private static final String TEST_VALUE = "testValue";
  private static final String TEST_DEFAULT_VALUE = "testDefaultValue";
  private static final String TEST_DESCRIPTION = "testDescription";

  ParamInfo paramInfo;

  @Before
  public void setUp() throws Exception {
    paramInfo = new ParamInfo();
  }

  @Test
  public void testNonDefaultConstructors() {
    paramInfo = new ParamInfo( TEST_NAME );
    paramInfo = new ParamInfo( TEST_NAME, TEST_VALUE );
    paramInfo = new ParamInfo( TEST_NAME, TEST_VALUE, TEST_DEFAULT_VALUE );
    paramInfo = new ParamInfo( TEST_NAME, TEST_VALUE, TEST_DESCRIPTION );
  }

  @Test
  public void testGetSetDefaultValue() throws Exception {
    assertNull( paramInfo.getDefaultValue() );
    paramInfo.setDefaultValue( TEST_DEFAULT_VALUE );
    assertEquals( TEST_DEFAULT_VALUE, paramInfo.getDefaultValue() );
  }

  @Test
  public void testGetSetValue() throws Exception {
    assertNull( paramInfo.getValue() );
    paramInfo.setValue( TEST_VALUE );
    assertEquals( TEST_VALUE, paramInfo.getValue() );
  }
}
