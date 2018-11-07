/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

public class OCIResourceInfo extends BaseDatabaseResourceInfo implements IExternalResourceInfo {

  public static final String OCI = "OCI";

  public static final String JSON_PROPERTY_USERNAME = "username";
  public static final String JSON_PROPERTY_PASSWORD = "password";
  public static final String JSON_PROPERTY_DATABASE_NAME = "databaseName";
  public static final String JSON_PROPERTY_DATA_TABLESPACE = "data_tablespace";
  public static final String JSON_PROPERTY_INDEX_TABLESPACE = "index_tablespace";

  private String username;
  private String password;
  private String databaseName;
  private String dataTablespace;
  private String indexTablespace;


  @Override
  public String getType() {
    return OCI;
  }

  public OCIResourceInfo() {
  }

  public OCIResourceInfo( DatabaseMeta databaseMeta ) {
    super( databaseMeta );
    if ( "OCI".equals( databaseMeta.getAccessTypeDesc() ) ) {
      setUsername( databaseMeta.environmentSubstitute( databaseMeta.getUsername() ) );
      setPassword( databaseMeta.environmentSubstitute( databaseMeta.getPassword() ) );
      setDatabaseName( databaseMeta.environmentSubstitute( databaseMeta.getDatabaseName() ) );
      setDataTablespace( databaseMeta.environmentSubstitute( databaseMeta.getDataTablespace() ) );
      setIndexTablespace( databaseMeta.environmentSubstitute( databaseMeta.getIndexTablespace() ) );
    } else {
      throw new IllegalArgumentException( "DatabaseMeta is not OCI, it is " + databaseMeta.getAccessTypeDesc() );
    }
  }

  public OCIResourceInfo( String databaseName, String username, String password ) {
    this.databaseName = databaseName;
    this.username = username;
    this.password = password;
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

  @JsonProperty( JSON_PROPERTY_DATA_TABLESPACE )
  public String getDataTablespace() {
    return dataTablespace;
  }

  public void setDataTablespace( String dataTablespace ) {
    this.dataTablespace = dataTablespace;
  }

  @JsonProperty( JSON_PROPERTY_INDEX_TABLESPACE )
  public String getIndexTablespace() {
    return indexTablespace;
  }

  public void setIndexTablespace( String indexTablespace ) {
    this.indexTablespace = indexTablespace;
  }

}
