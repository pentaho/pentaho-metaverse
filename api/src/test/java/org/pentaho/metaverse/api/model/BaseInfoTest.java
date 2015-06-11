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
