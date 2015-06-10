/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class LineageGraphCompletionService implements CompletionService<Graph> {

  private static final LineageGraphCompletionService INSTANCE = new LineageGraphCompletionService();

  private ExecutorCompletionService<Graph> executionCompletionService;
  private Executor executor = Executors.newCachedThreadPool();
  private static final Logger log = LoggerFactory.getLogger( LineageGraphCompletionService.class );
  private Queue<Future<Graph>> queue = new ConcurrentLinkedQueue<Future<Graph>>();


  public static LineageGraphCompletionService getInstance() {
    return INSTANCE;
  }

  private LineageGraphCompletionService() {
    executionCompletionService = new ExecutorCompletionService<Graph>( executor );
  }

  @Override
  public Future<Graph> submit( Callable<Graph> task ) {
    log.debug( "Submitting Callable task --> " + task.toString() );
    Future<Graph> f = executionCompletionService.submit( task );
    queue.add( f );
    return f;
  }

  @Override
  public Future<Graph> submit( Runnable task, Graph result ) {
    log.debug( "Submitting Runnable task --> " + result );
    Future<Graph> f = executionCompletionService.submit( task, result );
    queue.add( f );
    return f;
  }

  @Override
  public Future<Graph> take() throws InterruptedException {
    Future<Graph> result = executionCompletionService.take();
    queue.remove( result );
    return result;
  }

  @Override
  public Future<Graph> poll() {
    Future<Graph> result = executionCompletionService.poll();
    queue.remove( result );
    return result;
  }

  @Override
  public Future<Graph> poll( long timeout, TimeUnit unit ) throws InterruptedException {
    Future<Graph> result = executionCompletionService.poll( timeout, unit );
    queue.remove( result );
    return result;
  }

  public void waitTillEmpty() throws InterruptedException, ExecutionException {
    Future<Graph> result;
    while ( queue.size() > 0 ) {
      result = poll( 200, TimeUnit.MILLISECONDS );
      if ( result != null && !result.isCancelled() ) {
        try {
          Graph graph = result.get();
          log.debug( "Process Finished --> " + graph );
        } catch ( ExecutionException e ) {
          log.warn( e.getMessage(), e );
        }
      }
    }
  }

}
