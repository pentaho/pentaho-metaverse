/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.metaverse.impl.model.kettle;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: RFellows Date: 11/19/14
 */
public class LineageRepository implements Repository {

  Map<ObjectId, Map<String, Object>> stepAttributeCache;
  Map<ObjectId, List<Map<String, Object>>> stepFieldCache;

  public LineageRepository() {
    stepAttributeCache = new HashMap<ObjectId, Map<String, Object>>();
    stepFieldCache = new HashMap<ObjectId, List<Map<String, Object>>>();
  }

  @Override public String getName() {
    return null;
  }

  @Override public String getVersion() {
    return null;
  }

  @Override public RepositoryMeta getRepositoryMeta() {
    return null;
  }

  @Override public IUser getUserInfo() {
    return null;
  }

  @Override public RepositorySecurityProvider getSecurityProvider() {
    return null;
  }

  @Override public RepositorySecurityManager getSecurityManager() {
    return null;
  }

  @Override public LogChannelInterface getLog() {
    return null;
  }

  @Override public void connect( String username, String password ) throws KettleException, KettleSecurityException {

  }

  @Override public void disconnect() {

  }

  @Override public boolean isConnected() {
    return false;
  }

  @Override public void init( RepositoryMeta repositoryMeta ) {

  }

  @Override public boolean exists( String name, RepositoryDirectoryInterface repositoryDirectory,
                                   RepositoryObjectType objectType ) throws KettleException {
    return false;
  }

  @Override public ObjectId getTransformationID( String name, RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    return null;
  }

  @Override public ObjectId getJobId( String name, RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    return null;
  }

  @Override public void save( RepositoryElementInterface repositoryElement, String versionComment,
                              ProgressMonitorListener monitor ) throws KettleException {

  }

