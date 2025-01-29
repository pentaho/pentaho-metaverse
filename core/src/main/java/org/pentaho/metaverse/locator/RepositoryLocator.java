/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
