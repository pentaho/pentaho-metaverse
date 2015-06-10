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

package org.pentaho.metaverse.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;

public class JdbcResourceInfo extends BaseDatabaseResourceInfo implements IExternalResourceInfo {

  public static final String JDBC = "JDBC";

  public static final String JSON_PROPERTY_PORT = "port";
  public static final String JSON_PROPERTY_SERVER = "server";
  public static final String JSON_PROPERTY_USERNAME = "username";
  public static final String JSON_PROPERTY_PASSWORD = "password";
  public static final String JSON_PROPERTY_DATABASE_NAME = "databaseName";

  private Integer port;
  private String server;
  private String username;
  private String password;
  private String databaseName;

  @Override
  public String getType() {
    return JDBC;
  }

  public JdbcResourceInfo() {
  }

  public JdbcResourceInfo( DatabaseMeta databaseMeta ) {
    super( databaseMeta );
    if ( "Native".equals( databaseMeta.getAccessTypeDesc() ) ) {
      setServer( databaseMeta.getHostname() );
      String portString = databaseMeta.getDatabasePortNumberString();
      if ( portString != null ) {
        setPort( Integer.valueOf( portString ) );
      }
      setUsername( databaseMeta.getUsername() );
      setPassword( databaseMeta.getPassword() );
      setDatabaseName( databaseMeta.getDatabaseName() );
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


  @JsonProperty( JSON_PROPERTY_PORT )
  public Integer getPort() {
    return port;
  }

  public void setPort( Integer port ) {
    this.port = port;
  }

  @JsonProperty( JSON_PROPERTY_SERVER )
  public String getServer() {
    return server;
  }

  public void setServer( String server ) {
    this.server = server;
  }

  @JsonProperty( JSON_PROPERTY_USERNAME )
  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  @JsonIgnore
  public String getPassword() {
    return password;
  }

  @JsonProperty( JSON_PROPERTY_PASSWORD )
  public void setPassword( String password ) {
    this.password = Encr.decryptPasswordOptionallyEncrypted( password );
  }

  @JsonProperty( JSON_PROPERTY_PASSWORD )
  protected String getEncryptedPassword() {
    // Need "Encrypted prefix for decryptPasswordOptionallyEncrypted() to operate properly
    return Encr.PASSWORD_ENCRYPTED_PREFIX + Encr.encryptPassword( password );
  }

  @JsonProperty( JSON_PROPERTY_DATABASE_NAME )
  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName( String databaseName ) {
    this.databaseName = databaseName;
  }

}
