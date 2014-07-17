package com.pentaho.metaverse.graph;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.pentaho.metaverse.api.IMetaverseReader;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerGraphMetaverseReaderTest {

  @Before
  public void init() {
    TestTinkerGraphMetaverseReader.filePath = "src/test/resources/graph/test1.graphml";
  }

  @Test
  public void testGraphMetaverseReader() throws Exception {

    TestTinkerGraphMetaverseReader metaverseReader = new TestTinkerGraphMetaverseReader();

    Graph graph = metaverseReader.getMetaverse();
    assertNotNull( "Graph is null", graph );
    metaverseReader.dumpGraph( graph, "testGraphMetaverseReader.graphml" );

    String export = metaverseReader.export();
    assertNotNull( "Export is null", export );

    assertTrue( "Export content is wrong", export.indexOf( "data.txt" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "IP Addr" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "City" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "Sales" ) != -1 );
    assertTrue( "Export content is wrong", export.indexOf( "trans1.ktr" ) != -1 );

    assertEquals( "Vertex count is wrong", 30, countVertices( graph ) );
    assertEquals( "Edge count is wrong", 42, countEdges( graph ) );
  }

  @Test
  public void testFindNode() throws Exception {

    IMetaverseReader metaverseReader = new TestTinkerGraphMetaverseReader();

    IMetaverseNode node = metaverseReader.findNode( "data.txt" );

    assertNotNull( "Node is null", node );
    assertEquals( "Id is wrong", "data.txt", node.getStringID() );
    assertEquals( "Type is wrong", "file", node.getType() );
    assertEquals( "Name is wrong", "Text file: data.txt", node.getName() );

    node = metaverseReader.findNode( "bogus" );

    assertNull( "Node is not null", node );

  }

  @Test
  public void testGetGraph() throws Exception {
    TestTinkerGraphMetaverseReader metaverseReader = new TestTinkerGraphMetaverseReader();
    Graph g = metaverseReader.getGraph( "datasource1.table1.field1" );
    assertNotNull( "Graph is null", g );

    metaverseReader.dumpGraph( g, "testGetGraph.graphml" );

    g = metaverseReader.getGraph( "bogus" );
    assertNull( "Node is not null", g );

  }

  @Test
  public void testFindLink() throws Exception {
    TestTinkerGraphMetaverseReader metaverseReader = new TestTinkerGraphMetaverseReader();

    IMetaverseLink link = metaverseReader.findLink( "datasource1.table1.field1", "populates", "trans2.ktr;field1", Direction.OUT );
    assertNotNull( "Link is null", link );
    assertEquals( "Label is wrong", "populates", link.getLabel() );
    assertEquals( "Id is wrong", "datasource1.table1.field1", link.getFromNode().getStringID() );
    assertEquals( "Id is wrong", "trans2.ktr;field1", link.getToNode().getStringID() );

    link = metaverseReader.findLink( "bogus", "populates", "trans2.ktr;field1", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseReader.findLink( "datasource1.table1.field1", "bogus", "trans2.ktr;field1", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseReader.findLink( "datasource1.table1.field1", "populates", "bogus", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseReader.findLink( "datasource1.table1.field1", "populates", "trans2.ktr;field1", Direction.IN );
    assertNull( "Link is not null", link );

    link = metaverseReader.findLink( "job2.kjb", "populates", "trans2.ktr;field1", Direction.OUT );
    assertNull( "Link is not null", link );

    link = metaverseReader.findLink( "trans2.ktr;field1", "populates", "datasource1.table1.field1", Direction.IN );
    assertNotNull( "Link is null", link );
    assertEquals( "Label is wrong", "populates", link.getLabel() );
    assertEquals( "Id is wrong", "datasource1.table1.field1", link.getFromNode().getStringID() );
    assertEquals( "Id is wrong", "trans2.ktr;field1", link.getToNode().getStringID() );

  }

  @Test
  public void testSearch() throws Exception {
    TestTinkerGraphMetaverseReader metaverseReader = new TestTinkerGraphMetaverseReader();

    List<String> types = new ArrayList<String>();
    types.add( "trans" );
    List<String> ids = new ArrayList<String>();
    ids.add( "datasource1.table1.field1" );
    Graph graph = metaverseReader.search( types, ids );
    assertNotNull( "Node is null", graph );

    metaverseReader.dumpGraph( graph, "testSearch.graphml" );
    assertEquals( "Vertex count is wrong", 8, countVertices( graph ) );
    assertEquals( "Edge count is wrong", 7, countEdges( graph ) );

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

}
