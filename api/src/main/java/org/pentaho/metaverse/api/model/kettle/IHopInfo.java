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


package org.pentaho.metaverse.api.model.kettle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.pentaho.metaverse.api.model.IInfo;

/**
 * User: RFellows Date: 11/3/14
 */
@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = IInfo.JSON_PROPERTY_CLASS )
public interface IHopInfo {
  public static final String JSON_PROPERTY_FROM_STEP_NAME = "fromStepName";
  public static final String JSON_PROPERTY_TO_STEP_NAME = "toStepName";
  public static final String JSON_PROPERTY_TYPE = "type";
  public static final String JSON_PROPERTY_ENABLED = "enabled";

  @JsonProperty( JSON_PROPERTY_FROM_STEP_NAME )
  public String getFromStepName();

  @JsonProperty( JSON_PROPERTY_TO_STEP_NAME )
  public String getToStepName();

  @JsonProperty( JSON_PROPERTY_TYPE )
  public String getType();

  @JsonProperty( JSON_PROPERTY_ENABLED )
  public boolean isEnabled();
}
