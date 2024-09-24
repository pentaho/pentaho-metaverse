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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;

import java.util.List;

/**
 * The IMetaverseReader provides low-level methods for retrieving entities from the metaverse, such as nodes, links, and
 * graphs.
 */
public interface IMetaverseReader {

  /**
   * The token to use for XML format exports
   */
  String FORMAT_XML = "xml";

  /**
   * The token to use for GraphJSON format exports
   */
  String FORMAT_JSON = "json";

  /**
   * The token to use for CSV format exports
   */
  String FORMAT_CSV = "csv";

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
  String exportToXml( );

  /**
   * Export the metaverse to a String in the given format.
   * 
   * @param format The export format. One of "JSON", "GRAPHML", "CSV"
   * @return the string representation of the metaverse model
   */
  String exportFormat( String format );

  /**
   * Export the metaverse to a String.
   * 
   * @param resultTypes The export format. One of "JSON", "GRAPHML", "CSV"
   * @param out The output stream to export to
   * @throws IOException If the output stream cannot be written to
   * /
  public void exportToStream( String format, OutputStream out ) throws IOException;

  /**
   * Searches the metaverse for nodes of the specified result types, by traversing the graph model from the specified
   * starting node(s).
   * 
   * @param resultTypes the result types
   * @param startNodeIDs the start node ids
   * @param shortestOnly only return the shortest paths
   * @return the graph
   */
  Graph search( List<String> resultTypes, List<String> startNodeIDs, boolean shortestOnly );

  /**
   * Returns the graph for a given element
   * @param id The id of the root node
   * @return A graph of the upstream and downstream nodes that are connected
   */
  Graph getGraph( String id );

  /**
   * Returns a list of nodes that match a given property value
   * @param property The name of the property
   * @param value The value of the property
   * @return A list of matching nodes
   */
  List<IMetaverseNode> findNodes( String property, String value );

}
