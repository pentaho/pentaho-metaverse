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

package org.pentaho.metaverse.impl;

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

public class MetaverseCompletionService implements CompletionService<String> {

  private ExecutorCompletionService<String> executionCompletionService;
  private Executor executor = Executors.newCachedThreadPool();
  private static final Logger log = LoggerFactory.getLogger( MetaverseCompletionService.class );
  private Queue<Future<String>> queue = new ConcurrentLinkedQueue<Future<String>>();


  private static class Holder {
    private static final MetaverseCompletionService INSTANCE = new MetaverseCompletionService();
  }

  public static MetaverseCompletionService getInstance() {
    return Holder.INSTANCE;
  }

  private MetaverseCompletionService() {
    executionCompletionService = new ExecutorCompletionService<String>( executor );
  }

  @Override
  public Future<String> submit( Callable<String> task ) {
    log.debug( "Submitting Callable task --> " + task.toString() );
    Future<String> f = executionCompletionService.submit( task );
    queue.add( f );
    return f;
  }

  @Override
  public Future<String> submit( Runnable task, String result ) {
    log.debug( "Submitting Runnable task --> " + result );
    Future<String> f = executionCompletionService.submit( task, result );
    queue.add( f );
    return f;
  }

  @Override
  public Future<String> take() throws InterruptedException {
    Future<String> result = executionCompletionService.take();
    queue.remove( result );
    return result;
  }

  @Override
  public Future<String> poll() {
    Future<String> result = executionCompletionService.poll();
    queue.remove( result );
    return result;
  }

  @Override
  public Future<String> poll( long timeout, TimeUnit unit ) throws InterruptedException {
    Future<String> result = executionCompletionService.poll( timeout, unit );
    queue.remove( result );
    return result;
  }

  public void waitTillEmpty() throws InterruptedException, ExecutionException {
    Future<String> result;
    while ( queue.size() > 0 ) {
      result = poll( 200, TimeUnit.MILLISECONDS );
      if ( result != null && !result.isCancelled() ) {
        try {
          String id = result.get();
          log.debug( "Process Finished --> " + id );
        } catch ( ExecutionException e ) {
          log.warn( e.getMessage(), e );
        }
      }
    }
  }

}
