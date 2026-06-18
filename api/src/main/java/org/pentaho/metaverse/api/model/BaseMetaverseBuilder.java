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


package org.pentaho.metaverse.api.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Iterator;
import java.util.List;

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
   * Helper method: look up a vertex by id.
   */
  private Vertex getVertex( Object id ) {
    Iterator<Vertex> it = graph.vertices( id );
    return it.hasNext() ? it.next() : null;
  }

  /**
   * Helper method: look up an edge by id.
   */
  private Edge getEdge( Object id ) {
    Iterator<Edge> it = graph.edges( id );
    return it.hasNext() ? it.next() : null;
  }

  /**
   * Helper method: get-or-create a vertex with the given id.
   */
  private Vertex getOrCreateVertex( Object id ) {
    Vertex v = getVertex( id );
    if ( v == null ) {
      v = graph.addVertex( T.id, id );
    }
    return v;
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
      fromVertex.property( DictionaryConst.NODE_VIRTUAL, true );
    }
    // update the vertex properties from the fromNode
    copyNodePropertiesToVertex( link.getFromNode(), fromVertex );

    // add the "to" vertex to the graph if it wasn't found
    if ( toVertex == null ) {
      toVertex = addVertex( link.getToNode() );
      // set the virtual node property to true since this is an implicit adding of a node
      toVertex.property( DictionaryConst.NODE_VIRTUAL, true );
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
  public static String getEdgeId( Vertex fromVertex, String label, Vertex toVertex ) {
    return fromVertex.id() + SEPARATOR + label + SEPARATOR + toVertex.id();
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
    v.property( DictionaryConst.NODE_VIRTUAL, false );

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

    Vertex v = getOrCreateVertex( node.getStringID() );

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
    Vertex entityType = getVertex( ENTITY_PREFIX + entityName );
    if ( entityType == null ) {
      // the entity type node does not exist, so create it
      entityType = getOrCreateVertex( ENTITY_PREFIX + entityName );
      entityType.property( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_ENTITY );
      entityType.property( DictionaryConst.PROPERTY_NAME, entityName );

      // TODO move this to a map of types to strings or something
      if ( entityName.equals( DictionaryConst.NODE_TYPE_TRANS ) || entityName
        .equals( DictionaryConst.NODE_TYPE_JOB ) ) {
        entityType.property( DictionaryConst.PROPERTY_DESCRIPTION, DictionaryConst.EXECUTION_ENGINE_NAME );
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

    Vertex rootEntity = getVertex( ENTITY_NODE_ID );
    if ( rootEntity == null ) {
      rootEntity = getOrCreateVertex( ENTITY_NODE_ID );
      rootEntity.property( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_ROOT_ENTITY );
      rootEntity.property( DictionaryConst.PROPERTY_NAME, "METAVERSE" );

      // TODO get these properties from somewhere else
      rootEntity.property( "division", "Engineering" );
      rootEntity.property( "project", "Pentaho Data Lineage" );
      rootEntity.property( "description",
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

    Boolean vertexIsVirtual = v.property( DictionaryConst.NODE_VIRTUAL ).isPresent()
      ? v.<Boolean>value( DictionaryConst.NODE_VIRTUAL ) : false;
    vertexIsVirtual = vertexIsVirtual == null ? false : vertexIsVirtual;

    String vertexLogicalId = v.property( DictionaryConst.PROPERTY_LOGICAL_ID ).isPresent()
      ? v.<String>value( DictionaryConst.PROPERTY_LOGICAL_ID ) : null;
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
          v.property( propertyKey, value );
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
            e.property( propertyKey, value );
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
      Vertex vertex = getVertex( node.getStringID() );

      if ( vertex == null && !logicalId.equals( node.getStringID() ) ) {
        // check for matching logicalIds
        List<Vertex> logicalMatches = graph.traversal().V()
          .has( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId ).toList();
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
      Iterator<Edge> fromEdges = fromVertex.edges( Direction.OUT, link.getLabel() );
      while ( fromEdges.hasNext() ) {
        Edge edge = fromEdges.next();
        // if the IN vertex's id matches the toNode's id, then we have a matching edge
        toVertex = edge.inVertex();
        if ( toVertex.id().equals( link.getToNode().getStringID() ) ) {
          // matching link found
          deleteMe = edge;
          break;
        }
      }

      if ( deleteMe != null ) {
        deleteMe.remove();
        result = true;
      }

      // now remove any "virtual" nodes associated with the link
      if ( removeVirtualNodes ) {
        Vertex[] fromAndTo = new Vertex[] { fromVertex, toVertex };
        for ( Vertex v : fromAndTo ) {
          if ( isVirtual( v ) ) {
            v.remove();
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
      v.remove();
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

  public void addLink( Vertex fromVertex, String label, Vertex toVertex ) {
    addEdge( fromVertex, label, toVertex );
  }

  private Edge addEdge( Vertex fromVertex, String label, Vertex toVertex ) {
    return addEdge( fromVertex, label, toVertex, true );
  }

  private Edge addEdge( final Vertex fromVertex, final String label, Vertex toVertex, final boolean addLabel ) {
    String edgeId = getEdgeId( fromVertex, label, toVertex );
    Edge edge = getEdge( edgeId );
    // only add the link if the edge doesn't already exist
    if ( edge == null ) {
      edge = fromVertex.addEdge( label, toVertex, T.id, edgeId );
      if ( addLabel ) {
        edge.property( "text", label );
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

    Boolean isVirtual = vertex.property( DictionaryConst.NODE_VIRTUAL ).isPresent()
      ? vertex.<Boolean>value( DictionaryConst.NODE_VIRTUAL ) : null;
    return isVirtual == null ? false : isVirtual;
  }

}
