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
import com.pentaho.metaverse.impl.MetaverseBuilder;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the DIRepositoryLocator
 *
 * @author jdixon
 */
@RunWith( MockitoJUnitRunner.class )
public class DIRepositoryLocatorTest implements IDocumentListener {

  private List<IDocumentEvent> events;

  @Mock
  RepositoryFile repositoryFile;

  DIRepositoryLocator spyLocator;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
  }

  /**
   * Initializes the kettle system
   */
  @Before
  public void init() {
    spyLocator = spy( new DIRepositoryLocator() );
  }

  @Test
  public void testDefaultConstructor() {
    DIRepositoryLocator locator = new DIRepositoryLocator();
    assertEquals( locator.getLocatorType(), DIRepositoryLocator.LOCATOR_TYPE );
    assertNotNull( locator.listeners );
    assertTrue( locator.listeners.isEmpty() );
  }

  @Test
  public void testConstructorWithListeners() {
    assertNotNull( spyLocator.listeners );
    assertTrue( spyLocator.listeners.isEmpty() );
    List<IDocumentListener> listeners = new ArrayList<IDocumentListener>();
    listeners.add( mock( IDocumentListener.class ) );
    DIRepositoryLocator locator = new DIRepositoryLocator( listeners );
    assertEquals( locator.getLocatorType(), DIRepositoryLocator.LOCATOR_TYPE );
    assertNotNull( locator.listeners );
    assertEquals( 1, locator.listeners.size() );

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
    spyLocator.addDocumentListener( this );
    spyLocator.setRepository( LocatorTestUtils.getFakeDiRepository() );
    LocatorTestUtils.delay = 0;

    spyLocator.setRepositoryId( "testrepo" );
    assertEquals( "Repo id is wrong", "testrepo", spyLocator.getRepositoryId() );

    assertNotNull( "Indexer type is null", spyLocator.getLocatorType() );
    events = new ArrayList<IDocumentEvent>();
    spyLocator.startScan();

    spyLocator.futureTask.get();

    assertEquals( "Event count is wrong", 7, events.size() );

    for ( IDocumentEvent event : events ) {
      System.out.println( event.getDocument().getStringID() );
      assertNotNull( event.getDocument() );
      MetaverseDocument document = (MetaverseDocument) event.getDocument();
      if ( document.getExtension().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof TransMeta );
      } else if ( document.getExtension().equals( "kjb" ) ) {
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
   *
   * @throws Exception When bad things happen
   */
  @Test
  public void testStopLocatorScan() throws Exception {

    TinkerGraph graph = new TinkerGraph();
    IMetaverseBuilder metaverseBuilder = new MetaverseBuilder( graph );

    spyLocator.setMetaverseBuilder( metaverseBuilder );
    spyLocator.addDocumentListener( this );
    spyLocator.setRepository( LocatorTestUtils.getFakeDiRepository() );
    spyLocator.setUnifiedRepository( LocatorTestUtils.getMockIUnifiedRepository() );
    LocatorTestUtils.delay = 300;

    assertNotNull( "Indexer type is null", spyLocator.getLocatorType() );
    events = Collections.synchronizedList( new ArrayList<IDocumentEvent>() );
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
      if ( document.getExtension().equals( "ktr" ) ) {
        assertTrue( document.getContent() instanceof TransMeta );
      } else if ( document.getExtension().equals( "kjb" ) ) {
        assertTrue( document.getContent() instanceof JobMeta );
      }
    }

  }

  @Test
  public void testGetRepositoryNullRepository() throws Exception {
    DIRepositoryLocator locator = new DIRepositoryLocator();
    assertNull( locator.repository );
    locator.getRepository();
  }

  @Test
  public void testGetContentsKettleFileRepository() throws Exception {
    when( spyLocator.getRepository() ).thenReturn( new KettleFileRepository() );
    spyLocator.getContents( repositoryFile );
  }

  @Test
  public void testGetRootUriWithException() throws Exception {
    // The exception is consumed within getRootUri
    when( spyLocator.getRepository() ).thenThrow( Exception.class );
    assertNull( spyLocator.getRootUri() );
  }

  @Test
  public void testGetRootUri() throws Exception {
    Repository mockRepo = mock( Repository.class );
    when( spyLocator.getRepository() ).thenReturn( mockRepo );
    when( mockRepo.getRepositoryMeta() ).thenReturn( mock( RepositoryMeta.class ) );

    // This one won't get far due to no getRepositoryLocation() method
    spyLocator.getRootUri();

    GetRepositoryLocationMethodProvider getRepositoryLocationMethodProvider =
      new GetRepositoryLocationMethodProvider( null );
    when( mockRepo.getRepositoryMeta() ).thenReturn( getRepositoryLocationMethodProvider );
    // This one won't get too far due to no RepositoryLocation being determined
    spyLocator.getRootUri();

    getRepositoryLocationMethodProvider = new GetRepositoryLocationMethodProvider( new Object() );
    when( mockRepo.getRepositoryMeta() ).thenReturn( getRepositoryLocationMethodProvider );
    // This one won't go all the way through due to no getUrl() method defined
    spyLocator.getRootUri();

    getRepositoryLocationMethodProvider = new GetRepositoryLocationMethodProvider( new RepositoryLocationTestClass() );
    when( mockRepo.getRepositoryMeta() ).thenReturn( getRepositoryLocationMethodProvider );
    spyLocator.getRootUri();
  }

  @Test
  public void testGetUnifiedRepositoryWithBadRepoClass() throws Exception {
    spyLocator.setRepository( new KettleFileRepository() );
    spyLocator.getUnifiedRepository( mock( IPentahoSession.class ) );
  }

  @Override
  public void onEvent( IDocumentEvent event ) {
    events.add( event );
  }

  private class RepositoryLocationTestClass {

    public String getUrl() throws Exception {
      return "myURL";
    }

  }

  private class GetRepositoryLocationMethodProvider extends KettleFileRepositoryMeta {

    private Object repoLocation;

    public GetRepositoryLocationMethodProvider( Object repoLocation ) {
      super();
      this.repoLocation = repoLocation;
    }

    public Object getRepositoryLocation() {
      return repoLocation;
    }
  }

}
