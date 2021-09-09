/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2021 by Hitachi Vantara : http://www.pentaho.com
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


package org.pentaho.metaverse.graph.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.pentaho.metaverse.util.MetaverseUtil.safeListMatch;
import static org.pentaho.metaverse.util.MetaverseUtil.safeStringMatch;

public class LineageDataResource {

  private final String name;
  private String path;
  private List<String> fields;
  private final List<FieldLevelRelationship> fieldRelationships = new ArrayList<>();
  private String catalogResourceID;
  private Object vertexId;
  private String dbSchema;
  private String dbHost;
  private String dbName;
  private String dbPort;
  private String hdfsHost;
  private String hdfsPort;

  public LineageDataResource(String name ) {
    this.name = name;
    path = "";
    catalogResourceID = "";
    vertexId = null;
    dbSchema = "";
    dbHost = "";
    dbName = "";
    dbPort = "";
    hdfsHost = "";
    hdfsPort = "";
  }

  public String getName() {
    return name;
  }

  public List<String> getFields() {
    return fields;
  }

  public void setFields( List<String> fields ) {
    this.fields = fields;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public List<FieldLevelRelationship> getFieldLevelRelationships() {
    return fieldRelationships;
  }

  public void addFieldLevelRelationship( FieldLevelRelationship fieldRelationship ) {
    fieldRelationships.add( fieldRelationship );
  }

  public void setCatalogResourceID( String catalogResourceID ) {
    this.catalogResourceID = catalogResourceID;
  }

  public String getCatalogResourceID() {
    return catalogResourceID;
  }

  public Object getVertexId() {
    return vertexId;
  }

  public void setVertexId( Object vertexId ) {
    this.vertexId = vertexId;
  }

  public String getDbSchema() {
    return dbSchema;
  }

  public void setDbSchema( String dbSchema ) {
    this.dbSchema = dbSchema;
  }

  public String getDbHost() {
    return dbHost;
  }

  public void setDbHost( String dbHost ) {
    this.dbHost = dbHost;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName( String dbName ) {
    this.dbName = dbName;
  }

  public String getDbPort() {
    return dbPort;
  }

  public void setDbPort( String dbPort ) {
    this.dbPort = dbPort;
  }

  public String getHdfsPort() {
    return hdfsPort;
  }

  public void setHdfsPort( String hdfsPort ) {
    this.hdfsPort = hdfsPort;
  }

  public String getHdfsHost() {
    return hdfsHost;
  }

  public void setHdfsHost( String hdfsHost ) {
    this.hdfsHost = hdfsHost;
  }

  @Override
  public String toString() {
    return "name: ".concat( name )
      .concat( " path: " ).concat( path )
      .concat( " catalogResourceId: " ).concat( catalogResourceID )
      .concat( " dbHost: " ).concat( dbHost )
      .concat( " dbPort: " ).concat( dbPort )
      .concat( " dbSchema: " ).concat( dbSchema )
      .concat( " hdfsHost: " ).concat( hdfsHost )
      .concat( " hdfsPort: " ).concat( hdfsPort );
  }

  @Override
  public boolean equals( Object o ) {
    if ( o instanceof LineageDataResource ) {
      LineageDataResource r2 = ( LineageDataResource ) o;
      return this.shallowEquals( r2 )
        && safeListMatch( this.getFieldLevelRelationships(), r2.getFieldLevelRelationships() );
    } else {
      return false;
    }
  }

  public boolean shallowEquals( LineageDataResource r2 ) {
    return safeStringMatch( this.getName(), r2.getName() )
      && safeStringMatch( this.getPath(), r2.getPath() )
      && safeStringMatch( this.getDbSchema(), r2.getDbSchema() )
      && safeStringMatch( this.getDbHost(), r2.getDbHost() )
      && safeStringMatch( this.getDbPort(), r2.getDbPort() )
      && safeStringMatch( this.getDbName(), r2.getDbName() )
      && safeStringMatch( this.getHdfsHost(), r2.getHdfsHost() )
      && safeStringMatch( this.getHdfsPort(), r2.getHdfsPort() )
      && safeListMatch( this.getFields(), r2.getFields() )
      && safeStringMatch( this.getCatalogResourceID(), r2.getCatalogResourceID() );
  }

  @Override
  public int hashCode() {
    return Objects.hash( name, path, fields, fieldRelationships, catalogResourceID, vertexId, dbSchema, dbName, dbHost, dbPort, hdfsHost, hdfsPort );
  }
}
