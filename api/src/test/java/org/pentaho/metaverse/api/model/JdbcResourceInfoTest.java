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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

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

  @Test
  public void testDbMetaVarPort() throws Exception {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    DatabaseInterface dbInterface = mock( DatabaseInterface.class ); 
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbInterface );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "Native" );

    final VariableSpace vs = new Variables();
    when( dbMeta.getDatabasePortNumberString() ).thenReturn( "${port_var}" );
    when( dbMeta.environmentSubstitute( any( String.class ) ) ).thenAnswer( new Answer<String>() {
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return vs.environmentSubstitute( (String) invocation.getArguments()[0] );
      }
    } );

    // check if var replaced
    vs.setVariable( "port_var", "4321" );
    JdbcResourceInfo jdbcResourceInfo = new JdbcResourceInfo( dbMeta );
    assertEquals( jdbcResourceInfo.getPort(), new Integer( 4321 ) );

    // check no exception when empty
    vs.setVariable( "port_var", "" );
    jdbcResourceInfo = new JdbcResourceInfo( dbMeta );
  }

}
