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
 * An abstract implementation of a document locator for Pentaho repositories
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
