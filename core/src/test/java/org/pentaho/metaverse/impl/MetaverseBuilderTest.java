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


package org.pentaho.metaverse.impl;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseLink;
import org.pentaho.dictionary.MetaverseTransientNode;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetaverseBuilderTest {

  private BaseMetaverseBuilder builder;
  private Graph graph;
  private final MetaverseTransientNode node = new MetaverseTransientNode();

  @Before
  public void before() {
    graph = TinkerGraph.open();
    builder = new MetaverseBuilder( graph );

    node.setStringID( "node1" );
    node.setName( "node1 name" );
    node.setType( "test type" );
  }

  @Test
  public void testGetSetGraph() {
    assertEquals( graph, builder.getGraph() );
    builder.setGraph( null );
    assertNull( builder.getGraph() );
  }

  @Test
  public void testGetSetMetaverseObjectFactory() {
    IMetaverseObjectFactory objectFactory = mock( IMetaverseObjectFactory.class );
    builder.setMetaverseObjectFactory( objectFactory );
    assertEquals( objectFactory, builder.getMetaverseObjectFactory() );
  }

  @Test
  public void testAddNode() {
    builder.addNode( node );

    Vertex result = getVertex( node.getStringID() );
    assertNotNull( result );
    assertEquals( node.getStringID(), result.id() );
    assertEquals( node.getType(), getProperty( result, DictionaryConst.PROPERTY_TYPE ) );
    assertFalse( (Boolean) getProperty( result, DictionaryConst.NODE_VIRTUAL ) );
  }

  @Test
  public void testAddNodeThatAlreadyExists() {
    builder.addNode( node );

    Vertex result = getVertex( node.getStringID() );
    assertNotNull( result );
    assertEquals( "node1 name", getProperty( result, DictionaryConst.PROPERTY_NAME ) );
    assertEquals( "test type", getProperty( result, DictionaryConst.PROPERTY_TYPE ) );

    node.setName( "updated name" );
    node.setProperty( "test", "value" );
    builder.addNode( node );

    result = getVertex( node.getStringID() );
    assertEquals( "updated name", getProperty( result, DictionaryConst.PROPERTY_NAME ) );
    assertEquals( "value", getProperty( result, "test" ) );
  }

  @Test
  public void testAddLink() {
    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );
    MetaverseLink link = new MetaverseLink( node, "uses", node2 );

    builder.addLink( link );

    Vertex fromResult = getVertex( node.getStringID() );
    Vertex toResult = getVertex( node2.getStringID() );
    assertTrue( (Boolean) getProperty( fromResult, DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( (Boolean) getProperty( toResult, DictionaryConst.NODE_VIRTUAL ) );

    Iterator<Edge> outEdges = fromResult.edges( Direction.OUT, "uses" );
    assertTrue( outEdges.hasNext() );
    while ( outEdges.hasNext() ) {
      Edge edge = outEdges.next();
      assertEquals( node.getName(), getProperty( edge.outVertex(), DictionaryConst.PROPERTY_NAME ) );
      assertEquals( node2.getName(), getProperty( edge.inVertex(), DictionaryConst.PROPERTY_NAME ) );
      assertTrue( (Boolean) getProperty( edge.outVertex(), DictionaryConst.NODE_VIRTUAL ) );
    }

    Iterator<Edge> inEdges = toResult.edges( Direction.IN, "uses" );
    assertTrue( inEdges.hasNext() );
    while ( inEdges.hasNext() ) {
      Edge edge = inEdges.next();
      assertEquals( node.getName(), getProperty( edge.outVertex(), DictionaryConst.PROPERTY_NAME ) );
      assertEquals( node2.getName(), getProperty( edge.inVertex(), DictionaryConst.PROPERTY_NAME ) );
      assertTrue( (Boolean) getProperty( edge.inVertex(), DictionaryConst.NODE_VIRTUAL ) );
    }
  }

  @Test
  public void testAddLink2() {
    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );

    builder.addLink( node, "uses", node2 );

    Vertex fromResult = getVertex( node.getStringID() );
    Vertex toResult = getVertex( node2.getStringID() );
    assertTrue( (Boolean) getProperty( fromResult, DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( (Boolean) getProperty( toResult, DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( fromResult.edges( Direction.OUT, "uses" ).hasNext() );
    assertTrue( toResult.edges( Direction.IN, "uses" ).hasNext() );
  }

  @Test
  public void testAddLink_OneExistingNode() {
    builder.addNode( node );

    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );
    MetaverseLink link = new MetaverseLink( node, "uses", node2 );

    builder.addLink( link );

    Vertex fromResult = getVertex( node.getStringID() );
    Vertex toResult = getVertex( node2.getStringID() );
    assertFalse( (Boolean) getProperty( fromResult, DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( (Boolean) getProperty( toResult, DictionaryConst.NODE_VIRTUAL ) );
  }

  @Test
  public void testAddLink_existingLink() {
    int originalEdgeCount = countEdges( graph.edges() );
    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );
    MetaverseLink link = new MetaverseLink( node, "uses", node2 );

    builder.addLink( link );

    Vertex fromResult = getVertex( node.getStringID() );
    Vertex toResult = getVertex( node2.getStringID() );
    assertNotNull( getEdge( BaseMetaverseBuilder.getEdgeId( fromResult, link.getLabel(), toResult ) ) );
    assertEquals( originalEdgeCount + 1, countEdges( graph.edges() ) );

    builder.addLink( link );
    assertEquals( originalEdgeCount + 1, countEdges( graph.edges() ) );
  }

  @Test
  public void testDeleteNode() {
    builder.addNode( node );
    assertNotNull( getVertex( node.getStringID() ) );

    builder.deleteNode( node );
    assertNull( getVertex( node.getStringID() ) );
  }

  @Test
  public void testDeleteNode_null() {
    builder.addNode( node );
    assertNotNull( getVertex( node.getStringID() ) );

    builder.deleteNode( null );
    assertNotNull( getVertex( node.getStringID() ) );
  }

  private IMetaverseLink createAndTestLink() {
    builder.addNode( node );

    IMetaverseNode node2 = builder.createNodeObject( "nodeToId" );
    node2.setName( "to name" );

    IMetaverseLink link = builder.createLinkObject();
    link.setFromNode( node );
    link.setLabel( "uses" );
    link.setToNode( node2 );

    builder.addLink( link );

    Vertex fromResult = getVertex( node.getStringID() );
    Vertex toResult = getVertex( node2.getStringID() );
    assertFalse( (Boolean) getProperty( fromResult, DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( (Boolean) getProperty( toResult, DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( fromResult.edges( Direction.OUT, "uses" ).hasNext() );
    assertTrue( toResult.edges( Direction.IN, "uses" ).hasNext() );

    return link;
  }

  @Test
  public void testDeleteLink() {
    IMetaverseLink link = createAndTestLink();
    builder.deleteLink( link );

    Vertex fromResult = getVertex( link.getFromNode().getStringID() );
    Vertex toResult = getVertex( link.getToNode().getStringID() );
    assertNotNull( fromResult );
    assertFalse( fromResult.edges( Direction.OUT, "uses" ).hasNext() );
    assertNull( toResult );
  }

  @Test
  public void testDeleteLink_emptyLink() {
    builder.deleteLink( builder.createLinkObject() );
  }

  @Test
  public void testDeleteLink_nonExistentFromNode() {
    IMetaverseLink link = createAndTestLink();
    IMetaverseNode origFrom = link.getFromNode();
    IMetaverseNode mockFrom = mock( IMetaverseNode.class );
    link.setFromNode( mockFrom );

    when( mockFrom.getStringID() ).thenReturn( "not in graph" );
    when( mockFrom.getLogicalId() ).thenReturn( "not in graph" );

    builder.deleteLink( link );

    Vertex fromResult = getVertex( origFrom.getStringID() );
    Vertex toResult = getVertex( link.getToNode().getStringID() );
    assertNotNull( fromResult );
    assertTrue( fromResult.edges( Direction.OUT, "uses" ).hasNext() );
    assertNotNull( toResult );
  }

  @Test
  public void testDeleteLink_fromNodeHasMutipleLinks() {
    IMetaverseLink link = createAndTestLink();

    IMetaverseNode node3 = builder.createNodeObject( "another" );
    node3.setName( "to another" );
    builder.addLink( node, "uses", node3 );

    Vertex beforeDeleteFrom = getVertex( link.getFromNode().getStringID() );
    assertEquals( 2, countEdges( beforeDeleteFrom.edges( Direction.OUT, "uses" ) ) );

    builder.deleteLink( link );

    Vertex fromResult = getVertex( link.getFromNode().getStringID() );
    Vertex toResult = getVertex( link.getToNode().getStringID() );
    Vertex anotherResult = getVertex( node3.getStringID() );
    assertNotNull( fromResult );

    Iterator<Edge> edges = fromResult.edges( Direction.OUT, "uses" );
    assertTrue( edges.hasNext() );
    Edge remainingEdge = edges.next();
    assertEquals( "another", remainingEdge.inVertex().id() );
    assertFalse( edges.hasNext() );

    assertNull( toResult );
    assertNotNull( anotherResult );
  }

  @Test
  public void testUpdateNode() {
    builder.addNode( node );
    Vertex vertex = getVertex( node.getStringID() );
    assertEquals( node.getName(), getProperty( vertex, DictionaryConst.PROPERTY_NAME ) );
    assertEquals( node.getStringID(), vertex.id() );

    IMetaverseNode updateNode = builder.createNodeObject( node.getStringID() );
    updateNode.setName( "UPDATED NAME" );
    updateNode.setProperty( "new prop", "test" );
    updateNode.setType( "new type" );

    builder.updateNode( updateNode );

    vertex = getVertex( node.getStringID() );
    assertEquals( updateNode.getStringID(), vertex.id() );
    assertEquals( updateNode.getName(), getProperty( vertex, DictionaryConst.PROPERTY_NAME ) );
    assertEquals( "test", getProperty( vertex, "new prop" ) );
    assertEquals( updateNode.getType(), getProperty( vertex, DictionaryConst.PROPERTY_TYPE ) );
  }

  @Test
  public void testUpdateNode_null() {
    builder.updateNode( null );
  }

  @Test
  public void testUpdateLinkLabel() {
    IMetaverseLink link = createAndTestLink();
    Vertex vertex = getVertex( link.getFromNode().getStringID() );
    assertTrue( vertex.edges( Direction.OUT, "uses" ).hasNext() );

    builder.updateLinkLabel( link, "owns" );

    assertEquals( "owns", link.getLabel() );
    vertex = getVertex( link.getFromNode().getStringID() );
    assertFalse( vertex.edges( Direction.OUT, "uses" ).hasNext() );
    assertTrue( vertex.edges( Direction.OUT, "owns" ).hasNext() );
  }

  @Test
  public void testUpdateLinkLabel_nullLabel() {
    IMetaverseLink link = createAndTestLink();
    Vertex vertex = getVertex( link.getFromNode().getStringID() );
    assertTrue( vertex.edges( Direction.OUT, "uses" ).hasNext() );

    builder.updateLinkLabel( link, null );

    assertEquals( "uses", link.getLabel() );
    vertex = getVertex( link.getFromNode().getStringID() );
    assertTrue( vertex.edges( Direction.OUT, "uses" ).hasNext() );
  }

  @Test
  public void testUpdateLinkLabel_nullLink() {
    builder.updateLinkLabel( null, "owns" );
  }

  @Test
  public void testCreateMetaverseDocument() {
    IDocument doc = builder.createDocumentObject();
    assertNotNull( doc );
  }

  @Test
  public void testIsVirtual() {
    builder.addNode( node );
    Vertex vertex = getVertex( node.getStringID() );
    assertFalse( builder.isVirtual( vertex ) );

    IMetaverseNode virtual = builder.createNodeObject( "virtual node" );
    Vertex virtualVertex = graph.addVertex( T.id, virtual.getStringID() );
    virtualVertex.property( DictionaryConst.NODE_VIRTUAL, virtual.getProperty( DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( builder.isVirtual( virtualVertex ) );
    assertFalse( builder.isVirtual( null ) );
  }

  @Test
  public void testIsVirtual_noVirtualProperty() {
    builder.addNode( node );
    Vertex vertex = getVertex( node.getStringID() );
    assertFalse( builder.isVirtual( vertex ) );

    IMetaverseNode virtual = builder.createNodeObject( "virtual node" );
    Vertex virtualVertex = graph.addVertex( T.id, virtual.getStringID() );
    assertFalse( builder.isVirtual( virtualVertex ) );
  }

  @Test
  public void testCreateRootEntity() {
    assertNotNull( builder.createRootEntity() );
  }

  @Test
  public void testCopyLinkPropertiesToEdge() {
    final String label = "myLabel";

    IMetaverseLink link = new MetaverseLink();
    link.setLabel( "sourceLabel" );

    Vertex fromNode = graph.addVertex( T.id, "from" );
    Vertex toNode = graph.addVertex( T.id, "to" );
    Edge edge = fromNode.addEdge( label, toNode, T.id, "myId" );

    builder.copyLinkPropertiesToEdge( null, edge );
    builder.copyLinkPropertiesToEdge( link, null );
    builder.copyLinkPropertiesToEdge( link, edge );

    link.setProperty( DictionaryConst.PROPERTY_LABEL, "sourceLabel" );
    link.setProperty( DictionaryConst.PROPERTY_NAME, "sourceLink" );

    edge.property( DictionaryConst.PROPERTY_NAME, "myEdge" );
    edge.property( DictionaryConst.PROPERTY_TYPE, "relates to" );

    builder.copyLinkPropertiesToEdge( link, edge );
    assertEquals( "sourceLink", getProperty( edge, DictionaryConst.PROPERTY_NAME ) );
    assertEquals( "relates to", getProperty( edge, DictionaryConst.PROPERTY_TYPE ) );
    assertEquals( label, edge.label() );
    assertNull( getProperty( edge, DictionaryConst.PROPERTY_LABEL ) );
  }

  @Test
  public void testGetVertexForNodeWithDiffStringId() {
    node.setStringID( "test string id" );
    node.setName( "test name" );
    node.setType( "test type" );
    builder.addNode( node );
    Vertex vertex = builder.getVertexForNode( node );
    node.setStringID( "diff test string id" );
    Vertex newVertex = builder.getVertexForNode( node );
    assertEquals( vertex, newVertex );
  }

  private Vertex getVertex( String id ) {
    Iterator<Vertex> vertices = graph.vertices( id );
    return vertices.hasNext() ? vertices.next() : null;
  }

  private Edge getEdge( String id ) {
    Iterator<Edge> edges = graph.edges( id );
    return edges.hasNext() ? edges.next() : null;
  }

  private int countEdges( Iterator<Edge> edges ) {
    int count = 0;
    while ( edges.hasNext() ) {
      count++;
      edges.next();
    }
    return count;
  }

  private Object getProperty( Vertex vertex, String key ) {
    return vertex.property( key ).isPresent() ? vertex.value( key ) : null;
  }

  private Object getProperty( Edge edge, String key ) {
    return edge.property( key ).isPresent() ? edge.value( key ) : null;
  }
}
