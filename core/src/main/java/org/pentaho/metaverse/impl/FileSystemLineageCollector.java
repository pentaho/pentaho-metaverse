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


package org.pentaho.metaverse.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.metaverse.api.ILineageCollector;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.DateRangeFolderFilenameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @see org.pentaho.metaverse.impl.VfsLineageCollector
 * 
 * @deprecated Use above class as a direct replacement.  It provides backward compatibility
 * for the local file system, but allows use of VFS based file systems simply by changing
 * the lineage.execution.output.folder to a VFS supported specification path.
 */
@Deprecated
public class FileSystemLineageCollector implements ILineageCollector {

  public static final String DEFAULT_OUTPUT_FOLDER = ".";
  private String outputFolder = DEFAULT_OUTPUT_FOLDER;
  private static final Logger log = LoggerFactory.getLogger( FileSystemLineageCollector.class );
  protected SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd" );

  public FileSystemLineageCollector() {
    format.setLenient( false );
  }

  /**
   * Gets the output folder location for this collector
   *
   * @return a String folder location
   */
  public String getOutputFolder() {
    return outputFolder;
  }

  /**
   * Sets the output folder for this collector
   *
   * @param outputFolder
   *          The String output folder to gather lineage artifacts from
   */
  public void setOutputFolder( String outputFolder ) {
    this.outputFolder = outputFolder;
  }

  @Override
  public List<String> listArtifacts() throws IllegalArgumentException {
    return listArtifacts( null, null );
  }

  @Override
  public List<String> listArtifacts( final String startingDate ) throws IllegalArgumentException {
    return listArtifacts( startingDate, null );
  }

  @Override
  public List<String> listArtifacts( final String startingDate, final String endingDate )
    throws IllegalArgumentException {
    File lineageRootFolder = new File( getOutputFolder() );
    List<String> paths = new ArrayList<>();

    FilenameFilter filter = new DateRangeFolderFilenameFilter( format, startingDate, endingDate );

    if ( lineageRootFolder.exists() && lineageRootFolder.isDirectory() ) {
      // get the folders that come on or after the startingDate
      String[] dayFolders = lineageRootFolder.list( filter );
      for ( String dayFolder : dayFolders ) {
        File listThisFolder = new File( lineageRootFolder, dayFolder );
        Collection<File> files = FileUtils.listFiles( listThisFolder, null, true );
        paths.addAll( sanitizePaths( files ) );
      }
    }
    return paths;
  }

  @Override
  public List<String> listArtifactsForFile( String pathToArtifact ) throws IllegalArgumentException {
    return listArtifactsForFile( pathToArtifact, null );
  }

  @Override
  public List<String> listArtifactsForFile( String pathToArtifact, String startingDate ) {
    return listArtifactsForFile( pathToArtifact, startingDate, null );
  }

  @Override
  public List<String> listArtifactsForFile( String pathToArtifact, String startingDate, String endingDate )
    throws IllegalArgumentException {

    File lineageRootFolder = new File( getOutputFolder() );
    List<String> paths = new ArrayList<>();

    FilenameFilter filter = new DateRangeFolderFilenameFilter( format, startingDate, endingDate );

    if ( lineageRootFolder.exists() && lineageRootFolder.isDirectory() ) {

      // get all of the date folders of lineage we have
      String[] dayFolders = lineageRootFolder.list( filter );
      for ( String dayFolder : dayFolders ) {
        File listThisFolder = new File( lineageRootFolder, dayFolder );

        // construct a path to the requested file
        File requested = new File( listThisFolder, pathToArtifact );
        // see if we have a folder matching the path given to us in each
        if ( requested.exists() && requested.isDirectory() ) {
          // add all matching artifacts to the list
          Collection<File> files = FileUtils.listFiles( requested, null, true );
          paths.addAll( sanitizePaths( files ) );
        }
      }
    }

    return paths;
  }

  protected List<String> sanitizePaths( Collection<File> files ) {
    List<String> paths = new ArrayList<>();
    for ( File file : files ) {
      String path = file.getPath();
      if ( path.startsWith( "./" ) ) {
        // strip off the beginning ./ if it exists to make the zip file more friendly to look at
        path = path.substring( 2 );
      }
      paths.add( path );
    }
    return paths;
  }

  @Override
  public void compressArtifacts( List<String> paths, OutputStream os ) {
    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream( os );
      for ( String path : paths ) {
        File file = new File( path );
        try {
          // register the file as an entry in the zip file
          ZipEntry zipEntry = new ZipEntry( file.getPath() );
          zos.putNextEntry( zipEntry );

          // write the file's bytes to the zip stream
          FileInputStream fis = new FileInputStream( file );
          zos.write( IOUtils.toByteArray( fis ) );

        } catch ( IOException e ) {
          log.error( Messages.getString( "ERROR.FailedAddingFileToZip", file.getPath() ) );
        } finally {
          // indicate we are done with this file
          try {
            zos.closeEntry();
          } catch ( IOException e ) {
            log.error( Messages.getString( "ERROR.FailedToProperlyCloseZipEntry", file.getPath() ) );
          }
        }
      }
    } finally {
      IOUtils.closeQuietly( zos );
    }
  }

}
