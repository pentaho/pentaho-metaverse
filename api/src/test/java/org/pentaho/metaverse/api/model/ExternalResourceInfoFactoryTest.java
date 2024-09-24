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

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.dictionary.DictionaryConst;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExternalResourceInfoFactoryTest {

  @BeforeClass
  public static void init() throws Exception {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    Encr.init( "Kettle" );

  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull( new ExternalResourceInfoFactory() );
  }

  @Test
  public void testCreateDatabaseResource() throws Exception {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getName() ).thenReturn( "myConnection" );
    when( dbMeta.getDescription() ).thenReturn( "Description" );
    DatabaseInterface dbInterface = mock( DatabaseInterface.class );
    when( dbMeta.getDatabaseInterface() ).thenReturn( dbInterface );
    when( dbMeta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_NATIVE );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "Native" );
    IExternalResourceInfo resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta );
    assertTrue( resourceInfo.isInput() );
    assertThat( resourceInfo, instanceOf(JdbcResourceInfo.class) );
    resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta, false );
    assertFalse( resourceInfo.isInput() );
    assertThat( resourceInfo, instanceOf(JdbcResourceInfo.class) );

    when( dbMeta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "JNDI" );
    resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta );
    assertTrue( resourceInfo.isInput() );
    assertThat( resourceInfo, instanceOf(JndiResourceInfo.class) );
    resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta, false );
    assertFalse( resourceInfo.isInput() );
    assertThat( resourceInfo, instanceOf(JndiResourceInfo.class) );

    when( dbMeta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_OCI );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "OCI" );
    resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta );
    assertTrue( resourceInfo.isInput() );
    assertThat( resourceInfo, instanceOf(OCIResourceInfo.class) );
    resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta, false );
    assertFalse( resourceInfo.isInput() );
    assertThat( resourceInfo, instanceOf(OCIResourceInfo.class) );
  }

  @Test
  public void testCreateResource() throws Exception {
    ResourceEntry resourceEntry = mock( ResourceEntry.class );
    when( resourceEntry.getResource() ).thenReturn( "myResource" );
    when( resourceEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.ACTIONFILE );
    IExternalResourceInfo resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry );
    assertEquals( DictionaryConst.NODE_TYPE_FILE, resourceInfo.getType() );
    when( resourceEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.FILE );
    resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry, true );
    assertEquals( DictionaryConst.NODE_TYPE_FILE, resourceInfo.getType() );
    when( resourceEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.URL );
    resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry );
    assertEquals( DictionaryConst.NODE_TYPE_WEBSERVICE, resourceInfo.getType() );
    when( resourceEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.CONNECTION );
    resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry, false );
    assertEquals( DictionaryConst.NODE_TYPE_DATASOURCE, resourceInfo.getType() );
    when( resourceEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.DATABASENAME );
    resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry );
    assertEquals( DictionaryConst.NODE_TYPE_DATASOURCE, resourceInfo.getType() );
    when( resourceEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.SERVER );
    resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry );
    assertEquals( "SERVER", resourceInfo.getType() );
    when( resourceEntry.getResourcetype() ).thenReturn( ResourceEntry.ResourceType.OTHER );
    resourceInfo = ExternalResourceInfoFactory.createResource( resourceEntry );
    assertEquals( "OTHER", resourceInfo.getType() );
  }

  @Test
  public void testCreateFileResource() throws Exception {
    assertNull( ExternalResourceInfoFactory.createFileResource( null ) );
    FileObject mockFile = mock( FileObject.class );
    IExternalResourceInfo resource = ExternalResourceInfoFactory.createFileResource( mockFile, false );
    assertNotNull( resource );
    assertNull( resource.getName() );
    assertFalse( resource.isInput() );

    when( mockFile.getPublicURIString() ).thenReturn( "/path/to/file" );
    resource = ExternalResourceInfoFactory.createFileResource( mockFile, true );
    assertNotNull( resource );
    assertEquals( "/path/to/file", resource.getName() );
    assertTrue( resource.isInput() );
  }
}
