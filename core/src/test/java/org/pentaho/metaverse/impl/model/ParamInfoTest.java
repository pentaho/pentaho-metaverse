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
