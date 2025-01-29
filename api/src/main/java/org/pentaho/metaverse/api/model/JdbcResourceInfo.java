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

public class JdbcResourceInfo extends DbcResourceInfo implements IExternalResourceInfo {

  public static final String JDBC = "JDBC";


  public JdbcResourceInfo() {
  }

  @Override
  public String getType() {
    return JDBC;
  }

  public JdbcResourceInfo( DatabaseMeta databaseMeta ) {
    super( databaseMeta );
    if ( "Native".equals( databaseMeta.getAccessTypeDesc() ) ) {
      setServer( databaseMeta.environmentSubstitute( databaseMeta.getHostname() ) );
      String portString = databaseMeta.environmentSubstitute( databaseMeta.getDatabasePortNumberString() );
      if ( portString != null ) {
        try {
          setPort( Integer.valueOf( portString ) );
        } catch ( NumberFormatException e ) {
          // leave null
        }
      }
      setUsername( databaseMeta.environmentSubstitute( databaseMeta.getUsername() ) );
      setPassword( databaseMeta.environmentSubstitute( databaseMeta.getPassword() ) );
      setDatabaseName( databaseMeta.environmentSubstitute( databaseMeta.getDatabaseName() ) );
    } else {
      throw new IllegalArgumentException( "DatabaseMeta is not JDBC, it is " + databaseMeta.getAccessTypeDesc() );
    }
  }

  public JdbcResourceInfo( String server, String databaseName, Integer port, String username, String password ) {
    this.server = server;
    this.databaseName = databaseName;
    this.port = port;
    this.username = username;
    this.password = password;
  }

}
