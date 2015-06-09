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

package org.pentaho.metaverse.api;

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
                sb.append( value );
              } else {
                sb.append( QUOTE ).append( value ).append( QUOTE );
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
