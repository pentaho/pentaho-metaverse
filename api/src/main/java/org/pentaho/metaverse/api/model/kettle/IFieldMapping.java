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
 * User: RFellows Date: 12/11/14
 */
@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = IInfo.JSON_PROPERTY_CLASS )
public interface IFieldMapping {
  public static final String JSON_PROPERTY_SOURCE_FIELD_NAME = "sourceFieldName";
  public static final String JSON_PROPERTY_TARGET_FIELD_NAME = "targetFieldName";

  @JsonProperty( JSON_PROPERTY_SOURCE_FIELD_NAME )
  public String getSourceFieldName();
  public void setSourceFieldName( String sourceFieldName );

  @JsonProperty( JSON_PROPERTY_TARGET_FIELD_NAME )
  public String getTargetFieldName();
  public void setTargetFieldName( String targetFieldName );

}
