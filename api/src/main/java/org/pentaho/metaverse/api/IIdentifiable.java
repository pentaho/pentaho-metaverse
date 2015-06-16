/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
