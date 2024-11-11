/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.graph;

import com.tinkerpop.blueprints.Graph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.IntegrationTestUtil;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * User: RFellows Date: 8/19/14
 */
public class MetaverseReaderIT {

  private static IMetaverseReader reader;
  private static Graph graph;

  @BeforeClass
  public static void init() throws Exception {
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution/system/pentahoObjects.spring.xml" );
    MetaverseUtil.setDocumentController( PentahoSystem.get( IDocumentController.class ) );
    graph = IntegrationTestUtil.buildMetaverseGraph();
    reader = PentahoSystem.get( IMetaverseReader.class );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  @Test
  public void testSearch() throws Exception {
    List<String> whatWeAreLookingFor = new ArrayList<String>( Arrays.asList( DictionaryConst.NODE_TYPE_TRANS ) );
    List<IMetaverseNode> nodes = reader.findNodes( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_DATA_COLUMN );
    List<String> startingPoint = new ArrayList<String>( Arrays.asList( nodes.get( 0 ).getStringID() ) );

    System.out.println( "Looking for Transformations that write/read Database Column " + nodes.get( 0 ).getName() );

    Graph g = reader.search( whatWeAreLookingFor, startingPoint, false );

    // make sure the Transformation node is in the resulting graph
    assertNotNull( g.getVertices( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS ) );
    // make sure the starting point node is in the resulting graph
    assertNotNull( g.getVertex( nodes.get( 0 ).getStringID() ) );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( g, new FileOutputStream( IntegrationTestUtil.getOutputPath( "searchOut.graphml" ) ) );
  }
}
