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
