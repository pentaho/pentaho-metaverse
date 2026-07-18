/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metaverse.api.model;

import org.pentaho.di.core.database.DatabaseMeta;

public class JndiResourceInfo extends BaseDatabaseResourceInfo implements IExternalResourceInfo {

  public static final String JNDI = "JNDI";

  @Override public String getType() {
    return JNDI;
  }

  public JndiResourceInfo( DatabaseMeta databaseMeta ) {
    super( databaseMeta );
    if ( !getType().equals( databaseMeta.getAccessTypeDesc() ) ) {
      throw new IllegalArgumentException( "DatabaseMeta is not JNDI, it is " + databaseMeta.getAccessTypeDesc() );
    }
  }

  public JndiResourceInfo() {
  }

  public JndiResourceInfo( String name ) {
    setName( name );
  }
}
