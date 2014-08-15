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

package com.pentaho.dictionary;

import java.util.ArrayList;
import java.util.List;

import com.pentaho.metaverse.impl.PropertiesHolder;
import org.pentaho.platform.api.metaverse.IIdentifierModifiable;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

/**
 * An implementation of a metaverse node.
 */
public class MetaverseTransientNode extends PropertiesHolder implements IMetaverseNode, IIdentifierModifiable {

  /**
   * The links from this node
   */
  protected List<IMetaverseLink> links = new ArrayList<IMetaverseLink>();

  /**
   * Instantiates a new (empty) metaverse transient node.
   */
  public MetaverseTransientNode() {
  }

  /**
   * Instantiates a new metaverse transient node.
   *
   * @param id the id
   */
  public MetaverseTransientNode( String id ) {
    this();
    setStringID( id );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getName()
   */
  @Override
  public String getName() {
    return getPropertyAsString( DictionaryConst.PROPERTY_NAME );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    return getPropertyAsString( DictionaryConst.PROPERTY_ID );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return getPropertyAsString( DictionaryConst.PROPERTY_TYPE );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    setProperty( DictionaryConst.PROPERTY_NAME, name );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setStringID(java.lang.String)
   */
  @Override
  public void setStringID( String id ) {
    setProperty( DictionaryConst.PROPERTY_ID, id );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setType(java.lang.String)
   */
  @Override
  public void setType( String type ) {
    setProperty( "type", type );
    String category = DictionaryHelper.getCategoryForType( type );
    setProperty( DictionaryConst.PROPERTY_CATEGORY, category );
  }

  /**
   * Adds a link to this node
   *
   * @param link The link to add
   */
  public void addLink( IMetaverseLink link ) {
    links.add( link );
  }

  /**
   * Removes a link from this node
   *
   * @param link The link to remove
   */
  public void removeLink( IMetaverseLink link ) {
    links.remove( link );
  }

  /**
   * Returns the set of links for this node
   *
   * @return The links
   */
  public List<IMetaverseLink> getLinks() {
    return links;
  }

}
