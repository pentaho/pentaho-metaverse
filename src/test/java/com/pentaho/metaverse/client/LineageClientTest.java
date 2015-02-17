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

package com.pentaho.metaverse.client;

import com.google.common.collect.Sets;
import com.pentaho.dictionary.DictionaryConst;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.branch.LoopPipe;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LineageClientTest {

  private static final String TEST_FIELD = "testField";
  private LineageClient lineageClient;
  private Graph g;

  @Before
  public void setUp() throws Exception {
    lineageClient = new LineageClient();
    g = new TinkerGraph();
  }

  @Test
  public void testGetCreatorSteps() throws Exception {
    assertTrue( lineageClient.getCreatorSteps( (String) null, null, null ).isEmpty() );
    assertTrue( lineageClient.getCreatorSteps( "testTrans", null, null ).isEmpty() );
    assertTrue( lineageClient.getCreatorSteps( mock( TransMeta.class ), null, null ).isEmpty() );
  }

  @Test
  public void testGetCreatorFields() {

    Vertex step = g.addVertex( "1" );
    step.setProperty( DictionaryConst.PROPERTY_NAME, "testStep" );
    step.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    Vertex field = g.addVertex( "2" );
    field.setProperty( DictionaryConst.PROPERTY_NAME, TEST_FIELD );
    g.addEdge( "3", step, field, DictionaryConst.LINK_CREATES );
    Vertex targetStep = g.addVertex( "4" );
    targetStep.setProperty( DictionaryConst.PROPERTY_NAME, "targetStep" );
    targetStep.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    g.addEdge( "5", step, targetStep, DictionaryConst.LINK_HOPSTO );

    Map<String, Set<Vertex>> creatorFieldsMap = lineageClient.creatorFields( g, "targetStep", Arrays.asList( TEST_FIELD ) );
    assertNotNull( creatorFieldsMap );
    assertEquals( 1, creatorFieldsMap.size() );
    Set<Vertex> creatorFields = creatorFieldsMap.get( TEST_FIELD );
    assertNotNull( creatorFields );
    assertEquals( 1, creatorFields.size() );
    Vertex v = creatorFields.iterator().next();
    assertNotNull( v );
    assertEquals( v.getProperty( DictionaryConst.PROPERTY_NAME ), TEST_FIELD );
  }

  @Test
  public void testGetOriginSteps() throws Exception {
    assertTrue( lineageClient.getOriginSteps( mock( TransMeta.class ), null, null ).isEmpty() );
  }

  @Test
  public void testCreatorSteps() {
    Vertex step = g.addVertex( "1" );
    step.setProperty( DictionaryConst.PROPERTY_NAME, "testStep" );
    step.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    Vertex field = g.addVertex( "2" );
    field.setProperty( DictionaryConst.PROPERTY_NAME, TEST_FIELD );
    g.addEdge( "3", step, field, DictionaryConst.LINK_CREATES );
    Vertex targetStep = g.addVertex( "4" );
    targetStep.setProperty( DictionaryConst.PROPERTY_NAME, "targetStep" );
    targetStep.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    g.addEdge( "5", step, targetStep, DictionaryConst.LINK_HOPSTO );

    Map<String, Set<StepField>> creatorStepsMap = lineageClient.creatorSteps( g, "targetStep", Arrays.asList( TEST_FIELD ) );
    assertNotNull( creatorStepsMap );
    assertEquals( 1, creatorStepsMap.size() );
    Set<StepField> creatorSteps = creatorStepsMap.get( TEST_FIELD );
    assertNotNull( creatorSteps );
    assertEquals( 1, creatorSteps.size() );
    StepField stepField = creatorSteps.iterator().next();
    assertEquals( stepField.getStepName(), "testStep" );
    assertEquals( stepField.getFieldName(), TEST_FIELD );
  }

  @Test
  public void testGetOriginStepsPipe() {
    Vertex step = g.addVertex( "1" );
    step.setProperty( DictionaryConst.PROPERTY_NAME, "testStep" );
    step.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    Vertex field = g.addVertex( "2" );
    field.setProperty( DictionaryConst.PROPERTY_NAME, TEST_FIELD );
    g.addEdge( "3", step, field, DictionaryConst.LINK_CREATES );
    Vertex targetStep = g.addVertex( "4" );
    targetStep.setProperty( DictionaryConst.PROPERTY_NAME, "targetStep" );
    targetStep.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
    g.addEdge( "5", step, targetStep, DictionaryConst.LINK_HOPSTO );
    GremlinPipeline pipe = lineageClient.getOriginStepsPipe( Sets.newHashSet( field ), false );
    assertNotNull( pipe );
    assertNotNull( pipe.toList() );
  }

  @Test
  public void testNumLoops() {
    LoopPipe.LoopBundle bundle = mock( LoopPipe.LoopBundle.class );
    when( bundle.getLoops() ).thenReturn( LineageClient.MAX_LOOPS - 1 );
    LineageClient.NumLoops loopFunc = new LineageClient.NumLoops( LineageClient.MAX_LOOPS );
    assertTrue( loopFunc.compute( bundle ) );
    when( bundle.getLoops() ).thenReturn( LineageClient.MAX_LOOPS );
    assertFalse( loopFunc.compute( bundle ) );
  }

  @Test
  public void testNoNullObjectsInLoop() {
    LoopPipe.LoopBundle bundle = mock( LoopPipe.LoopBundle.class );
    when( bundle.getObject() ).thenReturn( null );
    LineageClient.NoNullObjectsInLoop loopFunc = new LineageClient.NoNullObjectsInLoop();
    assertFalse( loopFunc.compute( bundle ) );
    when( bundle.getObject() ).thenReturn( new Object() );
    assertTrue( loopFunc.compute( bundle ) );
  }

  @Test
  public void testNotNullAndNotDeriviativeLoop() {
    LoopPipe.LoopBundle bundle = mock( LoopPipe.LoopBundle.class );
    when( bundle.getObject() ).thenReturn( null );
    LineageClient.NotNullAndNotDeriviativeLoop loopFunc = new LineageClient.NotNullAndNotDeriviativeLoop();
    assertFalse( loopFunc.compute( bundle ) );
    Vertex v = mock( Vertex.class );
    when( v.getEdges( Direction.IN, DictionaryConst.LINK_DERIVES ) ).thenReturn( new ArrayList<Edge>() );
    when( bundle.getObject() ).thenReturn( v );
    assertTrue( loopFunc.compute( bundle ) );
  }

  @Test
  public void testHasDerivesLinks() {
    Vertex v = mock( Vertex.class );
    LineageClient.HasDerivesLink loopFunc = new LineageClient.HasDerivesLink();
    when( v.getVertices( Direction.IN, DictionaryConst.LINK_DERIVES ) ).thenReturn( new ArrayList<Vertex>() );
    assertFalse( loopFunc.compute( v ) );
    when( v.getVertices( Direction.IN, DictionaryConst.LINK_DERIVES ) ).thenReturn( Arrays.asList( mock( Vertex.class ) ) );
    assertTrue( loopFunc.compute( v ) );
  }

  @Test
  public void testStepFieldPipeFunction() {
    Vertex step = g.addVertex( "1" );
    step.setProperty( DictionaryConst.PROPERTY_NAME, "testStep" );
    Vertex field = g.addVertex( "2" );
    field.setProperty( DictionaryConst.PROPERTY_NAME, TEST_FIELD );
    g.addEdge( "3", step, field, DictionaryConst.LINK_CREATES );
    List<String> creatorSteps = new LineageClient.StepFieldPipeFunction().compute( field );
    assertNotNull( creatorSteps );
    assertEquals( 2, creatorSteps.size() );
    // The list should contain the field name followed by the step name
    assertEquals( TEST_FIELD, creatorSteps.get( 0 ) );
    assertEquals( "testStep", creatorSteps.get( 1 ) );
  }

  @Test
  public void testStepFieldOperationsPipeFunction() {
    Vertex step = g.addVertex( "1" );
    step.setProperty( DictionaryConst.PROPERTY_NAME, "testStep" );
    Vertex field = g.addVertex( "2" );
    field.setProperty( DictionaryConst.PROPERTY_NAME, TEST_FIELD );
    g.addEdge( "3", step, field, DictionaryConst.LINK_CREATES );
    Map<String, String> creatorSteps = new LineageClient.StepFieldOperationsPipeFunction().compute( field );
    assertNotNull( creatorSteps );
    assertEquals( 2, creatorSteps.size() );
    // The list should contain the field name followed by the step name
    assertEquals( TEST_FIELD, creatorSteps.get( "fieldName" ) );
    assertEquals( "testStep", creatorSteps.get( "stepName" ) );

  }

}
