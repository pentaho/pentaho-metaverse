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

package org.pentaho.metaverse.graph.adapter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DirectionAdapterTest {

  @Test
  public void testGetOppositeFromIn() {
    assertEquals( DirectionAdapter.OUT, DirectionAdapter.IN.getOpposite() );
  }

  @Test
  public void testGetOppositeFromOut() {
    assertEquals( DirectionAdapter.IN, DirectionAdapter.OUT.getOpposite() );
  }
}
