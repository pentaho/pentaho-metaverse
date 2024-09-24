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

package org.pentaho.metaverse.api;

/**
 * The IIdentifiable interface provides commonly used methods for identifying entities, such as name, ID, type
 * 
 */
public interface IIdentifiable {

  /**
   * Gets the name of this entity.
   * 
   * @return the String name of the entity
   */
  String getName();

  /**
   * Gets the metaverse-unique identifier for this entity.
   *
   * NOTE: This MUST return the same value as INamespace.getNamespaceId()
   *
   * @return the String ID of the entity.
   */
  String getStringID();

  /**
   * Gets the type of this entity.
   * 
   * @return the String type of the entity
   */
  String getType();

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  void setName( String name );

  /**
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  void setType( String type );


  /**
   * Gets a string representation of what makes this node logically unique
   * @return
   */
  String getLogicalId();

  /**
   * Sets the {@link ILogicalIdGenerator} to use for this node
   * @param idGenerator
   */
  void setLogicalIdGenerator( ILogicalIdGenerator idGenerator );

}
