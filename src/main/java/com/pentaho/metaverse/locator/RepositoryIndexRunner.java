package com.pentaho.metaverse.locator;

import java.util.List;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;

import com.pentaho.metaverse.impl.DocumentEvent;
import com.pentaho.metaverse.impl.MetaverseDocument;

public class RepositoryIndexRunner implements Runnable {

  List<RepositoryFileTree> repoTop;
  RepositoryIndexer repositoryIndexer;
  boolean stopping = false;
  boolean running = false;

  public void setRepoTop( List<RepositoryFileTree> repoTop ) {
    this.repoTop = repoTop;
  }

  public void setRepositoryIndexer( RepositoryIndexer repositoryIndexer ) {
    this.repositoryIndexer = repositoryIndexer;
  }

  @Override
  public void run() {
    running = true;
    indexFileTree( repoTop );
    running = false;
  }

  public boolean isRunning() {
    return running;
  }

  public void stop() {
    stopping = true;
  }
  
  private void indexFileTree( List<RepositoryFileTree> roots ) {

    for ( RepositoryFileTree fileTree : roots ) {
      if( stopping ) {
        return;
      }
      if ( fileTree.getFile() != null ) {
        RepositoryFile file = fileTree.getFile();
        if ( !file.isFolder() ) {
          indexFile( file );
        } else {
          List<RepositoryFileTree> kids = fileTree.getChildren();
          if ( kids != null && kids.size() > 0 ) {
            indexFileTree( kids );
          }
        }
      }

    }
  }

  private void indexFile( RepositoryFile file ) {

    if( stopping ) {
      return;
    }
    if ( file.isHidden() ) {
      // don't index hidden fields
      return;
    }
    String name = file.getName();
    String extension = "";
    int pos = name.lastIndexOf( '.' );
    if ( pos != -1 ) {
      extension = name.substring( pos + 1 ).toLowerCase();
    }

    if ( "".equals( extension ) ) {
      return;
    }

    String id = repositoryIndexer.getId( file.getPath() );
    try {
      Object contents = repositoryIndexer.getFileContents( file, extension );
      DocumentEvent event = new DocumentEvent();
      event.setType( "add" );
      MetaverseDocument metaverseDocument = new MetaverseDocument();
      metaverseDocument.setContent( contents );
      metaverseDocument.setID( id );
      metaverseDocument.setName( name );
      metaverseDocument.setType( extension );
      event.setDocument( metaverseDocument );
      repositoryIndexer.notifyListeners( event );
    } catch ( Exception e ) {
      repositoryIndexer.error("Could not get file contents for "+file.getPath());
    }
    
/*
    SearchContentItem obj;
    try {
      obj = metaIndex.getDocument( id, PentahoSessionHolder.getSession() );
      if( obj != null ) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String current = sdf.format( file.getLastModifiedDate() );
        if( current.compareTo( obj.getLastModifiedDate() ) <= 0 ) {
          // this file has not been modified
          return;
        }
      }
        
    } catch ( Exception e1 ) {
      e1.printStackTrace();
    }
    */
  }
  
}
