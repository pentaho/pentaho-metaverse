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

package org.pentaho.metaverse.service;

import org.junit.Test;

import static org.junit.Assert.*;

public class AnalyzerInfoTest {

  @Test
  public void testGetSetMeta() throws Exception {
    AnalyzerInfo info = new AnalyzerInfo();
    assertNull( info.getMeta() );
    info.setMeta( "hello" );
    assertEquals( "hello", info.getMeta() );
  }

}