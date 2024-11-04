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
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LineageGraphCompletionServiceTest {

  public static final Graph GRAPH = new TinkerGraph();
  LineageGraphCompletionService mcs = LineageGraphCompletionService.getInstance();

  @Before
  public void setUp() throws Exception {
    mcs.waitTillEmpty();
  }

  @Test
  public void testSubmit_Callable() throws Exception {
    mcs.submit( new Callable<Graph>() {
      @Override
      public Graph call() throws Exception {
        Thread.sleep( 150 );
        return GRAPH;
      }
    } );

    Graph result = mcs.take().get();
    assertEquals( GRAPH, result );
  }

  @Test
  public void testSubmit_Runnable() throws Exception {
    mcs.submit( new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep( 100 );
        } catch ( InterruptedException e ) {
          e.printStackTrace();
        }
      }
    }, GRAPH );

    Graph result = mcs.take().get();
    assertEquals( GRAPH, result );
  }

  @Test
  public void testWaitTillEmpty() throws Exception {
    mcs.submit( new Callable<Graph>() {
      @Override
      public Graph call() throws Exception {
        Thread.sleep( 400 );
        return GRAPH;
      }
    } );

    // handle the exception case as well
    mcs.submit( new Callable<Graph>() {
      @Override
      public Graph call() throws Exception {
        throw new ExecutionException( new Exception( "Exception" ) );
      }
    } );

    mcs.submit( new Callable<Graph>() {
      @Override
      public Graph call() throws Exception {
        Thread.sleep( 200 );
        return GRAPH;
      }
    } );

    mcs.waitTillEmpty();

    // there shouldn't be anything left to process
    assertNull( mcs.poll() );
  }


}
