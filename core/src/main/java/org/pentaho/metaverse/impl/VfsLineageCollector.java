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

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileDepthSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metaverse.api.ILineageCollector;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.VfsDateRangeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VfsLineageCollector implements ILineageCollector {

  public static final String DEFAULT_OUTPUT_FOLDER = "tmp://dir";

  private String outputFolder = DEFAULT_OUTPUT_FOLDER;
  private static final Logger log = LoggerFactory.getLogger( VfsLineageCollector.class );
  protected SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd" );

  public VfsLineageCollector() {
    format.setLenient( false );
    this.setOutputFolder( MetaverseConfig.getInstance().getExecutionOutputFolder() );
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
    int seperatorIndex = outputFolder.indexOf( ":" );
    if ( seperatorIndex > -1 ) {
      String prefix = outputFolder.substring( 0, outputFolder.indexOf( ":" ) );
      if ( VfsLineageWriter.isVFSPrefix( prefix ) ) {
        this.outputFolder = outputFolder;
      } else { // Prefix is not in VFS so try to make a local file from this
        File localFile = new File( outputFolder );
        try {
          this.outputFolder = "file://" + localFile.getCanonicalPath();
        } catch ( IOException e ) {
          log.error( Messages.getString( "ERROR.CantUseOutputFile", outputFolder ), e );
        }
      }
    } else { // Had no prefix so try to make a local file from this.
      File localFile = new File( outputFolder );
      try {
        this.outputFolder = "file://" + localFile.getCanonicalPath();
      } catch ( IOException e ) {
        log.error( Messages.getString( "ERROR.CantUseOutputFile", outputFolder ), e );
      }
    }
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
    List<String> paths = new ArrayList<>();
    try {
      FileSystemOptions opts = new FileSystemOptions();
      FileObject lineageRootFolder = KettleVFS.getInstance( DefaultBowl.getInstance() )
        .getFileObject( getOutputFolder(), opts );

      FileSelector dateRangeFilter = new VfsDateRangeFilter( format, startingDate, endingDate );
      FileSelector depthFilter = new FileDepthSelector( 1, 256 );

      if ( lineageRootFolder.exists() && lineageRootFolder.getType() == FileType.FOLDER ) {
        // get the folders that come on or after the startingDate
        FileObject[] dayFolders = lineageRootFolder.findFiles( dateRangeFilter );
        for ( FileObject dayFolder : dayFolders ) {
          FileObject[] listThisFolder = dayFolder.findFiles( depthFilter );
          for ( FileObject currentFile : listThisFolder ) {
            if ( currentFile.getType() == FileType.FILE ) {
              paths.add( currentFile.getName().getPath() );
            }
          }
        }
      }
      return paths;
    } catch ( Exception e ) {
      throw new IllegalArgumentException( e );
    }
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
    List<String> paths = new ArrayList<>();

    try {
      FileSystemOptions opts = new FileSystemOptions();
      FileObject lineageRootFolder = KettleVFS.getInstance( DefaultBowl.getInstance() )
        .getFileObject( getOutputFolder(), opts );

      FileSelector dateRangeFilter = new VfsDateRangeFilter( format, startingDate, endingDate );
      FileSelector depthFilter = new FileDepthSelector( 1, 256 );

      if ( lineageRootFolder.exists() && lineageRootFolder.getType() == FileType.FOLDER ) {

        // get all of the date folders of lineage we have
        FileObject[] dayFolders = lineageRootFolder.findFiles( dateRangeFilter );
        for ( FileObject dayFolder : dayFolders ) {
          FileObject[] listThisFolder = dayFolder.findFiles( depthFilter );
          for ( FileObject currentFile : listThisFolder ) {
            FileObject requested = currentFile.resolveFile( pathToArtifact );
            if ( requested.exists() && requested.getType() == FileType.FOLDER ) {
              FileObject[] requestedChildren = requested.getChildren();
              for ( FileObject requestedChild : requestedChildren ) {
                if ( requestedChild.getType() == FileType.FILE ) {
                  paths.add( requestedChild.getName().getPath() );
                }
              }
            }
          }
        }
      }
      return paths;
    } catch ( Exception e ) {
      throw new IllegalArgumentException( e );
    }
  }

  @Override
  public void compressArtifacts( List<String> paths, OutputStream os ) {
    ZipOutputStream zos = null;
    try {
      FileSystemOptions opts = new FileSystemOptions();

      zos = new ZipOutputStream( os );
      for ( String path : paths ) {
        FileObject file = KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( path, opts );
        try {
          // register the file as an entry in the zip file
          ZipEntry zipEntry = new ZipEntry( file.getName().getPath() );
          zos.putNextEntry( zipEntry );

          // write the file's bytes to the zip stream
          try ( InputStream fis = file.getContent().getInputStream() ) {
            zos.write( IOUtils.toByteArray( fis ) );
          }
        } catch ( IOException e ) {
          log.error( Messages.getString( "ERROR.FailedAddingFileToZip", file.getName().getPath() ) );
        } finally {
          // indicate we are done with this file
          try {
            zos.closeEntry();
          } catch ( IOException e ) {
            log.error( Messages.getString( "ERROR.FailedToProperlyCloseZipEntry", file.getName().getPath() ) );
          }
        }
      }
    } catch ( KettleFileException e ) {
      log.error( Messages.getString( "ERROR.UnexpectedVfsError", e.getMessage() ) );
    } finally {
      IOUtils.closeQuietly( zos );
    }
  }
}
