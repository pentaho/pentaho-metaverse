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
 * Implementation of an @see IDocument
 *
 * @author jdixon
 */
public class MetaverseDocument extends PropertiesHolder implements IDocument {

  protected ILogicalIdGenerator logicalIdGenerator = DictionaryConst.LOGICAL_ID_GENERATOR_DEFAULT;
  private String logicalId;

  /**
   * The content of this document.
   */
  private Object content;

  /**
   * The namespace which declares the domain for this document
   */
  private INamespace namespace;

  /**
   * The context (static, runtime, e.g.) associated with this metaverse document.
   */
  private IAnalysisContext context;

  /* (non-Javadoc)
   * @see IDocument#getExtension()
   */
  @Override
  public String getExtension() {
    return getPropertyAsString( "extension" );
  }

  /* (non-Javadoc)
   * @see IDocument#setExtension( String extension)
   */
  @Override
  public void setExtension( String extension ) {
    setProperty( "extension", extension );
  }

  /* (non-Javadoc)
   * @see IDocument#getMimeType()
   */
  @Override
  public String getMimeType() {
    return getPropertyAsString( "mimeType" );
  }

  /* (non-Javadoc)
   * @see IDocument#setMimeType( String mimeType )
   */
  @Override
  public void setMimeType( String mimeType ) {
    setProperty( "mimeType", mimeType );
  }

  /* (non-Javadoc)
   * @see IDocument#getContent()
   */
  @Override
  public Object getContent() {
    return content;
  }

  /**
   * Sets the content object for this document
   *
   * @param content the new content
   */
  @Override
  public void setContent( Object content ) {
    this.content = content;
  }

  @Override
  public void setName( String name ) {
    setProperty( "name", name );
  }

  @Override
  public void setType( String type ) {
    setProperty( "type", type );
  }

  /* (non-Javadoc)
   * @see IIdentifiable#getName()
   */
  @Override
  public String getName() {
    return getPropertyAsString( "name" );
  }

  /* (non-Javadoc)
   * @see IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    return getPropertyAsString( "id" );
  }

  /* (non-Javadoc)
   * @see IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return getPropertyAsString( "type" );
  }

  /**
   * Sets the string ID for this document.
   *
   * @param id the ID to set
   * @see IIdentifierModifiable#setStringID(java.lang.String)
   */
  @Override
  public void setStringID( String id ) {
    setProperty( "id", id );
  }

  @Override
  public void setNamespace( INamespace namespace ) {
    this.namespace = namespace;
  }

  @Override
  public INamespace getNamespace() {
    return namespace;
  }

  /**
   * The entity namespace
   *
   * @return the namespace id, represents the container for this element
   */
  @Override public String getNamespaceId() {
    return namespace.getNamespaceId();
  }

  /**
   * Get the namespace one level above the current entity namespace
   *
   * @return the INamespace of the entity one level above the current
   */
  @Override public INamespace getParentNamespace() {
    return namespace.getParentNamespace();
  }

  @Override public INamespace getSiblingNamespace( String name, String type ) {
    return namespace.getSiblingNamespace( name, type );
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
   * Sets the context ("static", "runtime", e.g.) associated with the component described by this descriptor.
   *
   * @param context the context for the described component
   */
  @Override
  public void setContext( IAnalysisContext context ) {
    this.context = context;
  }
  /**
   * Gets a string representation of what makes this node logically unique. If no logicalId is present, then
   * getStringId() is returned instead
   * @return
   */
  @Override
  public String getLogicalId() {
    if ( logicalIdGenerator == null ) {
      return getStringID();
    } else if ( logicalId == null || isDirty() ) {
      logicalId = logicalIdGenerator.generateId( this );
    }

    return logicalId == null ? getStringID() : logicalId;
  }

  @Override
  public void setLogicalIdGenerator( ILogicalIdGenerator idGenerator ) {
    // clear out the logicalId so it will be re-generated on the next call to getLogicalId
    logicalId = null;
    logicalIdGenerator = idGenerator;
  }
}
