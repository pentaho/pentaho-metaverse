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
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.IMetaverseNode;

import java.util.HashMap;
import java.util.Iterator;
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
    return v.property( DictionaryConst.PROPERTY_NAME ).isPresent() ? v.<String>value( DictionaryConst.PROPERTY_NAME ) : null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    final Object id = v.id();
    return id == null ? null : id.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return v.property( DictionaryConst.PROPERTY_TYPE ).isPresent() ? v.<String>value( DictionaryConst.PROPERTY_TYPE ) : null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    dirty = true;
    v.property( DictionaryConst.PROPERTY_NAME, name );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#setType(java.lang.String)
   */
  @Override
  public void setType( String type ) {
    dirty = true;
    v.property( DictionaryConst.PROPERTY_TYPE, type );
    String category = DictionaryHelper.getCategoryForType( type );
    v.property( DictionaryConst.PROPERTY_CATEGORY, category );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IMetaverseNode#getPropertyKeys()
   */
  @Override
  public Set<String> getPropertyKeys() {
    return v.keys();
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
        props.put( key, v.property( key ).isPresent() ? v.value( key ) : null );
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
        v.property( property.getKey(), property.getValue() );
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
        v.property( key ).remove();
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
    return v.property( key ).isPresent() ? v.value( key ) : null;
  }

  /*
     * (non-Javadoc)
     *
     * @see IMetaverseNode#setProperty(java.lang.String, java.lang.Object)
     */
  @Override
  public void setProperty( String key, Object value ) {
    dirty = true;
    v.property( key, value );
  }

  /**
   * Removes and returns the value assigned to the property for the given key.
   *
   * @param key the key for which to remove the property's value
   * @return the value that was removed, or null if the key or value could not be found
   */
  @Override public Object removeProperty( String key ) {
    dirty = true;
    Object val = v.property( key ).isPresent() ? v.value( key ) : null;
    v.property( key ).remove();
    return val;
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
  public Iterator<Edge> getEdges( Direction arg0, String... arg1 ) {
    return v.edges( arg0, arg1 );
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public Object getId() {
    return v.id();
  }

  /**
   * Gets the vertices.
   *
   * @param arg0 the arg0
   * @param arg1 the arg1
   * @return the vertices
   */
  public Iterator<Vertex> getVertices( Direction arg0, String... arg1 ) {
    return v.vertices( arg0, arg1 );
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
