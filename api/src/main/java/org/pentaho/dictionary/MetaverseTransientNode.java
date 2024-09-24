/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.dictionary;

import org.pentaho.metaverse.api.IIdentifierModifiable;
import org.pentaho.metaverse.api.ILogicalIdGenerator;
import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.PropertiesHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of a metaverse node.
 */
public class MetaverseTransientNode extends PropertiesHolder implements IMetaverseNode, IIdentifierModifiable {

  /**
   * The links from this node
   */
  protected List<IMetaverseLink> links = new ArrayList<IMetaverseLink>();
  protected ILogicalIdGenerator logicalIdGenerator = DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT;
  private String logicalId;

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
   * @see IIdentifiable#getName()
   */
  @Override
  public String getName() {
    return getPropertyAsString( DictionaryConst.PROPERTY_NAME );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    return getPropertyAsString( DictionaryConst.PROPERTY_ID );
  }

  /*
   * (non-Javadoc)
   * 
   * @see IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return getPropertyAsString( DictionaryConst.PROPERTY_TYPE );
  }

  @Override
  public void setName( String name ) {
    setProperty( DictionaryConst.PROPERTY_NAME, name );
  }

  @Override
  public void setStringID( String id ) {
    setProperty( DictionaryConst.PROPERTY_ID, id );
  }

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

  /**
   * Gets a string representation of what makes this node logically unique. If no logicalId is present, then
   * getStringId() is returned instead
   * @return
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
}
