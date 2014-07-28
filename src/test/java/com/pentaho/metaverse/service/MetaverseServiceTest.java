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

package com.pentaho.metaverse.service;

import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.messages.Messages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.metaverse.IDocumentLocator;

import javax.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class MetaverseServiceTest {

  @Mock
  private IMetaverseReader mockReader;

  @Mock
  private IDocumentLocatorProvider mockProvider;

  @Mock
  private IDocumentLocator mockLocator;

  private static final String TEST_XML = "<xml><graphml></graphml>";
  private Set<IDocumentLocator> locators;
  private MetaverseService service;

  @Before
  public void setUp() throws Exception {
    locators = new HashSet<IDocumentLocator>();
    locators.add( mockLocator );

    service = new MetaverseService( mockReader, mockProvider );
    service.setDelay( 0 );
  }

  @Test
  public void testExport() {
    when( mockReader.export() ).thenReturn( TEST_XML );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    Response response = service.export();
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_XML, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();
  }

  @Test
  public void testExport_MultipleCalls() {
    when( mockReader.export() ).thenReturn( TEST_XML );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    Response response = service.export();

    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_XML, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();

    response = service.export();

    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_XML, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();

  }


  @Test
  public void testExport_NoLocators() {
    when( mockReader.export() ).thenReturn( "" );
    when( mockProvider.getDocumentLocators() ).thenReturn( null );

    Response response = service.export();
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( "", response.getEntity().toString() );

    verify( mockLocator, times( 0 ) ).startScan();
  }

  @Test
  public void testExport_NullReader() throws Exception {
    when( mockReader.export() ).thenReturn( TEST_XML );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    service = new MetaverseService( null, mockProvider );
    Response response = service.export();
    assertNotNull( response );
    assertEquals( 500, response.getStatus() );
    assertEquals( Messages.getString( "ERROR.MetaverseReader.IsNull" ), response.getEntity().toString() );
  }

  @Test
  public void testExport_NullProvider() throws Exception {
    when( mockReader.export() ).thenReturn( "" );

    service = new MetaverseService( mockReader, null );
    Response response = service.export();
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertEquals( "", response.getEntity().toString() );
  }


}
