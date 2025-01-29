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
