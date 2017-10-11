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

import org.apache.commons.io.FileUtils;
import org.pentaho.metaverse.api.IDocumentListener;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * An abstract implementation of a document locator for Hitachi Vantara repositories
 * @author jdixon
 *
 */
public class FileSystemLocator extends BaseLocator<File> {

  /**
   * The type for this locator
   */
  public static final String LOCATOR_TYPE = "FileSystem";

  private static final long serialVersionUID = 3308953622126327699L;

  private static final Logger LOG = LoggerFactory.getLogger( FileSystemLocator.class );

  private String rootFolder;

  /**
   * Creates a filessytem locator
   */
  public FileSystemLocator() {
    super();
    setLocatorType( LOCATOR_TYPE );
  }

  /**
   * Creates a file system locator with a list of document listeners that will be informed
   * as documents are found by this locator.
   * 
   * @param documentListeners The document listeners
   */
  public FileSystemLocator( List<IDocumentListener> documentListeners ) {
    super( documentListeners );
    setLocatorType( LOCATOR_TYPE );
  }

  /**
   * A method that returns the payload (object or XML) for a document
   * @param file The repository file
   * @return The object or XML payload
   * @throws Exception When the document contents cannot be retrieved
   */
  @Override
  protected Object getContents( File file ) throws Exception {
    String content = "";
    try {
      content = FileUtils.readFileToString( file );
    } catch ( Throwable e ) {
      LOG.error( Messages.getString( "ERROR.IndexingDocument", file.getPath() ), e );
      // not fatal, continue
    }
    return content;
  }

  public String getRootFolder() {
    return rootFolder;
  }

  public void setRootFolder( String rootFolder ) {
    this.rootFolder = rootFolder;
  }

  @Override
  public void startScan() throws MetaverseLocatorException {

    File root = new File( rootFolder );
    if ( !root.exists() ) {
      LOG.error( Messages.getString("ERROR.FileSystemLocator.RootFolder.DoesNotExist", root.getAbsolutePath() ) );
      throw new MetaverseLocatorException(
          Messages.getString("ERROR.FileSystemLocator.RootFolder.DoesNotExist", root.getAbsolutePath() ) );
    }

    if ( !root.isDirectory() ) {
      LOG.error( Messages.getString("ERROR.FileSystemLocator.RootFolder.NotAFolder", root.getAbsolutePath() ) );
      throw new MetaverseLocatorException(
          Messages.getString("ERROR.FileSystemLocator.RootFolder.NotAFolder", root.getAbsolutePath() ) );
    }

    LocatorRunner<File> lr = new FileSystemLocatorRunner();
    lr.setRoot( root );
    startScan( lr );
  }

  @Override
  public URI getRootUri() {
    File root = new File( getRootFolder() );
    if ( root.exists() ) {
      URI rootUri = root.toURI();
      return rootUri;
    } else {
      return null;
    }
  }

}
