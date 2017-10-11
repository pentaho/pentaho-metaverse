/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
