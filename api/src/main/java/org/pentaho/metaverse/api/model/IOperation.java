/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
