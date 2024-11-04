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

import java.util.Map;

public interface IExternalResourceInfo extends IInfo, ISensitiveDataCleanable {

  String JSON_PROPERTY_TYPE = "type";
  String JSON_PROPERTY_INPUT = "input";
  String JSON_PROPERTY_OUTPUT = "output";
  String JSON_PROPERTY_ATTRIBUTES = "attributes";

  @JsonProperty( JSON_PROPERTY_TYPE )
  String getType();

  @JsonProperty( JSON_PROPERTY_INPUT )
  boolean isInput();

  @JsonProperty( JSON_PROPERTY_OUTPUT )
  boolean isOutput();

  @JsonProperty( JSON_PROPERTY_ATTRIBUTES )
  Map<Object, Object> getAttributes();
}
