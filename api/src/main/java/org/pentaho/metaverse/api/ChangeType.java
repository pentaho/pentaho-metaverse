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
