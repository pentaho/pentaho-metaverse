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

import org.apache.commons.io.FilenameUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A document locator for the DI repository @see org.pentaho.platform.api.metaverse.IDocumentLocator
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

  private Repository repository;
  private IUnifiedRepository unifiedRepository;

  /**
   * The constructor for the DIRepositoryLocator
   */
  public DIRepositoryLocator() {
    super();
    setLocatorType( LOCATOR_TYPE );
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
    return repository;
  }

  @Override
  protected IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception {

    if ( unifiedRepository == null && !( repository instanceof KettleFileRepository ) ) {
      getRepository();
      if ( repository instanceof PurRepository ) {
        PurRepository purRepository = (PurRepository) repository;
        IUnifiedRepository repo = purRepository.getPur();
        return repo;
      }
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

}
