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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public interface IExternalResourceInfo extends IInfo {

  public static final String JSON_PROPERTY_TYPE = "type";
  public static final String JSON_PROPERTY_INPUT = "input";
  public static final String JSON_PROPERTY_OUTPUT = "output";
  public static final String JSON_PROPERTY_ATTRIBUTES = "attributes";

  @JsonProperty( JSON_PROPERTY_TYPE )
  public String getType();

  @JsonProperty( JSON_PROPERTY_INPUT )
  public boolean isInput();

  @JsonProperty( JSON_PROPERTY_OUTPUT )
  public boolean isOutput();

  @JsonProperty( JSON_PROPERTY_ATTRIBUTES )
  public Map<Object, Object> getAttributes();

  /**
   * Cleaning sensitive data
   */
  default void cleanupSensitiveData() {
  }
}
