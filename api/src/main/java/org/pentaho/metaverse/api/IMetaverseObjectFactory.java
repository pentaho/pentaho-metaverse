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
