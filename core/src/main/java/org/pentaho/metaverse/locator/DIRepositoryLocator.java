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

import org.apache.commons.io.FilenameUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.impl.DocumentController;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

/**
 * A document locator for the DI repository @see IDocumentLocator
 * @author jdixon
 *
 */
public class DIRepositoryLocator extends RepositoryLocator {

  /**
   * The type for this locator
   */
  public static final String LOCATOR_TYPE = "DIRepo";

  private static final Logger LOG = LoggerFactory.getLogger( DIRepositoryLocator.class );


  private static final long serialVersionUID = 1324202912891938340L;

  /**
   * A reference to the PDI repository associated with this locator
   */
  protected Repository repository;

  /**
   * A reference to the IUnifiedRepository backing the repo associated with this locator
   */
  protected IUnifiedRepository unifiedRepository;

  /**
   * The constructor for the DIRepositoryLocator
   */
  public DIRepositoryLocator() {
    super();
    setRepositoryId( "DI_REPO" );
    setLocatorType( LOCATOR_TYPE );
    setMetaverseBuilder( MetaverseBuilder.getInstance() );
    this.addDocumentListener( DocumentController.getInstance() );
  }

  /**
   * Constructor that takes in a List of IDocumentListeners
   * @param documentListeners the List of listeners
   */
  public DIRepositoryLocator( List<IDocumentListener> documentListeners ) {
    super( documentListeners );
    setLocatorType( LOCATOR_TYPE );
  }

  public void setUnifiedRepository( IUnifiedRepository unifiedRepository ) {
    this.unifiedRepository = unifiedRepository;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  /**
   * Returns the DI repository instance to use
   * @return DI repository instance
   * @throws Exception If the repository instance cannot be returned
   */
  protected Repository getRepository() throws Exception {
    if ( repository == null  ) {
      TransformationMap transformationMap = CarteSingleton.getInstance().getTransformationMap();
      SlaveServerConfig slaveServerConfig = transformationMap.getSlaveServerConfig();
      repository = slaveServerConfig.getRepository();
    }

    if ( repository == null ) {
      LOG.error( Messages.getErrorString( "ERROR.RepositoryNotFoundInConfiguration" ) );
    }

    return repository;
  }

  @Override
  protected IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception {

    if ( unifiedRepository == null ) {

      getRepository();

      Object pur = null;
      Class[] parameters = {};

      // Looking for the unifiedRepository within the
      // server repository. Due to the inaccessible nature of the
      // method calls, we use reflection to locate the unifiedRepository.

      if ( repository != null ) {
        Class repositoryClass = repository.getClass();
        try {
          Method m = repositoryClass.getMethod( "getPur", parameters );
          pur = m.invoke( repository );
        } catch ( Exception e ) {
          LOG.warn( Messages.getString( "WARNING.NoUnifiedRepositoryFound", repository.getClass().getName() ) );
        }
      }

      if ( pur != null ) {
        unifiedRepository = (IUnifiedRepository) pur;
      }

      /**
       * If we had access to the Pur* classes, we could do this ...
       *
      if ( repository instanceof PurRepository ) {
        PurRepository purRepository = (PurRepository) repository;
        IUnifiedRepository repo = purRepository.getPur();
        return repo;
      }
      */
    }
    return unifiedRepository;
  }

  @Override
  protected Object getContents( RepositoryFile file ) throws Exception {
    Object object = null;

    ObjectId objectId;
    Repository repo = getRepository();
    if ( repo instanceof KettleFileRepository ) {
      objectId = new StringObjectId( file.getPath() );
    } else {
      objectId = new StringObjectId( file.getId().toString() );
    }

    String extension = FilenameUtils.getExtension( file.getName() );


    if ( "ktr".equals( extension ) ) {
      object = repo.loadTransformation( objectId, null );
    } else if ( "kjb".equals( extension ) ) {
      object = repo.loadJob( objectId, null );
    }
    return object;
  }

  @Override
  public URI getRootUri() {

    // Looking for the web service location of the data integration
    // server repository. Due to the inaccessible nature of the
    // method calls, we use reflection to drill to the location URI.

    Repository repo = null;
    URI uri = null;

    try {
      repo = getRepository();
    } catch ( Exception e ) {
      LOG.warn( Messages.getString( "WARNING.RepositoryNotFoundNoRootURI" ) );
    }

    if ( repo != null ) {

      Object repositoryMeta = repo.getRepositoryMeta();
      Object repositoryLocation = null;
      String location = null;

      Class[] parameters = {};

      if ( repositoryMeta != null ) {
        Class repositoryMetaClass = repositoryMeta.getClass();
        try {
          Method m = repositoryMetaClass.getMethod( "getRepositoryLocation", parameters );
          repositoryLocation = m.invoke( repositoryMeta );
        } catch ( Exception e ) {
          LOG.warn( Messages.getString( "WARNING.RepositoryUnknownMethodNoRootURI", repo.getClass().getName() ) );
        }
      }

      if ( repositoryLocation != null ) {
        Class repositoryLocationClass = repositoryLocation.getClass();
        try {
          Method m = repositoryLocationClass.getMethod( "getUrl", parameters );
          location = (String) m.invoke( repositoryLocation );
        } catch ( Exception e ) {
          LOG.warn( Messages.getString( "WARNING.ExceptionFindingLocationNoRootURI" ) );
        }

      }
      if ( location != null ) {
        uri = URI.create( location );
      }

      /*
      // Here's what we could do if these packages were available to our classloader ...
      // but they are not.

      if ( repository instanceof PurRepository ) {
        PurRepository purRepository = ( PurRepository ) repository;
        PurRepositoryMeta meta = ( PurRepositoryMeta ) purRepository.getRepositoryMeta();
        uri = URI.create( meta.getRepositoryLocation().getUrl() );
      }
       */
    }
    return uri;
  }
}
