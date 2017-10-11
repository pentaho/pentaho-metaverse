/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import java.util.List;

/**
 * An abstract implementation of a document locator for Hitachi Vantara repositories
 * @author jdixon
 *
 */
public abstract class RepositoryLocator extends BaseLocator<RepositoryFile> {
  private static final long serialVersionUID = 3308953622126327699L;

  private IUnifiedRepository unifiedRepository;

  /**
   * Default Constructor
   */
  protected RepositoryLocator() {
    super();
  }

  /**
   * Constructor that takes in a List of IDocumentListeners
   * @param documentListeners the List of listeners
   */
  public RepositoryLocator( List<IDocumentListener> documentListeners ) {
    super( documentListeners );
  }

  /**
   * A method that returns the IUnifiedRepository for this environment
   * @param session The user session to use
   * @return The IUnifiedRepository
   * @throws Exception When the repository instance cannot be accessed
   */
  protected abstract IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception;

  @Override
  public void startScan() throws MetaverseLocatorException {

    if ( unifiedRepository == null ) {
      try {
        unifiedRepository = getUnifiedRepository( session );
      } catch ( Exception e ) {
        throw new MetaverseLocatorException( Messages.getString( "ERROR.RepositoryLocator.ScanAbortedNoRepo" ), e );
      }
    }

    RepositoryRequest request = new RepositoryRequest( ClientRepositoryPaths.getRootFolderPath(), true, -1, null );
    RepositoryFileTree root = unifiedRepository.getTree( request );
    List<RepositoryFileTree> children = root.getChildren();

    LocatorRunner lr = new RepositoryLocatorRunner();
    lr.setRoot( children );
    startScan( lr );
  }

}
