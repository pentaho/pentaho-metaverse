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

package org.pentaho.metaverse.api;

import org.apache.commons.lang.StringEscapeUtils;
import org.pentaho.dictionary.DictionaryConst;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class MetaverseLogicalIdGenerator implements ILogicalIdGenerator {

  protected SortedSet<String> logicalIdPropertyKeys;
  protected static final String DEFUALT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  protected DateFormat dateFormat;
  protected static final String LEFT_BRACE = "{";
  protected static final String RIGHT_BRACE = "}";
  protected static final String EQUALS = ":";
  protected static final String QUOTE = "\"";

  public MetaverseLogicalIdGenerator( String... logicalIdPropertyKeys ) {
    setLogicalIdPropertyKeys( logicalIdPropertyKeys );
    setDateFormat( new SimpleDateFormat( DEFUALT_DATE_FORMAT ) );
  }

  public DateFormat getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat( DateFormat dateFormat ) {
    this.dateFormat = dateFormat;
  }

  @Override
  public void setLogicalIdPropertyKeys( String... keys ) {
    if ( logicalIdPropertyKeys == null ) {
      logicalIdPropertyKeys = new TreeSet<String>();
    } else {
      logicalIdPropertyKeys.clear();
    }

    logicalIdPropertyKeys.addAll( java.util.Arrays.asList( keys ) );
  }

  @Override
  public Set<String> getLogicalIdPropertyKeys() {
    return logicalIdPropertyKeys;
  }

  @Override
  public String generateId( IHasProperties propertiesNode ) {

    Set<String> propertyKeys = getLogicalIdPropertyKeys();

    if ( propertiesNode.getPropertyKeys().size() == 0 ) {
      return null;
    }

    String logicalId = null;
    if ( propertyKeys != null && propertyKeys.size() > 0 ) {

      StringBuilder sb = new StringBuilder();
      if ( propertyKeys.size() > 0 ) {
        sb.append( LEFT_BRACE );
        int i = 0;
        for ( String key : propertyKeys ) {
          if ( i++ > 0 ) {
            sb.append( ',' );
          }
          sb.append( QUOTE )
            .append( key )
            .append( QUOTE )
            .append( EQUALS );

          Object prop = propertiesNode.getProperty( key );
          if ( prop != null ) {
            if ( prop instanceof Date ) {
              DateFormat df = getDateFormat();
              sb.append( QUOTE ).append( df.format( prop ) ).append( QUOTE );
            } else {
              String value = prop.toString();
              if ( value.startsWith( LEFT_BRACE ) && value.endsWith( RIGHT_BRACE ) ) {
                // pre-stringified JSON, should already be escaped so don't do it again
                sb.append( value );
              } else {
                String escaped = StringEscapeUtils.escapeJavaScript( value );
                sb.append( QUOTE ).append( escaped ).append( QUOTE );
              }

            }
          } else {
            sb.append( QUOTE ).append( QUOTE );
          }
        }
        sb.append( RIGHT_BRACE );
      }
      logicalId = sb.toString();
      propertiesNode.setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, logicalId );
    }
    return logicalId;
  }

}
