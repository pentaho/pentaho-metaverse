/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