  @Override public void save( RepositoryElementInterface repositoryElement, String versionComment,
                              ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {

  }

  @Override public void save( RepositoryElementInterface repositoryElement, String versionComment, Calendar versionDate,
                              ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {

  }

  @Override public RepositoryDirectoryInterface getDefaultSaveDirectory( RepositoryElementInterface repositoryElement )
    throws KettleException {
    return null;
  }

  @Override public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    return null;
  }

  @Override public void clearSharedObjectCache() {

  }

  @Override public TransMeta loadTransformation( String transname, RepositoryDirectoryInterface repdir,
                                                 ProgressMonitorListener monitor, boolean setInternalVariables,
                                                 String revision ) throws KettleException {
    return null;
  }

  @Override public TransMeta loadTransformation( ObjectId id_transformation, String versionLabel )
    throws KettleException {
    return null;
  }

  @Override public SharedObjects readTransSharedObjects( TransMeta transMeta ) throws KettleException {
    return null;
  }

  @Override public ObjectId renameTransformation( ObjectId id_transformation, RepositoryDirectoryInterface newDirectory,
                                                  String newName ) throws KettleException {
    return null;
  }

  @Override public ObjectId renameTransformation( ObjectId id_transformation, String versionComment,
                                                  RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    return null;
  }

  @Override public void deleteTransformation( ObjectId id_transformation ) throws KettleException {

  }

  @Override public JobMeta loadJob( String jobname, RepositoryDirectoryInterface repdir,
                                    ProgressMonitorListener monitor, String revision ) throws KettleException {
    return null;
  }

  @Override public JobMeta loadJob( ObjectId id_job, String versionLabel ) throws KettleException {
    return null;
  }

  @Override public SharedObjects readJobMetaSharedObjects( JobMeta jobMeta ) throws KettleException {
    return null;
  }

  @Override public ObjectId renameJob( ObjectId id_job, String versionComment,
                                       RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    return null;
  }

  @Override public ObjectId renameJob( ObjectId id_job, RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    return null;
  }

  @Override public void deleteJob( ObjectId id_job ) throws KettleException {

  }

  @Override public DatabaseMeta loadDatabaseMeta( ObjectId id_database, String revision ) throws KettleException {
    return null;
  }

  @Override public void deleteDatabaseMeta( String databaseName ) throws KettleException {

  }

  @Override public ObjectId[] getDatabaseIDs( boolean includeDeleted ) throws KettleException {
    return new ObjectId[ 0 ];
  }

  @Override public String[] getDatabaseNames( boolean includeDeleted ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public List<DatabaseMeta> readDatabases() throws KettleException {
    return null;
  }

  @Override public ObjectId getDatabaseID( String name ) throws KettleException {
    return null;
  }

  @Override public ClusterSchema loadClusterSchema( ObjectId id_cluster_schema, List<SlaveServer> slaveServers,
                                                    String versionLabel ) throws KettleException {
    return null;
  }

  @Override public ObjectId[] getClusterIDs( boolean includeDeleted ) throws KettleException {
    return new ObjectId[ 0 ];
  }

  @Override public String[] getClusterNames( boolean includeDeleted ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public ObjectId getClusterID( String name ) throws KettleException {
    return null;
  }

  @Override public void deleteClusterSchema( ObjectId id_cluster ) throws KettleException {

  }

  @Override public SlaveServer loadSlaveServer( ObjectId id_slave_server, String versionLabel ) throws KettleException {
    return null;
  }

  @Override public ObjectId[] getSlaveIDs( boolean includeDeleted ) throws KettleException {
    return new ObjectId[ 0 ];
  }

  @Override public String[] getSlaveNames( boolean includeDeleted ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public List<SlaveServer> getSlaveServers() throws KettleException {
    return null;
  }

  @Override public ObjectId getSlaveID( String name ) throws KettleException {
    return null;
  }

  @Override public void deleteSlave( ObjectId id_slave ) throws KettleException {

  }

  @Override public PartitionSchema loadPartitionSchema( ObjectId id_partition_schema, String versionLabel )
    throws KettleException {
    return null;
  }

  @Override public ObjectId[] getPartitionSchemaIDs( boolean includeDeleted ) throws KettleException {
    return new ObjectId[ 0 ];
  }

  @Override public String[] getPartitionSchemaNames( boolean includeDeleted ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public ObjectId getPartitionSchemaID( String name ) throws KettleException {
    return null;
  }

  @Override public void deletePartitionSchema( ObjectId id_partition_schema ) throws KettleException {

  }

  @Override public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
    return null;
  }

  @Override public RepositoryDirectoryInterface findDirectory( String directory ) throws KettleException {
    return null;
  }

  @Override public void saveRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
  }

  @Override public void deleteRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {

  }

  @Override public ObjectId renameRepositoryDirectory( ObjectId id, RepositoryDirectoryInterface newParentDir,
                                                       String newName ) throws KettleException {
    return null;
  }

  @Override public RepositoryDirectoryInterface createRepositoryDirectory( RepositoryDirectoryInterface parentDirectory,
                                                                           String directoryPath )
    throws KettleException {
    return null;
  }

  @Override public String[] getTransformationNames( ObjectId id_directory, boolean includeDeleted )
    throws KettleException {
    return new String[ 0 ];
  }

  @Override public List<RepositoryElementMetaInterface> getJobObjects( ObjectId id_directory, boolean includeDeleted )
    throws KettleException {
    return null;
  }

  @Override public List<RepositoryElementMetaInterface> getTransformationObjects( ObjectId id_directory,
                                                                                  boolean includeDeleted )
    throws KettleException {
    return null;
  }

  @Override public List<RepositoryElementMetaInterface> getJobAndTransformationObjects( ObjectId id_directory,
                                                                                        boolean includeDeleted )
    throws KettleException {
    return null;
  }

  @Override public String[] getJobNames( ObjectId id_directory, boolean includeDeleted ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public String[] getDirectoryNames( ObjectId id_directory ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public ObjectId insertLogEntry( String description ) throws KettleException {
    return null;
  }

  @Override public void insertStepDatabase( ObjectId id_transformation, ObjectId id_step, ObjectId id_database )
    throws KettleException {

  }

  @Override public void insertJobEntryDatabase( ObjectId id_job, ObjectId id_jobentry, ObjectId id_database )
    throws KettleException {

  }

  @Override public void saveConditionStepAttribute( ObjectId id_transformation, ObjectId id_step, String code,
                                                    Condition condition ) throws KettleException {

  }

  @Override public Condition loadConditionFromStepAttribute( ObjectId id_step, String code ) throws KettleException {
    return null;
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code, boolean def )
    throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Boolean attr = (Boolean) attrs.get( code );
    if ( attr != null ) {
      return attr;
    } else {
      return def;
    }
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Boolean attr = (Boolean) attrs.get( code );
    if ( attr != null ) {
      return attr;
    } else {
      return false;
    }
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, String code ) throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    Boolean attr = (Boolean) attrs.get( code );
    return attr == null ? false : attr;
  }

  @Override public long getStepAttributeInteger( ObjectId id_step, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Number attr = (Number) attrs.get( code );
    return attr == null ? 0L : attr.longValue();
  }

  @Override public long getStepAttributeInteger( ObjectId id_step, String code ) throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    Number attr = (Number) attrs.get( code );
    return attr == null ? 0L : attr.longValue();
  }

  @Override public String getStepAttributeString( ObjectId id_step, int nr, String code ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    Object attr = attrs.get( code );
    return attr == null ? null : attr.toString();
  }

  @Override public String getStepAttributeString( ObjectId id_step, String code ) throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    Object attr = attrs.get( code );
    return attr == null ? null : attr.toString();
  }

  public Map<String, Object> getStepAttributesCache( ObjectId id_step ) {
    Map<String, Object> attrs = stepAttributeCache.get( id_step );
    if ( attrs == null ) {
      attrs = new TreeMap<String, Object>();
      stepAttributeCache.put( id_step, attrs );
    }
    return attrs;
  }

  public List<Map<String, Object>> getStepFieldsCache( ObjectId id_step ) {
    List<Map<String, Object>> fieldList = stepFieldCache.get( id_step );
    if ( fieldList == null ) {
      fieldList = new ArrayList<Map<String, Object>>( 100 );
      stepFieldCache.put( id_step, fieldList );
    }
    return fieldList;
  }

  public Map<String, Object> getStepFieldAttributesCache( ObjectId id_step, int number ) {
    List<Map<String, Object>> fieldList = getStepFieldsCache( id_step );
    if ( number + 1 > fieldList.size() ) {
      for ( int i = fieldList.size(); i < number + 1; i++ ) {
        fieldList.add( i, new TreeMap<String, Object>() );
      }
    }
    Map<String, Object> fieldAttrs = fieldList.get( number );
    if ( fieldAttrs == null ) {
      fieldAttrs = new TreeMap<String, Object>();
      fieldList.add( number, fieldAttrs );
    }
    return fieldAttrs;
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           String value ) throws KettleException {

    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );

  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, String value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           boolean value ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, boolean value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           long value ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, long value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           double value ) throws KettleException {
    Map<String, Object> attrs = getStepFieldAttributesCache( id_step, nr );
    attrs.put( code, value );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, double value )
    throws KettleException {
    Map<String, Object> attrs = getStepAttributesCache( id_step );
    attrs.put( code, value );
  }

