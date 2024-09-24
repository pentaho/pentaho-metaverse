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

/**
 * The IIdentifierModifiable interface augments the IIdentifiable interface by allowing for the changing of the unique
 * string identifier for objects that support it
 */
public interface IIdentifierModifiable extends IIdentifiable {

  /**
   * Sets the string id.
   * 
   * @param id
   *          the new string id
   */
  void setStringID( String id );

}
