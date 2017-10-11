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


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.metaverse.api.IDocument;

import static org.junit.Assert.*;

/**
 * @author mburgess
 * 
 */
@RunWith( MockitoJUnitRunner.class )
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
