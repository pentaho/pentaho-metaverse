/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.metaverse.client;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.StepFieldOperations;
import org.pentaho.metaverse.graph.LineageGraphMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LineageClient}. Several of these were deleted in 5682e04a1 (2015,
 * "neutralized LineageClient tests until refactor is complete") and are restored here against
 * the current tinkerpop-3 API.
 */
public class LineageClientTest {

  private static final String TEST_FIELD = "testField";
  private LineageClient lineageClient;
  private Graph g;

  @Before
  public void setUp() throws Exception {
    lineageClient = new LineageClient();
    g = TinkerGraph.open();
    LineageGraphMap.getInstance().clear();
  }

  @Test
  public void testGetOriginStepsEmptyResults() throws Exception {
    // a trans meta with no registered lineage graph -> no origins
    TransMeta mockTransMeta = mock( TransMeta.class );
    assertTrue( lineageClient.getOriginSteps( mockTransMeta, null, null ).isEmpty() );
  }

  @Test( expected = MetaverseException.class )
  public void testGetOriginStepsNullTransMeta() throws Exception {
    // LineageGraphMap is a ConcurrentHashMap: null key NPEs, wrapped as MetaverseException
    lineageClient.getOriginSteps( null, null, null );
  }

  @Test
  public void testGetOriginSteps() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );
    when( mockTransMeta.getFilename() ).thenReturn( "testTrans" );
    Future<Graph> future = mock( Future.class );
    when( future.get() ).thenReturn( g );
    LineageGraphMap.getInstance().put( mockTransMeta, future );

    // origin step --outputs--> origin field --derives--> target field (output of target step)
    Vertex originField = addStepField( g, "originStep", TEST_FIELD );
    Vertex targetField = addStepField( g, "targetStep", TEST_FIELD );
    originField.addEdge( DictionaryConst.LINK_DERIVES, targetField );

    Map<String, Set<StepField>> originSteps =
      lineageClient.getOriginSteps( mockTransMeta, "targetStep", List.of( TEST_FIELD ) );
    assertNotNull( originSteps );
    assertEquals( 1, originSteps.size() );
    Set<StepField> origins = originSteps.get( TEST_FIELD );
    assertNotNull( origins );
    assertEquals( 1, origins.size() );
    StepField origin = origins.iterator().next();
    assertEquals( "originStep", origin.getStepName() );
    assertEquals( TEST_FIELD, origin.getFieldName() );
  }

  @Test
  public void testGetOriginStepsFollowsJoinsLinks() throws Exception {
    // joins links are traversed to find origins but do not terminate the walk by themselves;
    // a field with incoming joins but no incoming derives is still recorded as an origin
    TransMeta mockTransMeta = mock( TransMeta.class );
    when( mockTransMeta.getFilename() ).thenReturn( "testTrans" );
    Future<Graph> future = mock( Future.class );
    when( future.get() ).thenReturn( g );
    LineageGraphMap.getInstance().put( mockTransMeta, future );

    Vertex joinOrigin = addStepField( g, "joinOriginStep", TEST_FIELD );
    Vertex derivedMiddle = addStepField( g, "middleStep", TEST_FIELD );
    Vertex targetField = addStepField( g, "targetStep", TEST_FIELD );
    derivedMiddle.addEdge( DictionaryConst.LINK_DERIVES, targetField );
    joinOrigin.addEdge( DictionaryConst.LINK_JOINS, derivedMiddle );

    Map<String, Set<StepField>> originSteps =
      lineageClient.getOriginSteps( mockTransMeta, "targetStep", List.of( TEST_FIELD ) );
    assertNotNull( originSteps );
    Set<StepField> origins = originSteps.get( TEST_FIELD );
    assertNotNull( origins );
    // middle has no in-derives so it is recorded as an origin, and the walk crosses the joins
    // link to reach the join origin as well
    assertEquals( 2, origins.size() );
    Set<String> originStepNames = origins.stream().map( StepField::getStepName ).collect( java.util.stream.Collectors
      .toSet() );
    assertTrue( originStepNames.contains( "middleStep" ) );
    assertTrue( originStepNames.contains( "joinOriginStep" ) );
  }

  @Test( expected = MetaverseException.class )
  public void testGetOriginStepsWithException() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );
    when( mockTransMeta.getFilename() ).thenReturn( "testTrans" );
    Future<Graph> future = mock( Future.class );
    when( future.get() ).thenThrow( new java.util.concurrent.ExecutionException( new Exception( "boom" ) ) );
    LineageGraphMap.getInstance().put( mockTransMeta, future );
    lineageClient.getOriginSteps( mockTransMeta, "targetStep", List.of( TEST_FIELD ) );
  }

  @Test
  public void testGetOperationPaths() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );
    when( mockTransMeta.getFilename() ).thenReturn( "testTrans" );
    Future<Graph> future = mock( Future.class );
    when( future.get() ).thenReturn( g );
    LineageGraphMap.getInstance().put( mockTransMeta, future );

    Vertex originField = addStepField( g, "originStep", TEST_FIELD );
    Vertex targetField = addStepField( g, "targetStep", TEST_FIELD );
    originField.addEdge( DictionaryConst.LINK_DERIVES, targetField );

    Map<String, Set<List<StepFieldOperations>>> operationPaths =
      lineageClient.getOperationPaths( mockTransMeta, "targetStep", List.of( TEST_FIELD ) );
    assertNotNull( operationPaths );
    assertEquals( 1, operationPaths.size() );
    Set<List<StepFieldOperations>> paths = operationPaths.get( TEST_FIELD );
    assertNotNull( paths );
    assertEquals( 1, paths.size() );
    List<StepFieldOperations> path = paths.iterator().next();
    // getOperationPaths builds the path origin-first (each vertex is prepended)
    assertEquals( 2, path.size() );
    assertEquals( TEST_FIELD, path.get( 0 ).getFieldName() );
    assertEquals( "originStep", path.get( 0 ).getStepName() );
    assertEquals( TEST_FIELD, path.get( 1 ).getFieldName() );
    assertEquals( "targetStep", path.get( 1 ).getStepName() );
  }

  @Test( expected = MetaverseException.class )
  public void testGetOperationPathsWithException() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );
    when( mockTransMeta.getFilename() ).thenReturn( "testTrans" );
    Future<Graph> future = mock( Future.class );
    when( future.get() ).thenThrow( new java.util.concurrent.ExecutionException( new Exception( "boom" ) ) );
    LineageGraphMap.getInstance().put( mockTransMeta, future );
    lineageClient.getOperationPaths( mockTransMeta, "targetStep", List.of( TEST_FIELD ) );
  }

  @Test
  public void testGetTargetFields() {
    Vertex targetField = addStepField( g, "targetStep", TEST_FIELD );
    addStepField( g, "targetStep", "otherField" );

    List<Vertex> targetFields = lineageClient.getTargetFields( g, "targetStep", List.of( TEST_FIELD ) );
    assertNotNull( targetFields );
    assertEquals( 1, targetFields.size() );
    assertEquals( targetField.id(), targetFields.get( 0 ).id() );

    // unknown step -> nothing
    assertTrue( lineageClient.getTargetFields( g, "noSuchStep", List.of( TEST_FIELD ) ).isEmpty() );
    // unknown field -> nothing
    assertTrue( lineageClient.getTargetFields( g, "targetStep", List.of( "noSuchField" ) ).isEmpty() );
  }

  private static Vertex addStepField( Graph graph, String stepName, String fieldName ) {
    Vertex step = graph.addVertex();
    step.property( DictionaryConst.PROPERTY_NAME, stepName );
    step.property( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    Vertex field = graph.addVertex();
    field.property( DictionaryConst.PROPERTY_NAME, fieldName );
    field.property( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_FIELD );
    step.addEdge( DictionaryConst.LINK_OUTPUTS, field );
    return field;
  }
}
