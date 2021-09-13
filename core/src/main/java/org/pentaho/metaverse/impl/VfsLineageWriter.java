/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.ILineageWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.graph.GraphCatalogWriter;
import org.pentaho.metaverse.graph.GraphMLWriter;
import org.pentaho.metaverse.graph.GraphSONWriter;
import org.pentaho.metaverse.impl.model.ExecutionProfileUtil;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.min;

/**
 * Created by wseyler on 10/27/15.
 */
public class VfsLineageWriter implements ILineageWriter {

  public static final String DEFAULT_OUTPUT_FOLDER = "tmp://dir";

  private static final Logger log = LoggerFactory.getLogger( VfsLineageWriter.class );
  private static final String UNKNOWN_ARTIFACT = "unknown_artifact";
  private static final int MAX_NAME_LEN = 150;  // should be a safe, conservative number

  private IGraphWriter graphWriter = new GraphMLWriter();
  private GraphCatalogWriter catalogWriter
    = new GraphCatalogWriter( "", "", "", "", "", "" );
  private String outputFolder = DEFAULT_OUTPUT_FOLDER;
  private String outputStrategy = DEFAULT_OUTPUT_STRATEGY;

  protected static SimpleDateFormat dateFolderFormat = new SimpleDateFormat( "YYYYMMdd" );

  public GraphCatalogWriter getCatalogWriter() {
    return catalogWriter;
  }

  public void setCatalogWriter( GraphCatalogWriter catalogWriter ) {
    this.catalogWriter = catalogWriter;
  }

  private static enum VFS_Prefixes {
    BZIP2, FILE, FTP, FTPS, GZIP, HDFS, HTTP, HTTPS, JAR, RAM, RES, SFTP, TAR, TEMP, WEBDAV, ZIP
  }

  public VfsLineageWriter() {
  }

  @Override
  public void outputExecutionProfile( LineageHolder holder ) throws IOException {
    if ( holder != null ) {
      IExecutionProfile profile = holder.getExecutionProfile();
      if ( profile != null ) {
        try ( OutputStream fis = getProfileOutputStream( holder ) ) {
          if ( fis != null ) {
            ExecutionProfileUtil.outputExecutionProfile( fis, profile );
          } else {
            log.debug( Messages.getString( "DEBUG.noProfileOutputStream" ) );
          }
        }
      }
    }
  }

  @Override
  public void outputLineageGraph( LineageHolder holder ) throws IOException {
    if ( holder != null ) {
      IMetaverseBuilder builder = holder.getMetaverseBuilder();
      if ( builder != null ) {
        // no-op by default, can be used to introduce an artificial delay in the graphml file, for testing purposes
        MetaverseUtil.delay();
        if ( catalogWriter.clientConfigured() ) {
          catalogWriter.outputGraph( builder.getGraph(), null );
        }
        try ( OutputStream fos = getGraphOutputStream( holder ) ) {
          if ( fos != null ) {
            graphWriter.outputGraph( builder.getGraph(), fos );
          } else {
            log.debug( Messages.getString( "DEBUG.noGraphOutputStream" ) );
          }
        }
        MetaverseUtil.delay();
      }
    }
  }

  /**
   * Returns the graph writer associated with this lineage writer object
   *
   * @return an IGraphWriter instance
   */
  public IGraphWriter getGraphWriter() {
    return graphWriter;
  }

  /**
   * Sets the graph writer associated with this lineage writer object
   *
   * @param graphWriter an IGraphWriter instance
   */
  public void setGraphWriter( IGraphWriter graphWriter ) {
    this.graphWriter = graphWriter;
  }

  /**
   * Gets the output folder location for this writer
   *
   * @return a String folder location
   */
  public String getOutputFolder() {
    return outputFolder;
  }

