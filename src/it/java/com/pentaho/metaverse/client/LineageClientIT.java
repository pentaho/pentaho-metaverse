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

package com.pentaho.metaverse.client;

import com.google.common.collect.Multimap;
import com.pentaho.metaverse.IntegrationTestUtil;
import com.pentaho.metaverse.api.IDocumentController;
import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.impl.DocumentController;
import com.pentaho.metaverse.impl.Namespace;
import com.pentaho.metaverse.locator.FileSystemLocator;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.IDocument;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class LineageClientIT {

  private static final String ROOT_FOLDER = "src/it/resources/repo/validation";

  private static final String MERGE_JOIN_KTR_FILENAME = ROOT_FOLDER + "/merge_join.ktr";

  private static final LineageClient client = new LineageClient();

  private static DocumentController docController;

  private TransMeta transMeta = null;

  @BeforeClass
  public static void init() throws Exception {
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution/system/pentahoObjects.spring.xml" );

    // we only care about the demo folder
    FileSystemLocator fileSystemLocator = PentahoSystem.get( FileSystemLocator.class );
    IDocumentLocatorProvider provider = PentahoSystem.get( IDocumentLocatorProvider.class );
    // remove the original locator so we can set the modified one back on it
    provider.removeDocumentLocator( fileSystemLocator );
    fileSystemLocator.setRootFolder( ROOT_FOLDER );
    provider.addDocumentLocator( fileSystemLocator );
    docController = (DocumentController) PentahoSystem.get( IDocumentController.class );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  @Before
  public void setUp() throws Exception {
    transMeta = new TransMeta( MERGE_JOIN_KTR_FILENAME );
    IDocument doc = MetaverseUtil.createDocument(
      docController.getMetaverseObjectFactory(),
      new Namespace( "SPOON" ),
      transMeta,
      transMeta.getFilename(),
      transMeta.getName(),
      "ktr",
      URLConnection.getFileNameMap().getContentTypeFor( transMeta.getFilename() ) );
    Graph graph = new TinkerGraph();
    MetaverseUtil.addLineageGraph( doc, graph );
  }

  @Test
  public void testGetCreatorSteps() throws Exception {

    List<String> creatorSteps = client.getCreatorSteps( transMeta, "Dummy (do nothing)", "COUNTRY_1" );
    assertNotNull( creatorSteps );
    assertEquals( 1, creatorSteps.size() );
    assertEquals( "Merge Join", creatorSteps.get( 0 ) );

    creatorSteps = client.getCreatorSteps( transMeta, "Merge Join", "COUNTRY_1" );
    assertNotNull( creatorSteps );
    assertEquals( 1, creatorSteps.size() );
    assertEquals( "Merge Join", creatorSteps.get( 0 ) );

    creatorSteps = client.getCreatorSteps( transMeta, "Dummy (do nothing)", "COUNTRY" );
    assertNotNull( creatorSteps );
    assertEquals( 2, creatorSteps.size() );
    assertTrue( creatorSteps.contains( "Data Grid" ) );
    assertTrue( creatorSteps.contains( "Table input" ) );

    creatorSteps = client.getCreatorSteps( transMeta, "Merge Join", "COUNTRY" );
    assertNotNull( creatorSteps );
    assertEquals( 2, creatorSteps.size() );
    assertTrue( creatorSteps.contains( "Data Grid" ) );
    assertTrue( creatorSteps.contains( "Table input" ) );

    // Test non-API version that takes a String filename for the transformation
    creatorSteps = client.getCreatorSteps( MERGE_JOIN_KTR_FILENAME, "Dummy (do nothing)", "COUNTRY_1" );
    assertNotNull( creatorSteps );
    assertEquals( 1, creatorSteps.size() );
    assertEquals( "Merge Join", creatorSteps.get( 0 ) );
  }

  @Test
  public void testGetOriginSteps() throws Exception {
    Multimap<String, String> originSteps = client.getOriginSteps( transMeta, "Dummy (do nothing)", "COUNTRY_1" );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( Map.Entry<String, String> stepField : originSteps.entries() ) {
      assertEquals( "COUNTRY", stepField.getKey() );
    }

    originSteps = client.getOriginSteps( transMeta, "Merge Join", "COUNTRY_1" );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( Map.Entry<String, String> stepField : originSteps.entries() ) {
      assertEquals( "COUNTRY", stepField.getKey() );
    }

    originSteps = client.getOriginSteps( transMeta, "Dummy (do nothing)", "COUNTRY" );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( Map.Entry<String, String> stepField : originSteps.entries() ) {
      assertEquals( "COUNTRY", stepField.getKey() );
    }
  }

}
