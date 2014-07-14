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

/**
 * Implementation of an @see org.pentaho.platform.api.metaverse.IMetaverseDocument
 * 
 * @author jdixon
 * 
 */
public class MetaverseDocument implements IMetaverseDocument {

  /** The name of the document. */
  private String name;
  
  /** The identifier for this document. */
  private String id;
  
  /** The type of this document. */
  private String type;
  
  /** The content of this document. */
  private Object content;

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IMetaverseDocument#getContent()
   */
  public Object getContent() {
    return content;
  }

  /**
   * Sets the content object for this document
   *
   * @param content the new content
   */
  public void setContent( Object content ) {
    this.content = content;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    this.name = name;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setType(java.lang.String)
   */
  @Override
  public void setType( String type ) {
    this.type = type;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getStringID()
   */
  @Override
  public String getStringID() {
    return id;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.metaverse.IIdentifiable#getType()
   */
  @Override
  public String getType() {
    return type;
  }

  /**
   * Sets the string ID for this document.
   *
   * @param id          the ID to set
   * @see org.pentaho.platform.api.metaverse.IIdentifiableWritable#setStringID(java.lang.String)
   */
  @Override
  public void setStringID( String id ) {
    this.id = id;
  }

}
