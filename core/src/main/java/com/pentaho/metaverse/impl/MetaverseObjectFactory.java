/*
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

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.MetaverseLink;
import com.pentaho.dictionary.MetaverseTransientNode;
import com.pentaho.metaverse.api.IDocument;
import com.pentaho.metaverse.api.ILogicalIdGenerator;
import com.pentaho.metaverse.api.IMetaverseLink;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;

import java.util.Map;
import java.util.UUID;

public class MetaverseObjectFactory implements IMetaverseObjectFactory {

  @Override
  public IDocument createDocumentObject() {
    return new MetaverseDocument( );
  }

  @Override
  public IMetaverseLink createLinkObject() {
    return new MetaverseLink( );
  }

  @Override
  public IMetaverseNode createNodeObject( String id ) {
    MetaverseTransientNode node = new MetaverseTransientNode();
    node.setStringID( id );
    node.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT );
    node.setProperty( DictionaryConst.NODE_VIRTUAL, true );
    return node;
  }

  @Override
  public IMetaverseNode createNodeObject( String id, String name, String type ) {
    IMetaverseNode node = createNodeObject( id );
    node.setName( name );
    node.setType( type );
    return node;
  }

  @Override
  public IMetaverseNode createNodeObject( INamespace namespace, String name, String type ) {
    IMetaverseNode node = createNodeObject( UUID.randomUUID().toString(), name, type );
    node.setProperty( DictionaryConst.PROPERTY_NAMESPACE, namespace.getNamespaceId() );
    return node;
  }

  @Override
  public IMetaverseNode createNodeObject( INamespace namespace,
                                          ILogicalIdGenerator idGenerator,
                                          Map<String, Object> properties ) {

    MetaverseTransientNode node = new MetaverseTransientNode();
    node.setProperties( properties );
    node.setProperty( DictionaryConst.PROPERTY_NAMESPACE, namespace.getNamespaceId() );
    node.setLogicalIdGenerator( idGenerator );
    node.setProperty( DictionaryConst.NODE_VIRTUAL, true );
    String id = node.getLogicalId();
    node.setStringID( id );

    return node;

  }
}
