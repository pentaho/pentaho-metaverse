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

public interface IExecutionProfile extends IInfo {

  public static final String JSON_PROPERTY_PATH = "path";
  public static final String JSON_PROPERTY_TYPE = "type";
  public static final String JSON_PROPERTY_ENGINE = "engine";
  public static final String JSON_PROPERTY_EXECUTION_DATA = "executionData";

  @JsonProperty( JSON_PROPERTY_PATH )
  public String getPath();

  public void setPath( String path );

  @JsonProperty( JSON_PROPERTY_TYPE )
  public String getType();

  public void setType( String type );

  @JsonProperty( JSON_PROPERTY_ENGINE )
  public IExecutionEngine getExecutionEngine();

  public void setExecutionEngine( IExecutionEngine executionEngine );

  @JsonProperty( JSON_PROPERTY_EXECUTION_DATA )
  public IExecutionData getExecutionData();

  public void setExecutionData( IExecutionData executionData );
}
