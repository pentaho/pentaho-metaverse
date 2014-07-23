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

package com.pentaho.metaverse.api;

import java.util.List;

import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;

/**
 * The IMetaverseReader provides low-level methods for retrieving entities from the metaverse, such as nodes, links, and
 * graphs.
 */
public interface IMetaverseReader {

  /**
   * Retrieves the node having the specified ID.
   * 
   * @param id
   *          the identifier of the node
   * @return the node with the specified ID (if it exists), null if no node was found
   */
  IMetaverseNode findNode( String id );

  /**
   * Retrieves the link having the specified nodes, type, and direction
   * 
   * @param leftNodeID
   *          the id of the left node
   * @param linkType
   *          the link type
   * @param rightNodeID
   *          the id of the right node
   * @param direction
   *          the direction of the link
   * @return the link object corresponding to the specified parameters, or null if none was found
   */
  IMetaverseLink findLink( String leftNodeID, String linkType, String rightNodeID, Direction direction );

  /**
   * Gets the metaverse graph model
   * 
   * @return a graph of the metaverse
   */
  Graph getMetaverse();

  /**
   * Export the metaverse to a String.
   * 
   * @return the string representation of the metaverse model
   */
  String export();

  /**
   * Searches the metaverse for nodes of the specified result types, by traversing the graph model from the specified
   * starting node(s).
   * 
   * @param resultTypes
   *          the result types
   * @param startNodeIDs
   *          the start node i ds
   * @return the graph
   */
  Graph search( List<String> resultTypes, List<String> startNodeIDs, boolean shortestOnly );

  /**
   * Returns the graph for a given element
   * @param id The id of the root node
   * @return A graph of the upstream and downstream nodes that are connected
   */
  Graph getGraph( String id );

}
