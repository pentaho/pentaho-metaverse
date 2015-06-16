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

import com.tinkerpop.blueprints.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseDocument;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.impl.MetaverseNode;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )

public class RepositoryLocatorTest {

  RepositoryLocator baseLocator;

  @Mock
  Future<String> futureTask;

  @Mock
  IMetaverseBuilder metaverseBuilder;

  @Mock
  IMetaverseObjectFactory metaverseObjectFactory;

  @Mock
  IUnifiedRepository repository;

  @Mock
  IPentahoSession session;

  @Before
  public void setUp() throws Exception {
    RepositoryLocator loc = new RepositoryLocator() {
      @Override protected IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception {
        return null;
      }

      @Override protected Object getContents( RepositoryFile locatedItem ) throws Exception {
        return null;
      }

      /**
       * Returns the locator node for this locator. The locator node is the node in the metaverse
       * that represents this locator. It is used to create a link from this locator to the documents
       * that are found by/within it.
       *
       * @return The locator node in the metaverse
       */
      @Override public IMetaverseNode getLocatorNode() {
        return new MetaverseNode( mock( Vertex.class ) );
      }

      @Override public URI getRootUri() {
        return null;
      }
    };
    loc.setMetaverseBuilder( metaverseBuilder );
    baseLocator = spy( loc );
    when( baseLocator.getMetaverseBuilder() ).thenReturn( metaverseBuilder );
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( metaverseObjectFactory );
    when( metaverseObjectFactory.createDocumentObject() ).thenReturn( new MetaverseDocument() );
  }

  @Test
  public void testConstructorListParam() {
    RepositoryLocator reploc = new RepositoryLocator( new ArrayList<IDocumentListener>() ) {
      @Override protected IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception {
        return null;
      }

      @Override protected Object getContents( RepositoryFile locatedItem ) throws Exception {
        return null;
      }

      @Override public URI getRootUri() {
        return null;
      }
    };
  }

  @Test
  public void testGetUnifiedRepository() throws Exception {
    RepositoryLocator reploc = new RepositoryLocator() {
      @Override protected IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception {
        return repository;
      }

      @Override protected Object getContents( RepositoryFile locatedItem ) throws Exception {
        return null;
      }

      @Override public URI getRootUri() {
        return null;
      }
    };
    assertEquals( reploc.getUnifiedRepository( session ), repository );
  }

  @Test
  public void testStartScan() throws Exception {
    when( baseLocator.getUnifiedRepository( any( IPentahoSession.class ) ) ).thenReturn( repository );
    when( repository.getTree( any( RepositoryRequest.class ) ) ).thenReturn( mock( RepositoryFileTree.class ) );

    baseLocator.startScan();
  }

  @Test( expected = MetaverseLocatorException.class )
  public void testStartScanAlreadyExecuting() throws Exception {
    when( baseLocator.getUnifiedRepository( any( IPentahoSession.class ) ) ).thenReturn( repository );
    when( repository.getTree( any( RepositoryRequest.class ) ) ).thenReturn( mock( RepositoryFileTree.class ) );
    baseLocator.futureTask = futureTask;
    baseLocator.startScan();
  }

  @Test( expected = MetaverseLocatorException.class )
  public void testStartScanException() throws Exception {
    when( baseLocator.getUnifiedRepository( any( IPentahoSession.class ) ) ).thenThrow( Exception.class );
    baseLocator.startScan();
  }
}
