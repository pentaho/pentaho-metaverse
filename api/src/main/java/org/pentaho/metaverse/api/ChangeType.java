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

package org.pentaho.metaverse.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.pentaho.dictionary.DictionaryConst;

public enum ChangeType {
  METADATA( DictionaryConst.PROPERTY_METADATA_OPERATIONS ),
  DATA( DictionaryConst.PROPERTY_DATA_OPERATIONS ),
  DATA_FLOW( DictionaryConst.PROPERTY_DATA_FLOW_OPERATIONS );

  private final String name;

  private ChangeType( String name ) {
    this.name = name;
  }

  @JsonCreator
  public static ChangeType forValue( String value ) {
    for ( ChangeType val : values() ) {
      if ( val.toString().equals( value ) ) {
        return val;
      }
    }
    return null;
  }

  @JsonValue
  public String toString() {
    return name;
  }

}
