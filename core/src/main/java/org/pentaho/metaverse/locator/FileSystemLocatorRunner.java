/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.locator;

import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A runnable (and stoppable) class for crawling a Hitachi Vantara repository for documents
 * @author jdixon
 *
 */
public class FileSystemLocatorRunner extends LocatorRunner<File> {

  private static final Logger LOG = LoggerFactory.getLogger( LocatorRunner.class );
  /**
   * Indexes a set of files/folders. Folders are recursed into and files are passed to indexFile.
   * @param folder The files/folders to examine
   */
  public void locate( File folder ) {

    File[] files = folder.listFiles();
    for ( File file : files ) {
      if ( stopping ) {
        return;
      }
      if ( !file.isDirectory() ) {
        try {
          if ( !file.isHidden( ) ) {
            processFile( locator.getNamespace(), file.getName(), file.getCanonicalPath(), file );
          }
        } catch ( Exception e ) {
          // something truly unexpected would have to have happened ... NPE or similar ugliness
          LOG.error( Messages.getString( "ERROR.ProcessFileFailed", file.getName() ), e );
        }
      } else {
        locate( file );
      }
    }
  }

}
