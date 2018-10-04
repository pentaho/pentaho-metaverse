/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse;

import com.tinkerpop.blueprints.Graph;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
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
