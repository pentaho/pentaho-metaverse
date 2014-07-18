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

import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

/**
 * An abstract implementation of a document locator for Pentaho repositories
 * @author jdixon
 *
 */
public abstract class RepositoryLocator extends BaseLocator {

  private static final long serialVersionUID = 3308953622126327699L;

  private static final int POLLING_INTERVAL = 100;

  private IUnifiedRepository unifiedRepository;

  private RepositoryLocatorRunner indexRunner;

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

  /**
   * A method that returns the payload (object or XML) for a document
   * @param file The repository file
   * @param type The type of the file
   * @return The object or XML payload
   * @throws Exception When the document contents cannot be retrieved
   */
  protected abstract Object getFileContents( RepositoryFile file, String type ) throws Exception;

  @Override
  public String getId( String... tokens ) {
    return getIndexerType() + "." + getRepositoryId() + "." + tokens[0];
  }

  @Override
  public void startScan() {
    if ( indexRunner != null ) {
//      throw new Exception("Locator is already scanning");
      return;
    }
    if ( unifiedRepository == null ) {
      try {
        unifiedRepository = getUnifiedRepository( session );
      } catch ( Exception e ) {
//        throw new Exception( "Could not create metaverse index for repository", e );
        return;
      }
    }

    RepositoryFileTree root = unifiedRepository.getTree( ClientRepositoryPaths.getRootFolderPath(), -1, null, true );
    List<RepositoryFileTree> kids = root.getChildren();

    indexRunner = new RepositoryLocatorRunner();
    indexRunner.setRepositoryIndexer( this );
    indexRunner.setRepoTop( kids );
    Thread runnerThread = new Thread( indexRunner );
    runnerThread.start();
  }

  @Override
  public void stopScan() {
    System.out.println( "RepositoryLocator stopScan" );
    indexRunner.stop();
    while ( indexRunner.isRunning() ) {
      try {
        System.out.println( "RepositoryLocator stopScan polling" );
        Thread.sleep( POLLING_INTERVAL );
      } catch ( InterruptedException e ) {
        // intentional
        break;
      }
    }
    indexRunner = null;
  }

}
