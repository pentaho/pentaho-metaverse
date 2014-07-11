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

import java.lang.reflect.Method;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

/**
 * A document locator for the DI repository @see org.pentaho.platform.api.metaverse.IDocumentLocator
 * @author jdixon
 *
 */
public class DIRepositoryLocator extends RepositoryIndexer {

  /**
   * The type for this locator
   */
  public static final String LOCATOR_TYPE = "DIRepo";

  private static final long serialVersionUID = 1324202912891938340L;

  private Repository repository;
  private IUnifiedRepository unifiedRepository;

  /**
   * The constructor for the DIRepositoryLocator
   */
  public DIRepositoryLocator() {
    super();
    setIndexerType( LOCATOR_TYPE );
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
      Method method = repository.getClass().getMethod( "getPur" );
      Object result = method.invoke( repository );
      if ( result instanceof IUnifiedRepository ) {
        IUnifiedRepository repo = (IUnifiedRepository) result;
        return repo;
      }
    }
    return unifiedRepository;
  }

  @Override
  protected Object getFileContents( RepositoryFile file, String type ) throws Exception {

    Object object = null;
    ObjectId objectId = new StringObjectId( file.getId().toString() );
    if ( "ktr".equals( type ) ) {
      object = repository.loadTransformation( objectId, null );
    } else if ( "kjb".equals( type ) ) {
      object = repository.loadJob( objectId, null );
    }
    return object;

  }

  @Override
  public String[] getTypes() {
    return new String[] {};
  }

}
