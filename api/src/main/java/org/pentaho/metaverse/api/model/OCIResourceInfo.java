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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.pentaho.di.core.database.DatabaseMeta;

import java.util.Objects;

public class OCIResourceInfo extends DbcResourceInfo implements IExternalResourceInfo {

  public static final String OCI = "OCI";

  public static final String JSON_PROPERTY_DATA_TABLESPACE = "data_tablespace";
  public static final String JSON_PROPERTY_INDEX_TABLESPACE = "index_tablespace";

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
    OCIResourceInfo that = (OCIResourceInfo) o;
    return Objects.equals( dataTablespace, that.dataTablespace )
      && Objects.equals( indexTablespace, that.indexTablespace );
  }

  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), dataTablespace, indexTablespace );
  }
}
