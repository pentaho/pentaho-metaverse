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

import java.util.Map;

/**
 * A factory interface for creating metaverse objects.
 */
public interface IMetaverseObjectFactory {

  /**
   * Creates a new metaverse document object.
   * 
   * @return the new IDocument instance
   */
  IDocument createDocumentObject();

  /**
   * Creates a new metaverse node object and adds it to the current metaverse
   * @param id id of the new node
   * @return the new IMetaverseNode instance
   */
  IMetaverseNode createNodeObject( String id );

  /**
   * Creates a new metaverse node and sets its name and type properties as well
   * @param id id of the new node
   * @param name name of the new node
   * @param type type of the new node
   * @return
   */
  IMetaverseNode createNodeObject( String id, String name, String type );

  /**
   * Creates a new metaverse node and sets its name and type properties as well.
   * The id of the node will be a UUID string.
   * @param namespace namespace of the new node
   * @param name name of the new node
   * @param type type of the new node
   * @return
   */
  IMetaverseNode createNodeObject( INamespace namespace, String name, String type );

  /**
   * Creates a new metaverse node and sets its name and type properties as well
   * @param namespace namespace of the new node
   * @param idGenerator ILogicalIdGenerator for the node
   * @param properties Map of properties to set on the node
   * @return
   */
  IMetaverseNode createNodeObject( INamespace namespace,
                                   ILogicalIdGenerator idGenerator,
                                   Map<String, Object> properties );

  /**
   * Creates a new metaverse link object and adds it to the current metaverse
   * 
   * @return the new IMetaverseLink instance
   */
  IMetaverseLink createLinkObject();
}
