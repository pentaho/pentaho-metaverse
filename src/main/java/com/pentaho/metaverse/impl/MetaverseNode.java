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

import java.util.Set;

import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.pentaho.dictionary.DictionaryConst;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;

/**
 * The MetaverseNode class is a wrapper around a corresponding Blueprints Vertex object, and delegates all methods to
 * that vertex.
 * 
 * @author mburgess
 */
public class MetaverseNode implements IMetaverseNode {

  /** The Blueprints-backed Vertex for this metaverse node */
  protected Vertex v;

  /**
   * Private constructor to prevent instantiation without an ID or backing Vertex
   */
  @SuppressWarnings( "unused" )
  private MetaverseNode() {
  }

  /**
   * Constructor that builds a MetaverseNode from a Vertex.
   * @param v Vertex
   */
  public MetaverseNode( Vertex v ) {
    this.v = v;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getName()
   */
  @Override
  public String getName() {
    return v.getProperty( DictionaryConst.PROPERTY_NAME );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    return ( v.getId() == null ) ? null : v.getId().toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return v.getProperty( DictionaryConst.PROPERTY_TYPE );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    v.setProperty( DictionaryConst.PROPERTY_NAME, name );

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#setType(java.lang.String)
   */
  @Override
  public void setType( String type ) {
    v.setProperty( DictionaryConst.PROPERTY_TYPE, type );

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#getProperty(java.lang.String)
   */
  @Override
  public <T> T getProperty( String key ) {
    return v.getProperty( key );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#getPropertyKeys()
   */
  @Override
  public Set<String> getPropertyKeys() {
    return v.getPropertyKeys();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#setProperty(java.lang.String, java.lang.Object)
   */
  @Override
  public void setProperty( String key, Object value ) {
    v.setProperty( key, value );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#removeProperty(java.lang.String)
   */
  @Override
  public <T> T removeProperty( String key ) {
    return v.removeProperty( key );
  }

  /**
   * Adds the edge.
   * 
   * @param arg0
   *          the arg0
   * @param arg1
   *          the arg1
   * @return the edge
   */
  public Edge addEdge( String arg0, Vertex arg1 ) {
    return v.addEdge( arg0, arg1 );
  }

  /**
   * Gets the edges.
   * 
   * @param arg0
   *          the arg0
   * @param arg1
   *          the arg1
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
   * @param arg0
   *          the arg0
   * @param arg1
   *          the arg1
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

}
