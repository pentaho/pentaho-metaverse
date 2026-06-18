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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseReader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BlueprintsGraphMetaverseReaderTest {

  private Graph graph;

  public static final String OUTPUT_FOLDER = "target/outputfiles/";

  @BeforeClass
  public static void beforeClass() {
    File f = new File( OUTPUT_FOLDER );
    if ( !f.exists() ) {
      f.mkdirs();
    }
  }

  @Before
  public void init() {
    graph = TinkerGraph.open();
    loadGraph( graph );
  }

  @Test
  public void testGraphMetaverseReader() throws Exception {
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    Graph metaverse = metaverseReader.getMetaverse();
    assertNotNull( metaverse );

    GraphMLWriter writerXml = new GraphMLWriter();
    writerXml.outputGraph( metaverse, new FileOutputStream( OUTPUT_FOLDER + "testGraphMetaverseReader.graphml" ) );

    GraphSONWriter writerJson = new GraphSONWriter();
    writerJson.outputGraph( metaverse, new FileOutputStream( OUTPUT_FOLDER + "testGraphMetaverseReader.graphjson" ) );

    assertEquals( 30, countVertices( metaverse ) );
    assertEquals( 35, countEdges( metaverse ) );
  }

  @Test
  public void testExportXml() throws Exception {
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    String export = metaverseReader.exportToXml();
    assertNotNull( export );
    assertEquals( 0, export.indexOf( "<?xml" ) );
    assertTrue( export.contains( "data.txt" ) );
    assertTrue( export.contains( "IP Addr" ) );
    assertTrue( export.contains( "City" ) );
    assertTrue( export.contains( "Sales" ) );
    assertTrue( export.contains( "trans1.ktr" ) );
    assertTrue( export.contains( "color" ) );
    assertTrue( export.contains( DictionaryConst.COLOR_DOCUMENT ) );

    export = metaverseReader.exportFormat( IMetaverseReader.FORMAT_XML );
    assertNotNull( export );
    assertEquals( 0, export.indexOf( "<?xml" ) );
    assertTrue( export.contains( "data.txt" ) );
    assertTrue( export.contains( "IP Addr" ) );
    assertTrue( export.contains( "City" ) );
    assertTrue( export.contains( "Sales" ) );
    assertTrue( export.contains( "trans1.ktr" ) );
  }

  @Test
  public void testExportJson() throws Exception {
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    String export = metaverseReader.exportFormat( IMetaverseReader.FORMAT_JSON );
    assertNotNull( export );
    assertEquals( 0, export.indexOf( "{" ) );
    assertTrue( export.contains( "data.txt" ) );
    assertTrue( export.contains( "IP Addr" ) );
    assertTrue( export.contains( "City" ) );
    assertTrue( export.contains( "Sales" ) );
    assertTrue( export.contains( "trans1.ktr" ) );
    assertTrue( export.contains( DictionaryConst.COLOR_DOCUMENT ) );
  }

  @Test
  public void testExportCsv() throws Exception {
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    String export = metaverseReader.exportFormat( IMetaverseReader.FORMAT_CSV );
    assertNotNull( export );
    assertEquals( 0, export.indexOf( "\"SourceId\",\"SourceVirtual\"" ) );
    assertTrue( export.contains( "data.txt" ) );
    assertTrue( export.contains( "IP Addr" ) );
    assertTrue( export.contains( "City" ) );
    assertTrue( export.contains( "Sales" ) );
    assertTrue( export.contains( "trans1.ktr" ) );
  }

  @Test
  public void testFindNode() throws Exception {
    IMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    IMetaverseNode node = metaverseReader.findNode( "data.txt" );
    assertNotNull( node );
    assertEquals( "data.txt", node.getStringID() );
    assertEquals( DictionaryConst.NODE_TYPE_FILE, node.getType() );
    assertEquals( "Text file: data.txt", node.getName() );
    assertNull( node.getProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED ) );

    assertNull( metaverseReader.findNode( "bogus" ) );

    node = metaverseReader.findNode( "trans1.ktr" );
    assertNotNull( node );
    assertEquals( "trans1.ktr", node.getStringID() );
    assertEquals( DictionaryConst.NODE_TYPE_TRANS, node.getType() );
    assertEquals( "Transformation", node.getProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED ) );
    assertEquals( "Document", node.getProperty( DictionaryConst.PROPERTY_CATEGORY_LOCALIZED ) );
  }

  @Test
  public void testFindNodes() throws Exception {
    IMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    List<IMetaverseNode> nodes =
      metaverseReader.findNodes( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_DATA_COLUMN );
    assertNotNull( nodes );
    assertEquals( 7, nodes.size() );
    Set<String> ids = new HashSet<String>();
    for ( IMetaverseNode node : nodes ) {
      ids.add( node.getStringID() );
    }
    assertTrue( ids.contains( "datasource1.table1.field1" ) );
    assertTrue( ids.contains( "datasource1.table1.field2" ) );
    assertTrue( ids.contains( "datasource1.table1.field3" ) );
    assertTrue( ids.contains( "datasource1.table2.field1" ) );
    assertTrue( ids.contains( "datasource1.table2.field2" ) );
    assertTrue( ids.contains( "datasource1.table2.field3" ) );
    assertTrue( ids.contains( "datasource1.table2.field4" ) );

    nodes = metaverseReader.findNodes( DictionaryConst.PROPERTY_NAME, "Transformation: trans1.ktr" );
    assertNotNull( nodes );
    assertEquals( 1, nodes.size() );
    assertEquals( "Transformation: trans1.ktr", nodes.get( 0 ).getProperty( DictionaryConst.PROPERTY_NAME ) );
  }

  @Test
  public void testGetGraph() throws Exception {
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );
    Graph subgraph = metaverseReader.getGraph( "datasource1.table1.field1" );
    assertNotNull( subgraph );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( graph, new FileOutputStream( OUTPUT_FOLDER + "testGetGraph.graphml" ) );

    assertNull( metaverseReader.getGraph( "bogus" ) );
  }

  @Test
  public void testFindLink() throws Exception {
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    IMetaverseLink link =
      metaverseReader.findLink( "datasource1.table1.field1", "populates", "trans2.ktr;field1", Direction.OUT );
    assertNotNull( link );
    assertEquals( "populates", link.getLabel() );
    assertEquals( "datasource1.table1.field1", link.getFromNode().getStringID() );
    assertEquals( "trans2.ktr;field1", link.getToNode().getStringID() );

    assertNull( metaverseReader.findLink( "bogus", "populates", "trans2.ktr;field1", Direction.OUT ) );
    assertNull( metaverseReader.findLink( "datasource1.table1.field1", "bogus", "trans2.ktr;field1", Direction.OUT ) );
    assertNull( metaverseReader.findLink( "datasource1.table1.field1", "populates", "bogus", Direction.OUT ) );
    assertNull( metaverseReader.findLink( "datasource1.table1.field1", "populates", "trans2.ktr;field1", Direction.IN ) );
    assertNull( metaverseReader.findLink( "job2.kjb", "populates", "trans2.ktr;field1", Direction.OUT ) );

    link = metaverseReader.findLink( "trans2.ktr;field1", "populates", "datasource1.table1.field1", Direction.IN );
    assertNotNull( link );
    assertEquals( "populates", link.getLabel() );
    assertEquals( "datasource1.table1.field1", link.getFromNode().getStringID() );
    assertEquals( "trans2.ktr;field1", link.getToNode().getStringID() );
    assertEquals( "Populates", link.getProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED ) );
  }

  @Test
  public void testSearch() throws Exception {
    BlueprintsGraphMetaverseReader metaverseReader = new BlueprintsGraphMetaverseReader( graph );

    List<String> types = new ArrayList<String>();
    types.add( DictionaryConst.NODE_TYPE_TRANS );
    List<String> ids = new ArrayList<String>();
    ids.add( "datasource1.table1.field1" );
    Graph subgraph = metaverseReader.search( types, ids, true );
    assertNotNull( subgraph );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( subgraph, new FileOutputStream( OUTPUT_FOLDER + "testSearch1.graphml" ) );

    GraphSONWriter writerJson = new GraphSONWriter();
    writerJson.outputGraph( subgraph, new FileOutputStream( OUTPUT_FOLDER + "testSearch1.json" ) );

    assertEquals( 9, countVertices( subgraph ) );
    assertEquals( 8, countEdges( subgraph ) );

    types = new ArrayList<String>();
    types.add( DictionaryConst.NODE_TYPE_FILE );
    types.add( DictionaryConst.NODE_TYPE_DATA_TABLE );
    ids = new ArrayList<String>();
    ids.add( "datasource1.table1.field1" );
    subgraph = metaverseReader.search( types, ids, true );
    assertNotNull( subgraph );

    writer.outputGraph( subgraph, new FileOutputStream( OUTPUT_FOLDER + "testSearch2.graphml" ) );
    writerJson.outputGraph( subgraph, new FileOutputStream( OUTPUT_FOLDER + "testSearch2.json" ) );

    assertEquals( 8, countVertices( subgraph ) );
    assertEquals( 7, countEdges( subgraph ) );

    types = new ArrayList<String>();
    ids = new ArrayList<String>();
    ids.add( "datasource1.table2.field1" );
    subgraph = metaverseReader.search( types, ids, false );
    assertNotNull( subgraph );

    writer.outputGraph( subgraph, new FileOutputStream( OUTPUT_FOLDER + "testSearch3.graphml" ) );
    writerJson.outputGraph( subgraph, new FileOutputStream( OUTPUT_FOLDER + "testSearch3.json" ) );

    assertEquals( 14, countVertices( subgraph ) );
    assertEquals( 15, countEdges( subgraph ) );
  }

  private int countEdges( Graph graph ) {
    Iterator<Edge> edges = graph.edges();
    int edgeCount = 0;
    while ( edges.hasNext() ) {
      edgeCount++;
      edges.next();
    }
    return edgeCount;
  }

  private int countVertices( Graph graph ) {
    Iterator<Vertex> vertices = graph.vertices();
    int vertexCount = 0;
    while ( vertices.hasNext() ) {
      vertexCount++;
      vertices.next();
    }
    return vertexCount;
  }

  private void loadGraph( Graph graph ) {
    Vertex textFile = createVertex( "data.txt", DictionaryConst.NODE_TYPE_FILE, "Text file: data.txt" );
    Vertex textField1 = createVertex( "data.txt;field1", DictionaryConst.NODE_TYPE_FILE_FIELD, "Text field: IP Addr" );
    Vertex textField2 = createVertex( "data.txt;field2", DictionaryConst.NODE_TYPE_FILE_FIELD, "Text field: Product" );
    Vertex trans1 = createVertex( "trans1.ktr", DictionaryConst.NODE_TYPE_TRANS, "Transformation: trans1.ktr" );
    Vertex trans1Field1 = createVertex( "trans1.ktr;field1", DictionaryConst.NODE_TYPE_TRANS_FIELD, "Trans field: IP Addr" );
    Vertex trans1Field2 = createVertex( "trans1.ktr;field2", DictionaryConst.NODE_TYPE_TRANS_FIELD, "Trans field: Product" );
    Vertex trans1Field3 = createVertex( "trans1.ktr;field3", DictionaryConst.NODE_TYPE_TRANS_FIELD, "Trans field: City" );
    Vertex trans1Step1 = createVertex( "trans1.ktr;TextFileInput", DictionaryConst.NODE_TYPE_TRANS_STEP, "Step: Read file" );
    Vertex trans1Step2 = createVertex( "trans1.ktr;Calc", DictionaryConst.NODE_TYPE_TRANS_STEP, "Step: Calc city" );
    Vertex trans1Step3 = createVertex( "trans1.ktr;TableOutputStep", DictionaryConst.NODE_TYPE_TRANS_STEP, "Step: Write temp table" );
    Vertex datasource1 = createVertex( "datasource1", DictionaryConst.NODE_TYPE_DATASOURCE, "Datasource: Postgres staging" );
    Vertex table1 = createVertex( "datasource1.table1", DictionaryConst.NODE_TYPE_DATA_TABLE, "Table: temp table" );
    Vertex table1field1 = createVertex( "datasource1.table1.field1", DictionaryConst.NODE_TYPE_DATA_COLUMN, "Table field: Tmp IP Addr" );
    Vertex table1field2 = createVertex( "datasource1.table1.field2", DictionaryConst.NODE_TYPE_DATA_COLUMN, "Table field: Tmp Product" );
    Vertex table1field3 = createVertex( "datasource1.table1.field3", DictionaryConst.NODE_TYPE_DATA_COLUMN, "Table field: Tmp City" );
    Vertex trans2 = createVertex( "trans2.ktr", DictionaryConst.NODE_TYPE_TRANS, "Transformation: trans2.ktr" );
    Vertex trans2Field1 = createVertex( "trans2.ktr;field1", DictionaryConst.NODE_TYPE_TRANS_FIELD, "Trans field: IP Addr" );
    Vertex trans2Field2 = createVertex( "trans2.ktr;field2", DictionaryConst.NODE_TYPE_TRANS_FIELD, "Trans field: Product" );
    Vertex trans2Field3 = createVertex( "trans2.ktr;field3", DictionaryConst.NODE_TYPE_TRANS_FIELD, "Trans field: City" );
    Vertex trans2Field4 = createVertex( "trans2.ktr;fiel4", DictionaryConst.NODE_TYPE_TRANS_FIELD, "Transfield: Sales" );
    Vertex trans2Step1 = createVertex( "trans2.ktr;TableInputStep", DictionaryConst.NODE_TYPE_TRANS_STEP, "Step: Table input step" );
    Vertex trans2Step2 = createVertex( "trans2.ktr;Javascript", DictionaryConst.NODE_TYPE_TRANS_STEP, "Step: calc sales" );
    Vertex trans2Step3 = createVertex( "trans2.ktr;TableOuptutStep", DictionaryConst.NODE_TYPE_TRANS_STEP, "Step: Write facttable" );
    Vertex table2 = createVertex( "datasource1.table2", DictionaryConst.NODE_TYPE_DATA_TABLE, "Table: fact table" );
    Vertex job1 = createVertex( "job1.kjb", DictionaryConst.NODE_TYPE_JOB, "Job: job1.kjb" );
    Vertex table2field1 = createVertex( "datasource1.table2.field1", DictionaryConst.NODE_TYPE_DATA_COLUMN, "Table field: Ip Addr" );
    Vertex table2field2 = createVertex( "datasource1.table2.field2", DictionaryConst.NODE_TYPE_DATA_COLUMN, "Table field: Product" );
    Vertex table2field3 = createVertex( "datasource1.table2.field3", DictionaryConst.NODE_TYPE_DATA_COLUMN, "Table field: City" );
    Vertex table2field4 = createVertex( "datasource1.table2.field4", DictionaryConst.NODE_TYPE_DATA_COLUMN, "Table field: Sales" );
    createVertex( "job2.kjb", DictionaryConst.NODE_TYPE_JOB, "Job: job2.kjb" );

    addEdge( job1, trans1, DictionaryConst.LINK_EXECUTES );
    addEdge( job1, trans2, DictionaryConst.LINK_EXECUTES );
    addEdge( trans1, trans1Step1, DictionaryConst.LINK_EXECUTES );
    addEdge( trans1, trans1Step2, DictionaryConst.LINK_EXECUTES );
    addEdge( trans1, trans1Step3, DictionaryConst.LINK_EXECUTES );
    addEdge( textFile, trans1Step1, DictionaryConst.LINK_READBY );
    addEdge( textFile, textField1, DictionaryConst.LINK_CONTAINS );
    addEdge( textFile, textField2, DictionaryConst.LINK_CONTAINS );
    addEdge( textField1, trans1Field1, DictionaryConst.LINK_POPULATES );
    addEdge( textField2, trans1Field2, DictionaryConst.LINK_POPULATES );
    addEdge( trans1Step3, table1, DictionaryConst.LINK_WRITESTO );
    addEdge( datasource1, table1, DictionaryConst.LINK_CONTAINS );
    addEdge( table1, table1field1, DictionaryConst.LINK_CONTAINS );
    addEdge( table1, table1field2, DictionaryConst.LINK_CONTAINS );
    addEdge( table1, table1field3, DictionaryConst.LINK_CONTAINS );
    addEdge( trans1Field1, table1field1, DictionaryConst.LINK_POPULATES );
    addEdge( trans1Field2, table1field2, DictionaryConst.LINK_POPULATES );
    addEdge( trans1Field3, table1field3, DictionaryConst.LINK_POPULATES );

    addEdge( trans2, trans2Step1, DictionaryConst.LINK_EXECUTES );
    addEdge( trans2, trans2Step2, DictionaryConst.LINK_EXECUTES );
    addEdge( trans2, trans2Step3, DictionaryConst.LINK_EXECUTES );
    addEdge( table1, trans2Step1, DictionaryConst.LINK_READBY );
    addEdge( table1field1, trans2Field1, DictionaryConst.LINK_POPULATES );
    addEdge( table1field2, trans2Field2, DictionaryConst.LINK_POPULATES );
    addEdge( table1field3, trans2Field3, DictionaryConst.LINK_POPULATES );
    addEdge( trans2Step3, table2, DictionaryConst.LINK_WRITESTO );
    addEdge( datasource1, table2, DictionaryConst.LINK_CONTAINS );
    addEdge( table2, table2field1, DictionaryConst.LINK_CONTAINS );
    addEdge( table2, table2field2, DictionaryConst.LINK_CONTAINS );
    addEdge( table2, table2field3, DictionaryConst.LINK_CONTAINS );
    addEdge( table2, table2field4, DictionaryConst.LINK_CONTAINS );
    addEdge( trans2Field1, table2field1, DictionaryConst.LINK_POPULATES );
    addEdge( trans2Field2, table2field2, DictionaryConst.LINK_POPULATES );
    addEdge( trans2Field3, table2field3, DictionaryConst.LINK_POPULATES );
    addEdge( trans2Field4, table2field4, DictionaryConst.LINK_POPULATES );
  }

  private void addEdge( Vertex from, Vertex to, String label ) {
    from.addEdge( label, to );
  }

  private Vertex createVertex( String id, String type, String name ) {
    Vertex vertex = graph.addVertex( T.id, id );
    vertex.property( DictionaryConst.PROPERTY_TYPE, type );
    vertex.property( DictionaryConst.PROPERTY_NAME, name );
    return vertex;
  }
}
