/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.graph;

import org.junit.Test;

public class GraphUtilTest {

  @Test( expected = UnsupportedOperationException.class )
  public void testProtectedConstructor() {
    new GraphUtil();
  }

  @Test
  public void testCloneVertexIntoGraph() throws Exception {

  }
}
