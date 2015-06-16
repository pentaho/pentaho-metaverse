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

package org.pentaho.metaverse.impl.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class VersionInfoTest {

  VersionInfo versionInfo;

  @Before
  public void setUp() throws Exception {
    versionInfo = new VersionInfo();
  }

  @Test
  public void testGetSetVersion() throws Exception {
    assertNull( versionInfo.getVersion() );
    versionInfo.setVersion( "test" );
    assertEquals( "test", versionInfo.getVersion() );
  }

  @Test
  public void testGetSetName() throws Exception {
    assertNull( versionInfo.getName() );
    versionInfo.setName( "test" );
    assertEquals( "test", versionInfo.getName() );
  }

  @Test
  public void testGetSetDescription() throws Exception {
    assertNull( versionInfo.getDescription() );
    versionInfo.setDescription( "test" );
    assertEquals( "test", versionInfo.getDescription() );
  }
}
