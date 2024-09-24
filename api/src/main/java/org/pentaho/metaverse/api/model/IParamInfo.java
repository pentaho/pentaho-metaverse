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

public interface IParamInfo<T> extends IInfo {

  public static final String JSON_PROPERTY_DEFAULT_VALUE = "defaultValue";
  public static final String JSON_PROPERTY_VALUE = "value";

  @JsonProperty( JSON_PROPERTY_DEFAULT_VALUE )
  public T getDefaultValue();

  @JsonProperty( JSON_PROPERTY_VALUE )
  public T getValue();
}
