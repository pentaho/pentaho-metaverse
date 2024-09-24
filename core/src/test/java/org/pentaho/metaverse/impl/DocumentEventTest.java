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

package org.pentaho.metaverse.impl;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.metaverse.api.IDocument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author mburgess
 * 
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class DocumentEventTest {

  DocumentEvent docEvent;

  @Mock
  private IDocument document;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    docEvent = new DocumentEvent();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetDocument() {
    assertNull( docEvent.getDocument() );
  }

  @Test
  public void testSetDocument() {
    assertNotNull( document );
    docEvent.setDocument( document );
    assertNotNull( docEvent.getDocument() );
  }

  @Test
  public void testGetEventType() {
    assertNull( docEvent.getEventType() );
  }

  @Test
  public void testSetEventType() {
    docEvent.setEventType( "myType" );
    assertEquals( docEvent.getEventType(), "myType" );
  }
}
