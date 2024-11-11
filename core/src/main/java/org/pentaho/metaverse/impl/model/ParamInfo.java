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


package org.pentaho.metaverse.impl.model;

import org.pentaho.metaverse.api.model.BaseInfo;
import org.pentaho.metaverse.api.model.IParamInfo;

public class ParamInfo extends BaseInfo implements IParamInfo<String> {
  private String defaultValue;
  private String value;

  public ParamInfo() {
    this( null );
  }

  public ParamInfo( String name ) {
    this( name, null );
  }

  public ParamInfo( String name, String value ) {
    this( name, value, null );
  }

  public ParamInfo( String name, String value, String defaultValue ) {
    this( name, value, defaultValue, null );
  }

  public ParamInfo( String name, String value, String defaultValue, String description ) {
    super();
    setName( name );
    setValue( value );
    setDescription( description );
    setDefaultValue( defaultValue );
  }

  @Override public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue( String defaultValue ) {
    this.defaultValue = defaultValue;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  @Override public String getValue() {
    return value;
  }
}
