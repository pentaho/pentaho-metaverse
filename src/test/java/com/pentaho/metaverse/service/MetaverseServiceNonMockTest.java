package com.pentaho.metaverse.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.graph.BlueprintsGraphMetaverseReader;
import com.pentaho.metaverse.graph.GraphMLWriter;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class MetaverseServiceNonMockTest {

  private Graph graph;

  private MetaverseService metaverseService;

  @Mock
  private IDocumentLocatorProvider mockProvider;

  @Before
  public void setUp() {
    graph = new TinkerGraph();
    loadGraph( graph );
    IMetaverseReader reader = new BlueprintsGraphMetaverseReader( graph );
    metaverseService = new MetaverseService( reader, mockProvider );
    metaverseService.setDelay( 0 );
  }

  @Test
  public void testGraphMetaverseReader() throws Exception {

    Graph graph = metaverseService.getMetaverse();
    assertNotNull( "Graph is null", graph );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( graph, new FileOutputStream( "testGraphMetaverseReader.graphml" ) );

    assertEquals( "Vertex count is wrong", 30, countVertices( graph ) );
    assertEquals( "Edge count is wrong", 42, countEdges( graph ) );
  }

  @Test
  public void testExportXml() throws Exception {

    String export = metaverseService.exportToXml();
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "<?xml" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );

    export = metaverseService.exportFormat( "XML" );
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "<?xml" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );

    export = metaverseService.exportFormat( "xml" );
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "<?xml" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );
  }

  @Test
  public void testExportJson() throws Exception {

    String export = metaverseService.exportFormat( "JSON" );
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "{" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );

    export = metaverseService.exportFormat( "json" );
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "{" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );
  }

  @Test
  public void testExportStream() throws Exception {

/*
    OutputStream out = new ByteArrayOutputStream();
    try {
      metaverseService.exportToStream( "JSON", out );
    } catch ( IOException e ) {
    } finally {
      try {
        out.close();
      } catch ( IOException e ) {
      }
    }
    String export = out.toString();

    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "{" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );
*/
    String export = metaverseService.exportFormat( "json" );
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "{" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );
  }

  @Test
  public void testExportCsv() throws Exception {

    String export = metaverseService.exportFormat( "CSV" );
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "\"SourceId\",\"SourceVirtual\"" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );

    export = metaverseService.exportFormat( "csv" );
    assertNotNull( "Export is null", export );
    assertTrue( "Export content is wrong", export.indexOf( "\"SourceId\",\"SourceVirtual\"" ) == 0 );
    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );
  }

  @Test
  public void testFindNode() throws Exception {

    IMetaverseNode node = metaverseService.findNode( "data.txt" );

    assertNotNull( "Node is null", node );
    assertEquals( "Id is wrong", "data.txt", node.getStringID() );
    assertEquals( "Type is wrong", DictionaryConst.NODE_TYPE_FILE, node.getType() );
    assertEquals( "Name is wrong", "Text file: data.txt", node.getName() );

    node = metaverseService.findNode( "bogus" );

    assertNull( "Node is not null", node );

  }

  @Test
  public void testFindNodes() throws Exception {

    List<IMetaverseNode> nodes = metaverseService.findNodes( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_DATA_COLUMN );
    assertNotNull( "Node is null", nodes );
    assertEquals( "Node count is wrong", 7, nodes.size() );
    Set<String> ids = new HashSet<String>();
    for ( IMetaverseNode node : nodes ) {
      ids.add( node.getStringID() );
    }
    assertTrue( "Id is missing", ids.contains( "datasource1.table1.field1" ) );
    assertTrue( "Id is missing", ids.contains( "datasource1.table1.field2" ) );
    assertTrue( "Id is missing", ids.contains( "datasource1.table1.field3" ) );
    assertTrue( "Id is missing", ids.contains( "datasource1.table2.field1" ) );
    assertTrue( "Id is missing", ids.contains( "datasource1.table2.field2" ) );
    assertTrue( "Id is missing", ids.contains( "datasource1.table2.field3" ) );
    assertTrue( "Id is missing", ids.contains( "datasource1.table2.field4" ) );

    nodes = metaverseService.findNodes( DictionaryConst.PROPERTY_NAME, "Transformation: trans1.ktr" );
    assertNotNull( "Node is null", nodes );
    assertEquals( "Node count is wrong", 1, nodes.size() );
    assertEquals( "Id is missing", "Transformation: trans1.ktr", nodes.get( 0 ).getProperty( DictionaryConst.PROPERTY_NAME ) );

  }

  @Test
  public void testGetGraph() throws Exception {

    Graph g = metaverseService.getGraph( "datasource1.table1.field1" );
    assertNotNull( "Graph is null", g );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( graph, new FileOutputStream( "testGetGraph.graphml" ) );

    g = metaverseService.getGraph( "bogus" );
    assertNull( "Node is not null", g );

  }

  @Test
  public void testFindLink() throws Exception {

    IMetaverseLink link = metaverseService.findLink( "datasource1.table1.field1", "populates", "trans2.ktr;field1", Direction.OUT );
    assertNotNull( "Link is null", link );
    assertEquals( "Label is wrong", "populates", link.getLabel() );
    assertEquals( "Id is wrong", "datasource1.table1.field1", link.getFromNode().getStringID() );
    assertEquals( "Id is wrong", "trans2.ktr;field1", link.getToNode().getStringID() );

    link = metaverseService.findLink( "bogus", "populates", "trans2.ktr;field1", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseService.findLink( "datasource1.table1.field1", "bogus", "trans2.ktr;field1", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseService.findLink( "datasource1.table1.field1", "populates", "bogus", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseService.findLink( "datasource1.table1.field1", "populates", "trans2.ktr;field1", Direction.IN );
    assertNull( "Link is not null", link );

    link = metaverseService.findLink( "job2.kjb", "populates", "trans2.ktr;field1", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseService.findLink( "trans2.ktr;field1", "populates", "datasource1.table1.field1", Direction.IN );
    assertNotNull( "Link is null", link );
    assertEquals( "Label is wrong", "populates", link.getLabel() );
    assertEquals( "Id is wrong", "datasource1.table1.field1", link.getFromNode().getStringID() );
    assertEquals( "Id is wrong", "trans2.ktr;field1", link.getToNode().getStringID() );

  }

  @Test
  public void testSearch() throws Exception {

    // search for transformations connected to datasource1.table1.field1
    List<String> types = new ArrayList<String>();
    types.add( DictionaryConst.NODE_TYPE_TRANS );
    List<String> ids = new ArrayList<String>();
    ids.add( "datasource1.table1.field1" );
    Graph graph = metaverseService.search( types, ids, true );
    assertNotNull( "Node is null", graph );

    GraphMLWriter writer = new GraphMLWriter();
    writer.outputGraph( graph, new FileOutputStream( "testSearch1.graphml" ) );

    assertEquals( "Vertex count is wrong", 7, countVertices( graph ) );
    assertEquals( "Edge count is wrong", 6, countEdges( graph ) );

    // search for files and tables connected to datasource1.table1.field1
    types = new ArrayList<String>();
    types.add( DictionaryConst.NODE_TYPE_FILE );
    types.add( DictionaryConst.NODE_TYPE_DATA_TABLE );
    ids = new ArrayList<String>();
    ids.add( "datasource1.table1.field1" );
    graph = metaverseService.search( types, ids, true );
    assertNotNull( "Node is null", graph );

    writer.outputGraph( graph, new FileOutputStream( "testSearch2.graphml" ) );

    assertEquals( "Vertex count is wrong", 8, countVertices( graph ) );
    assertEquals( "Edge count is wrong", 7, countEdges( graph ) );

    // search for everything connected to datasource1.table2.field2
    types = new ArrayList<String>();
    ids = new ArrayList<String>();
    ids.add( "datasource1.table2.field1" );
    graph = metaverseService.search( types, ids, false );
    assertNotNull( "Node is null", graph );

    writer.outputGraph( graph, new FileOutputStream( "testSearch3.graphml" ) );

    assertEquals( "Vertex count is wrong", 16, countVertices( graph ) );
    assertEquals( "Edge count is wrong", 21, countEdges( graph ) );

  }

  private int countEdges( Graph graph ) {
    Iterator<Edge> edges = graph.getEdges().iterator();
    int edgeCount = 0;
    while ( edges.hasNext() ) {
      edgeCount++;
      edges.next();
    }
    return edgeCount;
  }

  private int countVertices( Graph graph ) {
    Iterator<Vertex> vertices = graph.getVertices().iterator();
    int vertexCount = 0;
    while ( vertices.hasNext() ) {
      vertexCount++;
      vertices.next();
    }
    return vertexCount;
  }


  // ------- Helper methods -------

  private void loadGraph( Graph graph ) {
    // these are the nodes
    Vertex textFile = createVertex( "data.txt", DictionaryConst.NODE_TYPE_FILE, "Text file: data.txt" );
    Vertex textField1 = createVertex( "data.txt;field1", DictionaryConst.NODE_TYPE_FILE_FIELD, "Text field: IP Addr" );
    Vertex textField2 = createVertex( "data.txt;field2", DictionaryConst.NODE_TYPE_FILE_FIELD, "Text field: Product" );
    Vertex trans1 =  createVertex( "trans1.ktr", DictionaryConst.NODE_TYPE_TRANS, "Transformation: trans1.ktr" );
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
    Vertex trans2 =  createVertex( "trans2.ktr", DictionaryConst.NODE_TYPE_TRANS, "Transformation: trans2.ktr" );
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

    // these are the links
    graph.addEdge( null, job1, trans1, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, job1, trans2, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, trans1, trans1Step1, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, trans1, trans1Step2, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, trans1, trans1Step3, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, textFile, trans1Step1, DictionaryConst.LINK_READBY );
    graph.addEdge( null, textFile, textField1, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, textFile, textField2, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, trans1Step1, trans1Field1, DictionaryConst.LINK_CREATES );
    graph.addEdge( null, trans1Step1, trans1Field2, DictionaryConst.LINK_CREATES );
    graph.addEdge( null, textField1, trans1Field1, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, textField2, trans1Field2, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, trans1Step2, trans1Field3, DictionaryConst.LINK_CREATES );
    graph.addEdge( null, trans1Step3, table1, DictionaryConst.LINK_WRITESTO );
    graph.addEdge( null, datasource1, table1, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, table1, table1field1, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, table1, table1field2, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, table1, table1field3, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, trans1Field1, table1field1, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, trans1Field2, table1field2, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, trans1Field3, table1field3, DictionaryConst.LINK_POPULATES );

    graph.addEdge( null, trans2, trans2Step1, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, trans2, trans2Step2, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, trans2, trans2Step3, DictionaryConst.LINK_EXECUTES );
    graph.addEdge( null, table1, trans2Step1, DictionaryConst.LINK_READBY );
    graph.addEdge( null, trans2Step1, trans2Field1, DictionaryConst.LINK_CREATES );
    graph.addEdge( null, trans2Step1, trans2Field2, DictionaryConst.LINK_CREATES );
    graph.addEdge( null, trans2Step1, trans2Field3, DictionaryConst.LINK_CREATES );
    graph.addEdge( null, table1field1, trans2Field1, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, table1field2, trans2Field2, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, table1field3, trans2Field3, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, trans2Step2, trans2Field4, DictionaryConst.LINK_CREATES );
    graph.addEdge( null, trans2Step3, table2, DictionaryConst.LINK_WRITESTO );
    graph.addEdge( null, datasource1, table2, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field1, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field2, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field3, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, table2, table2field4, DictionaryConst.LINK_CONTAINS );
    graph.addEdge( null, trans2Field1, table2field1, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, trans2Field2, table2field2, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, trans2Field3, table2field3, DictionaryConst.LINK_POPULATES );
    graph.addEdge( null, trans2Field4, table2field4, DictionaryConst.LINK_POPULATES );
  }

  private Vertex createVertex( String id, String type, String name ) {
    Vertex v = graph.addVertex( id );
    v.setProperty( DictionaryConst.PROPERTY_TYPE, type );
    v.setProperty( DictionaryConst.PROPERTY_NAME, name );
    return v;
  }


}
