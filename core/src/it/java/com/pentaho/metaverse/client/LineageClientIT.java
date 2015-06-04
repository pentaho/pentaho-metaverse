/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.IntegrationTestUtil;
import com.pentaho.metaverse.api.ChangeType;
import com.pentaho.metaverse.api.IDocumentController;
import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.StepFieldOperations;
import com.pentaho.metaverse.api.model.IOperation;
import com.pentaho.metaverse.api.model.Operations;
import com.pentaho.metaverse.graph.LineageGraphMap;
import com.pentaho.metaverse.impl.DocumentController;
import com.pentaho.metaverse.api.Namespace;
import com.pentaho.metaverse.locator.FileSystemLocator;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;
import com.pentaho.metaverse.api.IDocument;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

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
  public void testGetTargetFields() throws Exception {

    Future<Graph> fg = LineageGraphMap.getInstance().get( transMeta );
    assertNotNull( fg );
    Graph g = fg.get();
    assertNotNull( g );
    List<Vertex> targetFieldNodes =
      client.getTargetFields( g, "Passthru", Arrays.asList( "COUNTRY_1" ) );
    assertNotNull( targetFieldNodes );
    assertEquals( 1, targetFieldNodes.size() );
    Vertex targetStep = targetFieldNodes.get( 0 );
    assertNotNull( targetStep );
    assertEquals( "COUNTRY_1", targetStep.getProperty( DictionaryConst.PROPERTY_NAME ) );

    targetFieldNodes = client.getTargetFields( g, "Select values", Arrays.asList( "COUNTRY_1" ) );
    assertNotNull( targetFieldNodes );
    assertEquals( 0, targetFieldNodes.size() );

    targetFieldNodes = client.getTargetFields( g, "Select values", Arrays.asList( "HELLO" ) );
    assertNotNull( targetFieldNodes );
    assertEquals( 1, targetFieldNodes.size() );

    targetFieldNodes = client.getTargetFields( g, "Passthru", Arrays.asList( "COUNTRY", "COUNTRY_1", "HELLO" ) );
    assertNotNull( targetFieldNodes );
    assertEquals( 2, targetFieldNodes.size() );
  }

  @Test
  public void testGetOperationPaths() throws Exception {
    Map<String, Set<List<StepFieldOperations>>> operationPathMap =
      client.getOperationPaths( transMeta, "Select values", Arrays.asList( "HELLO" ) );

    assertNotNull( operationPathMap );
    assertEquals( 1, operationPathMap.size() );
    assertNull( operationPathMap.get( "COUNTRY" ) );
    Set<List<StepFieldOperations>> operationPaths = operationPathMap.get( "HELLO" );
    assertNotNull( operationPaths );
    assertEquals( 2, operationPaths.size() );
    for ( List<StepFieldOperations> operationPath : operationPaths ) {
      // Should be 6 nodes along one path and 4 along the other
      int pathLength = operationPath.size();
      assertTrue( pathLength == 6 || pathLength == 4 );

      // The end and last nodes should be the same for both paths
      StepFieldOperations last = operationPath.get( pathLength - 1 );
      assertEquals( "Select values", last.getStepName() );
      assertEquals( "HELLO", last.getFieldName() );
      Operations ops = last.getOperations();
      assertNotNull( ops );
      assertEquals( 1, ops.size() );
      List<IOperation> dataOps = ops.get( ChangeType.DATA );
      assertNull( dataOps );
      List<IOperation> metadataOps = ops.get( ChangeType.METADATA );
      assertNotNull( metadataOps );
      assertEquals( 1, metadataOps.size() );
      IOperation metadataOp = metadataOps.get( 0 );
      assertEquals( IOperation.METADATA_CATEGORY, metadataOp.getCategory() );
      assertEquals( DictionaryConst.PROPERTY_MODIFIED, metadataOp.getName() );
      assertEquals( "name", metadataOp.getDescription() );

      StepFieldOperations passthru = operationPath.get( pathLength - 2 );
      assertEquals( "Passthru", passthru.getStepName() );
      assertEquals( "COUNTRY_1", passthru.getFieldName() );
      metadataOps = ops.get( ChangeType.METADATA );
      assertNotNull( metadataOps );
      assertEquals( 1, metadataOps.size() );
      metadataOp = metadataOps.get( 0 );
      assertEquals( IOperation.METADATA_CATEGORY, metadataOp.getCategory() );
      assertEquals( DictionaryConst.PROPERTY_MODIFIED, metadataOp.getName() );

      StepFieldOperations middle = operationPath.get( pathLength - 3 );
      assertEquals( "Merge Join", middle.getStepName() );
      assertEquals( "COUNTRY_1", middle.getFieldName() );
      metadataOps = ops.get( ChangeType.METADATA );
      assertNotNull( metadataOps );
      assertEquals( 1, metadataOps.size() );
      metadataOp = metadataOps.get( 0 );
      assertEquals( IOperation.METADATA_CATEGORY, metadataOp.getCategory() );
      assertEquals( DictionaryConst.PROPERTY_MODIFIED, metadataOp.getName() );
      assertEquals( "name", metadataOp.getDescription() );

      StepFieldOperations first = operationPath.get( 0 );
      assertEquals( "COUNTRY", first.getFieldName() );
      // The step name is either "Table input" or "Data Grid"
      String firstStepName = first.getStepName();
      assertTrue( "Table input".equals( firstStepName ) || "Data Grid".equals( firstStepName ) );

      if ( pathLength == 4 ) {
        assertEquals( "COUNTRY", first.getFieldName() );
        // The step name is either "Table input" or "Data Grid"
        firstStepName = first.getStepName();
        assertTrue( "Table input".equals( firstStepName ) || "Data Grid".equals( firstStepName ) );
      }
    }
  }
}
