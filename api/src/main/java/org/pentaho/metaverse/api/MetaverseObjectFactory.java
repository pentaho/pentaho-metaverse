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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseLink;
import org.pentaho.dictionary.MetaverseTransientNode;

import java.util.Map;
import java.util.UUID;

public class MetaverseObjectFactory implements IMetaverseObjectFactory {

  private static MetaverseObjectFactory instance;

  @VisibleForTesting
  public MetaverseObjectFactory() {
  }

  public static MetaverseObjectFactory getInstance() {
    if ( null == instance ) {
      instance = new MetaverseObjectFactory();
    }
    return instance;
  }

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
