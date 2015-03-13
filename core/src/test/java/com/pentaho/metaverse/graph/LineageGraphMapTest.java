package com.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Graph;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

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
