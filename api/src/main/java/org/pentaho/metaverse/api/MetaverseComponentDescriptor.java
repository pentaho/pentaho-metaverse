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

/**
 * Created by mburgess on 8/12/14.
 */
public class MetaverseComponentDescriptor implements IComponentDescriptor {

  private String name;
  private String type;
  private INamespace namespace;
  private IAnalysisContext context;

  public MetaverseComponentDescriptor( String name, String type, INamespace namespace ) {
    this.name = name;
    this.type = type;
    this.namespace = namespace;
    this.context = new AnalysisContext( DictionaryConst.CONTEXT_DEFAULT, null );
  }

  public MetaverseComponentDescriptor( String name, String type, INamespace namespace, IAnalysisContext context ) {
    this.name = name;
    this.type = type;
    this.namespace = namespace;
    this.context = context;
  }

  public MetaverseComponentDescriptor( String name, String type, IMetaverseNode parentNode ) {
    this( name, type, parentNode, new AnalysisContext( DictionaryConst.CONTEXT_DEFAULT ) );
  }

  public MetaverseComponentDescriptor( String name, String type, IMetaverseNode parentNode, IAnalysisContext context ) {
    this.name = name;
    this.type = type;
    if ( parentNode != null ) {
      namespace = new Namespace( parentNode.getLogicalId() );
    }
    this.context = context;
  }

  /**
   * Gets the name of this entity.
   *
   * @return the String name of the entity
   */
  @Override public String getName() {
    return name;
  }

  /**
   * Gets the metaverse-unique identifier for this entity.
   *
   * @return the String ID of the entity.
   */
  @Override public String getStringID() {
    return getNamespaceId();
  }

  /**
   * Gets the type of this entity.
   *
   * @return the String type of the entity
   */
  @Override public String getType() {
    return type;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  @Override public void setName( String name ) {
    this.name = name;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  @Override public void setType( String type ) {
    this.type = type;
  }

  @Override public String getLogicalId() {
    return getNamespaceId();
  }

  @Override public void setLogicalIdGenerator( ILogicalIdGenerator idGenerator ) {
    // ignore this for now
  }

  /**
   * The entity namespace
   *
   * @return the namespace id, represents the container for this element
   */
  @Override public String getNamespaceId() {
    return namespace == null ? null : namespace.getNamespaceId();
  }

  /**
   * @return the INamespace of the entity one level above the current
   */
  @Override public INamespace getParentNamespace() {
    return namespace.getParentNamespace();
  }

  @Override public INamespace getSiblingNamespace( String name, String type ) {
    return namespace.getSiblingNamespace( name, type );
  }

  @Override public void setNamespace( INamespace namespace ) {
    this.namespace = namespace;
  }

  @Override public INamespace getNamespace() {
    return namespace;
  }

  /**
   * Gets the context ("static", "runtime", e.g.) associated with the component described by this descriptor.
   *
   * @return A string containing a description of the context associated with the described component
   */
  @Override
  public IAnalysisContext getContext() {
    return context;
  }

  /**
   * Sets the context (static, runtime, e.g.) associated with the component described by this descriptor.
   *
   * @param context the context for the described component
   */
  @Override
  public void setContext( IAnalysisContext context ) {
    this.context = context;
  }
}
