/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
