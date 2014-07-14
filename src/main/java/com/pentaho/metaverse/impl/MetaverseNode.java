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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pentaho.platform.api.metaverse.IMetaverseNode;

/**
 * An implementation of a metaverse node
 *
 */
public class MetaverseNode implements IMetaverseNode {
  
  protected Map<String,Object> propertyMap = new HashMap<String,Object>(5);
  
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getName()
   */
  @Override
  public String getName() {
    Object name = propertyMap.get("name");
    if(name == null) {
      return null;
    }
    return (String)name;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    Object id = propertyMap.get("id");
    if(id == null) {
      return null;
    }
    return (String)id;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getType()
   */
  @Override
  public String getType() {
    Object type = propertyMap.get("type");
    if(type == null) {
      return null;
    }
    return (String)type;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    propertyMap.put( "name", name );    
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setStringID(java.lang.String)
   */
  @Override
  public void setStringID( String id ) {
    propertyMap.put( "id", id );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setType(java.lang.String)
   */
  @Override
  public void setType( String type ) {
    propertyMap.put( "type", type );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#getProperty(java.lang.String)
   */
  @Override
  public <T> T getProperty( String key ) {
    return (T)propertyMap.get(key);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#getPropertyKeys()
   */
  @Override
  public Set<String> getPropertyKeys() {
    return propertyMap.keySet();
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#setProperty(java.lang.String, java.lang.Object)
   */
  @Override
  public void setProperty( String key, Object value ) {
    propertyMap.put( key, value );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseNode#removeProperty(java.lang.String)
   */
  @Override
  public <T> T removeProperty( String key ) {
    return (T) propertyMap.remove( key );
  }

}
