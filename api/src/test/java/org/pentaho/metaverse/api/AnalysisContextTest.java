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

package org.pentaho.metaverse.api;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class tests the functionality of the AnalysisContext class
 */
public class AnalysisContextTest {

  @Test
  public void testCtorName() {
    AnalysisContext context = new AnalysisContext( "justTheName" );
    assertNotNull( context );
    assertEquals( "Context name is not as expected!", "justTheName", context.getContextName() );
    assertNull( context.getContextObject() );
  }

  @Test
  public void testGetContextName() throws Exception {
    AnalysisContext context = new AnalysisContext( "myContext", null );
    assertNotNull( context );
    assertEquals( "Name of context is not as expected!", "myContext", context.getContextName() );
  }

  @Test
  public void testGetContextObject() throws Exception {

    AnalysisContext context = new AnalysisContext( "myContext", null );
    assertNotNull( context );
    assertEquals( "Name of context is not as expected!", "myContext", context.getContextName() );

    context = new AnalysisContext( "myContext", new String( "myContextObject" ) );
    assertNotNull( context );
    Object contextObject = context.getContextObject();
    assertNotNull( contextObject );
    assertEquals( contextObject.toString(), "myContextObject" );

    context = new AnalysisContext( "myContext", new Integer( 5 ) );
    contextObject = context.getContextObject();
    assertNotNull( contextObject );
    assertTrue( "Object is not of the expected type!", contextObject instanceof Integer );
    assertEquals( contextObject, 5 );

  }

  @Test
  public void testSetContextName() throws Exception {
    AnalysisContext context = new AnalysisContext();
    assertNull( context.getContextName() );
    context.setContextName( "testContextName1" );
    assertEquals( "Context name is not the expected string!", context.getContextName(), "testContextName1" );
  }

  @Test
  public void testSetContextObject() throws Exception {
    AnalysisContext context = new AnalysisContext();
    assertNull( context.getContextObject() );
    context.setContextObject( new String[] { "testObjectContents" } );
    Object contextObject = context.getContextObject();
    assertNotNull( contextObject );
    assertTrue( "Context object is not of array type!", contextObject.getClass().isArray() );
    String[] contextArray = (String[]) contextObject;
    assertEquals( "Context object is array but not of correct size!", contextArray.length, 1 );
    assertEquals( "Context object is array of correct size but wrong contents!", contextArray[0],
        "testObjectContents" );
  }
}
