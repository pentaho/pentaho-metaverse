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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;

import com.pentaho.metaverse.graph.GraphMLWriter;
import com.pentaho.metaverse.impl.MetaverseBuilder;
import com.pentaho.metaverse.impl.MetaverseDocument;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Test class for the FileSystemLocator
 * @author jdixon
 *
 */
@SuppressWarnings( { "all" } )
public class FileSystemLocatorTest implements IDocumentListener {

  private List<IDocumentEvent> events;

  /**
   * Initializes the kettle system
   */
  @Before
  public void init() {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * Runs the locator and checks the results
   * @throws Exception When bad things happen
   */
  @Test
  public void testStartLocator() throws Exception {

    TinkerGraph graph = new TinkerGraph();
    IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

    TestFileSystemLocator locator = new TestFileSystemLocator();
    locator.setMetaverseBuilder( metaverseBuilder );
    locator.setRepositoryId( "testrepo" );
    locator.addDocumentListener( this );
    locator.setRootFolder( "src/test/resources/solution" );
    assertEquals( "Root folder is wrong", "src/test/resources/solution", locator.getRootFolder() );
    TestFileSystemLocator.delay = 0;

    locator.setRootFolder( "bogus" );
    events = new ArrayList<IDocumentEvent>();
    locator.startScan();
    Thread.sleep( 3000 );
    assertEquals( "Event count is wrong", 0, events.size() );

    locator.setRootFolder( "src/test/resources/solution/folder 2/parse.ktr" );
    events = new ArrayList<IDocumentEvent>();
    locator.startScan();
    Thread.sleep( 3000 );
    assertEquals( "Event count is wrong", 0, events.size() );

    locator.setRootFolder( "src/test/resources/solution" );
    assertEquals( "Repo id is wrong", "testrepo", locator.getRepositoryId() );

    assertNotNull("Locator logger is null", locator.getLogger() );

    assertNotNull("Locator types is null", locator.getTypes() );

    assertNotNull("Indexer type is null", locator.getLocatorType() );
    events = new ArrayList<IDocumentEvent>();
    locator.startScan();
    Thread.sleep( 3000 );

    assertEquals( "Event count is wrong", 7, events.size() );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getStringID() );
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      if ( document.getType().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof String );
      } else if ( document.getType().equals( "kjb" ) ) {
        assertTrue( document.getContent() instanceof String );
      }
    }

    locator.removeDocumentListener( this );
    events = new ArrayList<IDocumentEvent>();
    locator.startScan();
    Thread.sleep( 3000 );

    assertEquals( "Event count is wrong", 0, events.size() );

    assertNotNull( "Graph is null", graph );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( graph, new FileOutputStream( "FileSystemLocatorTest.graphml" ) );

  }

  /**
   * Runs the locator and checks the results
   * @throws Exception When bad things happen
   */
  @Test
  public void testStopLocatorScan() throws Exception {

    TinkerGraph graph = new TinkerGraph();
    IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

    TestFileSystemLocator locator = new TestFileSystemLocator( new ArrayList<IDocumentListener>() );
    locator.setRepositoryId( "test_repo" );
    locator.setMetaverseBuilder( metaverseBuilder );
    locator.addDocumentListener( this );
    locator.setRootFolder( "src/test/resources/solution" );
    TestFileSystemLocator.delay = 300;

    assertNotNull("Indexer type is null", locator.getLocatorType() );
    events = new ArrayList<IDocumentEvent>();
    System.out.println( "call startScan" );
    locator.startScan();
    Thread.sleep( 1000 );
    System.out.println( "call stopScan" );
    locator.stopScan();

    assertTrue( "Event count is wrong", events.size() < 5 );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getStringID() );
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      if ( document.getType().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof String );
      } else if ( document.getType().equals( "kjb" ) ) {
        assertTrue( document.getContent() instanceof String );
      }
    }

  }

  @Override
  public void onEvent( IDocumentEvent event ) {
    events.add( event );
  }

}
