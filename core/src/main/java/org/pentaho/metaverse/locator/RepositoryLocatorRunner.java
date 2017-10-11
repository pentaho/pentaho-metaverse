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
