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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.pentaho.metaverse.api.INamespaceFactory;
import com.pentaho.metaverse.impl.MetaverseNamespace;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.IDocumentEvent;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;

import com.pentaho.metaverse.impl.MetaverseBuilder;
import com.pentaho.metaverse.impl.MetaverseDocument;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.pentaho.platform.api.metaverse.INamespace;

/**
 * Test class for the DIRepositoryLocator
 * @author jdixon
 *
 */
@SuppressWarnings( { "all" } )
@RunWith( MockitoJUnitRunner.class )
public class DIRepositoryLocatorTest implements IDocumentListener {

  private List<IDocumentEvent> events;

  @Mock
  INamespaceFactory namespaceFactory;

  DIRepositoryLocator spyLocator;


  /**
   * Initializes the kettle system
   */
  @Before
  public void init() {
    DIRepositoryLocator locator = new DIRepositoryLocator();
    spyLocator = spy(locator);
    when(spyLocator.getNamespaceFactory()).thenReturn( namespaceFactory );
    when(namespaceFactory.createNameSpace(
        any(INamespace.class), anyString() )).thenReturn( new MetaverseNamespace( null, "", namespaceFactory ) );
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * Runs the spyLocator and checks the results
   * @throws Exception When bad things happen
   */
  @Test
  public void testStartLocator() throws Exception {

    TinkerGraph graph = new TinkerGraph();
    IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

    spyLocator.setMetaverseBuilder( metaverseBuilder );
    spyLocator.addDocumentListener( this );
    spyLocator.setRepository( LocatorTestUtils.getMockDiRepository() );
//    spyLocator.setUnifiedRepository( LocatorTestUtils.getMockIUnifiedRepository() );
    LocatorTestUtils.delay = 0;

    spyLocator.setRepositoryId( "testrepo" );
    assertEquals( "Repo id is wrong", "testrepo", spyLocator.getRepositoryId() );

    assertNotNull("Indexer type is null", spyLocator.getLocatorType() );
    events = new ArrayList<IDocumentEvent>();
    spyLocator.startScan();

    spyLocator.futureTask.get();

    assertEquals( "Event count is wrong", 7, events.size() );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getStringID() );
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      if ( document.getType().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof TransMeta );
      } else if ( document.getType().equals( "kjb" ) ) {
        assertTrue( document.getContent() instanceof JobMeta );
      }
    }

    spyLocator.removeDocumentListener( this );
    events = new ArrayList<IDocumentEvent>();
    spyLocator.startScan();
    spyLocator.futureTask.get();

    assertEquals( "Event count is wrong", 0, events.size() );

  }

  /**
   * Runs the spyLocator and checks the results
   * @throws Exception When bad things happen
   */
  @Test
  public void testStopLocatorScan() throws Exception {

    TinkerGraph graph = new TinkerGraph();
    IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

    spyLocator.setMetaverseBuilder( metaverseBuilder );
    spyLocator.addDocumentListener( this );
    spyLocator.setRepository( LocatorTestUtils.getMockDiRepository() );
    spyLocator.setUnifiedRepository( LocatorTestUtils.getMockIUnifiedRepository() );
    LocatorTestUtils.delay = 300;

    assertNotNull( "Indexer type is null", spyLocator.getLocatorType() );
    events = new ArrayList<IDocumentEvent>();
    System.out.println( "call startScan" );
    spyLocator.startScan();
    Thread.sleep( 1000 );
    System.out.println( "call stopScan" );
    spyLocator.stopScan();

    assertTrue( "Event count is wrong", events.size() < 5 );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getStringID() );
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      if ( document.getType().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof TransMeta );
      } else if ( document.getType().equals( "kjb" ) ) {
        assertTrue( document.getContent() instanceof JobMeta );
      }
    }

  }

  @Override
  public void onEvent( IDocumentEvent event ) {
    events.add( event );
  }

}
