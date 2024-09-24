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
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = IInfo.JSON_PROPERTY_CLASS )
public interface IInfo {
  public static final String JSON_PROPERTY_CLASS = "@class";
  public static final String JSON_PROPERTY_NAME = "name";
  public static final String JSON_PROPERTY_DESCRIPTION = "description";

  @JsonProperty( JSON_PROPERTY_NAME )
  public String getName();

  public void setName( String name );

  @JsonProperty( JSON_PROPERTY_DESCRIPTION )
  public String getDescription();

  public void setDescription( String description );
}
