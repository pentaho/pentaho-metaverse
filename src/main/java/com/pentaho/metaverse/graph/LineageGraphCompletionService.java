/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.graph;

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

  private ExecutorCompletionService<Graph> executionCompletionService;
  private Executor executor = Executors.newCachedThreadPool();
  private static final Logger log = LoggerFactory.getLogger( LineageGraphCompletionService.class );
  private Queue<Future<Graph>> queue = new ConcurrentLinkedQueue<Future<Graph>>();


  private static class Holder {
    private static final LineageGraphCompletionService INSTANCE = new LineageGraphCompletionService();
  }

  public static LineageGraphCompletionService getInstance() {
    return Holder.INSTANCE;
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
