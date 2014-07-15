/*!
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

package com.pentaho.metaverse.impl;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;

/**
 * @author mburgess
 * 
 */
public class MetaverseBuilder implements IMetaverseBuilder, IMetaverseObjectFactory {


  /**
   * Property
   */
  public static final String VIRTUAL = "virtual";
  private Graph graph;

  /**
   * Instantiates a new Metaverse builder.
   */
  public MetaverseBuilder() {

  }

  public void setGraph( Graph graph ) {
    this.graph = graph;
  }

  /**
   * Adds a link between 2 nodes in the underlying graph. If either node does not exist, it will be created.
   * @param link
   *          the link to add
   * @return the builder
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseLink link ) {

    // make sure the from and to nodes exist in the graph
    Vertex fromVertex = getVertexForNode( link.getFromNode() );
    Vertex toVertex = getVertexForNode( link.getToNode() );

    // add the "from" vertex to the graph if it wasn't found
    if ( fromVertex == null ) {
      fromVertex = addVertex( link.getFromNode() );
      // set the virtual node property to true since this is an implicit adding of a node
      fromVertex.setProperty( VIRTUAL, true );
    }
    // update the vertex properties from the fromNode
    copyNodePropertiesToVertex( link.getFromNode(), fromVertex );

    // add the "to" vertex to the graph if it wasn't found
    if ( toVertex == null ) {
      toVertex = addVertex( link.getToNode() );
      // set the virtual node property to true since this is an implicit adding of a node
      toVertex.setProperty( VIRTUAL, true );
    }
    // update the to vertex properties from the toNode
    copyNodePropertiesToVertex( link.getToNode(), toVertex );

    graph.addEdge( null, fromVertex, toVertex, link.getLabel() );

    return this;
  }

  /**
   * Add a node to the underlying graph. If the node already exists, it's properties will get updated
   * @param node
   *          the node to add
   * @return the builder
   */
  @Override
  public IMetaverseBuilder addNode( IMetaverseNode node ) {
    // does the node already exist?
    Vertex v = getVertexForNode( node );

    if ( v == null ) {
      // it's a new node, add it to the graph
      v = addVertex( node );
    }

    // adding this node means that it is no longer a virtual node
    v.setProperty( VIRTUAL, false );

    copyNodePropertiesToVertex( node, v );

    return this;
  }

  /**
   * adds a node as a Vertex in the graph
   * @param node node to add as a Vertex
   * @return the Vertex added
   */
  private Vertex addVertex( IMetaverseNode node ) {
    Vertex v = graph.addVertex( node.getStringID() );
    return v;
  }

  /**
   * Copies all properties from a node into the properties of a Vertex
   * @param node node with properties desired in a Vertex
   * @param v Vertex to set properties on
   */
  private void copyNodePropertiesToVertex( IMetaverseNode node, Vertex v ) {
    // set all of the properties, except the id and virtual (since that is an internally set prop)
    for ( String propertyKey : node.getPropertyKeys() ) {
      if ( propertyKey.equals( "id" ) || propertyKey.equals( VIRTUAL ) ) {
        continue;
      } else {
        v.setProperty( propertyKey, node.getProperty( propertyKey ) );
      }
    }
  }

  /**
   * Helper method to get a Vertex from a node
   * @param node the node to find a corresponding Vertex for
   * @return a matching Vertex or null if none found
   */
  protected Vertex getVertexForNode( IMetaverseNode node ) {
    return graph.getVertex( node.getStringID() );
  }

  @Override
  public IMetaverseBuilder deleteLink( IMetaverseLink link ) {
    deleteLink( link, true );
    return this;
  }

  /**
   * Deletes the specific link from the metaverse model and optionally removing virtual nodes associated
   * with the link
   * @param link the link to remove
   * @param removeVirtualNodes should any virtual nodes be removed or not?
   * @return true/false if the delete happened
   */
  private boolean deleteLink( IMetaverseLink link, boolean removeVirtualNodes ) {
    // is there an edge in the graph that corresponds to the link?
    Vertex fromVertex = getVertexForNode( link.getFromNode() );
    Vertex toVertex = null;
    Edge deleteMe = null;
    boolean result = false;

    if ( fromVertex != null ) {

      // find all of the OUT linked Vertex's from this node
      for ( Edge edge : fromVertex.getEdges( Direction.OUT, link.getLabel() ) ) {
        // if the IN vertex's id matches the toNode's id, then we have a matching edge
        toVertex = edge.getVertex( Direction.IN );
        if ( toVertex.getId().equals( link.getToNode().getStringID() ) ) {
          // matching link found
          deleteMe = edge;
          break;
        }
      }

      if ( deleteMe != null ) {
        graph.removeEdge( deleteMe );
        result = true;
      }

      // now remove any "virtual" nodes associated with the link
      if ( removeVirtualNodes ) {
        Vertex[] fromAndTo = new Vertex[] {fromVertex, toVertex};
        for ( Vertex v: fromAndTo ) {
          if ( isVirtual( v ) ) {
            graph.removeVertex( v );
          }
        }
      }
    }
    return result;
  }

  @Override
  public IMetaverseBuilder deleteNode( IMetaverseNode node ) {
    Vertex v = getVertexForNode( node );
    if ( v != null ) {
      graph.removeVertex( v );
    }
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#updateLink(org.pentaho.platform.api.metaverse.IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder updateLinkLabel( IMetaverseLink link, String label ) {
    if ( deleteLink( link, false ) ) {
      link.setLabel( label );
      addLink( link );
    }

    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#updateNode(org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder updateNode( IMetaverseNode node ) {

    Vertex v = getVertexForNode( node );
    if ( v != null ) {
      copyNodePropertiesToVertex( node, v );
    }

    return this;
  }

  @Override
  public IMetaverseDocument createDocumentObject() {
    return new MetaverseDocument( );
  }

  @Override
  public IMetaverseLink createLinkObject() {
    return new MetaverseLink( );
  }

  @Override
  public IMetaverseNode createNodeObject( String id ) {
    MetaverseTransientNode node = new MetaverseTransientNode();
    node.setStringID( id );
    node.setProperty( VIRTUAL, true );
    return node;
  }

  /**
   * Adds the specified link to the model
   * 
   * @param fromNode
   *          the from node
   * @param label
   *          the label
   * @param toNode
   *          the to node
   * @return this metaverse builder
   * @see org.pentaho.platform.api.metaverse.IMetaverseBuilder#addLink(
   *    org.pentaho.platform.api.metaverse.IMetaverseNode,
   *    java.lang.String,
   *    org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode ) {
    IMetaverseLink link = createLinkObject();

    link.setFromNode( fromNode );
    link.setLabel( label );
    link.setToNode( toNode );
    return addLink( link );
  }

  /**
   * determines if the node passed in is a virtual node
   * (meaning it has been implicitly added to the graph by an addLink)
   * @param vertex node to determine if it is virtual
   * @return true/false
   */
  protected boolean isVirtual( Vertex vertex ) {
    if ( vertex == null ) {
      return false;
    }

    Boolean isVirtual = vertex.getProperty( VIRTUAL );
    return isVirtual == null ? false : isVirtual;
  }

}
