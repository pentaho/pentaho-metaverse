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


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * The IMetaverseBuilder is a Builder that creates and maintains a metaverse model, which contains nodes and links.
 */
public interface IMetaverseBuilder {

  /**
   * Adds the specified node to the metaverse model.
   *
   * @param node the node to add
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder addNode( IMetaverseNode node );

  /**
   * Adds the specified link to the model. If the link refers to nodes that do not yet exist in the model, then
   * placeholder node(s) should also be inserted to maintain a valid graph model.
   *
   * @param link the link to add
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder addLink( IMetaverseLink link );

  /**
   * Adds the link.
   *
   * @param fromNode the from node
   * @param label    the label
   * @param toNode   the to node
   * @return the i metaverse builder
   */
  IMetaverseBuilder addLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode );

  /**
   * Adds a link between the two vertices to the builder.
   *
   * @param fromVertex the from {@link Vertex}
   * @param label      the label
   * @param toVertex   the to {@link Vertex}
   */
  void addLink( Vertex fromVertex, String label, Vertex toVertex );

  /**
   * Deletes the specified node from the metaverse model.
   *
   * @param node the node to remove
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder deleteNode( IMetaverseNode node );

  /**
   * Deletes the specified link from the metaverse model.
   *
   * @param link the link to remove
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder deleteLink( IMetaverseLink link );

  /**
   * Updates the specified node to have the provided attributes.
   *
   * @param updatedNode the node with updated attributes
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder updateNode( IMetaverseNode updatedNode );

  /**
   * Updates the specified link to have the provided attributes.
   *
   * @param link     the link
   * @param newLabel the new label
   * @return the metaverse builder (for chaining)
   */
  IMetaverseBuilder updateLinkLabel( IMetaverseLink link, String newLabel );

  /**
   * Returns a metaverse object factory for creating metaverse components (nodes, links, e.g.)
   *
   * @return a metaverse object factory
   */
  IMetaverseObjectFactory getMetaverseObjectFactory();

  /**
   * Sets the metaverse object factory for this builder. Most classes that need a metaverse object factory will get it
   * from their builder (if they have implemented IRequiresMetaverseBuilder
   *
   * @param metaverseObjectFactory the metaverse object factory to set
   */
  void setMetaverseObjectFactory( IMetaverseObjectFactory metaverseObjectFactory );

  /**
   * Returns the underlying graph associated with this builder
   *
   * @return the backing Graph object
   */
  Graph getGraph();

  /**
   * Sets the underlying graph for this builder
   *
   * @param graph the graph to set for this builder
   */
  void setGraph( Graph graph );

}
