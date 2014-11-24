/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.impl.model;

import com.pentaho.metaverse.api.model.IParamInfo;

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
