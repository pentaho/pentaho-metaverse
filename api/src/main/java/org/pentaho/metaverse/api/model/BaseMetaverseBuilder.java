/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Iterator;

/**
 * This is the reference implementation for IMetaverseBuilder, offering the ability to add nodes, links, etc. to an
 * underlying graph
 */
public class BaseMetaverseBuilder extends MetaverseObjectFactory implements IMetaverseBuilder {

  private static final String ENTITY_PREFIX = "entity_";

  private static final String ENTITY_NODE_ID = "entity";

  private static final String SEPARATOR = "~";

  private Graph graph;

  /**
   * This is a possible delegate reference to a metaverse object factory. This builder is itself a metaverse object
   * factory, so the reference is initialized to "this".
   */
  private IMetaverseObjectFactory metaverseObjectFactory = this;

  /**
   * Instantiates a new Metaverse builder.
   *
   * @param graph the Graph to write to
   */
  public BaseMetaverseBuilder( Graph graph ) {
    this.graph = graph;
    registerStaticNodes();
  }

  protected void registerStaticNodes() {
    DictionaryHelper.registerEntityTypes();
  }

  /**
   * Retrieves the underlying graph object for this metaverse.
   *
   * @return the backing Graph object
   */
  public Graph getGraph() {
    return graph;
  }

  /**
   * Sets the underlying graph for this builder
   *
   * @param graph the graph to set for this builder
   */
  @Override
  public void setGraph( Graph graph ) {
    this.graph = graph;
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

    final Edge edge = addEdge( fromVertex, link.getLabel(), toVertex );
    copyLinkPropertiesToEdge( link, edge );

    return this;
  }

  /**
   * Returns the edge id for the given vertices and edge label.
   *
   * @param fromVertex the source vertex
   * @param label      the edge label
   * @param toVertex   the target vertex
   * @return the String edge ID
   */
  public String getEdgeId( Vertex fromVertex, String label, Vertex toVertex ) {
    return fromVertex.getId() + SEPARATOR + label + SEPARATOR + toVertex.getId();
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

      // Add a link from the entity type to the node
      addEdge( addEntityType( node.getType() ), DictionaryHelper.getNonEntityToEntityLinkType(), v,
        DictionaryHelper.addNonEntityToEntityLinkTypeLabel() );
    }

