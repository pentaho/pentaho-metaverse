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

package org.pentaho.metaverse.locator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseDocument;
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
