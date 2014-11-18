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

import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.di.core.database.DatabaseMeta;

public class JndiResourceInfo extends BaseResourceInfo implements IExternalResourceInfo {

  public static final String JNDI = "JNDI";

  @Override public String getType() {
    return JNDI;
  }

  public JndiResourceInfo( DatabaseMeta databaseMeta ) {
    if ( getType().equals( databaseMeta.getAccessTypeDesc() ) ) {
      setName( databaseMeta.getName() );
      setDescription( databaseMeta.getDescription() );
    } else {
      throw new IllegalArgumentException( "DatabaseMeta is not JNDI, it is " + databaseMeta.getAccessTypeDesc() );
    }
  }

  public JndiResourceInfo() {
  }

  public JndiResourceInfo( String name ) {
    setName( name );
  }
}
