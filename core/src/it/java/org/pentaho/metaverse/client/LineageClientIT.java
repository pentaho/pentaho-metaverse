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

package org.pentaho.metaverse.client;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.IntegrationTestUtil;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.StepFieldOperations;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operations;
import org.pentaho.metaverse.graph.LineageGraphMap;
import org.pentaho.metaverse.impl.DocumentController;
import org.pentaho.metaverse.locator.FileSystemLocator;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
  @Test
  public void testGetOriginSteps() throws Exception {
    Set<StepField> originStepsSet;
    Map<String, Set<StepField>> originSteps;

    originSteps = client.getOriginSteps( transMeta, "Select values", Arrays.asList( "HELLO" ) );
    assertNotNull( originSteps );
    assertEquals( 1, originSteps.size() );
    originStepsSet = originSteps.get( "HELLO" );
    assertNotNull( originStepsSet );
    assertEquals( 2, originStepsSet.size() );
    // We're not sure which step will be in which order, but both fields are named COUNTRY
    for ( StepField stepField : originStepsSet ) {
      assertEquals( "COUNTRY", stepField.getFieldName() );
    }

    originSteps = client.getOriginSteps( transMeta, "Passthru", Arrays.asList( "COUNTRY", "COUNTRY_1" ) );
    assertNotNull( originSteps );
    assertEquals( 2, originSteps.size() );
    for ( Set<StepField> originStepsSetValues : originSteps.values() ) {
      assertNotNull( originStepsSetValues );
      assertEquals( 2, originStepsSetValues.size() );
      // We're not sure which step will be in which order, but both fields are named COUNTRY
      for ( StepField stepField : originStepsSetValues ) {
        assertEquals( "COUNTRY", stepField.getFieldName() );
      }
    }

    originSteps = client.getOriginSteps( transMeta, "Select values", Arrays.asList( "COUNTRY", "COUNTRY_1", "HELLO" ) );
    assertNotNull( originSteps );
    assertEquals( 1, originSteps.size() );
    for ( String stepName : originSteps.keySet() ) {
      // Only HELLO will return values
      if ( !"HELLO".equals( stepName ) ) {
        assertEquals( 0, originSteps.get( stepName ).size() );

      } else {
        Set<StepField> originStepsSetValues = originSteps.get( stepName );
        assertNotNull( originStepsSetValues );
        assertEquals( 2, originStepsSetValues.size() );
        // We're not sure which step will be in which order, but both fields are named COUNTRY
        for ( StepField stepField : originStepsSetValues ) {
          assertEquals( "COUNTRY", stepField.getFieldName() );
        }
      }
    }
  }
}
