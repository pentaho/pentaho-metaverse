/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: rfellows
 */
public class MetaverseBuilderTest {

  private BaseMetaverseBuilder builder;
  private Graph graph;
  MetaverseTransientNode node = new MetaverseTransientNode();

  @Before
  public void before() {
    graph = new TinkerGraph();
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

    // make sure the node was added to the graph
    Vertex result = graph.getVertex( node.getStringID() );
    assertNotNull( "Node was not added as a Vertex in the graph", result );
    assertEquals( node.getStringID(), result.getId() );
    assertEquals( node.getType(), result.getProperty( "type" ) );

    // this should be a non-virtual node
    assertNotNull( result.getProperty( DictionaryConst.NODE_VIRTUAL ) );
    assertFalse( (Boolean) result.getProperty( DictionaryConst.NODE_VIRTUAL ) );

  }

  @Test
  public void testAddNodeThatAlreadyExists() {

    builder.addNode( node );

    Vertex result = graph.getVertex( node.getStringID() );
    assertNotNull( "Node was not added as a Vertex in the graph", result );
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
    assertTrue( (Boolean) fromResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );
    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );

    assertNotNull( fromResult.getEdges( Direction.OUT, "uses" ) );
    for ( Edge e : fromResult.getEdges( Direction.OUT, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.OUT ).getProperty( DictionaryConst.NODE_VIRTUAL ) );
    }

    assertNotNull( toResult.getEdges( Direction.IN, "uses" ) );
    for ( Edge e : fromResult.getEdges( Direction.IN, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.IN ).getProperty( DictionaryConst.NODE_VIRTUAL ) );
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
    assertTrue( (Boolean) fromResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );
    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );

    assertNotNull( fromResult.getEdges( Direction.OUT, "uses" ) );
    for ( Edge e : fromResult.getEdges( Direction.OUT, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.OUT ).getProperty( DictionaryConst.NODE_VIRTUAL ) );
    }

    assertNotNull( toResult.getEdges( Direction.IN, "uses" ) );
    for ( Edge e : fromResult.getEdges( Direction.IN, "uses" ) ) {
      assertEquals( e.getVertex( Direction.OUT ).getProperty( "name" ), node.getName() );
      assertEquals( e.getVertex( Direction.IN ).getProperty( "name" ), node2.getName() );
      // we added this node implicitly through the addLink, it should be flagged as virtual
      assertTrue( (Boolean) e.getVertex( Direction.IN ).getProperty( DictionaryConst.NODE_VIRTUAL ) );
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
    assertFalse( (Boolean) fromResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );

    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );

  }

  @Test
  public void testAddLink_existingLink() throws Exception {
    // Retain current number of edges
    int originalEdgeCount = 0;
    for ( Edge e : graph.getEdges() ) {
      originalEdgeCount++;
    }
    MetaverseTransientNode node2 = new MetaverseTransientNode();
    node2.setStringID( "nodeToId" );
    node2.setName( "to name" );
    MetaverseLink link = new MetaverseLink( node, "uses", node2 );

    builder.addLink( link );

    Vertex fromResult = graph.getVertex( node.getStringID() );
    Vertex toResult = graph.getVertex( node2.getStringID() );

    // make sure the edge exits before we try to add it again
    assertNotNull( graph.getEdge( builder.getEdgeId( fromResult, link.getLabel(), toResult ) ) );

    // make sure we only added 1
    int count = 0;
    for ( Edge e : graph.getEdges() ) {
      count++;
    }
    assertEquals( originalEdgeCount + 1, count );

    // now lets add it again
    builder.addLink( link );

    // make sure we still only have one edge
    count = 0;
    for ( Edge e : graph.getEdges() ) {
      count++;
    }
    assertEquals( originalEdgeCount + 1, count );

  }

  @Test
  public void testDeleteNode() {
    builder.addNode( node );

    Vertex result = graph.getVertex( node.getStringID() );
    assertNotNull( "Node was not added as a Vertex in the graph", result );

    builder.deleteNode( node );
    result = graph.getVertex( node.getStringID() );
    assertNull( "Node was not deleted from the graph", result );
  }

  @Test
  public void testDeleteNode_null() {
    builder.addNode( node );

    Vertex result = graph.getVertex( node.getStringID() );
    assertNotNull( "Node was not added as a Vertex in the graph", result );

    builder.deleteNode( null );
    result = graph.getVertex( node.getStringID() );
    // should still be there
    assertNotNull( "Node was deleted from the graph when it should not have been", result );
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
    assertFalse( (Boolean) fromResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );
    // we added this node implicitly through the addLink, it should be flagged as virtual
    assertTrue( (Boolean) toResult.getProperty( DictionaryConst.NODE_VIRTUAL ) );

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
  public void testDeleteLink_emptyLink() {
    IMetaverseLink link = builder.createLinkObject();

    // now lets try to delete the link, no errors should happen
    builder.deleteLink( link );
  }

  @Test
  public void testDeleteLink_nonExistentFromNode() {
    IMetaverseLink link = createAndTestLink();
    IMetaverseNode origFrom = link.getFromNode();
    IMetaverseNode mockFrom = mock( IMetaverseNode.class );
    link.setFromNode( mockFrom );

    when( mockFrom.getStringID() ).thenReturn( "not in graph" );
    when( mockFrom.getLogicalId() ).thenReturn( "not in graph" );

    // now lets try to delete the link
    builder.deleteLink( link );

    Vertex fromResult = graph.getVertex( origFrom.getStringID() );
    Vertex toResult = graph.getVertex( link.getToNode().getStringID() );

    // the from node was explicitly added, it should still be there
    assertNotNull( fromResult );

    // the link should still be there
    assertTrue( fromResult.getEdges( Direction.OUT, "uses" ).iterator().hasNext() );

    // should still be there
    assertNotNull( toResult );

  }

  @Test
  public void testDeleteLink_fromNodeHasMutipleLinks() {
    IMetaverseLink link = createAndTestLink();

    // add another link using the same test node
    IMetaverseNode node3 = builder.createNodeObject( "another" );
    node3.setName( "to another" );

    // add another link
    builder.addLink( node, "uses", node3 );

    Vertex beforeDeleteFrom = graph.getVertex( link.getFromNode().getStringID() );
    int count = 0;
    for ( Edge edge : beforeDeleteFrom.getEdges( Direction.OUT, "uses" ) ) {
      count++;
      System.out.println( edge.toString() );
    }
    // we should have 2 edges for this node before we delete one
    assertEquals( 2, count );

    // now lets try to delete the link
    builder.deleteLink( link );

    Vertex fromResult = graph.getVertex( link.getFromNode().getStringID() );
    Vertex toResult = graph.getVertex( link.getToNode().getStringID() );
    Vertex anotherResult = graph.getVertex( node3.getStringID() );

    // the from node was explicitly added, it should still be there
    assertNotNull( fromResult );

    // the uses link should be gone
    // the "another" link should still be there
    count = 0;
    for ( Edge edge : fromResult.getEdges( Direction.OUT, "uses" ) ) {
      count++;
      assertEquals( "another", edge.getVertex( Direction.IN ).getId() );
      System.out.println( edge.toString() );
    }
    assertEquals( 1, count );

    // any virtual nodes that were associated with the link should also be removed
    assertNull( toResult );

    assertNotNull( anotherResult );
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
  public void testUpdateNode_null() {
    // make sure no NPE is thrown in this scenario
    builder.updateNode( null );
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
  public void testUpdateLinkLabel_nullLabel() {
    // if a null value is passed in for a label, it should NOT perform an update
    IMetaverseLink link = createAndTestLink();
    Vertex v = graph.getVertex( link.getFromNode().getStringID() );
    assertNotNull( v.getEdges( Direction.OUT, "uses" ) );

    builder.updateLinkLabel( link, null );

    assertEquals( "uses", link.getLabel() );

    v = graph.getVertex( link.getFromNode().getStringID() );
    assertTrue( v.getEdges( Direction.OUT, "uses" ).iterator().hasNext() );
  }

  @Test
  public void testUpdateLinkLabel_nullLink() {
    // make sure no NPE is thrown in this scenario
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
    Vertex v = graph.getVertex( node.getStringID() );

    assertFalse( builder.isVirtual( v ) );

    IMetaverseNode virtual = builder.createNodeObject( "virtual node" );
    Vertex virtualVertex = graph.addVertex( virtual.getStringID() );
    virtualVertex.setProperty( DictionaryConst.NODE_VIRTUAL, virtual.getProperty( DictionaryConst.NODE_VIRTUAL ) );
    assertTrue( builder.isVirtual( virtualVertex ) );

    assertFalse( builder.isVirtual( null ) );

  }

  @Test
  public void testIsVirtual_noVirtualProperty() {
    builder.addNode( node );
    Vertex v = graph.getVertex( node.getStringID() );

    assertFalse( builder.isVirtual( v ) );

    IMetaverseNode virtual = builder.createNodeObject( "virtual node" );
    Vertex virtualVertex = graph.addVertex( virtual.getStringID() );
    assertFalse( builder.isVirtual( virtualVertex ) );
  }

  @Test
  public void testCreateRootEntity() {
    assertNotNull( builder.createRootEntity() );
  }

  @Test
  public void testCopyLinkPropertiesToEdge() {
    final String LABEL = "myLabel";

    IMetaverseLink link = new MetaverseLink();
    link.setLabel( "sourceLabel" );

    // Create from/to nodes and an edge between them
    Vertex fromNode = graph.addVertex( "from" );
    Vertex toNode = graph.addVertex( "to" );

    Edge edge = graph.addEdge( "myId", fromNode, toNode, LABEL );

    // Call with null for branch coverage (and to prove no NPE occurs)
    builder.copyLinkPropertiesToEdge( null, edge );
    builder.copyLinkPropertiesToEdge( link, null );
    // Call with empty list for branch coverage (and to prove no NPE occurs)
    builder.copyLinkPropertiesToEdge( link, edge );

    // Set some properties on the source link
    link.setProperty( DictionaryConst.PROPERTY_LABEL, "sourceLabel" );
    link.setProperty( DictionaryConst.PROPERTY_NAME, "sourceLink" );

    // Set some properties on the target edge (including the reserved one "label")
    edge.setProperty( DictionaryConst.PROPERTY_NAME, "myEdge" );
    edge.setProperty( DictionaryConst.PROPERTY_TYPE, "relates to" );

    // Invoke the method under test and see that the appropriate properties are set on the target edge
    builder.copyLinkPropertiesToEdge( link, edge );
    assertEquals( "sourceLink", edge.getProperty( DictionaryConst.PROPERTY_NAME ) );
    assertEquals( "relates to", edge.getProperty( DictionaryConst.PROPERTY_TYPE ) );
    // The label should not be overridden (Blueprints does not allow it)
    assertEquals( LABEL, edge.getLabel() );

    // The property "label" is not set on the edge by either setLabel() or the method under test
    // It's a Blueprints thing
    assertNull( edge.getProperty( DictionaryConst.PROPERTY_LABEL ) );

  }

  @Test
  public void testGetVertexForNodeWithDiffStringId(){
    node.setStringID( "test string id" );
    node.setName( "test name" );
    node.setType( "test type" );
    builder.addNode( node );
    Vertex vertex = builder.getVertexForNode( node );
    node.setStringID( "diff test string id" );
    Vertex newVertex = builder.getVertexForNode( node );
    assertEquals( vertex, newVertex );
  }
}
