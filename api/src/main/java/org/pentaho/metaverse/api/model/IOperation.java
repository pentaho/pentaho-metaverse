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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.pentaho.metaverse.api.ChangeType;

/**
 * Created by mburgess on 2/24/15.
 */
@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = IInfo.JSON_PROPERTY_CLASS )
public interface IOperation extends IInfo {
  String JSON_PROPERTY_CATEGORY = "category";
  String JSON_PROPERTY_TYPE = "type";

  String METADATA_CATEGORY = "changeMetadata";
  String MAPPING_CATEGORY = "mapping";
  String CALC_CATEGORY = "calculation";
  String AGG_CATEGORY = "aggregation";

  @JsonProperty( JSON_PROPERTY_CATEGORY )
  String getCategory();

  void setCategory( String category );

  @JsonProperty( JSON_PROPERTY_TYPE )
  ChangeType getType();

  void setType( ChangeType type );
}