    return v;
  }

  /**
   * Adds an entity type node to the metaverse.
   *
   * @param entityName the String type of the entity
   * @param parent     The String name of the parent entity
   * @deprecated use {@link #addEntityType(String)} instead
   */
  @Deprecated
  protected Vertex addEntityType( String entityName, String parent ) {
    return addEntityType( entityName );
  }

  protected Vertex addEntityType( String entityName ) {
    if ( graph == null ) {
      return null;
    }

    // the node is an entity, so link it to its entity type node
    Vertex entityType = graph.getVertex( ENTITY_PREFIX + entityName );
    if ( entityType == null ) {
      // the entity type node does not exist, so create it
      entityType = graph.addVertex( ENTITY_PREFIX + entityName );
      entityType.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_ENTITY );
      entityType.setProperty( DictionaryConst.PROPERTY_NAME, entityName );

      // TODO move this to a map of types to strings or something
      if ( entityName.equals( DictionaryConst.NODE_TYPE_TRANS ) || entityName
        .equals( DictionaryConst.NODE_TYPE_JOB ) ) {
        entityType.setProperty( DictionaryConst.PROPERTY_DESCRIPTION, DictionaryConst.EXECUTION_ENGINE_NAME );
      }

      // get all available entity link types
      final Iterator<String> entityLinkTypeIter = DictionaryHelper.getEntityLinkTypes().iterator();
      while ( entityLinkTypeIter.hasNext() ) {
        final String entityLinkType = entityLinkTypeIter.next();
        // check if there is a parent node for the given node entityName with the current link type
        final String parentEntityNode = DictionaryHelper.getParentEntityNodeType( entityLinkType, entityName );
        // if the node exists, add it and add a link to it
        if ( parentEntityNode != null ) {
          addEdge( addEntityType( parentEntityNode ), entityLinkType, entityType );
        } else if ( DictionaryHelper.linksToRoot( entityLinkType, entityName ) ) {
          addEdge( createRootEntity(), entityLinkType, entityType );
        }
      }
    }
    return entityType;
  }

  /**
   * Creates the root entity for this metaverse.
   */
  public Vertex createRootEntity() {

    Vertex rootEntity = graph.getVertex( ENTITY_NODE_ID );
    if ( rootEntity == null ) {
      rootEntity = graph.addVertex( ENTITY_NODE_ID );
      rootEntity.setProperty( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_ROOT_ENTITY );
      rootEntity.setProperty( DictionaryConst.PROPERTY_NAME, "METAVERSE" );

      // TODO get these properties from somewhere else
      rootEntity.setProperty( "division", "Engineering" );
      rootEntity.setProperty( "project", "Pentaho Data Lineage" );
      rootEntity.setProperty( "description",
        "Data lineage is tracing the path that data has traveled upstream from its destination, through Pentaho "
          + "systems and artifacts as well as external systems and artifacts." );
    }
    return rootEntity;
  }

  /**
   * Copies all properties from a node into the properties of a Vertex
   *
   * @param node node with properties desired in a Vertex
   * @param v    Vertex to set properties on
   */
  private void copyNodePropertiesToVertex( IMetaverseNode node, Vertex v ) {

    // don't copy the node logicalId to a vertex if the node is virtual and the vertex is not
    Boolean nodeIsVirtual = (Boolean) node.getProperty( DictionaryConst.NODE_VIRTUAL );
    nodeIsVirtual = nodeIsVirtual == null ? true : nodeIsVirtual;

    Boolean vertexIsVirtual = v.getProperty( DictionaryConst.NODE_VIRTUAL );
    vertexIsVirtual = vertexIsVirtual == null ? false : vertexIsVirtual;

    String vertexLogicalId = v.getProperty( DictionaryConst.PROPERTY_LOGICAL_ID );
    boolean skipLogicalId = false;
    if ( vertexLogicalId != null && nodeIsVirtual && !vertexIsVirtual ) {
      skipLogicalId = true;
    }

    // set all of the properties, except the id and virtual (since that is an internally set prop)
    for ( String propertyKey : node.getPropertyKeys() ) {
      if ( !propertyKey.equals( DictionaryConst.PROPERTY_ID )
        && !propertyKey.equals( DictionaryConst.NODE_VIRTUAL )
        && !( skipLogicalId && propertyKey.equals( DictionaryConst.PROPERTY_LOGICAL_ID ) ) ) {
        Object value = node.getProperty( propertyKey );
        if ( value != null ) {
          v.setProperty( propertyKey, value );
        }
      }
    }
    node.setDirty( false );
  }

  /**
   * Copies all properties from a link into the properties of an Edge
   *
   * @param link link with properties desired in an Edge
   * @param e    Edge to set properties on
   */
  public void copyLinkPropertiesToEdge( IMetaverseLink link, Edge e ) {
    // set all of the properties, except the id and virtual (since that is an internally set prop)
    if ( link != null && link.getPropertyKeys() != null && e != null ) {
      for ( String propertyKey : link.getPropertyKeys() ) {
        // Skip the "label" property, that's reserved
        if ( !DictionaryConst.PROPERTY_LABEL.equals( propertyKey ) ) {
          Object value = link.getProperty( propertyKey );
          if ( value != null ) {
            e.setProperty( propertyKey, value );
          }
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
  public Vertex getVertexForNode( IMetaverseNode node ) {
    if ( node != null ) {
      String logicalId = node.getLogicalId();
      Vertex vertex = graph.getVertex( node.getStringID() );

      if ( vertex == null && !logicalId.equals( node.getStringID() ) ) {
        // check for matching logicalIds
        Iterable<Vertex> logicalMatches = graph.getVertices( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );
        for ( Vertex match : logicalMatches ) {
          // just return the first match for now
          vertex = match;
          break;
        }
      }

      return vertex;
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
   * Deletes the specific link from the metaverse model and optionally removing virtual nodes associated with the link
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
   * IMetaverseBuilder#updateLink(IMetaverseLink)
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
   * IMetaverseBuilder#updateNode(IMetaverseNode)
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
   * @see IMetaverseBuilder#addLink(IMetaverseNode, java.lang.String, IMetaverseNode)
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
   * @deprecated use {@link #addEdge(Vertex, String, Vertex)} instead
   */
  @Deprecated
  protected void addLink( Vertex fromVertex, String label, Vertex toVertex ) {
    addEdge( fromVertex, label, toVertex );
  }

  private Edge addEdge( Vertex fromVertex, String label, Vertex toVertex ) {
    return addEdge( fromVertex, label, toVertex, true );
  }

  private Edge addEdge( final Vertex fromVertex, final String label, Vertex toVertex, final boolean addLabel ) {
    String edgeId = getEdgeId( fromVertex, label, toVertex );
    Edge edge = graph.getEdge( edgeId );
    // only add the link if the edge doesn't already exist
    if ( edge == null ) {
      edge = graph.addEdge( edgeId, fromVertex, toVertex, label );
      if ( addLabel ) {
        edge.setProperty( "text", label );
      }
    }
    return edge;
  }

  /**
   * determines if the node passed in is a virtual node (meaning it has been implicitly added to the graph by an
   * addLink)
   *
   * @param vertex node to determine if it is virtual
   * @return true/false
   */
  public boolean isVirtual( Vertex vertex ) {
    if ( vertex == null ) {
      return false;
    }

    Boolean isVirtual = vertex.getProperty( DictionaryConst.NODE_VIRTUAL );
    return isVirtual == null ? false : isVirtual;
  }

}
