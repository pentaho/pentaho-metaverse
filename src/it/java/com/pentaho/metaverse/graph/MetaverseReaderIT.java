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

package com.pentaho.metaverse.graph;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.IntegrationTestUtil;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Graph;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
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
    writer.outputGraph( g, new FileOutputStream( "src/it/resources/searchOut.graphml" ) );
  }
}
