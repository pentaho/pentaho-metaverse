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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcResourceInfoTest {

  JdbcResourceInfo jdbcResourceInfo;

  @BeforeClass
  public static void init() throws Exception {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    Encr.init( "Kettle" );
  }

  @Before
  public void setUp() throws Exception {
    jdbcResourceInfo = new JdbcResourceInfo();
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorDatabaseMeta() {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getName() ).thenReturn( "myConnection" );
    when( dbMeta.getDescription() ).thenReturn( "Description" );
    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbInterface.getPluginId() ).thenReturn( "myPlugin" );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbInterface );
    when( dbMeta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    jdbcResourceInfo = new JdbcResourceInfo( dbMeta );
  }

  @Test
  public void testGetType() throws Exception {
    assertEquals( JdbcResourceInfo.JDBC, jdbcResourceInfo.getType() );
  }

  @Test
  public void testGetSetPort() throws Exception {
    assertNull( jdbcResourceInfo.getPort() );
    Integer port = new Integer( 1000 );
    jdbcResourceInfo.setPort( port );
    assertEquals( port, jdbcResourceInfo.getPort() );
  }

  @Test
  public void testGetSetServer() throws Exception {
    assertNull( jdbcResourceInfo.getServer() );
    String server = "test.pentaho.com";
    jdbcResourceInfo.setServer( server );
    assertEquals( server, jdbcResourceInfo.getServer() );
  }

  @Test
  public void testGetSetUsername() throws Exception {
    assertNull( jdbcResourceInfo.getUsername() );
    String username = "joe";
    jdbcResourceInfo.setUsername( username );
    assertEquals( username, jdbcResourceInfo.getUsername() );
  }

  @Test
  public void testGetSetPassword() throws Exception {
    assertNull( jdbcResourceInfo.getPassword() );
    String password = "password";
    jdbcResourceInfo.setPassword( password );
    assertEquals( password, jdbcResourceInfo.getPassword() );
  }

  @Test
  public void testGetEncryptedPassword() {
    jdbcResourceInfo.setPassword( "1234567890" );
    assertEquals( Encr.PASSWORD_ENCRYPTED_PREFIX + "2be98afc86aa7c3d6f84dfb2689caf68a", jdbcResourceInfo.getEncryptedPassword() );
  }

  @Test
  public void testGetSetDatabaseName() throws Exception {
    assertNull( jdbcResourceInfo.getDatabaseName() );
    String dbName = "foodmart";
    jdbcResourceInfo.setDatabaseName( dbName );
    assertEquals( dbName, jdbcResourceInfo.getDatabaseName() );
  }
}
