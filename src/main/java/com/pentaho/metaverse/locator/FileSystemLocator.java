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

import com.pentaho.metaverse.messages.Messages;
import org.apache.commons.io.FileUtils;
import org.pentaho.platform.api.metaverse.IDocumentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * An abstract implementation of a document locator for Pentaho repositories
 * @author jdixon
 *
 */
public class FileSystemLocator extends BaseLocator<File> {
  private static final long serialVersionUID = 3308953622126327699L;

  /**
   * The type for this locator
   */
  public static final String LOCATOR_TYPE = "FileSystem";

  private static final Logger log = LoggerFactory.getLogger( FileSystemLocator.class );

  private String rootFolder;

  public FileSystemLocator() {
    super();
    setLocatorType( LOCATOR_TYPE );
  }

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
      log.error( Messages.getString( "ERROR.IndexingDocument", file.getPath() ), e );
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
  protected String getId( String... tokens ) {
    return getLocatorType() + "." + getRepositoryId() + "." + tokens[0];
  }

  @Override
  public void startScan() {

    File root = new File( rootFolder );
    if ( !root.exists() ) {
      log.error( Messages.getString("ERROR.FileSystemLocator.RootFolder.DoesNotExist", rootFolder ) );
      //TODO      throw new IndexException(  );
      return;
    }

    if ( !root.isDirectory() ) {
      log.error( Messages.getString("ERROR.FileSystemLocator.RootFolder.NotAFolder", rootFolder ) );
      //TODO      throw new IndexException(  );
      return;
    }

    LocatorRunner lr = new FileSystemLocatorRunner();
    lr.setRoot( root );
    startScan( lr );
  }

}
