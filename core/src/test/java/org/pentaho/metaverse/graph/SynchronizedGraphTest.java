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

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.api.model.BaseSynchronizedGraph;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SynchronizedGraphTest {

  private SynchronizedGraph synchronizedGraph;

  @Before
  public void setUp() {
    synchronizedGraph = new SynchronizedGraph( TinkerGraph.open() );
  }

  @Test
  public void testAddVertexWithExistingId() {
    Vertex original = synchronizedGraph.addVertexWithId( "id" );
    Vertex duplicate = synchronizedGraph.addVertexWithId( "id" );

    assertSame( original, duplicate );
    assertEquals( 1, countVertices( synchronizedGraph.vertices() ) );
  }

  @Test
  public void testAddVertexWithNewId() {
    Vertex vertex = synchronizedGraph.addVertexWithId( "id" );
    assertNotNull( vertex );
    assertEquals( "id", vertex.id() );
  }

  @Test
  public void testGraphDelegates() throws Exception {
    Vertex source = synchronizedGraph.addVertex( T.id, "source" );
    Vertex target = synchronizedGraph.addVertex( T.id, "target" );
    Edge edge = source.addEdge( "self link", target, T.id, "edge-id" );

    assertSame( source, synchronizedGraph.getVertex( "source" ) );
    assertSame( edge, synchronizedGraph.getEdge( "edge-id" ) );
    assertEquals( 2, countVertices( synchronizedGraph.vertices() ) );
    assertEquals( 1, countEdges( synchronizedGraph.edges() ) );
    assertEquals( 1, countEdges( synchronizedGraph.edges( "edge-id" ) ) );
    assertNotNull( synchronizedGraph.variables() );
    assertNotNull( synchronizedGraph.configuration() );
    assertTrue( synchronizedGraph.getGraph() instanceof TinkerGraph );

    synchronizedGraph.close();
  }

  @Test
  public void testWriteWaitsForLockAndIsReentrant() throws Exception {
    assertWaitsForWriteLock( synchronizedGraph, () -> BaseSynchronizedGraph.write( synchronizedGraph,
      () -> synchronizedGraph.addVertexWithId( "locked" ) ) );
    assertNotNull( synchronizedGraph.getVertex( "locked" ) );
  }

  @Test( expected = AssertionError.class )
  public void testLockAssertionRejectsUnlockedAction() throws Exception {
    assertWaitsForWriteLock( synchronizedGraph, () -> {
    } );
  }

  @FunctionalInterface
  public interface GraphWrite {
    void run() throws Exception;
  }

  public static void assertWaitsForWriteLock( Graph graph, GraphWrite write ) throws InterruptedException {
    AtomicReference<Throwable> failure = new AtomicReference<>();
    Thread writer = new Thread( () -> {
      try {
        write.run();
      } catch ( Throwable throwable ) {
        failure.set( throwable );
      }
    }, "graph-write-lock-test" );
    writer.setDaemon( true );
    ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    long ownerId = Thread.currentThread().getId();
    try {
      BaseSynchronizedGraph.write( graph, () -> {
        writer.start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos( 10 );
        boolean blockedByOwner = false;
        while ( writer.isAlive() && System.nanoTime() < deadline ) {
          ThreadInfo info = threads.getThreadInfo( writer.getId() );
          if ( info != null && info.getThreadState() == Thread.State.BLOCKED && info.getLockOwnerId() == ownerId ) {
            blockedByOwner = true;
            break;
          }
          LockSupport.parkNanos( TimeUnit.MILLISECONDS.toNanos( 1 ) );
          if ( Thread.interrupted() ) {
            throw new InterruptedException();
          }
        }
        assertTrue( "graph write did not wait for the graph write lock", blockedByOwner );
        return null;
      } );
    } finally {
      writer.join( 10000 );
      if ( writer.isAlive() ) {
        writer.interrupt();
      }
    }
    assertFalse( "graph write did not finish after lock release", writer.isAlive() );
    if ( failure.get() != null ) {
      throw new AssertionError( "graph write failed", failure.get() );
    }
  }

  private int countVertices( Iterator<Vertex> vertices ) {
    int count = 0;
    while ( vertices.hasNext() ) {
      count++;
      vertices.next();
    }
    return count;
  }

  private int countEdges( Iterator<Edge> edges ) {
    int count = 0;
    while ( edges.hasNext() ) {
      count++;
      edges.next();
    }
    return count;
  }
}
