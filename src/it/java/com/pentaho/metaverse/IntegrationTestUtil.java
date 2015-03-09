/*
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

package com.pentaho.metaverse;

import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.impl.MetaverseCompletionService;
import com.pentaho.metaverse.messages.Messages;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Graph;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.api.metaverse.MetaverseException;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: RFellows Date: 8/19/14
 */
public class IntegrationTestUtil {

  public static final String OUTPUT_FOLDER = "target/outputfiles/";

  public static String getOutputPath( String fileName ) {
    File f = new File( OUTPUT_FOLDER );
    if ( !f.exists() ) {
      f.mkdirs();
    }

    return OUTPUT_FOLDER + fileName;
  }

  public static synchronized void initializePentahoSystem( String solutionPath ) throws Exception {

    // this setting is useful only for running the integration tests from within IntelliJ
    // this same property is set for integration tests via the pom when running with mvn
    String folderPaths = "target/spoon/plugins";
    File f = new File( folderPaths );
    System.setProperty( "KETTLE_PLUGIN_BASE_FOLDERS", f.getAbsolutePath() );

    StandaloneApplicationContext appContext = new StandaloneApplicationContext( solutionPath, "" );
    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    if ( solutionPath == null ) {
      throw new MetaverseException( Messages.getString( "ERROR.MetaverseInit.BadConfigPath", solutionPath ) );
    }

    try {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      Thread.currentThread().setContextClassLoader( MetaverseUtil.class.getClassLoader() );
      IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
      pentahoObjectFactory.init( solutionPath, PentahoSystem.getApplicationContext() );
      PentahoSystem.registerObjectFactory( pentahoObjectFactory );

      // Restore context classloader
      Thread.currentThread().setContextClassLoader( cl );
    } catch ( Exception e ) {
      throw new MetaverseException( Messages.getString( "ERROR.MetaverseInit.CouldNotInit" ), e );
    }
    PentahoSystem.init( appContext );
    PentahoSessionHolder.setSession( new StandaloneSession() );

    registerKettlePlugins();

    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  private static void registerKettlePlugins() throws KettlePluginException {
    Map<Class<?>, String> mongoInputClassMap = new HashMap<Class<?>, String>( 1 );
    mongoInputClassMap.put( StepMetaInterface.class, MongoDbInputMeta.class.getName() );
    List<String> empty = Collections.emptyList();
    Plugin mongodbInputPlugin = new Plugin(
        new String[]{ "MongoDbInput" },
        StepPluginType.class,
        StepMetaInterface.class,
        "Big Data",
        "MongoDB Input",
        null,
        null,
        false,
        false,
        mongoInputClassMap,
        empty,
        null,
        null
    );
    PluginRegistry.getInstance().registerPlugin( StepPluginType.class, mongodbInputPlugin );
  }

  public static void shutdownPentahoSystem() {
    PentahoSystem.refreshSettings();
    PentahoSystem.clearGlobals();
    PentahoSystem.clearObjectFactory();
    PentahoSystem.shutdown();
  }

  public static synchronized Graph buildMetaverseGraph( IDocumentLocatorProvider provider ) throws Exception {
    IDocumentLocatorProvider documentLocatorProvider = provider;
    IMetaverseReader reader = PentahoSystem.get( IMetaverseReader.class );
    Set<IDocumentLocator> locators = documentLocatorProvider.getDocumentLocators();

    long freeMemAtInit = Runtime.getRuntime().freeMemory();
    MetaverseCompletionService mcs = MetaverseCompletionService.getInstance();
    System.out.println( "freeMemAtInit = " + freeMemAtInit );
    // build it
    for ( IDocumentLocator locator : locators ) {
      locator.startScan();
    }
    mcs.waitTillEmpty();
    long freeMemAtEnd = Runtime.getRuntime().freeMemory();
    System.out.println( "MetaverseIT mem usage after waitTillEmpty() = " + ( freeMemAtInit - freeMemAtEnd ) );

    return reader.getMetaverse();
  }

  public static synchronized Graph buildMetaverseGraph() throws Exception {
    return buildMetaverseGraph( PentahoSystem.get( IDocumentLocatorProvider.class ) );
  }

}
