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

package org.pentaho.dictionary;

import org.pentaho.metaverse.api.IMetaverseLink;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.PropertiesHolder;

/**
 * Metaverse Link
 */
public class MetaverseLink extends PropertiesHolder implements IMetaverseLink {

  private IMetaverseNode fromNode;
  private IMetaverseNode toNode;

  /**
   * Default constructor
   */
  public MetaverseLink() {

  }

  /**
   * Constructor for creating a link with a single call
   *
   * @param fromNode from node
   * @param label    label for the link
   * @param toNode   to node
   */
  public MetaverseLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode ) {
    setFromNode( fromNode );
    setToNode( toNode );
    setLabel( label );
  }

  @Override
  public IMetaverseNode getFromNode() {
    return fromNode;
  }

  @Override
  public IMetaverseNode getToNode() {
    return toNode;
  }

  @Override
  public String getLabel() {
    if ( containsKey( DictionaryConst.PROPERTY_LABEL ) ) {
      return getPropertyAsString( DictionaryConst.PROPERTY_LABEL );
    }
    return null;
  }

  @Override
  public void setFromNode( IMetaverseNode fromNode ) {
    this.fromNode = fromNode;
  }

  @Override
  public void setToNode( IMetaverseNode toNode ) {
    this.toNode = toNode;
  }

  @Override
  public void setLabel( String label ) {
    properties.put( DictionaryConst.PROPERTY_LABEL, label );
  }
}
