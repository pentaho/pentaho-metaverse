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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IVersionInfo represents a version of some artifact, including the version string, name, and description
 */
public interface IVersionInfo extends IInfo {

  String JSON_PROPERTY_VERSION = "version";

  /**
   * Returns the version string for this artifact
   *
   * @return a string representation of the version
   */
  @JsonProperty( JSON_PROPERTY_VERSION )
  public String getVersion();

  /**
   * Sets the version string for this artifact
   *
   * @param version the version string to set for the artifact
   */
  public void setVersion( String version );
}
