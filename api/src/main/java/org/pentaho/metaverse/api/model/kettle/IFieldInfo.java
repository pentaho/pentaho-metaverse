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

package org.pentaho.metaverse.api.model.kettle;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.pentaho.metaverse.api.model.IInfo;

/**
 * User: RFellows Date: 11/3/14
 */
public interface IFieldInfo extends IInfo {
  public static final String JSON_PROPERTY_DATA_TYPE = "dataType";
  public static final String JSON_PROPERTY_PRECISION = "precision";
  public static final String JSON_PROPERTY_LENGTH = "length";
  public static final String JSON_PROPERTY_STEP_NAME = "stepName";

  @JsonProperty( JSON_PROPERTY_DATA_TYPE )
  public String getDataType();

  @JsonProperty( JSON_PROPERTY_PRECISION )
  public Integer getPrecision();

  @JsonProperty( JSON_PROPERTY_LENGTH )
  public Integer getLength();

  @JsonProperty( JSON_PROPERTY_STEP_NAME )
  public String getStepName();

}
