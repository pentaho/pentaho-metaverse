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


package org.pentaho.metaverse.api.model.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil.safeListMatch;
import static org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil.safeStringMatch;

public class LineageDataResource {

  // eg. of path from an HDFS source as presented in a lineage graph: /devuser:***@hdp31n1.pentaho.net:8020/user/devuser/waterline/sales_data.csv
  private final Pattern hdfsPathPattern = Pattern.compile( "/?(\\w+\\:.+\\@)?([\\w\\.]+)(\\:(\\d+))?(/.*)" );
  // /bucket/key/to/file.txt
  private final Pattern s3PvfsPathPattern = Pattern.compile( "/(\\w+)/(.*)" );

  private final String name;
  private String path;
  private List<String> fields;
  private final List<FieldLevelRelationship> fieldRelationships = new ArrayList<>();
  private String catalogResourceID;
  private String catalogResourcePath;
  private String catalogDataSourceName;

  public String getCatalogResourceLogicalPath() {
    return catalogResourceLogicalPath;
  }

  public void setCatalogResourceLogicalPath( String catalogResourceLogicalPath ) {
    this.catalogResourceLogicalPath = catalogResourceLogicalPath;
  }

  private String catalogResourceLogicalPath;
  private Object vertexId;
  private String dbSchema;
  private String dbHost;
  private String dbName;
  private String dbPort;
  private String hdfsHost;
  private String hdfsPort;
  private String s3Bucket;

  public LineageDataResource( String name ) {
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

  public void setCatalogDataSourceName( String catalogDataSourceName ) {
    this.catalogDataSourceName = catalogDataSourceName;
  }

  public String getCatalogDataSourceName() {
    return catalogDataSourceName;
  }
  public void setCatalogResourcePath( String catalogResourcePath ) {
    this.catalogResourcePath = catalogResourcePath;
  }

  public String getCatalogResourcePath() {
    return catalogResourcePath;
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

  public void parseHdfsPath( String path ) {
    Matcher m = hdfsPathPattern.matcher( path );
    if ( m.matches() ) {
      String host = m.group( 2 );
      String port = m.group( 4 );
      String filePath = m.group( 5 );
      this.setHdfsPort( null == port ? "" : port );
      this.setHdfsHost( null == host ? "" : host );
      this.setPath( null == filePath ? "" : filePath );
    }
  }

  public void parseS3PvfsPath( String path ) {
    Matcher m = s3PvfsPathPattern.matcher( path );
    if ( m.matches() ) {
      String bucket = m.group( 1 );
      String key = m.group( 2 );
      this.setS3Bucket( null == bucket ? "" : bucket );
      this.setPath( null == key ? "" : key );
    }
  }

  public String getHdfsDataSourceUri() {
    return "hdfs://".concat( this.getHdfsHost() ).concat( ":" ).concat( this.getHdfsPort() );
  }

  public boolean isHdfsResource() {
    return null != this.getHdfsHost() && !this.getHdfsHost().isEmpty();
  }

  public boolean isS3Resource() {
    return null != this.getS3Bucket() && !this.getS3Bucket().isEmpty();
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
      LineageDataResource r2 = (LineageDataResource) o;
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
      && safeStringMatch( this.getS3Bucket(), r2.getS3Bucket() )
      && safeListMatch( this.getFields(), r2.getFields() )
      && safeStringMatch( this.getCatalogResourceID(), r2.getCatalogResourceID() )
      && ( ( ( null != this.vertexId ) && this.vertexId.equals( r2.getVertexId() ) )
          || ( null == this.getVertexId() && null == r2.getVertexId() ) );
  }

  @Override
  public int hashCode() {
    return Objects.hash( name, path, fields, fieldRelationships, catalogResourceID, vertexId, dbSchema, dbName, dbHost, dbPort, hdfsHost, hdfsPort, s3Bucket );
  }

  public String getS3Bucket() {
    return s3Bucket;
  }

  public void setS3Bucket( String s3Bucket ) {
    this.s3Bucket = s3Bucket;
  }
}
