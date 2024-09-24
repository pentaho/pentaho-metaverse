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
