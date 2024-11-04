/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.locator;

import org.pentaho.metaverse.messages.Messages;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A runnable (and stoppable) class for crawling a Hitachi Vantara repository for documents
 * @author jdixon
 *
 */
public class RepositoryLocatorRunner extends LocatorRunner<List<RepositoryFileTree>> {

  private static final Logger LOG = LoggerFactory.getLogger( LocatorRunner.class );

  /**
   * Indexes a set of files/folders. Folders are recursed into and files are passed to indexFile.
   * @param fileTrees The files/folders to examine
   */
  @Override
  public void locate( List<RepositoryFileTree> fileTrees ) {

    for ( RepositoryFileTree fileTree : fileTrees ) {
      if ( stopping ) {
        return;
      }
      if ( fileTree.getFile() != null ) {
        RepositoryFile file = fileTree.getFile();
        if ( !file.isFolder() ) {

          if ( !file.isHidden() ) {
            // don't index hidden fields
            try {
              processFile( locator.getNamespace(), file.getName(), file.getPath(),  file );
            } catch ( Exception e ) {
              // something truly unexpected would have to have happened ... NPE or similar ugliness
              LOG.error( Messages.getString( "ERROR.ProcessFileFailed", file.getName() ), e );
            }
          }
        } else {
          List<RepositoryFileTree> kids = fileTree.getChildren();
          if ( kids != null && kids.size() > 0 ) {
            locate( kids );
          }
        }
      }

    }
  }

}
