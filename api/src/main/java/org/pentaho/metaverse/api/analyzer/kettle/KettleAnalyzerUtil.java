/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class KettleAnalyzerUtil {

  /**
   * Utility method for normalizing file paths used in Metaverse Id generation. It will convert a valid path into a
   * consistent path regardless of URI notation or filesystem absolute path.
   *
   * @param filePath full path to normalize
   * @return the normalized path
   */
  public static String normalizeFilePath( String filePath ) throws MetaverseException {
    try {
      String path = filePath;
      FileObject fo = KettleVFS.getFileObject( filePath );
      try {
        path = fo.getURL().getPath();
      } catch ( Throwable t ) {
        // Something went wrong with VFS, just try the filePath
      }
      File f = new File( path );
      return f.getAbsolutePath();
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }
  }

  public static Collection<IExternalResourceInfo> getResourcesFromMeta(
    final StepMeta parentStepMeta, final String[] filePaths ) {
    Collection<IExternalResourceInfo> resources = Collections.emptyList();

    if ( parentStepMeta != null && filePaths != null && filePaths.length > 0 ) {
      resources = new ArrayList<>( filePaths.length );
      for ( final String path : filePaths ) {
        if ( !Const.isEmpty( path ) ) {
          try {

            final IExternalResourceInfo resource = ExternalResourceInfoFactory
              .createFileResource( KettleVFS.getFileObject( path ), true );
            if ( resource != null ) {
              resources.add( resource );
            } else {
              throw new KettleFileException( "Error getting file resource!" );
            }
          } catch ( KettleFileException kfe ) {
            // TODO throw or ignore?
          }
        }
      }
    }
    return resources;
  }

  public static Collection<IExternalResourceInfo> getResourcesFromRow(
    BaseFileInputStep step, RowMetaInterface rowMeta, Object[] row ) {

    Collection<IExternalResourceInfo> resources = new LinkedList<>();
    // For some reason the step doesn't return the StepMetaInterface directly, so go around it
    BaseFileInputMeta meta = (BaseFileInputMeta) step.getStepMetaInterface();
    if ( meta == null ) {
      meta = (BaseFileInputMeta) step.getStepMeta().getStepMetaInterface();
    }

    try {
      String filename = meta == null ? null : step.environmentSubstitute(
        rowMeta.getString( row, meta.getAcceptingField(), null ) );
      if ( !Const.isEmpty( filename ) ) {
        FileObject fileObject = KettleVFS.getFileObject( filename, step );
        resources.add( ExternalResourceInfoFactory.createFileResource( fileObject, true ) );
      }
    } catch ( KettleException kve ) {
      // TODO throw exception or ignore?
    }
    return resources;
  }

  public static TransMeta getSubTransMeta( final ISubTransAwareMeta meta ) throws MetaverseAnalyzerException {

    final TransMeta parentTransMeta = meta.getParentStepMeta().getParentTransMeta();
    final Repository repo = parentTransMeta.getRepository();

    TransMeta subTransMeta = null;
    switch ( meta.getSpecificationMethod() ) {
      case FILENAME:
        String transPath = null;
        try {
          transPath = KettleAnalyzerUtil.normalizeFilePath( parentTransMeta.environmentSubstitute(
            meta.getFileName() ) );
          subTransMeta = getSubTransMeta( transPath );
        } catch ( Exception e ) {
          throw new MetaverseAnalyzerException( "Sub transformation can not be found - " + transPath, e );
        }
        break;
      case REPOSITORY_BY_NAME:
        if ( repo != null ) {
          String dir = parentTransMeta.environmentSubstitute( meta.getDirectoryPath() );
          String file = parentTransMeta.environmentSubstitute( meta.getTransName() );
          try {
            RepositoryDirectoryInterface rdi = repo.findDirectory( dir );
            subTransMeta = repo.loadTransformation( file, rdi, null, true, null );
          } catch ( KettleException e ) {
            throw new MetaverseAnalyzerException( "Sub transformation can not be found in repository - " + file, e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the transformation" );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subTransMeta = repo.loadTransformation( meta.getTransObjectId(), null );
          } catch ( KettleException e ) {
            throw new MetaverseAnalyzerException( "Sub transformation can not be found by reference - "
              + meta.getTransObjectId(), e );
          }
        } else {
          throw new MetaverseAnalyzerException( "Not connected to a repository, can't get the transformation" );
        }
        break;
    }
    return subTransMeta;
  }


  public static String getSubTransMetaPath( final ISubTransAwareMeta meta, final TransMeta subTransMeta ) throws
    MetaverseAnalyzerException {

    final TransMeta parentTransMeta = meta.getParentStepMeta().getParentTransMeta();
    String transPath = null;
    switch ( meta.getSpecificationMethod() ) {
      case FILENAME:
        try {
          transPath = KettleAnalyzerUtil.normalizeFilePath( parentTransMeta.environmentSubstitute(
            meta.getFileName() ) );
        } catch ( Exception e ) {
          throw new MetaverseAnalyzerException( "Sub transformation can not be found - " + transPath, e );
        }
        break;
      case REPOSITORY_BY_NAME:
        transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
        break;
      case REPOSITORY_BY_REFERENCE:
        transPath = subTransMeta.getPathAndName() + "." + subTransMeta.getDefaultExtension();
        break;
    }
    return transPath;
  }

  private static TransMeta getSubTransMeta( final String filePath ) throws FileNotFoundException, KettleXMLException,
    KettleMissingPluginsException {
    FileInputStream fis = new FileInputStream( filePath );
    return new TransMeta( fis, null, true, null, null );
  }
}
