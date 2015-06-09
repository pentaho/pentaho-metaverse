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

package com.pentaho.metaverse.locator;

import org.pentaho.metaverse.api.MetaverseDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class LocatorRunnerTest {

  @Mock
  IMetaverseBuilder metaverseBuilder;

  @Mock
  IMetaverseObjectFactory metaverseObjectFactory;

  @Mock
  LocatorRunner locatorRunner;

  @Mock
  BaseLocator baseLocator;

  @Mock
  INamespace namespace;

  @Before
  public void setUp() throws Exception {
    when( baseLocator.getMetaverseBuilder() ).thenReturn( metaverseBuilder );
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( metaverseObjectFactory );
    when( metaverseObjectFactory.createDocumentObject() ).thenReturn( new MetaverseDocument() );
  }

  LocatorRunner<String> stringLocatorRunner = new LocatorRunner<String>() {
    @Override
    protected void locate( String root ) {
      // Don't need this for the test
    }
  };

  @Test
  public void testSetRoot() throws Exception {

    stringLocatorRunner.setRoot( "myRoot" );
    assertEquals( stringLocatorRunner.root, "myRoot" );
  }

  @Test
  public void testSetLocator() throws Exception {
    stringLocatorRunner.setLocator( baseLocator );
    assertEquals( stringLocatorRunner.locator, baseLocator );
  }

  @Test
  public void testRun() throws Exception {
    LocatorRunner lr = spy( stringLocatorRunner );
    lr.root = "myRoot";
    lr.run();
    Mockito.verify( lr, Mockito.times( 1 ) ).locate( "myRoot" );
  }

  @Test
  public void testIsRunning() throws Exception {

    LocatorRunner<String> stringLocatorRunner = new LocatorRunner<String>() {
      @Override
      protected void locate( String root ) {
        assertTrue( this.isRunning() );
      }
    };
    assertFalse( stringLocatorRunner.isRunning() );
    stringLocatorRunner.run();

  }

  @Test
  public void testStop() throws Exception {
    assertFalse( stringLocatorRunner.stopping );
    stringLocatorRunner.stop();
    assertTrue( stringLocatorRunner.stopping );
  }

  @Test
  public void testProcessFileStopping() throws Exception {
    when( baseLocator.getMetaverseBuilder() ).thenThrow( MetaverseAnalyzerException.class );
    stringLocatorRunner.setLocator( baseLocator );
    stringLocatorRunner.stop();
    stringLocatorRunner.processFile( namespace, null, null, null );
  }

  @Test
  public void testProcessFileNoExtension() throws Exception {
    when( baseLocator.getMetaverseBuilder() ).thenThrow( MetaverseAnalyzerException.class );
    stringLocatorRunner.setLocator( baseLocator );
    stringLocatorRunner.processFile( namespace, "", null, null );
  }

  @Test
  public void testProcessFileTransformationExtension() throws Exception {
    doAnswer(
        new Answer() {
          /**
           * @param invocation the invocation on the mock.
           * @return the value to be returned
           * @throws Throwable the throwable to be thrown
           */
          @Override public Void answer( InvocationOnMock invocation ) throws Throwable {
            IDocumentEvent event = (IDocumentEvent) invocation.getArguments()[0];
            IDocument doc = event.getDocument();
            assertNotNull( doc );
            assertNull( doc.getMimeType() );
            assertEquals( doc.getName(), "test.ktr" );
            assertEquals( doc.getStringID(), "myKTR" );
            return null;
          }
        }
    ).when( baseLocator ).notifyListeners( any( IDocumentEvent.class ) );
    stringLocatorRunner.setLocator( baseLocator );
    File file = new File( "test.ktr" );
    File spyFile = spy( file );
    stringLocatorRunner.processFile( namespace, "test.ktr", "myKTR", spyFile );
    when( baseLocator.getContents( any( Object.class ) ) ).thenThrow( MetaverseException.class );
    stringLocatorRunner.processFile( namespace, "test.ktr", "myKTR", spyFile );
  }
}
