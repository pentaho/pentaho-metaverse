/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
 *
 */

package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.IGraphWriter;
import com.pentaho.metaverse.api.ILineageWriter;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.MetaverseException;
import com.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.graph.GraphMLWriter;
import com.pentaho.metaverse.graph.GraphSONWriter;
import com.pentaho.metaverse.impl.model.ExecutionProfileUtil;
import org.pentaho.di.core.Const;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rfellows on 3/31/15.
 */
public class FileSystemLineageWriter implements ILineageWriter {

  public static final String DEFAULT_OUTPUT_FOLDER = ".";

  private IGraphWriter graphWriter = new GraphMLWriter();
  private String outputFolder = DEFAULT_OUTPUT_FOLDER;

  protected static SimpleDateFormat dateFolderFormat = new SimpleDateFormat( "YYYYMMdd" );

  public FileSystemLineageWriter() {
  }

  @Override
  public void outputExecutionProfile( LineageHolder holder ) throws IOException {
    if ( holder != null ) {
      IExecutionProfile profile = holder.getExecutionProfile();
      if ( profile != null ) {
        OutputStream fis = getProfileOutputStream( holder );
        if ( fis != null ) {
          ExecutionProfileUtil.outputExecutionProfile( fis, profile );
        } else {
          throw new IOException(
            "No profile output stream associated with this LineageWriter" );
        }
      }
    }
  }

  @Override
  public void outputLineageGraph( LineageHolder holder ) throws IOException {
    if ( holder != null ) {
      IMetaverseBuilder builder = holder.getMetaverseBuilder();
      if ( builder != null ) {
        OutputStream fis = getGraphOutputStream( holder );
        if ( fis != null ) {
          graphWriter.outputGraph( builder.getGraph(), fis );
        } else {
          throw new IOException(
            "No graph output stream associated with this LineageWriter" );
        }
      } // TODO else {
      // static graph
      //}
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
   * @return a String folder location
   */
  public String getOutputFolder() {
    return outputFolder;
  }

  /**
   * Sets the output folder for this writer
   * @param outputFolder The String output folder to write to
   */
  public void setOutputFolder( String outputFolder ) {
    this.outputFolder = outputFolder;
  }

  protected OutputStream createOutputStream( LineageHolder holder, String extension ) {
    if ( holder != null ) {
      File rootFolder = getDateFolder( outputFolder, holder );
      boolean rootFolderExists = rootFolder.exists();
      if ( !rootFolderExists ) {
        rootFolderExists = rootFolder.mkdirs();
      }
      if ( rootFolderExists ) {
        IExecutionProfile profile = holder.getExecutionProfile();
        String id = holder.getId() == null ? "unknown_artifact" : holder.getId();
        try {
          id = KettleAnalyzerUtil.normalizeFilePath( id );

          // strip off the colon from C:\path\to\file on windows
          if ( isWindows() ) {
            id = replaceColonInPath( id );
          }

          String name = Const.NVL( profile.getName(), "unknown" );
          File folder = new File( rootFolder, id );
          if ( !folder.exists() ) {
            folder.mkdirs();
          } else if ( folder.isFile() ) {
            // must be a folder
            throw new IllegalStateException( "Output folder must be a folder, not a file. ["
              + folder.getAbsolutePath() + "]" );
          }
          String timestampString = Long.toString( profile.getExecutionData().getStartTime().getTime() );

          File file = new File( folder, timestampString + "_" + name + extension );
          try {
            return new FileOutputStream( file );
          } catch ( FileNotFoundException e ) {
            e.printStackTrace();
            return null;
          }
        } catch ( MetaverseException e ) {
          e.printStackTrace();
          return null;
        }
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  protected String replaceColonInPath( String id ) {
    String newPath = id;
    if ( id.matches( "([A-Za-z]:.*)" ) ) {
      newPath = id.replaceFirst( ":", "" );
    }
    return newPath;
  }

  protected boolean isWindows() {
    return getOsName().indexOf( "win" ) >= 0;
  }

  protected String getOsName() {
    return System.getProperty( "os.name" ).toLowerCase();
  }

  protected File getDateFolder( String parentDir, LineageHolder holder ) {
    String dir = ( parentDir == null ) ? "" : parentDir + File.separator;
    if ( holder != null && holder.getExecutionProfile() != null ) {
      IExecutionProfile profile = holder.getExecutionProfile();
      dir += dateFolderFormat.format( profile.getExecutionData().getStartTime() );
    } else {
      dir += dateFolderFormat.format( new Date() );
    }
    return new File( dir );
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

}
