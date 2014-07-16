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
package com.pentaho.metaverse;

import com.pentaho.metaverse.analyzer.kettle.TransformationAnalyzer;
import com.pentaho.metaverse.impl.DocumentController;
import com.pentaho.metaverse.impl.MetaverseBuilder;
import com.pentaho.metaverse.locator.DIRepositoryLocator;
import com.pentaho.metaverse.locator.LocatorTestUtils;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.metaverse.IDocumentAnalyzer;
import org.pentaho.platform.api.metaverse.IDocumentLocator;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MetaverseBuilderIT {

  private DocumentController controller;
  private MetaverseBuilder builder;
  private Graph graph;
  private Set<IDocumentLocator> locators;
  private DIRepositoryLocator diLocator;
  private Set<IDocumentAnalyzer> analyzers;

  private TransformationAnalyzer transAnalyzer;

  @BeforeClass
  public static void init() {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  @Before
  public void before() {
    controller = new DocumentController();
    builder = new MetaverseBuilder();
    graph = new TinkerGraph();
    builder.setGraph( graph );
    locators = new HashSet<>();

    diLocator = new DIRepositoryLocator();
    diLocator.addDocumentListener( controller );
    diLocator.setRepository( LocatorTestUtils.getMockDiRepository() );
    diLocator.setUnifiedRepository( LocatorTestUtils.getMockIUnifiedRepository() );
    locators.add( diLocator );

    analyzers = new HashSet<>();

    transAnalyzer = new TransformationAnalyzer();
    analyzers.add( transAnalyzer );

    controller.setMetaverseObjectFactory( builder );
    controller.setMetaverseBuilder( builder );
    controller.setDocumentAnalyzers( analyzers );
  }

  @Test
  public void testLocatorAndDocumentAnalyzers() throws Exception {

    for( IDocumentLocator locator : locators ) {
      locator.startScan();
      Thread.sleep( 1000 );
    }

    assertTrue( graph.getVertices().iterator().hasNext() );

    // TODO: enable this once we start adding edges
    // assertTrue( graph.getEdges().iterator().hasNext() );

  }

}
