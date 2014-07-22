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

import java.io.File;

import com.pentaho.metaverse.impl.DocumentEvent;
import com.pentaho.metaverse.impl.MetaverseDocument;

/**
 * A runnable (and stoppable) class for crawling a Pentaho repository for documents
 * @author jdixon
 *
 */
public class FileSystemLocatorRunner implements Runnable {

  /**
   * The top-level repository files and folders to search into
   */
  private File repoTop;

  /**
   * The repository crawler to use for getting document contents and generating ids
   */
  private FileSystemLocator repositoryIndexer;

  /**
   * A flag to identify if we should stop crawling the repository (due to an external cancel event)
   */
  private boolean stopping;

  /**
   * A flag to identify we if are currently crawling the repository
   */
  private boolean running;

  public void setRepoTop( File repoTop ) {
    this.repoTop = repoTop;
  }

  public void setRepositoryIndexer( FileSystemLocator repositoryIndexer ) {
    this.repositoryIndexer = repositoryIndexer;
  }

  @Override
  public void run() {
    running = true;
    indexFolder( repoTop );
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
   * @param folder The files/folders to examine
   */
  private void indexFolder( File folder ) {

    File[] files = folder.listFiles();
    for ( File file : files ) {
      if ( stopping ) {
        return;
      }
      if ( !file.isDirectory() ) {
        indexFile( file );
      } else {
        indexFolder( file );
      }
    }
  }

  /**
   * Gets the content of a document and notifies the repository document locator listeners of it
   * @param file The file to examine
   */
  private void indexFile( File file ) {

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
      event.setEventType( "add" );
      MetaverseDocument metaverseDocument = new MetaverseDocument();
      metaverseDocument.setContent( contents );
      metaverseDocument.setStringID( id );
      metaverseDocument.setName( name );
      metaverseDocument.setType( extension );
      event.setDocument( metaverseDocument );
      repositoryIndexer.notifyListeners( event );
    } catch ( Exception e ) {
      repositoryIndexer.error( "Could not get file contents for " + file.getPath() );
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
