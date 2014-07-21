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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract implementation of a document locator for Pentaho repositories
 * @author jdixon
 *
 */
public class FileSystemLocator extends BaseLocator {

  /**
   * The type for this locator
   */
  public static final String LOCATOR_TYPE = "FileSystem";

  private static final Log LOGGER = LogFactory.getLog( RepositoryLocator.class );

  private static final long serialVersionUID = 3308953622126327699L;

  private static final int POLLING_INTERVAL = 100;

  private FileSystemLocatorRunner indexRunner;

  private String rootFolder;

  public FileSystemLocator() {
    super();
    setIndexerType( LOCATOR_TYPE );
  }

  /**
   * A method that returns the payload (object or XML) for a document
   * @param file The repository file
   * @param type The type of the file
   * @return The object or XML payload
   * @throws Exception When the document contents cannot be retrieved
   */
  protected Object getFileContents( File file, String type ) throws Exception {
    String content = "";
    try {

      InputStream in = new FileInputStream( file );
      byte[] buffer = new byte[2048];
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int n = 0;
      try {
        while ( n != -1 ) {
          n = in.read( buffer );
          if ( n != -1 ) {
            out.write( buffer, 0, n );
          }
        }
      } finally {
        in.close();
      }
      content = new String( out.toByteArray() );
    } catch ( Throwable e ) {
      error( "Could not index document: " + file.getPath(), e );
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
  public String getId( String... tokens ) {
    return getIndexerType() + "." + getRepositoryId() + "." + tokens[0];
  }

  @Override
  public void startScan() {
    if ( indexRunner != null ) {
//TODO      throw new Exception("Locator is already scanning");
      return;
    }

    File root = new File( rootFolder );
    if ( !root.exists() ) {
      error( "Root folder does not exist: " + rootFolder );
//TODO      throw new IndexException(  ); 
      return;
    }

    if ( !root.isDirectory() ) {
      error( "Root is not a folder: " + rootFolder );
//TODO      throw new IndexException(  ); 
      return;
    }

    indexRunner = new FileSystemLocatorRunner();
    indexRunner.setRepositoryIndexer( this );
    indexRunner.setRepoTop( root );
    Thread runnerThread = new Thread( indexRunner );
    runnerThread.start();
  }

  @Override
  public void stopScan() {
    System.out.println( "RepositoryLocator stopScan" );
    indexRunner.stop();
    while ( indexRunner.isRunning() ) {
      try {
        System.out.println( "RepositoryLocator stopScan polling" );
        Thread.sleep( POLLING_INTERVAL );
      } catch ( InterruptedException e ) {
        // intentional
        break;
      }
    }
    indexRunner = null;
  }

  @Override
  public String[] getTypes() {
    return new String[] {};
  }

  @Override
  public Log getLogger() {
    return LOGGER;
  }

}
