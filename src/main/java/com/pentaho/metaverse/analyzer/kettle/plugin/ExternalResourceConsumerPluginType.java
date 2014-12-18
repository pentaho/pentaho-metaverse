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
package com.pentaho.metaverse.analyzer.kettle.plugin;

import com.pentaho.metaverse.analyzer.kettle.extensionpoints.IExternalResourceConsumer;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.KettleSelectiveParentFirstClassLoader;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@PluginMainClassType( IExternalResourceConsumer.class )
@PluginAnnotationType( ExternalResourceConsumer.class )
public final class ExternalResourceConsumerPluginType extends BasePluginType implements PluginTypeInterface {

  /* Use the parent classloader to load classes from this package when loading an ExternalResourceConsumer plugin */
  private static final String[] PLUGIN_PACKAGE_PATTERNS = {
    "com\\.pentaho\\.metaverse\\.analyzer\\.kettle\\.plugin.*",
  };

  private static final ExternalResourceConsumerPluginType instance = new ExternalResourceConsumerPluginType();

  private ExternalResourceConsumerPluginType() {
    super( ExternalResourceConsumer.class, "EXTERNAL_RESOURCE_CONSUMER", "External Resource Consumer" );
    populateFolders( null );
  }

  public static ExternalResourceConsumerPluginType getInstance() {
    return instance;
  }

  @Override
  protected void registerNatives() throws KettlePluginException {
    // noop
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return ( (ExternalResourceConsumer) annotation ).categoryDescription();
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (ExternalResourceConsumer) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (ExternalResourceConsumer) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (ExternalResourceConsumer) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return ( (ExternalResourceConsumer) annotation ).i18nPackageName();
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return false;
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    // noop -- TODO support XML plugins?
  }

  /**
   * Create a new URL class loader with the jar file specified. Also include all the jar files in the lib folder next to
   * that file. We will also selectively ignore classes in our internal product such that loading those classes will
   * defer to the parent classloader (where they've already been loaded due to the PluginRegistryExtension already
   * having been loaded. This alleviates an issue where the ExternalResourceConsumer annotation class would be loaded
   * both by the parent and the child, and getting the annotation of a child-loaded class would not match the parent's
   * class (as they have different classloaders).
   *
   * @param jarFileUrl  The jar file to include
   * @param classLoader the parent class loader to use
   * @return The URL class loader
   */
  @Override
  protected URLClassLoader createUrlClassLoader( URL jarFileUrl, ClassLoader classLoader ) {
    List<URL> urls = new ArrayList<URL>();

    // Also append all the files in the underlying lib folder if it exists...
    //
    try {
      String libFolderName = new File( URLDecoder.decode( jarFileUrl.getFile(), "UTF-8" ) ).getParent() + "/lib";
      if ( new File( libFolderName ).exists() ) {
        PluginFolder pluginFolder = new PluginFolder( libFolderName, false, true, searchLibDir );
        FileObject[] libFiles = pluginFolder.findJarFiles( true );
        for ( FileObject libFile : libFiles ) {
          urls.add( libFile.getURL() );
        }
      }
    } catch ( Exception e ) {
      LogChannel.GENERAL.logError(
        "Unexpected error searching for jar files in lib/ folder next to '" + jarFileUrl + "'", e );
    }

    urls.add( jarFileUrl );

    return new KettleSelectiveParentFirstClassLoader(
      urls.toArray( new URL[urls.size()] ), classLoader, PLUGIN_PACKAGE_PATTERNS );
  }
}
