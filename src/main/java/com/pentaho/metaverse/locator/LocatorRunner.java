package com.pentaho.metaverse.locator;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.DocumentEvent;
import com.pentaho.metaverse.messages.Messages;
import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * The LocatorRunner is a execution construct for concurrently running document locator logic.
 *
 * @param <T> The type of the locator for this runner
 * @author jdixon
 */
public abstract class LocatorRunner<T> implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger( LocatorRunner.class );

  private static FileNameMap fileNameMap = URLConnection.getFileNameMap();
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
   *
   * @param root The files/folders to examine
   */
  protected abstract void locate( T root );

  /**
   * Processes the contents of a file. Creates a metaverse document, sets the main properties,
   * and calls the document listeners to parse/process the file.
   *
   * @param namespace The namespace to use for creating ids
   * @param name      The name of the file
   * @param id        The id of the file
   * @param contents  The contents of the file
   */
  public void processFile( INamespace namespace, String name, String id, Object contents ) {

    if ( stopping ) {
      return;
    }

    String extension = FilenameUtils.getExtension( name );

    if ( "".equals( extension ) ) {
      return;
    }

    String mimeType;
    try {
      mimeType = fileNameMap.getContentTypeFor( name );
    } catch ( Exception e ) {
      mimeType = null;
      // optional attribute, continue...
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
      metaverseDocument.setProperty( DictionaryConst.PROPERTY_PATH, id );

      DocumentEvent event = new DocumentEvent();
      event.setEventType( "add" );
      event.setDocument( metaverseDocument );

      locator.notifyListeners( event );
    } catch ( Exception e ) {

      LOG.error( Messages.getString( "ERROR.NoContentForFile", name ), e );

    }

  }

}
