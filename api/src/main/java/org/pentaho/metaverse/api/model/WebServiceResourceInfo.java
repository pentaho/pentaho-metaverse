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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.pentaho.dictionary.DictionaryConst;

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
