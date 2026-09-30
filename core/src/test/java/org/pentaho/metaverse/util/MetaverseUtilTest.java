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


package org.pentaho.metaverse.util;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseTransientNode;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IClonableDocumentAnalyzer;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentAnalyzer;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.IRequiresMetaverseBuilder;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.model.BaseSynchronizedGraph;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;
import org.pentaho.metaverse.graph.LineageGraphMap;
import org.pentaho.metaverse.impl.MetaverseBuilder;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class MetaverseUtilTest {

  private IDocumentController originalController;

  @Before
  public void setUp() throws Exception {
    originalController = MetaverseUtil.documentController;
    MetaverseUtil.setDocumentController( MetaverseTestUtils.getDocumentController() );
  }

  @After
  public void tearDown() {
    MetaverseUtil.setDocumentController( originalController );
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull( new MetaverseUtil() );
  }

  @Test
  public void testGetDocumentController() throws Exception {
    IDocumentController documentController = MetaverseUtil.getDocumentController();
    assertNotNull( documentController );
    MetaverseUtil.documentController = null;
    assertNull( MetaverseUtil.getDocumentController() );

    // Generate an exception
    MetaverseBeanUtil instance = MetaverseBeanUtil.getInstance();
    assertNull( MetaverseUtil.getDocumentController() );
  }

  @Test
  public void testCreateDocument() throws Exception {
    Object content = new Object();
    INamespace namespace = mock( INamespace.class );
    IDocument document = MetaverseUtil.createDocument(
      namespace,
      content,
      "myID",
      "myName",
      "myExtension",
      "application/text" );

    assertEquals( document.getNamespace(), namespace );
    assertEquals( document.getContent(), content );
    assertEquals( "myID", document.getStringID() );
    assertEquals( "myName", document.getName() );
    assertEquals( "application/text", document.getMimeType() );

  }

  @Test( expected = MetaverseException.class )
  public void testAddLineageGraphNullDocument() throws Exception {
    MetaverseUtil.addLineageGraph( null, null );
  }

  @Test
  public void testAddLineageGraph() throws Exception {

    IDocument document = mock( IDocument.class );
    when( document.getName() ).thenReturn( "myDoc" );
    Object content = new Object();
    when( document.getContent() ).thenReturn( content );

    IDocumentController documentController =
      mock( IDocumentController.class, withSettings().extraInterfaces( IRequiresMetaverseBuilder.class ) );
    List<IDocumentAnalyzer> analyzers = new ArrayList<>();
    when( documentController.getDocumentAnalyzers( Mockito.anyString() ) ).thenReturn( analyzers );

    MetaverseUtil.documentController = documentController;

    // Empty analyzer set
    MetaverseUtil.addLineageGraph( document, null );

    IDocumentAnalyzer<IMetaverseNode> documentAnalyzer = mock( IDocumentAnalyzer.class );
    when( documentAnalyzer.analyze(
      Mockito.any( IComponentDescriptor.class ), Mockito.any( IDocument.class ) ) )
      .thenReturn( mock( IMetaverseNode.class ) );
    analyzers.add( documentAnalyzer );

    Graph graph = TinkerGraph.open();
    MetaverseUtil.addLineageGraph( document, graph );

    MetaverseUtil.addLineageGraph( document, null );
  }

  @Test
  public void testAddLineageGraphSharesSynchronizedGraph() throws Exception {
    assertSharedLineageGraph( TinkerGraph.open() );
    assertSharedLineageGraph( new BaseSynchronizedGraph( TinkerGraph.open() ) );
    assertSharedLineageGraph( null );
  }

  private void assertSharedLineageGraph( Graph inputGraph ) throws Exception {
    IDocument document = mock( IDocument.class );
    when( document.getContent() ).thenReturn( new Object() );
    IDocumentController documentController = mock( IDocumentController.class );
    IClonableDocumentAnalyzer<?> analyzer = mock( IClonableDocumentAnalyzer.class );
    IClonableDocumentAnalyzer<?> clonedAnalyzer = mock( IClonableDocumentAnalyzer.class );
    when( analyzer.cloneAnalyzer() ).thenReturn( clonedAnalyzer );
    when( documentController.getDocumentAnalyzers( Mockito.anyString() ) )
      .thenReturn( List.of( analyzer ) );
    MetaverseUtil.documentController = documentController;

    MetaverseUtil.addLineageGraph( document, inputGraph );

    Graph result = LineageGraphMap.getInstance().remove( document.getContent() ).get( 10, TimeUnit.SECONDS );
    ArgumentCaptor<IMetaverseBuilder> builder = ArgumentCaptor.forClass( IMetaverseBuilder.class );
    verify( clonedAnalyzer ).setMetaverseBuilder( builder.capture() );
    verify( documentController, never() ).setMetaverseBuilder( Mockito.any() );
    Graph builderGraph = builder.getValue().getGraph();
    assertTrue( builderGraph instanceof BaseSynchronizedGraph );
    if ( inputGraph instanceof BaseSynchronizedGraph ) {
      assertSame( inputGraph, builderGraph );
    }
    assertSame( builderGraph, result );
  }

  @Test
  public void testOverlappingClonedAnalyzersKeepRequestBuilders() throws Exception {
    IDocumentController controller = mock( IDocumentController.class );
    IClonableDocumentAnalyzer<?> analyzer = mock( IClonableDocumentAnalyzer.class );
    IClonableDocumentAnalyzer<?> firstAnalyzer = mock( IClonableDocumentAnalyzer.class );
    IClonableDocumentAnalyzer<?> secondAnalyzer = mock( IClonableDocumentAnalyzer.class );
    when( analyzer.cloneAnalyzer() ).thenReturn( firstAnalyzer, secondAnalyzer );
    when( controller.getDocumentAnalyzers( "ktr" ) ).thenReturn( List.of( analyzer ) );
    MetaverseUtil.setDocumentController( controller );
    IMetaverseBuilder originalBuilder = new MetaverseBuilder();
    AtomicReference<IMetaverseBuilder> firstBuilder = stubBuilderBinding( firstAnalyzer, originalBuilder );
    AtomicReference<IMetaverseBuilder> secondBuilder = stubBuilderBinding( secondAnalyzer, originalBuilder );
    CountDownLatch firstEntered = new CountDownLatch( 1 );
    CountDownLatch releaseFirst = new CountDownLatch( 1 );
    when( firstAnalyzer.analyze( Mockito.any( IComponentDescriptor.class ), Mockito.any( IDocument.class ) ) )
      .thenAnswer( invocation -> {
        firstEntered.countDown();
        assertTrue( releaseFirst.await( 10, TimeUnit.SECONDS ) );
        firstBuilder.get().addNode( new MetaverseTransientNode( "first" ) );
        return null;
      } );
    when( secondAnalyzer.analyze( Mockito.any( IComponentDescriptor.class ), Mockito.any( IDocument.class ) ) )
      .thenAnswer( invocation -> {
        secondBuilder.get().addNode( new MetaverseTransientNode( "second" ) );
        return null;
      } );
    IDocument firstDocument = lineageDocument( "first" );
    IDocument secondDocument = lineageDocument( "second" );
    Future<Graph> firstResult = null;
    Future<Graph> secondResult = null;
    try {
      MetaverseUtil.addLineageGraph( firstDocument, null );
      firstResult = LineageGraphMap.getInstance().remove( firstDocument.getContent() );
      assertTrue( firstEntered.await( 10, TimeUnit.SECONDS ) );
      MetaverseUtil.addLineageGraph( secondDocument, null );
      secondResult = LineageGraphMap.getInstance().remove( secondDocument.getContent() );
      Graph secondGraph = secondResult.get( 10, TimeUnit.SECONDS );
      releaseFirst.countDown();
      Graph firstGraph = firstResult.get( 10, TimeUnit.SECONDS );
      assertTrue( firstGraph.vertices( "first" ).hasNext() );
      assertTrue( secondGraph.vertices( "second" ).hasNext() );
      assertEquals( 1, firstGraph.traversal().V().count().next().longValue() );
      assertEquals( 1, secondGraph.traversal().V().count().next().longValue() );
      verify( controller, never() ).setMetaverseBuilder( Mockito.any() );
    } finally {
      releaseFirst.countDown();
      if ( firstResult != null ) {
        firstResult.get( 10, TimeUnit.SECONDS );
      }
      if ( secondResult != null ) {
        secondResult.get( 10, TimeUnit.SECONDS );
      }
    }
  }

  @Test
  public void testSharedAnalyzerRestoresBuilderAfterFailure() throws Exception {
    IDocumentController controller = mock( IDocumentController.class );
    IDocumentAnalyzer<?> analyzer = mock( IDocumentAnalyzer.class );
    when( controller.getDocumentAnalyzers( "ktr" ) ).thenReturn( List.of( analyzer ) );
    MetaverseUtil.setDocumentController( controller );
    IMetaverseBuilder originalBuilder = new MetaverseBuilder();
    AtomicReference<IMetaverseBuilder> binding = stubBuilderBinding( analyzer, originalBuilder );
    Graph graph = new BaseSynchronizedGraph( TinkerGraph.open() );
    IllegalStateException expected = new IllegalStateException( "analysis failed" );
    when( analyzer.analyze( Mockito.any( IComponentDescriptor.class ), Mockito.any( IDocument.class ) ) )
      .thenAnswer( invocation -> {
        assertSame( graph, binding.get().getGraph() );
        throw expected;
      } );
    IDocument document = lineageDocument( "failed" );
    MetaverseUtil.addLineageGraph( document, graph );
    try {
      LineageGraphMap.getInstance().remove( document.getContent() ).get( 10, TimeUnit.SECONDS );
      fail( "Expected analysis failure" );
    } catch ( ExecutionException exception ) {
      assertSame( expected, exception.getCause() );
    }
    assertSame( originalBuilder, binding.get() );
  }

  @Test
  public void testNonClonableAnalyzerTasksAreSerialized() throws Exception {
    assertSharedAnalyzerTasksAreSerialized( mock( IDocumentAnalyzer.class ) );
  }

  @Test
  public void testSelfReturningCloneTasksAreSerialized() throws Exception {
    IClonableDocumentAnalyzer<?> analyzer = mock( IClonableDocumentAnalyzer.class );
    when( analyzer.cloneAnalyzer() ).thenReturn( analyzer );
    assertSharedAnalyzerTasksAreSerialized( analyzer );
  }

  private void assertSharedAnalyzerTasksAreSerialized( IDocumentAnalyzer<?> analyzer ) throws Exception {
    IDocumentController controller = mock( IDocumentController.class );
    when( controller.getDocumentAnalyzers( "ktr" ) ).thenReturn( List.of( analyzer ) );
    MetaverseUtil.setDocumentController( controller );
    IMetaverseBuilder originalBuilder = new MetaverseBuilder();
    AtomicReference<IMetaverseBuilder> binding = stubBuilderBinding( analyzer, originalBuilder );
    IDocument firstDocument = lineageDocument( "first" );
    IDocument secondDocument = lineageDocument( "second" );
    CountDownLatch firstEntered = new CountDownLatch( 1 );
    CountDownLatch releaseFirst = new CountDownLatch( 1 );
    AtomicReference<Thread> firstThread = new AtomicReference<>();
    when( analyzer.analyze( Mockito.any( IComponentDescriptor.class ), Mockito.any( IDocument.class ) ) )
      .thenAnswer( invocation -> {
        IDocument document = invocation.getArgument( 1 );
        if ( document == firstDocument ) {
          firstThread.set( Thread.currentThread() );
          firstEntered.countDown();
          assertTrue( releaseFirst.await( 30, TimeUnit.SECONDS ) );
        }
        binding.get().addNode( new MetaverseTransientNode( document.getName() ) );
        return null;
      } );
    Future<Graph> firstResult = null;
    Future<Graph> secondResult = null;
    try {
      MetaverseUtil.addLineageGraph( firstDocument, null );
      firstResult = LineageGraphMap.getInstance().remove( firstDocument.getContent() );
      assertTrue( firstEntered.await( 10, TimeUnit.SECONDS ) );
      MetaverseUtil.addLineageGraph( secondDocument, null );
      secondResult = LineageGraphMap.getInstance().remove( secondDocument.getContent() );
      assertBlockedByAnalyzer( firstThread.get(), secondResult );
      releaseFirst.countDown();
      Graph firstGraph = firstResult.get( 10, TimeUnit.SECONDS );
      Graph secondGraph = secondResult.get( 10, TimeUnit.SECONDS );
      assertTrue( firstGraph.vertices( "first" ).hasNext() );
      assertTrue( secondGraph.vertices( "second" ).hasNext() );
      assertEquals( 1, firstGraph.traversal().V().count().next().longValue() );
      assertEquals( 1, secondGraph.traversal().V().count().next().longValue() );
      assertSame( originalBuilder, binding.get() );
      verify( controller, never() ).setMetaverseBuilder( Mockito.any() );
    } finally {
      releaseFirst.countDown();
      if ( firstResult != null ) {
        firstResult.get( 10, TimeUnit.SECONDS );
      }
      if ( secondResult != null ) {
        secondResult.get( 10, TimeUnit.SECONDS );
      }
    }
  }

  private void assertBlockedByAnalyzer( Thread owner, Future<Graph> waitingTask ) throws InterruptedException {
    ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos( 10 );
    while ( !waitingTask.isDone() && System.nanoTime() < deadline ) {
      for ( ThreadInfo info : threads.dumpAllThreads( false, false ) ) {
        if ( info.getThreadState() == Thread.State.BLOCKED && info.getLockOwnerId() == owner.getId() ) {
          return;
        }
      }
      LockSupport.parkNanos( TimeUnit.MILLISECONDS.toNanos( 1 ) );
      if ( Thread.interrupted() ) {
        throw new InterruptedException();
      }
    }
    fail( "second task did not wait for the shared analyzer" );
  }

  private AtomicReference<IMetaverseBuilder> stubBuilderBinding( IDocumentAnalyzer<?> analyzer,
                                                                 IMetaverseBuilder initialBuilder ) {
    AtomicReference<IMetaverseBuilder> binding = new AtomicReference<>( initialBuilder );
    doAnswer( invocation -> {
      binding.set( invocation.getArgument( 0 ) );
      return null;
    } ).when( analyzer ).setMetaverseBuilder( Mockito.any() );
    when( analyzer.getMetaverseBuilder() ).thenAnswer( invocation -> binding.get() );
    return binding;
  }

  private IDocument lineageDocument( String name ) {
    IDocument document = mock( IDocument.class );
    when( document.getName() ).thenReturn( name );
    when( document.getContent() ).thenReturn( new Object() );
    return document;
  }

  @Test
  public void testEnhanceEdge() {
    Graph graph = TinkerGraph.open();
    Vertex v1 = graph.addVertex( T.id, 1 );
    Vertex v2 = graph.addVertex( T.id, 2 );
    Edge edge = v1.addEdge( "testLabel", v2, T.id, 3 );
    MetaverseUtil.enhanceEdge( edge );
  }

  @Test
  public void testEnhanceVertex() {
    Graph graph = TinkerGraph.open();
    Vertex v1 = graph.addVertex( T.id, 1 );
    MetaverseUtil.enhanceVertex( v1 );
  }

  @Test
  public void testConvertOperationsStringToMap() {
    // Test null string
    assertNull( MetaverseUtil.convertOperationsStringToMap( null ) );
    assertNull( MetaverseUtil.convertOperationsStringToMap( "" ) );
    assertNull( MetaverseUtil.convertOperationsStringToMap( "{" ) );
    assertNotNull( MetaverseUtil.convertOperationsStringToMap( "{}" ) );
    Operations ops = MetaverseUtil.convertOperationsStringToMap(
      "{\"metadataOperations\":[{\"category\":\"changeMetadata\",\"class\":"
        + "\"Operation\",\"description\":\"name\","
        + "\"name\":\"modified\",\"type\":\"METADATA\"}]}"
    );
    assertNotNull( ops );
    assertNull( ops.get( ChangeType.DATA ) );
    List<IOperation> metadataOps = ops.get( ChangeType.METADATA );
    assertNotNull( metadataOps );
    assertEquals( 1, metadataOps.size() );
    IOperation op = metadataOps.get( 0 );
    assertEquals( Operation.METADATA_CATEGORY, op.getCategory() );
    assertEquals( DictionaryConst.PROPERTY_MODIFIED, op.getName() );
    assertEquals( "name", op.getDescription() );

  }
}
