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


package org.pentaho.metaverse.api;

/**
 * The IDocument interface represents a document in the metaverse.
 */
public interface IDocument extends IComponentDescriptor, IIdentifierModifiable, IHasProperties {

  /**
   * Gets the object representing the content of this document
   * 
   * @return the content of this object
   */
  public Object getContent();

  /**
   * Sets the content object for this document.
   * 
   * @param content
   *          the new content
   */
  public void setContent( Object content );

  /**
   * Gets the file extension for this document;
   * characters only, the dot (.) is excluded
   *
   * @return the extension associated with this document
   */
  public String getExtension();

  /**
   *  Set the extension for this document
   *
   * @param extension
   */
  public void setExtension( String extension );

  /**
   * Returns the RFC compliant string version of the MIME content type,
   * if it can be determined for this document.
   *
   * @return MIME content type if available; otherwise null
   */
  public String getMimeType();

  /**
   *  Set the MIME content type for this document.
   *
   * @param type
   */
  public void setMimeType( String type );


}
