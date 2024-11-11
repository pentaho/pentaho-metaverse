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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.IMetaverseNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The MetaverseNode class is a wrapper around a corresponding Blueprints Vertex object, and delegates all methods to
 * that vertex.
 *
 * @author mburgess
 */
public class MetaverseNode implements IMetaverseNode {

  /**
   * The Blueprints-backed Vertex for this metaverse node
   */
  protected Vertex v;

  private String logicalId;
  protected ILogicalIdGenerator logicalIdGenerator = DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT;
  private boolean dirty = false;

  /**
   * Private constructor to prevent instantiation without an ID or backing Vertex
   */
  @SuppressWarnings( "unused" )
  private MetaverseNode() {
  }

  /**
   * Constructor that builds a MetaverseNode from a Vertex.
   *
   * @param v Vertex
   */
  public MetaverseNode( Vertex v ) {
    this();
    this.v = v;
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#getName()
   */
  @Override
  public String getName() {
    return v.getProperty( DictionaryConst.PROPERTY_NAME );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    return ( v.getId() == null ) ? null : v.getId().toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return v.getProperty( DictionaryConst.PROPERTY_TYPE );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    dirty = true;
    v.setProperty( DictionaryConst.PROPERTY_NAME, name );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#setType(java.lang.String)
   */
  @Override
  public void setType( String type ) {
    dirty = true;
    v.setProperty( DictionaryConst.PROPERTY_TYPE, type );
    String category = DictionaryHelper.getCategoryForType( type );
    v.setProperty( DictionaryConst.PROPERTY_CATEGORY, category );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IMetaverseNode#getPropertyKeys()
   */
  @Override
  public Set<String> getPropertyKeys() {
    return v.getPropertyKeys();
  }

  /**
   * Returns the properties as a key/value Map.
   *
   * @return the property key/value assignments
   */
  @Override public Map<String, Object> getProperties() {
    Map<String, Object> props = new HashMap<String, Object>();
    Set<String> keys = getPropertyKeys();
    if ( keys != null ) {
      for ( String key : keys ) {
        props.put( key, v.getProperty( key ) );
      }
    }
    return props;
  }

  /**
   * Sets the given property keys to the given property values.
   *
   * @param properties
   */
  @Override public void setProperties( Map<String, Object> properties ) {
    if ( properties != null ) {
      dirty = true;
      for ( Map.Entry<String, Object> property : properties.entrySet() ) {
        v.setProperty( property.getKey(), property.getValue() );
      }
    }
  }

  /**
   * Removes the values assigned to the given property keys.
   *
   * @param keys
   */
  @Override public void removeProperties( Set<String> keys ) {
    if ( keys != null ) {
      dirty = true;
      for ( String key : keys ) {
        v.removeProperty( key );
      }
    }
  }

  /**
   * Removes all properties (key/value assignments).
   */
  @Override public void clearProperties() {
    Set<String> keys = getPropertyKeys();
    if ( keys != null ) {
      dirty = true;
      for ( String key : keys ) {
        removeProperty( key );
      }
    }
  }

  @Override public boolean containsKey( String key ) {
    return getProperty( key ) != null;
  }

  /**
   * Gets the property value for the specified key.
   *
   * @param key the lookup key
   * @return the value object for the property, or null if none is found
   */
  @Override
  public Object getProperty( String key ) {
    return v.getProperty( key );
  }

  /*
     * (non-Javadoc)
     *
     * @see IMetaverseNode#setProperty(java.lang.String, java.lang.Object)
     */
  @Override
  public void setProperty( String key, Object value ) {
    dirty = true;
    v.setProperty( key, value );
  }

  /**
   * Removes and returns the value assigned to the property for the given key.
   *
   * @param key the key for which to remove the property's value
   * @return the value that was removed, or null if the key or value could not be found
   */
  @Override public Object removeProperty( String key ) {
    dirty = true;
    return v.removeProperty( key );
  }

  /**
   * Adds the edge.
   *
   * @param arg0 the arg0
   * @param arg1 the arg1
   * @return the edge
   */
  public Edge addEdge( String arg0, Vertex arg1 ) {
    return v.addEdge( arg0, arg1 );
  }

  /**
   * Gets the edges.
   *
   * @param arg0 the arg0
   * @param arg1 the arg1
   * @return the edges
   */
  public Iterable<Edge> getEdges( Direction arg0, String... arg1 ) {
    return v.getEdges( arg0, arg1 );
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public Object getId() {
    return v.getId();
  }

  /**
   * Gets the vertices.
   *
   * @param arg0 the arg0
   * @param arg1 the arg1
   * @return the vertices
   */
  public Iterable<Vertex> getVertices( Direction arg0, String... arg1 ) {
    return v.getVertices( arg0, arg1 );
  }

  /**
   * Query.
   *
   * @return the vertex query
   */
  public VertexQuery query() {
    return v.query();
  }

  /**
   * Removes the.
   */
  public void remove() {
    v.remove();
  }

  /**
   * Gets a string representation of what makes this node logically unique. If no logicalId is present, then
   * getStringId() is returned instead
   * @return the string representation of the logical id
   */
  @Override
  public String getLogicalId() {
    if ( logicalIdGenerator == null ) {
      return getStringID();
    } else if ( logicalId == null || isDirty() ) {
      logicalId = logicalIdGenerator.generateId( this );
    }

    return logicalId == null ? getStringID() : logicalId;
  }

  @Override
  public void setLogicalIdGenerator( ILogicalIdGenerator idGenerator ) {
    // clear out the logicalId so it will be re-generated on the next call to getLogicalId
    logicalId = null;
    logicalIdGenerator = idGenerator;
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void setDirty( boolean dirty ) {
    this.dirty = dirty;
  }
}
