/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
 *
 */

package com.pentaho.metaverse.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pentaho.dictionary.DictionaryConst;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin wrapper around BaseResourceInfo to make we service resources easier and more consistent to work with
 */
public class WebServiceResourceInfo extends BaseResourceInfo {

  public WebServiceResourceInfo() {
    setType( DictionaryConst.NODE_TYPE_WEBSERVICE );
  }

  @JsonIgnore
  public void setMethod( String method ) {
    putAttribute( "method", method );
  }

  @JsonIgnore
  public void setBody( String body ) {
    putAttribute( "body", body );
  }

  @JsonIgnore
  public void setApplicationType( String applicationType ) {
    putAttribute( "applicationType", applicationType );
  }

  @JsonIgnore
  public void addParameter( String name, Object value ) {
    Object parameters = getAttributes().get( "parameters" );
    if ( parameters == null ) {
      parameters = new HashMap<String, Object>();
      getAttributes().put( "parameters", parameters );
    }

    if ( parameters instanceof Map ) {
      Map paramMap = (Map) parameters;
      paramMap.put( name, value );
    }
  }

  @JsonIgnore
  public void addHeader( String name, Object value ) {
    Object headers = getAttributes().get( "headers" );
    if ( headers == null ) {
      headers = new HashMap<String, Object>();
      getAttributes().put( "headers", headers );
    }

    if ( headers instanceof Map ) {
      Map headerMap = (Map) headers;
      headerMap.put( name, value );
    }
  }

}
