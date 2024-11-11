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
