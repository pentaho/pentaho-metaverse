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


package org.pentaho.metaverse.impl.model;

import org.pentaho.metaverse.api.model.BaseInfo;
import org.pentaho.metaverse.api.model.IVersionInfo;

/**
 * VersionInfo is a base implementation of a bean for IVersionInfo, providing version, name, and description
 */
public class VersionInfo extends BaseInfo implements IVersionInfo {

  protected String version;

  /**
   * Returns the version string for this artifact
   *
   * @return a string representation of the version
   */
  @Override
  public String getVersion() {
    return version;
  }

  /**
   * Sets the version string for this artifact
   *
   * @param version the version string to set for the artifact
   */
  @Override
  public void setVersion( String version ) {
    this.version = version;
  }
}
