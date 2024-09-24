/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
