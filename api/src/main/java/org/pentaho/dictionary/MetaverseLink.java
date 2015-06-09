/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
