/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.metaverse.api.IDocumentEvent;
import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.MetaverseDocument;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    assertFalse( locator.listeners.isEmpty() );
  }

  @Test
  public void testConstructorWithListeners() {
    assertNotNull( spyLocator.listeners );
    assertFalse( spyLocator.listeners.isEmpty() );
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
