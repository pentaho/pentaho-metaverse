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

package com.pentaho.metaverse.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MetaverseCompletionServiceTest {

  public static final String HELLO_WORLD = "Hello World!";
  MetaverseCompletionService mcs = MetaverseCompletionService.getInstance();

  @Before
  public void setUp() throws Exception {
    mcs.waitTillEmpty();
  }

  @Test
  public void testSubmit_Callable() throws Exception {
    mcs.submit( new Callable<String>() {
      @Override public String call() throws Exception {
        Thread.sleep( 150 );
        return HELLO_WORLD;
      }
    } );

    String result = mcs.take().get();
    assertEquals( HELLO_WORLD, result );
  }

  @Test
  public void testSubmit_Runnable() throws Exception {
    mcs.submit( new Runnable() {
      @Override public void run() {
        try {
          Thread.sleep( 100 );
        } catch ( InterruptedException e ) {
          e.printStackTrace();
        }
      }
    }, HELLO_WORLD );

    String result = mcs.take().get();
    assertEquals( HELLO_WORLD, result );
  }

  @Test
  public void testWaitTillEmpty() throws Exception {
    mcs.submit( new Callable<String>() {
      @Override public String call() throws Exception {
        Thread.sleep( 400 );
        return "Callable1";
      }
    } );

    // handle the exception case as well
    mcs.submit( new Callable<String>() {
      @Override public String call() throws Exception {
        throw new ExecutionException( new Exception( "Exception" ) );
      }
    } );

    mcs.submit( new Callable<String>() {
      @Override public String call() throws Exception {
        Thread.sleep( 200 );
        return "Callable2";
      }
    } );

    mcs.waitTillEmpty();

    // there shouldn't be anything left to process
    assertNull( mcs.poll() );
  }



}
