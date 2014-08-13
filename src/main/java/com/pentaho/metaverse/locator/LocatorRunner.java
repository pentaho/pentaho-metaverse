package com.pentaho.metaverse.locator;

import com.pentaho.metaverse.impl.DocumentEvent;
import com.pentaho.metaverse.messages.Messages;
import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * The LocatorRunner is a execution construct for concurrently running document locator logic.
 */
public abstract class LocatorRunner<T> implements Runnable {

  private static final Logger log = LoggerFactory.getLogger( LocatorRunner.class );

  /**
   * The top-level repository files and folders to search into
   */
  protected T root;

  /**
   * The repository crawler to use for getting document contents and generating ids
   */
  protected BaseLocator locator;

  /**
   * A flag to identify if we should stop crawling the repository (due to an external cancel event)
   */
  protected boolean stopping;

  /**
   * A flag to identify we if are currently crawling the repository
   */
  protected boolean running;

  public void setRoot( T root ) {
    this.root = root;
  }

  public void setLocator( BaseLocator repoLocator ) {
    this.locator = repoLocator;
  }

  @Override
  public void run() {
    running = true;
    locate( root );
    running = false;
  }

  public boolean isRunning() {
    return running;
  }

  /**
   * Stops the crawling of the repository
   */
  public void stop() {
    stopping = true;
  }

  /**
   * Indexes a set of files/folders. Folders are recursed into and files are passed to indexFile.
   * @param root  The files/folders to examine
   */
  protected abstract void locate( T root );


    /**
     * Gets the content of a document and notifies the repository document locator listeners of it
     */
  public void processFile( INamespace namespace, String name, String id, Object contents ) {

    if ( stopping ) {
      return;
    }

    Path path = Paths.get( name );
    String extension = FilenameUtils.getExtension( name );

    if ( "".equals( extension ) ) {
      return;
    }

    String mimeType;
    try {
      mimeType = Files.probeContentType( path );
    } catch ( IOException e ) {
      mimeType = null;
    }

    try {

      IMetaverseBuilder metaverseBuilder = locator.getMetaverseBuilder();
      IMetaverseObjectFactory objectFactory = metaverseBuilder.getMetaverseObjectFactory();

      IMetaverseDocument metaverseDocument = objectFactory.createDocumentObject();

      metaverseDocument.setNamespace( namespace );
      metaverseDocument.setContent( contents );
      metaverseDocument.setStringID( id );
      metaverseDocument.setName( name );
      metaverseDocument.setExtension( extension );
      metaverseDocument.setMimeType( mimeType );

      DocumentEvent event = new DocumentEvent();
      event.setEventType( "add" );
      event.setDocument( metaverseDocument );
      locator.notifyListeners( event );
    } catch ( Exception e ) {
      log.error( Messages.getString( "ERROR.NoContentForFile", name ) );
    }

  }

}
