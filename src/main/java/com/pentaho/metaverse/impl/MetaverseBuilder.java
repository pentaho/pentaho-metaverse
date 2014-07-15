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

package com.pentaho.metaverse.impl;

import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.IMetaverseLink;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;

import com.pentaho.metaverse.util.MetaverseUtil;

/**
 * @author mburgess
 * 
 */
public class MetaverseBuilder implements IMetaverseBuilder, IMetaverseObjectFactory {

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseBuilder#
   * addLink(org.pentaho.platform.api.metaverse.IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseLink arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseBuilder#
   * addNode(org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder addNode( IMetaverseNode arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#deleteLink(org.pentaho.platform.api.metaverse.IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder deleteLink( IMetaverseLink arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#deleteNode(org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder deleteNode( IMetaverseNode arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#updateLink(org.pentaho.platform.api.metaverse.IMetaverseLink)
   */
  @Override
  public IMetaverseBuilder updateLinkLabel( IMetaverseLink arg0, String newLabel ) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.platform.api.metaverse.IMetaverseBuilder#updateNode(org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder updateNode( IMetaverseNode arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseObjectFactory#createDocumentObject()
   */
  @Override
  public IMetaverseDocument createDocumentObject() {
    return MetaverseUtil.getMetaverseObjectFactory().createDocumentObject();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseObjectFactory#createLinkObject()
   */
  @Override
  public IMetaverseLink createLinkObject() {
    return MetaverseUtil.getMetaverseObjectFactory().createLinkObject();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.metaverse.IMetaverseObjectFactory#createNodeObject()
   */
  @Override
  public IMetaverseNode createNodeObject( String id ) {
    return MetaverseUtil.getMetaverseObjectFactory().createNodeObject( id );
  }

  /**
   * Adds the specified link to the model
   * 
   * @param fromNode
   *          the from node
   * @param label
   *          the label
   * @param toNode
   *          the to node
   * @return this metaverse builder
   * @see org.pentaho.platform.api.metaverse.IMetaverseBuilder#
   *      addLink(org.pentaho.platform.api.metaverse.IMetaverseNode, java.lang.String,
   *      org.pentaho.platform.api.metaverse.IMetaverseNode)
   */
  @Override
  public IMetaverseBuilder addLink( IMetaverseNode fromNode, String label, IMetaverseNode toNode ) {
    IMetaverseObjectFactory factory = MetaverseUtil.getMetaverseObjectFactory();
    IMetaverseLink link = factory.createLinkObject();

    link.setFromNode( fromNode );
    link.setLabel( label );
    link.setToNode( toNode );
    return addLink( link );
  }

}
