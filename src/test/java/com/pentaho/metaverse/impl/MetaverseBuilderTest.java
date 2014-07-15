package com.pentaho.metaverse.impl;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import static org.junit.Assert.*;

/**
 * @author: rfellows
 */
public class MetaverseBuilderTest {

  private MetaverseBuilder builder;
  private Graph graph;
  MetaverseTransientNode node = new MetaverseTransientNode();

  @Before
  public void before() {
    builder = new MetaverseBuilder();
    graph = new TinkerGraph();
    builder.setGraph( graph );

    node.setStringID( "node1" );
    node.setName( "node1 name" );
    node.setType( "test type" );
  }

  @Test
  public void testAddNode() {
    builder.addNode( node );

    // make sure the node was added to the graph
    Vertex result = graph.getVertex( node.getStringID() );
    assertNotNull("Node was not added as a Vertex in the graph", result);
    assertEquals( node.getStringID(), result.getId() );
    assertEquals( node.getType(), result.getProperty( "type" ) );

    // this should be a non-virtual node
    assertNotNull( result.getProperty( MetaverseBuilder.VIRTUAL ) );
    assertFalse( (Boolean) result.getProperty( MetaverseBuilder.VIRTUAL ) );

  }

  @Test
  public void testAddNodeThatAlreadyExists() {

    builder.addNode( node );

    Vertex result = graph.getVertex( node.getStringID() );
    assertNotNull( "Node was not added as a Vertex in the graph", result);
    assertEquals( "Node name property was not set", "node1 name", result.getProperty( "name" ) );
    assertEquals( "Node type property was not set", "test type", result.getProperty( "type" ) );

    node.setName( "updated name" );
    node.setProperty( "test", "value" );

    builder.addNode( node );

    result = graph.getVertex( node.getStringID() );

    assertEquals( "Node name property was not set", "updated name", result.getProperty( "name" ) );
    assertEquals( "Node test property was not set", "value", result.getProperty( "test" ) );

  }

  @Test
  public void testAddLink() {

    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );
    MetaverseLink link = new MetaverseLink( node, "uses", node2 );

    builder.addLink( link );

