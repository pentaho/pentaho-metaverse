package com.pentaho.metaverse.locator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

public abstract class RepositoryIndexer extends BaseLocator {

  private static final long serialVersionUID = 3308953622126327699L;

  private static final Log logger = LogFactory.getLog( RepositoryIndexer.class );

  private IUnifiedRepository unifiedRepository;

  protected abstract IUnifiedRepository getUnifiedRepository( IPentahoSession session ) throws Exception;

  protected abstract Object getFileContents( RepositoryFile file, String type ) throws Exception;

  private RepositoryIndexRunner indexRunner = null;

  public String getId( String... tokens ) {
    return getIndexerType()+"."+getRepositoryId()+"."+tokens[0];
  }
  
  @Override
  public void startScan() {
    if( indexRunner != null ) {
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
    
    indexRunner = new RepositoryIndexRunner();
    indexRunner.setRepositoryIndexer( this );
    indexRunner.setRepoTop( kids );
    indexRunner.run();
  }

  @Override
  public void stopScan() {
    indexRunner.stop();
    while( indexRunner.isRunning() ) {
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
        // intentional
      }
    }
    indexRunner = null;
  }
  
  @Override
  public Log getLogger() {
    return logger;
  }

  protected IUnifiedRepository getRepo() {
    return unifiedRepository;
  }

}
