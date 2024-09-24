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
 * The IMetaverseLink interface represents methods operating on a link (i.e. relationship) in the metaverse
 */
public interface IMetaverseLink extends IMetaverseElement {

  /**
   * Gets the from node.
   *
   * @return the from node
   */
  public IMetaverseNode getFromNode();

  /**
   * Gets the to node.
   *
   * @return the to node
   */
  public IMetaverseNode getToNode();

  /**
   * Gets the label.
   *
   * @return the label
   */
  public String getLabel();

  /**
   * Sets the from node.
   *
   * @param fromNode the new from node
   */
  public void setFromNode( IMetaverseNode fromNode );

  /**
   * Sets the to node.
   *
   * @param toNode the new to node
   */
  public void setToNode( IMetaverseNode toNode );

  /**
   * Sets the label.
   *
   * @param label the new label
   */
  public void setLabel( String label );
}
