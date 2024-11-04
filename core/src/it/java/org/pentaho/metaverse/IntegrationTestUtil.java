/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse;

import com.tinkerpop.blueprints.Graph;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metaverse.api.IDocumentLocator;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.messages.Messages;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
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
      pentahoObjectFactory.init( solutionPath,PentahoSystem.getApplicationContext() );
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

  public static void registerKettlePlugin( final String metaClassName, final String pluginId,
                                           final String pluginCategory, final String stepName )
    throws KettlePluginException {

    final Map<Class<?>, String> classMap = new HashMap<Class<?>, String>( 1 );
    classMap.put( StepMetaInterface.class, metaClassName );

    Plugin plugin = new Plugin( new String[]{ pluginId }, StepPluginType.class, StepMetaInterface.class, pluginCategory,
      stepName, null, null, false, false, classMap, Collections.emptyList(), null, null );
    PluginRegistry.getInstance().registerPlugin( StepPluginType.class, plugin );
  }

  private static void registerKettlePlugins() throws KettlePluginException {
    // TODO: uncomment below once https://jira.pentaho.com/browse/ENGOPS-4612 is resolved
    //registerKettlePlugin( MetaInjectMeta.class.getName(), "MetaInject", "Flow", "ETL metadata injection" );
    //registerKettlePlugin( CheckSumMeta.class.getName(), "CheckSum", "Transform", "Add a checksum" );
    //registerKettlePlugin( JmsConsumerMeta.class.getName(), "Jms2Consumer", "Streaming", "JMS Consumer" );
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

    MetaverseCompletionService mcs = MetaverseCompletionService.getInstance();
    // Run a scan for each locator
    for ( IDocumentLocator locator : locators ) {
      locator.startScan();
    }
    mcs.waitTillEmpty();

    return reader.getMetaverse();
  }

  public static synchronized Graph buildMetaverseGraph() throws Exception {
    return buildMetaverseGraph( PentahoSystem.get( IDocumentLocatorProvider.class ) );
  }

}
