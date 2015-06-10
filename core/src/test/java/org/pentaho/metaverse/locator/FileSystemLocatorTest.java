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

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.MetaverseDocument;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.graph.GraphMLWriter;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.pentaho.metaverse.util.MetaverseUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

/**
 * Test class for the FileSystemLocator
 *
 * @author jdixon
 */
@SuppressWarnings( { "all" } )
@RunWith( MockitoJUnitRunner.class )
public class FileSystemLocatorTest implements IDocumentListener {

  private List<IDocumentEvent> events;

  TestFileSystemLocator spyLocator;

  public static final String OUTPUT_FOLDER = "target/outputfiles/";

  @BeforeClass
  public static void beforeClass() {
    File f = new File( OUTPUT_FOLDER );
    if ( !f.exists() ) {
      f.mkdirs();
    }
  }

  /**
   * Initializes the kettle system
   */
  @Before
  public void init() {
    TestFileSystemLocator locator = new TestFileSystemLocator( new ArrayList<IDocumentListener>() );
    spyLocator = spy( locator );
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }

    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
  }

  @Test
  public void testDefaultConstructor() {
    FileSystemLocator locator = new FileSystemLocator();
    assertEquals( locator.getLocatorType(), FileSystemLocator.LOCATOR_TYPE );
  }

  /**
   * Runs the spyLocator and checks the results
   *
   * @throws Exception When bad things happen
   */
  @Test
  public void testStartLocator() throws Exception {

    TinkerGraph graph = new TinkerGraph();
    IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

    spyLocator.setMetaverseBuilder( metaverseBuilder );
    spyLocator.setRepositoryId( "testrepo" );
    spyLocator.addDocumentListener( this );
    spyLocator.setRootFolder( "src/test/resources/solution" );
    assertEquals( "Root folder is wrong", "src/test/resources/solution", spyLocator.getRootFolder() );
    TestFileSystemLocator.delay = 0;

    spyLocator.setRootFolder( "bogus" );
    events = new ArrayList<IDocumentEvent>();
    try {
      spyLocator.startScan();
      MetaverseCompletionService.getInstance().waitTillEmpty();
      fail();
    } catch ( MetaverseLocatorException e ) {
      assertEquals( "Event count is wrong", 0, events.size() );
    }

    spyLocator.setRootFolder( "src/test/resources/solution/folder 2/parse.ktr" );
    events = new ArrayList<IDocumentEvent>();

    try {
      spyLocator.startScan();
      MetaverseCompletionService.getInstance().waitTillEmpty();
      fail();
    } catch ( MetaverseLocatorException e ) {
      assertEquals( "Event count is wrong", 0, events.size() );
    }

    spyLocator.setRootFolder( "src/test/resources/solution" );
    assertEquals( "Repo id is wrong", "testrepo", spyLocator.getRepositoryId() );

    assertNotNull( "Indexer type is null", spyLocator.getLocatorType() );
    events = new ArrayList<IDocumentEvent>();
    spyLocator.startScan();
    MetaverseCompletionService.getInstance().waitTillEmpty();

    assertEquals( "Event count is wrong", 7, events.size() );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getStringID() );
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      if ( document.getExtension().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof String );
      } else if ( document.getExtension().equals( "kjb" ) ) {
        assertTrue( document.getContent() instanceof String );
      }
    }

    spyLocator.removeDocumentListener( this );
    events = new ArrayList<IDocumentEvent>();
    spyLocator.startScan();
    MetaverseCompletionService.getInstance().waitTillEmpty();

    assertEquals( "Event count is wrong", 0, events.size() );

    assertNotNull( "Graph is null", graph );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( graph, new FileOutputStream( OUTPUT_FOLDER + "FileSystemLocatorTest.graphml" ) );

  }

  /**
   * Runs the spyLocator and checks the results
   *
   * @throws Exception When bad things happen
   */
  @Test
  public void testStopLocatorScan() throws Exception {

    TinkerGraph graph = new TinkerGraph();
    IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

    spyLocator.setRepositoryId( "test_repo" );
    spyLocator.setMetaverseBuilder( metaverseBuilder );
    spyLocator.addDocumentListener( this );
    spyLocator.setRootFolder( "src/test/resources/solution" );
    TestFileSystemLocator.delay = 300;

    assertNotNull( "Indexer type is null", spyLocator.getLocatorType() );
    events = new ArrayList<IDocumentEvent>();
    System.out.println( "call startScan" );
    spyLocator.startScan();
    Thread.sleep( 1000 );
    System.out.println( "call stopScan" );
    spyLocator.stopScan();

    assertTrue( "Event count is wrong", events.size() < 5 );
    assertTrue( "Event count is wrong", events.size() > 0 );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getStringID() );
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      if ( document.getExtension().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof String );
      } else if ( document.getExtension().equals( "kjb" ) ) {
        assertTrue( document.getContent() instanceof String );
      }
    }

  }

  @Test
  public void testGetContentsBadFile() throws Exception {
    assertEquals( "", spyLocator.getContents( new File( "not-a-file.txt" ) ) );
  }

  @Override
  public void onEvent( IDocumentEvent event ) {
    events.add( event );
  }

}
