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

package org.pentaho.metaverse.api;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseLink;
import org.pentaho.dictionary.MetaverseTransientNode;

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
