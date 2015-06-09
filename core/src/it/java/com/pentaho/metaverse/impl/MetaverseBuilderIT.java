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

import org.pentaho.dictionary.DictionaryHelper;
import com.pentaho.metaverse.IntegrationTestUtil;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
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

    assertTrue( readerGraph.getVertices().iterator().hasNext() );
    assertTrue( readerGraph.getEdges().iterator().hasNext() );

    // write out the graph so we can look at it
    File exportFile = new File( IntegrationTestUtil.getOutputPath( "testGraph.graphml" ) );
    FileUtils.writeStringToFile( exportFile, reader.exportToXml(), "UTF-8" );

    File exportJson = new File( IntegrationTestUtil.getOutputPath( "testGraph.graphjson" ) );
    FileUtils.writeStringToFile( exportJson, reader.exportFormat( IMetaverseReader.FORMAT_JSON ), "UTF-8" );

    File exportCsv = new File( IntegrationTestUtil.getOutputPath( "testGraph.csv" ) );
    FileUtils.writeStringToFile( exportCsv, reader.exportFormat( IMetaverseReader.FORMAT_CSV ), "UTF-8" );

  }

  private void testAndCountNodesByType( String type ) {
    int count = 0;
    for ( Vertex v : readerGraph.getVertices( "type", type ) ) {
      count++;
      assertNotNull( v.getId() );
      assertNotNull( v.getProperty( "type" ) );
      assertNotNull( v.getProperty( "name" ) );
    }

    System.out.println( "Found " + count + " " + type + " nodes" );
  }

  private void countTheEdgesByType( String label ) {
    int count = 0;
    for ( Edge e : readerGraph.getEdges( "text", label ) ) {
      count++;
    }
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

    for ( Vertex v : readerGraph.getVertices() ) {
      nodeCount++;
    }
    for ( Edge e : readerGraph.getEdges() ) {
      edgeCount++;
    }

    System.out.println( "\n===== SUMMARY =====" );
    System.out.println( "TOTAL NODES = " + nodeCount );
    System.out.println( "TOTAL EDGES = " + edgeCount );
  }


}
