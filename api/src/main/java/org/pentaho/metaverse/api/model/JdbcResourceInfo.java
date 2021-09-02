/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

  @Override
  public void cleanupSensitiveData() {
    password = "";
  }
}
