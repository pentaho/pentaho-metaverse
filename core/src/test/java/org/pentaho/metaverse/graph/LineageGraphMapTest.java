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


package org.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Graph;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LineageGraphMapTest {

  @Test
  public void testDefaultConstructor() {
    assertNotNull( new LineageGraphMap() );
  }

  @Test
  public void testGetInstance() throws Exception {

    Map<Object, Future<Graph>> map1 = LineageGraphMap.getInstance();
    Map<Object, Future<Graph>> map2 = LineageGraphMap.getInstance();
    assertNotNull( map1 );
    assertEquals( map1, map2 );

  }
}
