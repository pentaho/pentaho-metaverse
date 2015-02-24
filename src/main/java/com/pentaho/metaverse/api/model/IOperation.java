/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package com.pentaho.metaverse.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pentaho.metaverse.analyzer.kettle.ChangeType;

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

  @JsonProperty( JSON_PROPERTY_CATEGORY )
  String getCategory();

  void setCategory( String category );

  @JsonProperty( JSON_PROPERTY_TYPE )
  ChangeType getType();

  void setType( ChangeType type );
}