  @Override public int countNrStepAttributes( ObjectId id_step, String code ) throws KettleException {
    int number = 0;

    List<Map<String, Object>> fieldList = getStepFieldsCache( id_step );

    for ( int i = 0; i < fieldList.size(); i++ ) {
      Map<String, Object> fieldAttrs = fieldList.get( i );
      if ( fieldAttrs.containsKey( code ) ) {
        number++;
      }
    }

    return number;
  }

  @Override public int countNrJobEntryAttributes( ObjectId id_jobentry, String code ) throws KettleException {
    return 0;
  }

  @Override public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code ) throws KettleException {
    return false;
  }

  @Override public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, int nr, String code )
    throws KettleException {
    return false;
  }

  @Override public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code, boolean def )
    throws KettleException {
    return false;
  }

  @Override public long getJobEntryAttributeInteger( ObjectId id_jobentry, String code ) throws KettleException {
    return 0;
  }

  @Override public long getJobEntryAttributeInteger( ObjectId id_jobentry, int nr, String code )
    throws KettleException {
    return 0;
  }

  @Override public String getJobEntryAttributeString( ObjectId id_jobentry, String code ) throws KettleException {
    return null;
  }

  @Override public String getJobEntryAttributeString( ObjectId id_jobentry, int nr, String code )
    throws KettleException {
    return null;
  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code,
                                               String value ) throws KettleException {

  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, String value )
    throws KettleException {

  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code,
                                               boolean value ) throws KettleException {

  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, boolean value )
    throws KettleException {

  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, long value )
    throws KettleException {

  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, long value )
    throws KettleException {

  }

  @Override public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId id_step, String code,
                                                                   List<DatabaseMeta> databases )
    throws KettleException {
    return null;
  }

  @Override public void saveDatabaseMetaStepAttribute( ObjectId id_transformation, ObjectId id_step, String code,
                                                       DatabaseMeta database ) throws KettleException {

  }

  @Override public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId id_jobentry, String nameCode,
                                                                       String idCode, List<DatabaseMeta> databases )
    throws KettleException {
    return null;
  }

  @Override public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId id_jobentry, String nameCode, int nr,
                                                                       String idCode, List<DatabaseMeta> databases )
    throws KettleException {
    return null;
  }

  @Override public void saveDatabaseMetaJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String nameCode,
                                                           String idCode, DatabaseMeta database )
    throws KettleException {

  }

  @Override public void saveDatabaseMetaJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr,
                                                           String nameCode, String idCode, DatabaseMeta database )
    throws KettleException {

  }

  @Override public void undeleteObject( RepositoryElementMetaInterface repositoryObject ) throws KettleException {

  }

  @Override public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    return null;
  }

  @Override public IRepositoryService getService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    return null;
  }

  @Override public boolean hasService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    return false;
  }

  @Override public RepositoryObject getObjectInformation( ObjectId objectId, RepositoryObjectType objectType )
    throws KettleException {
    return null;
  }

  @Override public String getConnectMessage() {
    return null;
  }

  @Override public String[] getJobsUsingDatabase( ObjectId id_database ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public String[] getTransformationsUsingDatabase( ObjectId id_database ) throws KettleException {
    return new String[ 0 ];
  }

  @Override public IRepositoryImporter getImporter() {
    return null;
  }

  @Override public IRepositoryExporter getExporter() throws KettleException {
    return null;
  }

  @Override public IMetaStore getMetaStore() {
    return null;
  }

  @Override public RepositoryDirectoryInterface findDirectory( ObjectId directory ) throws KettleException {
    return null;
  }

}

