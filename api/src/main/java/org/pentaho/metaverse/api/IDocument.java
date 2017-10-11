/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
