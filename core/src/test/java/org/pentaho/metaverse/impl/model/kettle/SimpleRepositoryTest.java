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

package org.pentaho.metaverse.impl.model.kettle;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.StringObjectId;

public class SimpleRepositoryTest {

  SimpleRepository repo;

  @Before
  public void setUp() throws Exception {
    repo = new SimpleRepository();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetName() throws Exception {
    repo.getName();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetVersion() throws Exception {
    repo.getVersion();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetRepositoryMeta() throws Exception {
    repo.getRepositoryMeta();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetUserInfo() throws Exception {
    repo.getUserInfo();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetSecurityProvider() throws Exception {
    repo.getSecurityManager();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetSecurityManager() throws Exception {
    repo.getSecurityProvider();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetLog() throws Exception {
    repo.getLog();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testConnect() throws Exception {
    repo.connect( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDisconnect() throws Exception {
    repo.disconnect();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testIsConnected() throws Exception {
    repo.isConnected();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testInit() throws Exception {
    repo.init( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testExists() throws Exception {
    repo.exists( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetTransformationID() throws Exception {
    repo.getTransformationID( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobId() throws Exception {
    repo.getJobId( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSave() throws Exception {
    repo.save( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSave1() throws Exception {
    repo.save( null, null, null, true );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSave2() throws Exception {
    repo.save( null, null, null, null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetDefaultSaveDirectory() throws Exception {
    repo.getDefaultSaveDirectory( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetUserHomeDirectory() throws Exception {
    repo.getUserHomeDirectory();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testClearSharedObjectCache() throws Exception {
    repo.clearSharedObjectCache();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadTransformation() throws Exception {
    repo.loadTransformation( null, null, null, false, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadTransformation1() throws Exception {
    repo.loadTransformation( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testReadTransSharedObjects() throws Exception {
    repo.readTransSharedObjects( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testRenameTransformation() throws Exception {
    repo.renameTransformation( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testRenameTransformation1() throws Exception {
    repo.renameTransformation( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDeleteTransformation() throws Exception {
    repo.deleteTransformation( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadJob() throws Exception {
    repo.loadJob( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadJob1() throws Exception {
    repo.loadJob( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testReadJobMetaSharedObjects() throws Exception {
    repo.readJobMetaSharedObjects( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testRenameJob() throws Exception {
    repo.renameJob( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testRenameJob1() throws Exception {
    repo.renameJob( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDeleteJob() throws Exception {
    repo.deleteJob( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadDatabaseMeta() throws Exception {
    repo.loadDatabaseMeta( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDeleteDatabaseMeta() throws Exception {
    repo.deleteDatabaseMeta( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetDatabaseIDs() throws Exception {
    repo.getDatabaseIDs( false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetDatabaseNames() throws Exception {
    repo.getDatabaseNames( true );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testReadDatabases() throws Exception {
    repo.readDatabases();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetDatabaseID() throws Exception {
    repo.getDatabaseID( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadClusterSchema() throws Exception {
    repo.loadClusterSchema( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetClusterIDs() throws Exception {
    repo.getClusterIDs( false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetClusterNames() throws Exception {
    repo.getClusterNames( true );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetClusterID() throws Exception {
    repo.getClusterID( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDeleteClusterSchema() throws Exception {
    repo.deleteClusterSchema( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadSlaveServer() throws Exception {
    repo.loadSlaveServer( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetSlaveIDs() throws Exception {
    repo.getSlaveIDs( false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetSlaveNames() throws Exception {
    repo.getSlaveNames( false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetSlaveServers() throws Exception {
    repo.getSlaveServers();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetSlaveID() throws Exception {
    repo.getSlaveID( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDeleteSlave() throws Exception {
    repo.deleteSlave( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadPartitionSchema() throws Exception {
    repo.loadPartitionSchema( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetPartitionSchemaIDs() throws Exception {
    repo.getPartitionSchemaIDs( false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetPartitionSchemaNames() throws Exception {
    repo.getPartitionSchemaNames( false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetPartitionSchemaID() throws Exception {
    repo.getPartitionSchemaID( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDeletePartitionSchema() throws Exception {
    repo.deletePartitionSchema( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadRepositoryDirectoryTree() throws Exception {
    repo.loadRepositoryDirectoryTree();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testFindDirectory() throws Exception {
    repo.findDirectory( new String() );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testFindDirectory1() throws Exception {
    repo.findDirectory( new StringObjectId( "" ) );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveRepositoryDirectory() throws Exception {
    repo.saveRepositoryDirectory( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testDeleteRepositoryDirectory() throws Exception {
    repo.deleteRepositoryDirectory( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testRenameRepositoryDirectory() throws Exception {
    repo.renameRepositoryDirectory( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testCreateRepositoryDirectory() throws Exception {
    repo.createRepositoryDirectory( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetTransformationNames() throws Exception {
    repo.getTransformationNames( null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobObjects() throws Exception {
    repo.getJobObjects( null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetTransformationObjects() throws Exception {
    repo.getTransformationObjects( null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobAndTransformationObjects() throws Exception {
    repo.getJobAndTransformationObjects( null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobNames() throws Exception {
    repo.getJobNames( null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetDirectoryNames() throws Exception {
    repo.getDirectoryNames( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testInsertLogEntry() throws Exception {
    repo.insertLogEntry( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testInsertStepDatabase() throws Exception {
    repo.insertStepDatabase( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testInsertJobEntryDatabase() throws Exception {
    repo.insertJobEntryDatabase( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveConditionStepAttribute() throws Exception {
    repo.saveConditionStepAttribute( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadConditionFromStepAttribute() throws Exception {
    repo.loadConditionFromStepAttribute( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetStepAttributeBoolean() throws Exception {
    repo.getStepAttributeBoolean( null, 0, null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetStepAttributeBoolean1() throws Exception {
    repo.getStepAttributeBoolean( null, 0, null );

  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetStepAttributeBoolean2() throws Exception {
    repo.getStepAttributeBoolean( null, null );

  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetStepAttributeInteger() throws Exception {
    repo.getStepAttributeInteger( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetStepAttributeInteger1() throws Exception {
    repo.getStepAttributeInteger( null, 0, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetStepAttributeString() throws Exception {
    repo.getStepAttributeString( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetStepAttributeString1() throws Exception {
    repo.getStepAttributeString( null, 0, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute() throws Exception {
    repo.saveStepAttribute( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute1() throws Exception {
    repo.saveStepAttribute( null, null, 0, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute2() throws Exception {
    repo.saveStepAttribute( null, null, 0, null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute3() throws Exception {
    repo.saveStepAttribute( null, null, null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute4() throws Exception {
    repo.saveStepAttribute( null, null, 0, null, 0L );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute5() throws Exception {
    repo.saveStepAttribute( null, null, null, 0L );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute6() throws Exception {
    repo.saveStepAttribute( null, null, 0, null, 0D );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveStepAttribute7() throws Exception {
    repo.saveStepAttribute( null, null, null, 0D );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testCountNrStepAttributes() throws Exception {
    repo.countNrStepAttributes( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testCountNrJobEntryAttributes() throws Exception {
    repo.countNrJobEntryAttributes( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobEntryAttributeBoolean() throws Exception {
    repo.getJobEntryAttributeBoolean( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobEntryAttributeBoolean1() throws Exception {
    repo.getJobEntryAttributeBoolean( null, 0, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobEntryAttributeBoolean2() throws Exception {
    repo.getJobEntryAttributeBoolean( null, null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobEntryAttributeInteger() throws Exception {
    repo.getJobEntryAttributeInteger( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobEntryAttributeInteger1() throws Exception {
    repo.getJobEntryAttributeInteger( null, 0, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobEntryAttributeString() throws Exception {
    repo.getJobEntryAttributeString( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobEntryAttributeString1() throws Exception {
    repo.getJobEntryAttributeString( null, 0, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveJobEntryAttribute() throws Exception {
    repo.saveJobEntryAttribute( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveJobEntryAttribute1() throws Exception {
    repo.saveJobEntryAttribute( null, null, 0, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveJobEntryAttribute2() throws Exception {
    repo.saveJobEntryAttribute( null, null, null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveJobEntryAttribute3() throws Exception {
    repo.saveJobEntryAttribute( null, null, 0, null, false );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveJobEntryAttribute4() throws Exception {
    repo.saveJobEntryAttribute( null, null, null, 0L );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveJobEntryAttribute5() throws Exception {
    repo.saveJobEntryAttribute( null, null, 0, null, 0L );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadDatabaseMetaFromStepAttribute() throws Exception {
    repo.loadDatabaseMetaFromStepAttribute( null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveDatabaseMetaStepAttribute() throws Exception {
    repo.saveDatabaseMetaStepAttribute( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadDatabaseMetaFromJobEntryAttribute() throws Exception {
    repo.loadDatabaseMetaFromJobEntryAttribute( null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testLoadDatabaseMetaFromJobEntryAttribute1() throws Exception {
    repo.loadDatabaseMetaFromJobEntryAttribute( null, null, 0, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveDatabaseMetaJobEntryAttribute() throws Exception {
    repo.saveDatabaseMetaJobEntryAttribute( null, null, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testSaveDatabaseMetaJobEntryAttribute1() throws Exception {
    repo.saveDatabaseMetaJobEntryAttribute( null, null, 0, null, null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testUndeleteObject() throws Exception {
    repo.undeleteObject( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetServiceInterfaces() throws Exception {
    repo.getServiceInterfaces();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetService() throws Exception {
    repo.getService( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testHasService() throws Exception {
    repo.hasService( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetObjectInformation() throws Exception {
    repo.getObjectInformation( null, null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetConnectMessage() throws Exception {
    repo.getConnectMessage();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetJobsUsingDatabase() throws Exception {
    repo.getJobsUsingDatabase( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetTransformationsUsingDatabase() throws Exception {
    repo.getTransformationsUsingDatabase( null );
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetImporter() throws Exception {
    repo.getImporter();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetExporter() throws Exception {
    repo.getExporter();
  }

  @Test( expected=UnsupportedOperationException.class )
  public void testGetMetaStore() throws Exception {
    repo.getMetaStore();
  }
}
