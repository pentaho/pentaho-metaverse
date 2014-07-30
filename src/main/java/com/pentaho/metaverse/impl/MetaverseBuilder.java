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

import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.DictionaryHelper;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * @author mburgess
 */
public class MetaverseBuilder extends MetaverseObjectFactory implements IMetaverseBuilder {

  private static final String METAVERSE_PREFIX = "metaverse_";

  private static final String ENTITY_PREFIX = "entity_";

  private static final String ENTITY_NODE_ID = "entity";

  /**
   * The id for this metaverse instance
   */
  private String id;

  private Graph graph;

  /**
   * The node that represents this metaverse instance
   */
  private Vertex metaverseNode;

  /**
   * This is a possible delegate reference to a metaverse object factory. This builder is itself a
   * metaverse object factory, so the reference is initialized to "this".
   */
  private IMetaverseObjectFactory metaverseObjectFactory = this;

  /**
   * Instantiates a new Metaverse builder.
   *
   * @param graph the Graph to write to
   */
  public MetaverseBuilder( Graph graph ) {
    this.graph = graph;
    metaverseNode = graph.getVertex( METAVERSE_PREFIX + id );
    if ( metaverseNode == null ) {
      metaverseNode = graph.addVertex( METAVERSE_PREFIX + id );
    }
  }

  protected Graph getGraph() {
    return graph;
  }

  /**
   * Adds a link between 2 nodes in the underlying graph. If either node does not exist, it will be created.
   *
   * @param link the link to add
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
      fromVertex.setProperty( DictionaryConst.NODE_VIRTUAL, true );
    }
    // update the vertex properties from the fromNode
    copyNodePropertiesToVertex( link.getFromNode(), fromVertex );

    // add the "to" vertex to the graph if it wasn't found
    if ( toVertex == null ) {
      toVertex = addVertex( link.getToNode() );
      // set the virtual node property to true since this is an implicit adding of a node
      toVertex.setProperty( DictionaryConst.NODE_VIRTUAL, true );
    }
    // update the to vertex properties from the toNode
    copyNodePropertiesToVertex( link.getToNode(), toVertex );

    Edge e = graph.addEdge( null, fromVertex, toVertex, link.getLabel() );
    e.setProperty( "text", link.getLabel() );

    return this;
  }

  /**
   * Add a node to the underlying graph. If the node already exists, it's properties will get updated
   *
   * @param node the node to add
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
    v.setProperty( DictionaryConst.NODE_VIRTUAL, false );

    copyNodePropertiesToVertex( node, v );

    return this;
  }

  /**
   * adds a node as a Vertex in the graph
   *
   * @param node node to add as a Vertex
   * @return the Vertex added
   */
  private Vertex addVertex( IMetaverseNode node ) {

    Vertex v = graph.addVertex( node.getStringID() );

    if ( DictionaryHelper.isEntityType( node.getType() ) ) {
      // the node is an entity, so link it to its entity type node
      Vertex entityType = graph.getVertex( ENTITY_PREFIX + node.getType() );
      if ( entityType == null ) {
        // the entity type node does not exist, so create it
        entityType = graph.addVertex( ENTITY_PREFIX + node.getType() );
        entityType.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_ENTITY );
        entityType.setProperty( DictionaryConst.PROPERTY_NAME, node.getType() );
        Vertex rootEntity = graph.getVertex( ENTITY_NODE_ID );
        if ( rootEntity == null ) {
          // the root entity node does not exist, so create it
          rootEntity = graph.addVertex( ENTITY_NODE_ID );
          rootEntity.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_ROOT_ENTITY );
          rootEntity.setProperty( DictionaryConst.PROPERTY_NAME, "Root entity" );
        }
        // add the link from the root node to the entity type
        graph.addEdge( null, rootEntity, entityType, DictionaryConst.LINK_PARENT_CONCEPT );
      }
      // add a link from the entity type to the new node
      graph.addEdge( null, entityType, v, DictionaryConst.LINK_PARENT_CONCEPT );
    }

    return v;
  }

  /**
   * Copies all properties from a node into the properties of a Vertex
   *
   * @param node node with properties desired in a Vertex
   * @param v    Vertex to set properties on
   */
  private void copyNodePropertiesToVertex( IMetaverseNode node, Vertex v ) {
    // set all of the properties, except the id and virtual (since that is an internally set prop)
    for ( String propertyKey : node.getPropertyKeys() ) {
      if ( propertyKey.equals( DictionaryConst.PROPERTY_ID ) || propertyKey.equals( DictionaryConst.NODE_VIRTUAL ) ) {
        continue;
      } else {
        Object value = node.getProperty( propertyKey );
        if ( value != null ) {
          v.setProperty( propertyKey, value );
        }
      }
    }
  }

  /**
   * Helper method to get a Vertex from a node
   *
   * @param node the node to find a corresponding Vertex for
   * @return a matching Vertex or null if none found
   */
  protected Vertex getVertexForNode( IMetaverseNode node ) {
    if ( node != null ) {
      return graph.getVertex( node.getStringID() );
    } else {
      return null;
    }
  }

  @Override
  public IMetaverseBuilder deleteLink( IMetaverseLink link ) {
    deleteLink( link, true );
    return this;
  }

  /**
   * Deletes the specific link from the metaverse model and optionally removing virtual nodes associated
   * with the link
   *
   * @param link               the link to remove
   * @param removeVirtualNodes should any virtual nodes be removed or not?
   * @return true/false if the delete happened
   */
  private boolean deleteLink( IMetaverseLink link, boolean removeVirtualNodes ) {
    if ( link == null ) {
      return false;
    }

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
        Vertex[] fromAndTo = new Vertex[] { fromVertex, toVertex };
        for ( Vertex v : fromAndTo ) {
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
    if ( label != null && deleteLink( link, false ) ) {
      link.setLabel( label );
      addLink( link );
    }
    return this;
  }

  @Override
  public IMetaverseObjectFactory getMetaverseObjectFactory() {

    // Attempt to initialize the factory if it does not yet exist (or has been reset to null)
    if ( metaverseObjectFactory == null ) {
      // Attempt to find an injected class
      IMetaverseObjectFactory pentahoSystemMetaverseObjectFactory = PentahoSystem.get( IMetaverseObjectFactory.class );
      if ( pentahoSystemMetaverseObjectFactory != null ) {
        metaverseObjectFactory = pentahoSystemMetaverseObjectFactory;
      } else {
        // Default to ourselves (we are a subclass of MetaverseObjectFactory)
        metaverseObjectFactory = this;
      }
    }
    return metaverseObjectFactory;
  }

  @Override
  public void setMetaverseObjectFactory( IMetaverseObjectFactory metaverseObjectFactory ) {
    this.metaverseObjectFactory = metaverseObjectFactory;
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

  /**
   * Adds the specified link to the model
   *
   * @param fromNode the from node
   * @param label    the label
   * @param toNode   the to node
   * @return this metaverse builder
   * @see org.pentaho.platform.api.metaverse.IMetaverseBuilder#addLink(
   *org.pentaho.platform.api.metaverse.IMetaverseNode,
   * java.lang.String,
   * org.pentaho.platform.api.metaverse.IMetaverseNode)
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
   *
   * @param vertex node to determine if it is virtual
   * @return true/false
   */
  protected boolean isVirtual( Vertex vertex ) {
    if ( vertex == null ) {
      return false;
    }

    Boolean isVirtual = vertex.getProperty( DictionaryConst.NODE_VIRTUAL );
    return isVirtual == null ? false : isVirtual;
  }

}
