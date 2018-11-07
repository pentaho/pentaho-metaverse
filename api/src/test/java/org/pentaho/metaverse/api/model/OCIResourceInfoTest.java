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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OCIResourceInfoTest {

  OCIResourceInfo ociResourceInfo;

  @BeforeClass
  public static void init() throws Exception {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    Encr.init( "Kettle" );
  }

  @Before
  public void setUp() throws Exception {
    ociResourceInfo = new OCIResourceInfo();
  }

  @Test
  public void testConstructorDatabaseMeta1() {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getName() ).thenReturn( "myConnection" );
    when( dbMeta.getDescription() ).thenReturn( "Description" );
    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbInterface.getPluginId() ).thenReturn( "myPlugin" );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbInterface );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "OCI" );
    ociResourceInfo = new OCIResourceInfo( dbMeta );
    assertNotNull( ociResourceInfo );
  }

  @Test
  public void testConstructorDatabaseMeta2() {
    String databaseName = "myDbName";
    String username = "user1";
    String password = "password1";
    OCIResourceInfo oci = new OCIResourceInfo( databaseName, username, password );
    assertEquals( databaseName, oci.getDatabaseName() );
    assertEquals( username, oci.getUsername() );
    assertEquals( password, oci.getPassword() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorDatabaseMetaIllegalJNDI() {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getName() ).thenReturn( "myConnection" );
    when( dbMeta.getDescription() ).thenReturn( "Description" );
    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbInterface.getPluginId() ).thenReturn( "myPlugin" );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbInterface );
    when( dbMeta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    ociResourceInfo = new OCIResourceInfo( dbMeta );
  }

  @Test
  public void testGetType() throws Exception {
    assertEquals( ociResourceInfo.OCI, ociResourceInfo.getType() );
  }

  @Test
  public void testGetSetUsername() throws Exception {
    assertNull( ociResourceInfo.getUsername() );
    String username = "pentaho";
    ociResourceInfo.setUsername( username );
    assertEquals( username, ociResourceInfo.getUsername() );
  }

  @Test
  public void testGetSetPassword() throws Exception {
    assertNull( ociResourceInfo.getPassword() );
    String password = "password";
    ociResourceInfo.setPassword( password );
    assertEquals( password, ociResourceInfo.getPassword() );
  }

  @Test
  public void testGetEncryptedPassword() {
    ociResourceInfo.setPassword( "password0" );
    assertEquals( Encr.PASSWORD_ENCRYPTED_PREFIX + "2be98afc86aa7f294aa0abd67d180ab8a", ociResourceInfo.getEncryptedPassword() );
  }

  @Test
  public void testGetSetDatabaseName() throws Exception {
    assertNull( ociResourceInfo.getDatabaseName() );
    String dbName = "mydb";
    ociResourceInfo.setDatabaseName( dbName );
    assertEquals( dbName, ociResourceInfo.getDatabaseName() );
  }

  @Test
  public void testGetSetTablespace() throws Exception {
    assertNull( ociResourceInfo.getDataTablespace() );
    String dataTablespace = "space";
    ociResourceInfo.setDataTablespace( dataTablespace );
    assertEquals( dataTablespace, ociResourceInfo.getDataTablespace() );
  }

  @Test
  public void testGetSetIndexTablespace() throws Exception {
    assertNull( ociResourceInfo.getIndexTablespace() );
    String dataTablespace = "index_space";
    ociResourceInfo.setIndexTablespace( dataTablespace );
    assertEquals( dataTablespace, ociResourceInfo.getIndexTablespace() );
  }

}
