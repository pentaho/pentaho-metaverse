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
