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
