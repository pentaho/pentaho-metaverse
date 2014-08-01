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
package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.locator.FileSystemLocator;
import com.tinkerpop.blueprints.Graph;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.metaverse.IDocumentLocator;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import java.util.concurrent.Future;
import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetaverseBuilderIT {

  private MetaverseBuilder builder;
  private Graph graph;
  private Set<IDocumentLocator> locators;
  private IMetaverseReader reader;
  private IDocumentLocatorProvider documentLocatorProvider;

  public static String getSolutionPath() {
    return "src/it/resources/solution";
  }

  @BeforeClass
  public static void init() {

    StandaloneApplicationContext appContext = new StandaloneApplicationContext( getSolutionPath(), "" );
    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    ApplicationContext springApplicationContext = getSpringApplicationContext();

    IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    pentahoObjectFactory.init( null, springApplicationContext );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
    PentahoSystem.init( appContext );
    PentahoSessionHolder.setSession( new StandaloneSession() );

    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  private static ApplicationContext getSpringApplicationContext() {
    GenericApplicationContext ctx = new GenericApplicationContext();
    XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader( ctx );
    File f = new File( getSolutionPath() + "/system/pentahoObjects.spring.xml" );
    if ( f.exists() ) {
      try {
        reader.loadBeanDefinitions( new FileSystemResource( f ) );
      } catch ( Exception e ) {
        System.err.println( e.getMessage() );
        e.printStackTrace();
      }
    }

    String[] beanNames = ctx.getBeanDefinitionNames();
    System.out.println( "Loaded Beans: " ); //$NON-NLS-1$
    for ( String n : beanNames ) {
      System.out.println( "bean: " + n ); //$NON-NLS-1$
    }

    return ctx;
  }

  @Before
  public void setup() {

    builder = (MetaverseBuilder) PentahoSystem.get( IMetaverseBuilder.class );
    documentLocatorProvider = PentahoSystem.get( IDocumentLocatorProvider.class );
    graph = builder.getGraph();
    reader = PentahoSystem.get( IMetaverseReader.class );

  }

  @Test
  public void testBuildingAndReading() throws Exception {
    locators = documentLocatorProvider.getDocumentLocators();

    MetaverseCompletionService mcs = MetaverseCompletionService.getInstance();

    // build it
    for ( IDocumentLocator locator : locators ) {
      locator.startScan();
    }

    mcs.waitTillEmpty();

    Graph readerGraph = reader.getMetaverse();
    assertTrue( readerGraph.getVertices().iterator().hasNext() );
    assertTrue( readerGraph.getEdges().iterator().hasNext() );

    // write out the graph so we can look at it
    File exportFile = new File( "src/it/resources/testGraph.graphml" );
    FileUtils.writeStringToFile( exportFile, reader.exportToXml(), "UTF-8" );
  }

}
