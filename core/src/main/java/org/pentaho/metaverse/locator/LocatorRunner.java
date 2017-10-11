/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.locator;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.impl.DocumentEvent;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;
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
   * @param file      The contents of the file
   */
  public void processFile( INamespace namespace, String name, String id, Object file ) {

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

      IDocument metaverseDocument = MetaverseUtil.createDocument(
        namespace, locator.getContents( file ), id, name, extension, mimeType );

      DocumentEvent event = new DocumentEvent();
      event.setEventType( "add" );
      event.setDocument( metaverseDocument );

      locator.notifyListeners( event );
    } catch ( Exception e ) {

      LOG.error( Messages.getString( "ERROR.NoContentForFile", name ), e );

    }

  }

}
