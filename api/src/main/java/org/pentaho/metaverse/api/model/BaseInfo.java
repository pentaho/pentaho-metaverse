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

package org.pentaho.metaverse.api.model;

public class BaseInfo implements IInfo {
  private String name;
  private String description;

  /**
   * Returns the name of this artifact
   *
   * @return a string representing the name of this artifact
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns a description of this artifact
   *
   * @return a string representing the description of this artifact
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Sets the name of this artifact
   *
   * @param name the name to set for the artifact
   */
  @Override
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * Sets the description of this artifact
   *
   * @param description the description to set for the artifact
   */
  @Override
  public void setDescription( String description ) {
    this.description = description;
  }
}
