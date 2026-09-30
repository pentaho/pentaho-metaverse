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


package org.pentaho.metaverse.impl;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.metaverse.IntegrationTestUtil;
import org.pentaho.metaverse.analyzer.kettle.TransformationAnalyzer;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.graph.LineageGraphMap;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MetaverseBuilderIT {

  private static IMetaverseReader reader;
  private static Graph readerGraph;
  private int nodeCount = 0;
  private int edgeCount = 0;

  public static String getSolutionPath() {
    return "src/it/resources/solution/system/pentahoObjects.spring.xml";
  }

  @BeforeClass
  public static void init() throws Exception {

    cleanUpSampleData();

    IntegrationTestUtil.initializePentahoSystem( getSolutionPath() );

    IDocumentLocatorProvider provider = PentahoSystem.get( IDocumentLocatorProvider.class );

    MetaverseUtil.setDocumentController( PentahoSystem.get( IDocumentController.class ) );

    // Uncomment below to run integration test against only the "demo" folder
    /*
    FileSystemLocator fileSystemLocator = PentahoSystem.get( FileSystemLocator.class );
    provider.removeDocumentLocator( fileSystemLocator );
    fileSystemLocator.setRootFolder( "src/it/resources/repo/demo" );
    provider.addDocumentLocator( fileSystemLocator );
    */

    reader = PentahoSystem.get( IMetaverseReader.class );
    readerGraph = IntegrationTestUtil.buildMetaverseGraph( provider );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    // drain async lineage analysis before tearing down PentahoSystem; otherwise worker threads
    // keep analyzing against a torn-down system and can race export readers in the same fork JVM
    org.pentaho.metaverse.graph.LineageGraphCompletionService.getInstance().waitTillEmpty();
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  /**
   * clean up any lingering SampleData instances on disk
   */
  private static void cleanUpSampleData() {
    File dir = new File( "." );
    File[] sampleDataFiles = dir.listFiles( new FilenameFilter() {
      @Override
      public boolean accept( File dir, String name ) {
        return name.startsWith( "SampleData" );
      }
    } );

    if ( sampleDataFiles != null ) {
      for ( File f : sampleDataFiles ) {
        f.delete();
      }
    }

  }

  @Before
  public void setup() {
  }

  @Test
  public void testExport() throws Exception {

    assertTrue( readerGraph.vertices().hasNext() );
    assertTrue( readerGraph.edges().hasNext() );

    // write out the graph so we can look at it
    File exportFile = new File( IntegrationTestUtil.getOutputPath( "testGraph.graphml" ) );
    FileUtils.writeStringToFile( exportFile, reader.exportToXml(), "UTF-8" );

    File exportJson = new File( IntegrationTestUtil.getOutputPath( "testGraph.graphjson" ) );
    FileUtils.writeStringToFile( exportJson, reader.exportFormat( IMetaverseReader.FORMAT_JSON ), "UTF-8" );

    File exportCsv = new File( IntegrationTestUtil.getOutputPath( "testGraph.csv" ) );
    FileUtils.writeStringToFile( exportCsv, reader.exportFormat( IMetaverseReader.FORMAT_CSV ), "UTF-8" );

  }

  @Test
  public void testConcurrentTransformationAnalysisKeepsGraphsSeparate() throws Exception {
    IDocumentController originalController = MetaverseUtil.getDocumentController();
    MetaverseBuilder originalBuilder = new MetaverseBuilder();
    DocumentController controller = new DocumentController( originalBuilder );
    TransformationAnalyzer analyzer = new TransformationAnalyzer();
    controller.addAnalyzer( analyzer );
    CountDownLatch bothAnalyzing = new CountDownLatch( 2 );
    TransMeta first = concurrentTransformation( "first", bothAnalyzing );
    TransMeta second = concurrentTransformation( "second", bothAnalyzing );
    List<Future<Graph>> analyses = new ArrayList<>();
    ExecutionException taskFailure = null;
    try {
      MetaverseUtil.setDocumentController( controller );
      for ( TransMeta transformation : new TransMeta[] { first, second } ) {
        IDocument document = MetaverseUtil.createDocument( new Namespace( "concurrent-analysis" ), transformation,
          transformation.getFilename(), transformation.getName(), "ktr", "application/xml" );
        MetaverseUtil.addLineageGraph( document, null );
        Future<Graph> analysis = LineageGraphMap.getInstance().get( transformation );
        assertNotNull( analysis );
        analyses.add( analysis );
      }
      assertTrue( "Both real analyzers must enter before either can write", bothAnalyzing.await( 30,
        TimeUnit.SECONDS ) );
      Graph firstGraph = analyses.get( 0 ).get( 60, TimeUnit.SECONDS );
      Graph secondGraph = analyses.get( 1 ).get( 60, TimeUnit.SECONDS );
      assertTransformationGraph( firstGraph, "first", "second" );
      assertTransformationGraph( secondGraph, "second", "first" );
      assertSame( originalBuilder, controller.getMetaverseBuilder() );
      assertSame( controller, analyzer.getMetaverseBuilder() );
      assertFalse( originalBuilder.getGraph().vertices().hasNext() );
    } finally {
      while ( bothAnalyzing.getCount() > 0 ) {
        bothAnalyzing.countDown();
      }
      try {
        for ( Future<Graph> analysis : analyses ) {
          try {
            analysis.get( 60, TimeUnit.SECONDS ).close();
          } catch ( ExecutionException failure ) {
            if ( taskFailure == null ) {
              taskFailure = failure;
            } else {
              taskFailure.addSuppressed( failure );
            }
          }
        }
      } finally {
        for ( Future<Graph> analysis : analyses ) {
          analysis.cancel( true );
        }
        LineageGraphMap.getInstance().remove( first );
        LineageGraphMap.getInstance().remove( second );
        MetaverseUtil.setDocumentController( originalController );
        originalBuilder.getGraph().close();
      }
    }
    if ( taskFailure != null ) {
      throw taskFailure;
    }
  }

  private TransMeta concurrentTransformation( String name, CountDownLatch bothAnalyzing ) {
    TransMeta transformation = new TransMeta() {
      @Override
      public String getDescription() {
        bothAnalyzing.countDown();
        try {
          assertTrue( "Timed out waiting for overlapping transformation analysis",
            bothAnalyzing.await( 30, TimeUnit.SECONDS ) );
        } catch ( InterruptedException exception ) {
          Thread.currentThread().interrupt();
          throw new AssertionError( "Transformation analysis interrupted", exception );
        }
        return super.getDescription();
      }
    };
    transformation.setName( name );
    transformation.setFilename( name + ".ktr" );
    RowGeneratorMeta generator = new RowGeneratorMeta();
    generator.setDefault();
    generator.allocate( 1 );
    generator.getFieldName()[ 0 ] = name + "_field";
    generator.getFieldType()[ 0 ] = "String";
    generator.getValue()[ 0 ] = name;
    StepMeta source = new StepMeta( "RowGenerator", name + "_source", generator );
    StepMeta sink = new StepMeta( "Dummy", name + "_sink", new DummyTransMeta() );
    transformation.addStep( source );
    transformation.addStep( sink );
    transformation.addTransHop( new TransHopMeta( source, sink ) );
    return transformation;
  }

  private void assertTransformationGraph( Graph graph, String expected, String other ) {
    assertEquals( "Wrong transformation node count for " + expected, 1L,
      graph.traversal().V().has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS )
        .has( DictionaryConst.PROPERTY_NAME, expected ).count().next().longValue() );
    assertEquals( 2L, graph.traversal().V().has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS )
      .has( DictionaryConst.PROPERTY_NAME, expected ).out( DictionaryConst.LINK_CONTAINS )
      .has( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP ).count().next().longValue() );
    for ( String name : new String[] { expected + "_source", expected + "_sink", expected + "_field" } ) {
      assertTrue( "Missing lineage node: " + name,
        graph.traversal().V().has( DictionaryConst.PROPERTY_NAME, name ).hasNext() );
    }
    for ( String name : new String[] { other, other + "_source", other + "_sink", other + "_field" } ) {
      assertFalse( "Lineage leaked from the other transformation: " + name,
        graph.traversal().V().has( DictionaryConst.PROPERTY_NAME, name ).hasNext() );
    }
  }

  private void testAndCountNodesByType( String type ) {
    int count = 0;
    for ( Vertex v : readerGraph.traversal().V().has( "type", type ).toList() ) {
      count++;
      assertNotNull( v.id() );
      assertTrue( v.property( "type" ).isPresent() );
      assertTrue( v.property( "name" ).isPresent() );
    }

    System.out.println( "Found " + count + " " + type + " nodes" );
  }

  private void countTheEdgesByType( String label ) {
    int count = readerGraph.traversal().E().has( "text", label ).count().next().intValue();
    if ( count > 0 ) {
      System.out.println( "Found " + count + " " + label + " links" );
    }
  }

  @Test
  public void testTransformationProperties() throws Exception {
    Set<String> nodeTypes = DictionaryHelper.ENTITY_NODE_TYPES;
    System.out.println( "\n===== ENTITY NODES =====" );
    for ( String nodeType : nodeTypes ) {
      testAndCountNodesByType( nodeType );
    }

    System.out.println( "\n===== DATAFLOW LINKS =====" );
    Set<String> dataflowLinkTypes = DictionaryHelper.DATAFLOW_LINK_TYPES;
    for ( String dataflowLinkType : dataflowLinkTypes ) {
      countTheEdgesByType( dataflowLinkType );
    }

    System.out.println( "\n===== STRUCTURAL LINKS =====" );
    Set<String> structuralLinkTypes = DictionaryHelper.STRUCTURAL_LINK_TYPES;
    for ( String structuralLinkType : structuralLinkTypes ) {
      countTheEdgesByType( structuralLinkType );
    }

    nodeCount = readerGraph.traversal().V().count().next().intValue();
    edgeCount = readerGraph.traversal().E().count().next().intValue();

    System.out.println( "\n===== SUMMARY =====" );
    System.out.println( "TOTAL NODES = " + nodeCount );
    System.out.println( "TOTAL EDGES = " + edgeCount );
  }


}
