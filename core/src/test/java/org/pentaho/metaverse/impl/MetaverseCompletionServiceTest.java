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
