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