    Vertex fromResult = graph.getVertex( node.getStringID() );
    Vertex toResult = graph.getVertex( node2.getStringID() );

    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) fromResult.getProperty( MetaverseBuilder.VIRTUAL ) );
    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( MetaverseBuilder.VIRTUAL ) );

    assertNotNull( fromResult.getEdges( Direction.OUT, "uses" ) );
    for( Edge e : fromResult.getEdges( Direction.OUT, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.OUT ).getProperty( MetaverseBuilder.VIRTUAL ) );
    }

    assertNotNull( toResult.getEdges( Direction.IN, "uses" ) );
    for( Edge e : fromResult.getEdges( Direction.IN, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.IN ).getProperty( MetaverseBuilder.VIRTUAL ) );
    }
  }

  @Test
  public void testAddLink2() {

    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );

    builder.addLink( node, "uses", node2 );

    Vertex fromResult = graph.getVertex( node.getStringID() );
    Vertex toResult = graph.getVertex( node2.getStringID() );

    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) fromResult.getProperty( MetaverseBuilder.VIRTUAL ) );
    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( MetaverseBuilder.VIRTUAL ) );

    assertNotNull( fromResult.getEdges( Direction.OUT, "uses" ) );
    for( Edge e : fromResult.getEdges( Direction.OUT, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.OUT ).getProperty( MetaverseBuilder.VIRTUAL ) );
    }

    assertNotNull( toResult.getEdges( Direction.IN, "uses" ) );
    for( Edge e : fromResult.getEdges( Direction.IN, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.IN ).getProperty( MetaverseBuilder.VIRTUAL ) );
    }
  }

  @Test
  public void testAddLink_OneExistingNode() {
    // explicitly add the fromNode
    builder.addNode( node );

    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );
    MetaverseLink link = new MetaverseLink( node, "uses", node2 );

    builder.addLink( link );

    Vertex fromResult = graph.getVertex( node.getStringID() );
    Vertex toResult = graph.getVertex( node2.getStringID() );

      // we added this node explicitly through the addNode, it should be flagged as NOT virtual
    assertFalse( (Boolean) fromResult.getProperty( MetaverseBuilder.VIRTUAL ) );

    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( MetaverseBuilder.VIRTUAL ) );

  }

  @Test
  public void testDeleteNode() {
    builder.addNode( node );

    Vertex result = graph.getVertex( node.getStringID() );
    assertNotNull( "Node was not added as a Vertex in the graph", result);

    builder.deleteNode( node );
    result = graph.getVertex( node.getStringID() );
    assertNull( "Node was not deleted from the graph", result );
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

    Vertex fromResult = graph.getVertex( node.getStringID() );
    Vertex toResult = graph.getVertex( node2.getStringID() );

    // we added this node explicitly through addNode, it should NOT be flagged as virtual
    assertFalse( (Boolean) fromResult.getProperty( MetaverseBuilder.VIRTUAL ) );
    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( MetaverseBuilder.VIRTUAL ) );

    // verify the link is there
    assertNotNull( fromResult.getEdges( Direction.OUT, "uses" ) );
    assertNotNull( toResult.getEdges( Direction.IN, "uses" ) );

    return link;
  }


  @Test
  public void testDeleteLink() {
    IMetaverseLink link = createAndTestLink();

    // now lets try to delete the link
    builder.deleteLink( link );

    Vertex fromResult = graph.getVertex( link.getFromNode().getStringID() );
    Vertex toResult = graph.getVertex( link.getToNode().getStringID() );

    // the from node was explicitly added, it should still be there
    assertNotNull( fromResult );

    // the link should be gone
    assertFalse( fromResult.getEdges( Direction.OUT, "uses" ).iterator().hasNext() );

    // any virtual nodes that were associated with the link should also be removed
    assertNull( toResult );

  }

  @Test
  public void testUpdateNode() {
    builder.addNode( node );
    Vertex v = graph.getVertex( node.getStringID() );

    assertEquals( node.getName(), v.getProperty( "name" ) );
    assertEquals( node.getStringID(), v.getId() );

    IMetaverseNode updateNode = builder.createNodeObject( node.getStringID() );
    updateNode.setName( "UPDATED NAME" );
    updateNode.setProperty( "new prop", "test" );
    updateNode.setType( "new type" );

    builder.updateNode( updateNode );

    v = graph.getVertex( node.getStringID() );
    assertEquals( node.getStringID(), v.getId() );
    assertEquals( updateNode.getStringID(), v.getId() );
    assertEquals( updateNode.getName(), v.getProperty( "name" ) );
    assertEquals( "test", v.getProperty( "new prop" ) );
    assertEquals( updateNode.getType(), v.getProperty( "type" ) );

  }

  @Test
  public void testUpdateLinkLabel() {
    IMetaverseLink link = createAndTestLink();
    Vertex v = graph.getVertex( link.getFromNode().getStringID() );
    assertNotNull( v.getEdges( Direction.OUT, "uses" ) );

    builder.updateLinkLabel( link, "owns" );

    assertEquals( "owns", link.getLabel() );

    v = graph.getVertex( link.getFromNode().getStringID() );
    assertFalse( v.getEdges( Direction.OUT, "uses" ).iterator().hasNext() );
    assertTrue( v.getEdges( Direction.OUT, "owns" ).iterator().hasNext() );

  }

  @Test
  public void testCreateMetaverseDocument() {
    IMetaverseDocument doc = builder.createDocumentObject();
    assertNotNull( doc );
  }

  @Test
  public void testIsVirtual() {
    builder.addNode( node );
    Vertex v = graph.getVertex( node.getStringID() );

    assertFalse( builder.isVirtual( v ) );

    IMetaverseNode virtual = builder.createNodeObject( "virtual node" );
    Vertex virtualVertex = graph.addVertex( virtual.getStringID() );
    virtualVertex.setProperty( builder.VIRTUAL, virtual.getProperty( builder.VIRTUAL ) );
    assertTrue( builder.isVirtual( virtualVertex ) );

    assertFalse( builder.isVirtual( null ) );

  }

}
