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

package com.pentaho.metaverse.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class BaseResourceInfo extends BaseInfo implements IExternalResourceInfo {

  private String type;
  private Boolean isInput = false;

  private Map<Object, Object> attributes = new HashMap<Object, Object>();

  @Override
  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  @Override
  public boolean isInput() {
    return isInput;
  }

  @JsonIgnore
  @Override
  public boolean isOutput() {
    return !isInput;
  }

  public void setInput( boolean isInput ) {
    this.isInput = isInput;
  }

  @Override
  public Map<Object, Object> getAttributes() {
    return attributes;
  }

  public void putAttribute( Object key, Object value ) {
    attributes.put( key, value );
  }

}
