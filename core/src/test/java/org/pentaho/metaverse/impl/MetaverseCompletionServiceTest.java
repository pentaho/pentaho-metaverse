/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.impl;

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
