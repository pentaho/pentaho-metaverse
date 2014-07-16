package com.pentaho.metaverse.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.pentaho.metaverse.api.IMetaverseReader;
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

    Iterator<Vertex> vertices = graph.getVertices().iterator();
    int vertexCount = 0;
    while ( vertices.hasNext() ) {
      vertexCount++;
      vertices.next();
    }
    assertEquals( "Vertex count is wrong", 29, vertexCount );

    Iterator<Edge> edges = graph.getEdges().iterator();
    int edgeCount = 0;
    while ( edges.hasNext() ) {
      edgeCount++;
      edges.next();
    }
    assertEquals( "Edge count is wrong", 42, edgeCount );
  }

  @Test
  public void testFindNode() throws Exception {

    IMetaverseReader metaverseReader = new TestTinkerGraphMetaverseReader();

    IMetaverseNode node = metaverseReader.findNode( "data.txt" );

    assertNotNull( "Node is null", node );
    assertEquals( "Id is wrong", "data.txt", node.getStringID() );
    assertEquals( "Type is wrong", "file", node.getType() );
    assertEquals( "Name is wrong", "Text file: data.txt", node.getName() );

  }

  @Test
  public void testGetGraph() throws Exception {
    TestTinkerGraphMetaverseReader metaverseReader = new TestTinkerGraphMetaverseReader();
    Graph g = metaverseReader.getGraph( "datasource1.table1.field1" );
    assertNotNull( "Node is null", g );

    metaverseReader.dumpGraph( g, "testGetGraph.graphml" );

  }

}
