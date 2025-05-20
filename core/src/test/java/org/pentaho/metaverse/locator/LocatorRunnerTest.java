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


package org.pentaho.metaverse.locator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseDocument;
import org.pentaho.metaverse.api.MetaverseException;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
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

  @BeforeClass
  public static void init() throws Exception {
    DefaultBowl.getInstance().setSharedObjectsIO( new MemorySharedObjectsIO() );
    DefaultBowl.getInstance().clearManagers();
  }

  @Before
  public void setUp() throws Exception {
    lenient().when( baseLocator.getMetaverseBuilder() ).thenReturn( metaverseBuilder );
    lenient().when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( metaverseObjectFactory );
    lenient().when( metaverseObjectFactory.createDocumentObject() ).thenReturn( new MetaverseDocument() );
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
    stringLocatorRunner.setLocator( baseLocator );
    stringLocatorRunner.stop();
    stringLocatorRunner.processFile( namespace, null, null, null );
  }

  @Test
  public void testProcessFileNoExtension() throws Exception {
    stringLocatorRunner.setLocator( baseLocator );
    stringLocatorRunner.processFile( namespace, "", null, null );
  }

  @Test
  public void testProcessFileTransformationExtension() throws Exception {
    lenient().doAnswer(
      new Answer() {
        /**
         * @param invocation the invocation on the mock.
         * @return the value to be returned
         * @throws Throwable the throwable to be thrown
         */
        @Override public Void answer( InvocationOnMock invocation ) throws Throwable {
          IDocumentEvent event = (IDocumentEvent) invocation.getArguments()[ 0 ];
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
