/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.impl.model;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.steps.mongodb.MongoDbMeta;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MongoDbResourceInfoTest {

  MongoDbResourceInfo info;

  MongoDbMeta meta;

  @BeforeClass
  public static void init() throws Exception {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    Encr.init( "Kettle" );
  }

  @Before
  public void setUp() throws Exception {
    meta = mock( MongoDbMeta.class );
    info = new MongoDbResourceInfo( meta );
  }

  @Test
  public void testStringConstructor() {
    info = new MongoDbResourceInfo( "localhost, remote.pentaho.com" , "1000", "myDb" );
    assertEquals( "localhost, remote.pentaho.com", info.getHostNames() );
    assertEquals( "1000", info.getPort() );
    assertEquals( "myDb", info.getDatabase() );
  }

  @Test
  public void testGetConnectTimeout() throws Exception {
    assertNull( info.getConnectTimeout() );
    info.setConnectTimeout( "1000" );
    assertEquals( "1000", info.getConnectTimeout() );
  }

  @Test
  public void testGetDatabase() throws Exception {
    assertNull( info.getDatabase() );
    info.setDatabase( "myDb" );
    assertEquals( "myDb", info.getDatabase() );
  }

  @Test
  public void testGetHostNames() throws Exception {
    assertNull( info.getHostNames() );
    info.setHostNames( "localhost, remote.pentaho.com" );
    assertEquals( "localhost, remote.pentaho.com", info.getHostNames() );
  }

  @Test
  public void testGetPort() throws Exception {
    assertNull( info.getPort() );
    info.setPort( "1000" );
    assertEquals( "1000", info.getPort() );
  }

  @Test
  public void testGetSocketTimeout() throws Exception {
    assertNull( info.getSocketTimeout() );
    info.setSocketTimeout( "1000" );
    assertEquals( "1000", info.getSocketTimeout() );
  }

  @Test
  public void testIsUseAllReplicaSetMembers() throws Exception {
    assertFalse( info.isUseAllReplicaSetMembers() );
    info.setUseAllReplicaSetMembers( true );
    assertTrue( info.isUseAllReplicaSetMembers() );
  }

  @Test
  public void testIsUseKerberosAuthentication() throws Exception {
    assertFalse( info.isUseKerberosAuthentication() );
    info.setUseKerberosAuthentication( true );
    assertTrue( info.isUseKerberosAuthentication() );
  }

  @Test
  public void testGetUser() throws Exception {
    assertNull( info.getUser() );
    info.setUser( "joe" );
    assertEquals( "joe", info.getUser() );
  }

  @Test
  public void testGetPassword() throws Exception {
    assertNull( info.getPassword() );
    info.setPassword( "password" );
    assertEquals( "password", info.getPassword() );
  }

  @Test
  public void testGetEncryptedPassword() throws Exception {
    assertNull( info.getPassword() );
    info.setPassword( "password" );
    assertEquals( "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde", info.getEncryptedPassword() );
  }
}
