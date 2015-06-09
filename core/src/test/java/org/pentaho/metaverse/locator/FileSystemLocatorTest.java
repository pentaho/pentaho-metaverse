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
