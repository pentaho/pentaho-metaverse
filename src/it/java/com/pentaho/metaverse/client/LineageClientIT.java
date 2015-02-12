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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    MetaverseUtil.setDocumentController( docController );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  @Before
  public void setUp() throws Exception {
    transMeta = new TransMeta( MERGE_JOIN_KTR_FILENAME );
    IDocument doc = MetaverseUtil.createDocument(
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

    List<StepField> creatorSteps = client.getCreatorSteps( transMeta, "Select values", "COUNTRY_1" );
    assertNotNull( creatorSteps );
    assertEquals( 1, creatorSteps.size() );
    assertEquals( "Merge Join", creatorSteps.get( 0 ).getStepName() );

    creatorSteps = client.getCreatorSteps( transMeta, "Merge Join", "COUNTRY_1" );
    assertNotNull( creatorSteps );
    assertEquals( 1, creatorSteps.size() );
    assertEquals( "Merge Join", creatorSteps.get( 0 ).getStepName() );

    creatorSteps = client.getCreatorSteps( transMeta, "Select values", "COUNTRY" );
    assertNotNull( creatorSteps );
    assertEquals( 2, creatorSteps.size() );
    List<String> stepNames = new ArrayList<String>( 2 );
    for ( StepField stepField : creatorSteps ) {
      stepNames.add( stepField.getStepName() );
    }
    assertTrue( stepNames.contains( "Data Grid" ) );
    assertTrue( stepNames.contains( "Table input" ) );

    creatorSteps = client.getCreatorSteps( transMeta, "Merge Join", "COUNTRY" );
    assertNotNull( creatorSteps );
    assertEquals( 2, creatorSteps.size() );
    stepNames = new ArrayList<String>( 2 );
    for ( StepField stepField : creatorSteps ) {
      stepNames.add( stepField.getStepName() );
    }
    assertTrue( stepNames.contains( "Data Grid" ) );
    assertTrue( stepNames.contains( "Table input" ) );

    creatorSteps = client.getCreatorSteps( transMeta, "Select values", new String[]{ "COUNTRY", "COUNTRY_1", "HELLO" } );
    assertNotNull( creatorSteps );
    assertEquals( 4, creatorSteps.size() );
    stepNames = new ArrayList<String>( 4 );
    for ( StepField stepField : creatorSteps ) {
      stepNames.add( stepField.getStepName() );
    }
    assertTrue( stepNames.contains( "Data Grid" ) );
    assertTrue( stepNames.contains( "Table input" ) );
    assertTrue( stepNames.contains( "Merge Join" ) );
    assertTrue( stepNames.contains( "Select values" ) );

    // Test non-API version that takes a String filename for the transformation
    creatorSteps = client.getCreatorSteps( MERGE_JOIN_KTR_FILENAME, "Select values", "COUNTRY_1" );
    assertNotNull( creatorSteps );
    assertEquals( 1, creatorSteps.size() );
    assertEquals( "Merge Join", creatorSteps.get( 0 ).getStepName() );
  }

  @Test
  public void testGetOriginSteps() throws Exception {
    Set<StepFieldTarget> originSteps = client.getOriginSteps( transMeta, "Select values", "COUNTRY_1" );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( StepFieldTarget stepField : originSteps ) {
      assertEquals( "COUNTRY", stepField.getFieldName() );
    }

    originSteps = client.getOriginSteps( transMeta, "Merge Join", "COUNTRY_1" );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( StepFieldTarget stepField : originSteps ) {
      assertEquals( "COUNTRY", stepField.getFieldName() );
    }

    originSteps = client.getOriginSteps( transMeta, "Select values", "COUNTRY" );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( StepFieldTarget stepField : originSteps ) {
      assertEquals( "COUNTRY", stepField.getFieldName() );
    }

    originSteps = client.getOriginSteps( transMeta, "Select values", "HELLO" );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( StepFieldTarget stepField : originSteps ) {
      assertEquals( "COUNTRY", stepField.getFieldName() );
    }

    originSteps = client.getOriginSteps( transMeta, "Select values", new String[]{ "COUNTRY", "COUNTRY_1", "HELLO" } );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( StepFieldTarget stepField : originSteps ) {
      assertEquals( "COUNTRY", stepField.getFieldName() );
    }
  }

}
