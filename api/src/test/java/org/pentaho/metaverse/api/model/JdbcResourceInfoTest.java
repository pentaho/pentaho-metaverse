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
