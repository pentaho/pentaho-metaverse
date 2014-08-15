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

import org.pentaho.platform.api.metaverse.IMetaverseDocument;
import org.pentaho.platform.api.metaverse.INamespace;

/**
 * Implementation of an @see org.pentaho.platform.api.metaverse.IMetaverseDocument
 *
 * @author jdixon
 */
public class MetaverseDocument extends PropertiesHolder implements IMetaverseDocument {

  /**
   * The content of this document.
   */
  private Object content;

  /**
   * The namespace which declares the domain for this document
   */
  private INamespace namespace;

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseDocument#getExtension()
   */
  @Override
  public String getExtension() {
    return getPropertyAsString( "extension" );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseDocument#setExtension( String extension)
   */
  @Override
  public void setExtension( String extension ) {
    setProperty( "extension", extension );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseDocument#getMimeType()
   */
  @Override
  public String getMimeType() {
    return getPropertyAsString( "mimeType" );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseDocument#setMimeType( String mimeType )
   */
  @Override
  public void setMimeType( String mimeType ) {
    setProperty( "mimeType", mimeType );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseDocument#getContent()
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

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    setProperty( "name", name );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setType(java.lang.String)
   */
  @Override
  public void setType( String type ) {
    setProperty( "type", type );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getName()
   */
  @Override
  public String getName() {
    return getPropertyAsString( "name" );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    return getPropertyAsString( "id" );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return getPropertyAsString( "type" );
  }

  /**
   * Sets the string ID for this document.
   *
   * @param id the ID to set
   * @see org.pentaho.platform.api.metaverse.IIdentifierModifiable#setStringID(java.lang.String)
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

  /**
   * get the name space for the current level entity
   *
   * @param child the string representation of hte current entity's contribution to the namespace path
   * @return the namespace object for the entity represented by child
   */
  @Override public INamespace getChildNamespace( String child, String type ) {
    return namespace.getChildNamespace( child, type );
  }
}