  public static boolean isVFSPrefix( String prefix ) {
    for ( VFS_Prefixes vfs_prefix : VFS_Prefixes.values() ) {
      if ( vfs_prefix.name().equalsIgnoreCase( prefix ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the output folder for this writer
   *
   * @param outputFolder The String output folder to write to
   */
  public void setOutputFolder( String outputFolder ) {
    int seperatorIndex = outputFolder.indexOf( ":" );
    if ( seperatorIndex > -1 ) {
      String prefix = outputFolder.substring( 0, outputFolder.indexOf( ":" ) );
      if ( isVFSPrefix( prefix ) ) {
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

  protected OutputStream createOutputStream( LineageHolder holder, String extension ) {
    if ( holder != null ) {
      try {
        IExecutionProfile profile = holder.getExecutionProfile();
        String timestampString = Long.toString( profile.getExecutionData().getStartTime().getTime() );
        FileObject destFolder = getOutputDirectoryAsFile( holder );
        String name = cleanseName( Const.NVL( profile.getName(), "unknown" ) );
        FileObject file = destFolder.resolveFile( timestampString + "_" + name + extension );
        FileContent content = file.getContent();
        return content.getOutputStream();
      } catch ( Exception e ) {
        log.error( Messages.getErrorString( "ERROR.CantCreateOutputStream" ), e );
        return null;
      }
    } else {
      return null;
    }
  }

  protected FileObject getOutputDirectoryAsFile( LineageHolder holder ) {
    try {
      FileObject dateRootFolder = getDateFolder( holder );
      dateRootFolder.createFolder();
      String id = getNameForHolder( dateRootFolder, holder );
      try {
        FileObject folder = dateRootFolder.resolveFile( id );
        folder.createFolder();
        if ( folder.isFile() ) {
          // must be a folder
          throw new IllegalStateException( Messages.getErrorString( "ERROR.OutputFolderWrongType", folder.getName()
            .getPath() ) );
        }
        return folder;
      } catch ( Exception e ) {
        log.error( Messages.getErrorString( "ERROR.CouldNotCreateFile" ), e );
        return null;
      }
    } catch ( Exception e ) {
      log.error( Messages.getErrorString( "ERROR.CouldNotCreateFile" ), e );
      throw new IllegalStateException( e );
    }
  }

  private String getNameForHolder( FileObject root, LineageHolder holder ) {
    String name = holder.getId() == null ? UNKNOWN_ARTIFACT : holder.getId();
    if ( name.startsWith( File.separator ) ) { // For *nix
      name = name.substring( 1 );
    } else if ( Const.isWindows() && name.charAt( 1 ) == ':' ) { // For windows
      name = name.replaceFirst( Pattern.quote( ":" ), "" );
    }
    name = cleanseName( name );
    try {
      // attempt to resolve.  This can fail if the filename we're attempting to use is invalid,
      // in which case we'll fall back to the "unknown" name.
      root.resolveFile( name );
    } catch ( FileSystemException e ) {
      return UNKNOWN_ARTIFACT;
    }
    return name;
  }

  private String cleanseName( String name ) {
    return name
      .replace( ":", "-" )  // colons are misparsed by vfs FileObject in some cases
      .substring( 0, min( name.length(), MAX_NAME_LEN ) );
  }

  protected FileObject getDateFolder( LineageHolder holder ) throws KettleFileException, FileSystemException {
    String dir = "";
    if ( holder != null && holder.getExecutionProfile() != null ) {
      IExecutionProfile profile = holder.getExecutionProfile();
      dir += dateFolderFormat.format( profile.getExecutionData().getStartTime() );
    } else {
      dir += dateFolderFormat.format( new Date() );
    }
    FileObject lineageRootFolder = KettleVFS.getFileObject( getOutputFolder() );
    FileObject dateFolder = lineageRootFolder.resolveFile( dir );
    return dateFolder;
  }

  protected OutputStream getProfileOutputStream( LineageHolder holder ) {
    return createOutputStream( holder, ".execution.js" );
  }

  protected OutputStream getGraphOutputStream( LineageHolder holder ) {
    final String ext;
    if ( graphWriter instanceof GraphMLWriter ) {
      ext = ".graphml";
    } else if ( graphWriter instanceof GraphSONWriter ) {
      ext = ".graphson";
    } else {
      ext = ".txt";
    }
    return createOutputStream( holder, ext );
  }

  /**
   * Returns the output strategy (all, latest, none, etc.) as a string
   *
   * @return The String name of the output strategy
   */
  @Override
  public String getOutputStrategy() {
    return outputStrategy;
  }

  /**
   * Sets the output strategy (all, latest, none) for this writer
   *
   * @param strategy The strategy to use when outputting lineage information
   */
  @Override
  public void setOutputStrategy( String strategy ) {
    this.outputStrategy = strategy;
  }

  /**
   * Method called on the writer to do any cleanup of the output artifacts, folders, etc.
   */
  @Override
  public void cleanOutput( LineageHolder holder ) {
    String folderName = "unknown";
    try {
      FileObject folder = getOutputDirectoryAsFile( holder );
      folderName = folder.getName().getPath();
      folder.deleteAll();
    } catch ( IOException ioe ) {
      log.error( Messages.getErrorString( "ERROR.CouldNotDeleteFile", folderName ), ioe );
    }
  }

}
