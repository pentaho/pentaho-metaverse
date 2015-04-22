/*!
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

package com.pentaho.metaverse.api;

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
