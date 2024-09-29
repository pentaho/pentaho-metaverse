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


package org.pentaho.metaverse.impl.model.kettle;

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
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import java.util.Calendar;
import java.util.List;

/**
 * Base implementation that does nothing but throw UnsupportedOperationException. The only reason to use this class
 * is to partially implement the Repository interface.
 */
public class SimpleRepository implements Repository {

  public static final String NOT_IMPLEMENTED = "not implemented";

  @Override public String getName() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String getVersion() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryMeta getRepositoryMeta() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public IUser getUserInfo() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositorySecurityProvider getSecurityProvider() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositorySecurityManager getSecurityManager() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public LogChannelInterface getLog() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void connect( String username, String password ) throws KettleException, KettleSecurityException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void disconnect() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean isConnected() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void init( RepositoryMeta repositoryMeta ) {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean exists( String name, RepositoryDirectoryInterface repositoryDirectory,
                                   RepositoryObjectType objectType ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId getTransformationID( String name, RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId getJobId( String name, RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void save( RepositoryElementInterface repositoryElement, String versionComment,
                              ProgressMonitorListener monitor ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void save( RepositoryElementInterface repositoryElement, String versionComment,
                              ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void save( RepositoryElementInterface repositoryElement, String versionComment, Calendar versionDate,
                              ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryDirectoryInterface getDefaultSaveDirectory( RepositoryElementInterface repositoryElement )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void clearSharedObjectCache() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public TransMeta loadTransformation( String transname, RepositoryDirectoryInterface repdir,
                                                 ProgressMonitorListener monitor, boolean setInternalVariables,
                                                 String revision ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public TransMeta loadTransformation( ObjectId id_transformation, String versionLabel )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public SharedObjects readTransSharedObjects( TransMeta transMeta ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId renameTransformation( ObjectId id_transformation, RepositoryDirectoryInterface newDirectory,
                                                  String newName ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId renameTransformation( ObjectId id_transformation, String versionComment,
                                                  RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void deleteTransformation( ObjectId id_transformation ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public JobMeta loadJob( String jobname, RepositoryDirectoryInterface repdir,
                                    ProgressMonitorListener monitor, String revision ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public JobMeta loadJob( ObjectId id_job, String versionLabel ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public SharedObjects readJobMetaSharedObjects( JobMeta jobMeta ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId renameJob( ObjectId id_job, String versionComment,
                                       RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId renameJob( ObjectId id_job, RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void deleteJob( ObjectId id_job ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public DatabaseMeta loadDatabaseMeta( ObjectId id_database, String revision ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void deleteDatabaseMeta( String databaseName ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId[] getDatabaseIDs( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getDatabaseNames( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public List<DatabaseMeta> readDatabases() throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId getDatabaseID( String name ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ClusterSchema loadClusterSchema( ObjectId id_cluster_schema, List<SlaveServer> slaveServers,
                                                    String versionLabel ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId[] getClusterIDs( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getClusterNames( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId getClusterID( String name ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void deleteClusterSchema( ObjectId id_cluster ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public SlaveServer loadSlaveServer( ObjectId id_slave_server, String versionLabel ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId[] getSlaveIDs( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getSlaveNames( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public List<SlaveServer> getSlaveServers() throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId getSlaveID( String name ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void deleteSlave( ObjectId id_slave ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public PartitionSchema loadPartitionSchema( ObjectId id_partition_schema, String versionLabel )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId[] getPartitionSchemaIDs( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getPartitionSchemaNames( boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId getPartitionSchemaID( String name ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void deletePartitionSchema( ObjectId id_partition_schema ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryDirectoryInterface findDirectory( String directory ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryDirectoryInterface findDirectory( ObjectId directory ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void deleteRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId renameRepositoryDirectory( ObjectId id, RepositoryDirectoryInterface newParentDir,
                                                       String newName ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryDirectoryInterface createRepositoryDirectory( RepositoryDirectoryInterface parentDirectory,
                                                                           String directoryPath )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getTransformationNames( ObjectId id_directory, boolean includeDeleted )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public List<RepositoryElementMetaInterface> getJobObjects( ObjectId id_directory, boolean includeDeleted )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public List<RepositoryElementMetaInterface> getTransformationObjects( ObjectId id_directory,
                                                                                  boolean includeDeleted )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public List<RepositoryElementMetaInterface> getJobAndTransformationObjects( ObjectId id_directory,
                                                                                        boolean includeDeleted )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getJobNames( ObjectId id_directory, boolean includeDeleted ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getDirectoryNames( ObjectId id_directory ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public ObjectId insertLogEntry( String description ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void insertStepDatabase( ObjectId id_transformation, ObjectId id_step, ObjectId id_database )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void insertJobEntryDatabase( ObjectId id_job, ObjectId id_jobentry, ObjectId id_database )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveConditionStepAttribute( ObjectId id_transformation, ObjectId id_step, String code,
                                                    Condition condition ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public Condition loadConditionFromStepAttribute( ObjectId id_step, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code, boolean def )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean getStepAttributeBoolean( ObjectId id_step, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public long getStepAttributeInteger( ObjectId id_step, int nr, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public long getStepAttributeInteger( ObjectId id_step, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String getStepAttributeString( ObjectId id_step, int nr, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String getStepAttributeString( ObjectId id_step, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           String value ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, String value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           boolean value ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, boolean value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           long value ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, long value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code,
                                           double value ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, double value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public int countNrStepAttributes( ObjectId id_step, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public int countNrJobEntryAttributes( ObjectId id_jobentry, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, int nr, String code )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, String code, boolean def )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public long getJobEntryAttributeInteger( ObjectId id_jobentry, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public long getJobEntryAttributeInteger( ObjectId id_jobentry, int nr, String code )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String getJobEntryAttributeString( ObjectId id_jobentry, String code ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String getJobEntryAttributeString( ObjectId id_jobentry, int nr, String code )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code,
                                               String value ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, String value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code,
                                               boolean value ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, boolean value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, long value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String code, long value )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId id_step, String code,
                                                                   List<DatabaseMeta> databases )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveDatabaseMetaStepAttribute( ObjectId id_transformation, ObjectId id_step, String code,
                                                       DatabaseMeta database ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId id_jobentry, String nameCode,
                                                                       String idCode, List<DatabaseMeta> databases )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId id_jobentry, String nameCode, int nr,
                                                                       String idCode, List<DatabaseMeta> databases )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveDatabaseMetaJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, String nameCode,
                                                           String idCode, DatabaseMeta database )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void saveDatabaseMetaJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr,
                                                           String nameCode, String idCode, DatabaseMeta database )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public void undeleteObject( RepositoryElementMetaInterface repositoryObject ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public IRepositoryService getService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public boolean hasService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public RepositoryObject getObjectInformation( ObjectId objectId, RepositoryObjectType objectType )
    throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String getConnectMessage() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getJobsUsingDatabase( ObjectId id_database ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public String[] getTransformationsUsingDatabase( ObjectId id_database ) throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public IRepositoryImporter getImporter() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public IRepositoryExporter getExporter() throws KettleException {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public IMetaStore getRepositoryMetaStore() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }

  @Override public IUnifiedRepository getUnderlyingRepository() {
    // TODO - Implement this
    throw new UnsupportedOperationException( NOT_IMPLEMENTED );
  }
}
