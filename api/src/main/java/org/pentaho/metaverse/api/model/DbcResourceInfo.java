/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Objects;

public abstract class DbcResourceInfo extends BaseDatabaseResourceInfo implements IExternalResourceInfo {

  public static final String JSON_PROPERTY_PORT = "port";
  public static final String JSON_PROPERTY_SERVER = "server";
  public static final String JSON_PROPERTY_USERNAME = "username";
  public static final String JSON_PROPERTY_PASSWORD = "password";
  public static final String JSON_PROPERTY_DATABASE_NAME = "databaseName";

  protected Integer port;
  protected String server;
  protected String username;
  protected String password;
  protected String databaseName;

  protected DbcResourceInfo() {
    super();
  }

  protected DbcResourceInfo( DatabaseMeta databaseMeta ) {
    super( databaseMeta );
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

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    if ( !super.equals( o ) ) {
      return false;
    }
    DbcResourceInfo that = (DbcResourceInfo) o;
    return Objects.equals( port, that.port )
      && Objects.equals( server, that.server )
      && Objects.equals( username, that.username )
      && Objects.equals( password, that.password )
      && Objects.equals( databaseName, that.databaseName );
  }

  @Override public int hashCode() {
    return Objects.hash( super.hashCode(), port, server, username, password, databaseName );
  }
}
