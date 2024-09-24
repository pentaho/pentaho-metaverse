/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.impl;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.IntegrationTestUtil;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.util.MetaverseUtil;
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
