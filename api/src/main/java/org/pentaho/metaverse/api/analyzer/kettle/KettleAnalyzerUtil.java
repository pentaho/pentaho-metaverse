/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.AnalysisContext;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseConfig;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.messages.Messages;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;

public class KettleAnalyzerUtil {

  private static final Logger log = LoggerFactory.getLogger( KettleAnalyzerUtil.class );

  private static final ExternalResourceCache rowResourceCache = ExternalResourceCache.getInstance();

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
        path = getDecodedUriString( fo.getURL().getPath() );
      } catch ( Throwable t ) {
        // Something went wrong with VFS, just try the filePath
      }
      File f = new File( path );
      return f.getAbsolutePath();
    } catch ( Exception e ) {
      throw new MetaverseException( e );
    }
  }

  /**
   *
   * @param filePath
   * @return
   * @throws MetaverseException
   */
  public static String getFilePathScheme( String filePath ) throws MetaverseException {
    try {
      FileObject fo = KettleVFS.getFileObject( filePath );
      return fo.getURI().getScheme();
    } catch ( KettleFileException e ) {
      throw new MetaverseException( e );
    }
  }

  /**
   * Normalizes the {@code filePath} safely, ignoring exceptions and returning the original string, if there is a
   * problem with the normalization.
   */
  public static String normalizeFilePathSafely( String filePath ) {
    try {
      return normalizeFilePath( filePath );
    } catch ( final MetaverseException e ) {
      log.error( e.getMessage() );
    }
    return filePath;
  }

  public static Collection<IExternalResourceInfo> getResourcesFromMeta(
    final BaseStepMeta meta, final String[] filePaths ) {

    ExternalResourceCache.Resources resources = rowResourceCache.get( meta );
    if ( resources == null ) {
      resources = rowResourceCache.newExternalResourceValues();
    }

    if ( meta != null && meta.getParentStepMeta() != null && filePaths != null && filePaths.length > 0 ) {
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
            if ( log.isDebugEnabled() ) {
              log.debug( kfe.getMessage() );
            }
          }
        }
      }
    }
    return resources.getInternal();
  }

  public static Collection<IExternalResourceInfo> getResourcesFromRow(
    BaseFileInputStep step, RowMetaInterface rowMeta, Object[] row ) {

    // For some reason the step doesn't return the StepMetaInterface directly, so go around it
    BaseFileInputMeta meta = (BaseFileInputMeta) step.getStepMetaInterface();
    if ( meta == null ) {
      meta = (BaseFileInputMeta) step.getStepMeta().getStepMetaInterface();
    }
    ExternalResourceCache.ExternalResourceValues resources =
      (ExternalResourceCache.ExternalResourceValues) rowResourceCache.get( meta );
    if ( resources == null ) {
      resources = rowResourceCache.newExternalResourceValues();
      rowResourceCache.cache( meta, resources );
    }

    try {
      String filename = meta == null ? null : step.environmentSubstitute(
        rowMeta.getString( row, meta.getAcceptingField(), null ) );
      if ( !Utils.isEmpty( filename ) && ( KettleVFS.startsWithScheme( filename ) || FileUtil.isFullyQualified( filename ) ) ) {
        FileObject fileObject = KettleVFS.getFileObject( filename, step );
        resources.add( ExternalResourceInfoFactory.createFileResource( fileObject, true ) );
      }
    } catch ( KettleException kve ) {
      if ( log.isDebugEnabled() ) {
        log.debug( kve.getMessage() );
      }
    }
    return resources.getInternal();
  }

  public static TransMeta getSubTransMeta( final ISubTransAwareMeta meta ) throws MetaverseAnalyzerException {

    final TransMeta parentTransMeta = meta.getParentStepMeta().getParentTransMeta();
    final Repository repo = parentTransMeta.getRepository();

    TransMeta subTransMeta = null;
    switch ( meta.getSpecificationMethod() ) {
      case FILENAME:
        subTransMeta = getSubTransByFilename( meta, parentTransMeta, repo, subTransMeta );
        break;
      case REPOSITORY_BY_NAME:
        if ( repo != null ) {
          subTransMeta = getTransMetaFromRepo( meta, parentTransMeta, repo );
        } else {
          throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForTransSubTrans",
            parentTransMeta.toString() ) );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( repo != null ) {
          try {
            subTransMeta = repo.loadTransformation( meta.getTransObjectId(), null );
          } catch ( KettleException e ) {
            throw new MetaverseAnalyzerException( Messages.getString( "ERROR.SubTransNotFoundInParentTrans",
              ( meta.getTransObjectId() == null ? "N/A" : meta.getTransObjectId().toString() ), parentTransMeta
                .toString() ), e );
          }
        } else {
          throw new MetaverseAnalyzerException( Messages.getString( "ERROR.MissingConnectionForTransSubTrans",
            parentTransMeta.toString() ) );
        }
        break;
    }
    subTransMeta.setFilename( KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ) );
    return subTransMeta;
  }

  private static TransMeta getSubTransByFilename( ISubTransAwareMeta meta, TransMeta parentTransMeta, Repository repo,
                                                  TransMeta subTransMeta ) throws MetaverseAnalyzerException {
    String transPath = parentTransMeta.environmentSubstitute( meta.getFileName() );

    // if repository is not null, try it first
    if ( repo != null ) {
      String dir = transPath.substring( 0, transPath.lastIndexOf( '/' ) );
      String file = transPath.substring( transPath.lastIndexOf( '/' ) + 1 );
      subTransMeta = getFileFromRepo( parentTransMeta, repo, dir, file );
    }
    // couldn't find in repo or no repo present, look in file system
    if ( null == subTransMeta ) {
      try {
        subTransMeta = getSubTransMeta( normalizeFilePathSafely( transPath ) );
      } catch ( Exception e ) {
        throw new MetaverseAnalyzerException( Messages.getString( "ERROR.SubTransNotFoundInParentTrans",
          transPath, parentTransMeta.toString() ), e );
      }
    }
    return subTransMeta;
  }

  private static TransMeta getFileFromRepo( TransMeta parentTransMeta, Repository repo, String dir, String file )
    throws MetaverseAnalyzerException {
    TransMeta subTransMeta;
    try {
      RepositoryDirectoryInterface rdi = repo.findDirectory( dir );
      subTransMeta = repo.loadTransformation( file, rdi, null, true, null );
    } catch ( KettleException e ) {
      throw new MetaverseAnalyzerException( Messages.getString( "ERROR.SubTransNotFoundInParentTrans",
        file, parentTransMeta.toString() ), e );
    }
    return subTransMeta;
  }

  private static TransMeta getTransMetaFromRepo( ISubTransAwareMeta meta, TransMeta parentTransMeta, Repository repo )
    throws MetaverseAnalyzerException {
    String dir = parentTransMeta.environmentSubstitute( meta.getDirectoryPath() );
    String file = parentTransMeta.environmentSubstitute( meta.getTransName() );
    return getFileFromRepo( parentTransMeta, repo, dir, file );
  }

  private static String getDecodedUriString( String uri ) {
    try {
      return UriParser.decode( uri );
    } catch ( FileSystemException e ) {
      // return the raw string if the URI is malformed (bad escape sequence)
      return uri;
    }
  }

  /**
   * Returns the meta path based on the specification method.
   */
  public static String getSubTransMetaPath( final ISubTransAwareMeta meta, final TransMeta subTransMeta ) throws
    MetaverseAnalyzerException {

    String transPath = meta == null ? null : meta.getFileName();
    if ( meta != null && meta.getSpecificationMethod() != null ) {
      switch ( meta.getSpecificationMethod() ) {
        case FILENAME:
          transPath = meta.getFileName();
          break;
        case REPOSITORY_BY_NAME:
        case REPOSITORY_BY_REFERENCE:
          if ( subTransMeta != null && subTransMeta.getPathAndName() != null ) {
            transPath = subTransMeta.getPathAndName()
              + ( subTransMeta.getDefaultExtension() == null ? "" : "." + subTransMeta.getDefaultExtension() );
          }
          break;
      }
    }
    final TransMeta parentTransMeta = meta == null || meta.getParentStepMeta() == null ? null
      : meta.getParentStepMeta().getParentTransMeta();
    transPath = normalizeFilePathSafely( parentTransMeta == null || transPath == null ? transPath
      : parentTransMeta.environmentSubstitute( transPath ) );
    return transPath;
  }

  public static TransMeta getSubTransMeta( final String filePath ) throws FileNotFoundException, KettleXMLException,
    KettleMissingPluginsException {
    FileInputStream fis = new FileInputStream( filePath );
    return new TransMeta( fis, null, true, null, null );
  }

  /**
   * Builds a {@link IDocument} given the provided details.
   *
   * @param builder   the {@link IMetaverseBuilder}
   * @param meta      the {@link AbstractMeta} (trans or job)
   * @param id        the meta id (filename)
   * @param namespace the {@link INamespace}
   * @return a new {@link IDocument}
   */
  public static IDocument buildDocument( final IMetaverseBuilder builder, final AbstractMeta meta,
                                         final String id, final INamespace namespace ) {

    if ( builder == null || builder.getMetaverseObjectFactory() == null ) {
      log.warn( Messages.getString( "WARN.UnableToBuildDocument" ) );
      return null;
    }

    final IDocument metaverseDocument = builder.getMetaverseObjectFactory().createDocumentObject();
    if ( metaverseDocument == null ) {
      log.warn( Messages.getString( "WARN.UnableToBuildDocument" ) );
      return null;
    }

    metaverseDocument.setNamespace( namespace );
    metaverseDocument.setContent( meta );
    metaverseDocument.setStringID( id );
    metaverseDocument.setName( meta.getName() );
    metaverseDocument.setExtension( meta.getDefaultExtension() );
    metaverseDocument.setMimeType( URLConnection.getFileNameMap().getContentTypeFor(
      meta instanceof TransMeta ? "trans.ktr" : "job.kjb" ) );
    metaverseDocument.setContext( new AnalysisContext( DictionaryConst.CONTEXT_RUNTIME ) );
    String normalizedPath = KettleAnalyzerUtil.normalizeFilePathSafely( id );
    metaverseDocument.setProperty( DictionaryConst.PROPERTY_NAME, meta.getName() );
    metaverseDocument.setProperty( DictionaryConst.PROPERTY_PATH, normalizedPath );
    metaverseDocument.setProperty( DictionaryConst.PROPERTY_NAMESPACE, namespace.getNamespaceId() );

    return metaverseDocument;
  }

  /**
   * Returns the {@link TransMeta} file name.
   *
   * @param transMeta the {@link TransMeta} whose file name is being returnes
   * @return the {@link TransMeta} file name
   */
  public static String getFilename( TransMeta transMeta ) {
    if ( transMeta != null ) {
      String filename = transMeta.getFilename();
      if ( filename == null ) {
        filename = transMeta.getPathAndName();
        if ( transMeta.getDefaultExtension() != null ) {
          filename = filename + "." + transMeta.getDefaultExtension();
        }
      }
      return filename;
    }
    return null;
  }

  public static String getFilename( JobMeta jobMeta ) {
    if ( jobMeta != null ) {
      String filename = jobMeta.getFilename();
      if ( filename == null ) {
        filename = jobMeta.getName();
        if ( jobMeta.getDefaultExtension() != null ) {
          filename = filename + "." + jobMeta.getDefaultExtension();
        }
      }
      return filename;
    }
    return null;
  }

  /**
   * Analyzes in {@link StepAnalyzer} that is sub-transformation aware}
   *
   * @param analyzer  the {@link StepAnalyzer}
   * @param transMeta the step's parent {@link TransMeta}
   * @param meta      the step {@link ISubTransAwareMeta}
   * @param rootNode  the {@link IMetaverseNode}
   * @return the {@link IMetaverseNode} representing the sub-transformation
   * @throws MetaverseAnalyzerException
   */
  public static IMetaverseNode analyze( final StepAnalyzer analyzer, final TransMeta transMeta,
                                        final ISubTransAwareMeta meta,
                                        final IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {

    final TransMeta subTransMeta = getSubTransMeta( meta );
    subTransMeta.copyVariablesFrom( transMeta );
    final String subTransMetaPath = getSubTransMetaPath( meta, subTransMeta );
    subTransMeta.setFilename( subTransMetaPath );
    rootNode.setProperty( "subTransformation", subTransMetaPath );
    rootNode.setProperty( DictionaryConst.PROPERTY_PATH, subTransMetaPath );

    final IMetaverseNode subTransNode = analyzer.getNode( subTransMeta.getName(), DictionaryConst.NODE_TYPE_TRANS,
      analyzer.getDocumentDescriptor().getNamespace(), null, null );
    subTransNode.setProperty( DictionaryConst.PROPERTY_NAMESPACE,
      analyzer.getDocumentDescriptor().getNamespace().getNamespaceId() );
    subTransNode.setProperty( DictionaryConst.PROPERTY_PATH, subTransMetaPath );
    subTransNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    analyzer.getMetaverseBuilder().addLink( rootNode, DictionaryConst.LINK_EXECUTES, subTransNode );

    // pull in the sub-job lineage only if the consolidateSubGraphs flag is set to true
    if ( consolidateSubGraphs() ) {
      final IDocument subTransDocument = buildDocument( analyzer.getMetaverseBuilder(), subTransMeta,
        subTransMetaPath, analyzer.getDocumentDescriptor().getNamespace() );
      if ( subTransDocument != null ) {
        final IComponentDescriptor subtransDocumentDescriptor = new MetaverseComponentDescriptor(
          subTransDocument.getStringID(), DictionaryConst.NODE_TYPE_TRANS,
          analyzer.getDocumentDescriptor().getNamespace(),
          analyzer.getDescriptor().getContext() );

        // analyze the sub-transformation
        return analyzer.getDocumentAnalyzer().analyze( subtransDocumentDescriptor, subTransMeta, subTransNode,
          subTransMetaPath );
      }
    }
    return null;
  }

  public static boolean consolidateSubGraphs() {
    final IMetaverseConfig config = PentahoSystem.get( IMetaverseConfig.class );
    // return true by default (if config is null)
    return config == null || config.getConsolidateSubGraphs();
  }

  public static boolean safeStringMatch( String s1, String s2 ) {
    return ( s1 != null && s1.equals( s2 ) ) || ( s1 == null && s2 == null );
  }

  public static boolean safeListMatch( List l1, List l2 ) {
    return ( l1 != null && l2 != null && l1.containsAll( l2 ) && l2.containsAll( l1 ) ) || ( l1 == null && l2 == null );
  }
}
