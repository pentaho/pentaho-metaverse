/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
