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
