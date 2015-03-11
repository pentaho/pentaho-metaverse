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
package com.pentaho.metaverse.api.model;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.resource.ResourceEntry;

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
    resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta, false );
    assertFalse( resourceInfo.isInput() );
    when( dbMeta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "JNDI" );
    resourceInfo = ExternalResourceInfoFactory.createDatabaseResource( dbMeta );
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
    FileName mockFilename = mock( FileName.class );
    when( mockFilename.getPath() ).thenReturn( "/path/to/file" );
    when( mockFile.getName() ).thenReturn( mockFilename );
    IExternalResourceInfo resource = ExternalResourceInfoFactory.createFileResource( mockFile, false );
    assertNotNull( resource );
    assertEquals( "/path/to/file", resource.getName() );
    assertFalse( resource.isInput() );
  }
}
