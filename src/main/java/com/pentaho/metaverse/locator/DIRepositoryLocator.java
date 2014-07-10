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

public class DIRepositoryLocator extends RepositoryIndexer {

  private static final long serialVersionUID = 1324202912891938340L;

  public static final String INDEXER_TYPE = "DIRepo";
  
  private Repository repository = null;
  private IUnifiedRepository unifiedRepository = null;

  public DIRepositoryLocator() {
    super();
    setIndexerType( INDEXER_TYPE );
  }
    
  public void setUnifiedRepository( IUnifiedRepository unifiedRepository ) {
    this.unifiedRepository = unifiedRepository;
  }
  
  public void setRepository( Repository repository ) {
    this.repository = repository;
  }
  
  protected Repository getRepository() throws Exception {
    if( repository == null  ) {
      TransformationMap transformationMap = CarteSingleton.getInstance().getTransformationMap();
      SlaveServerConfig slaveServerConfig = transformationMap.getSlaveServerConfig();
      repository = slaveServerConfig.getRepository();
    }
    return repository;
  }
  
  @Override
  protected IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception {

    if ( unifiedRepository == null && !(repository instanceof KettleFileRepository)) {
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
