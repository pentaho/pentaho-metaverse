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
import org.pentaho.platform.api.metaverse.MetaverseLocatorException;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
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

  @Mock
  private HttpHeaders mockHeadersXml;

  @Mock
  private HttpHeaders mockHeadersJson;

  @Mock
  private HttpHeaders mockHeadersText;

  private static final String TEST_XML = "<xml><graphml></graphml>";
  private static final String TEST_JSON = "{\"mode\":\"NORMAL\",\"vertices\":[],\"edges\":[]}";
  private static final String TEST_CSV = "\"SourceId\",\"SourceVirtual\",\"SourceFileType\",\"SourceName\"," +
    "\"SourceAuthor\",\"SourceModified\",\"LinkType\",\"DestinationId\",\"DestinationVirtual\"," +
    "\"DestinationFileType\",\"DestinationName\",\"DestinationAuthor\",\"DestinationModified\"";

  private Set<IDocumentLocator> locators;
  private MetaverseService service;

  @Before
  public void setUp() throws Exception {
    locators = new HashSet<IDocumentLocator>();
    locators.add( mockLocator );

    service = new MetaverseService( mockReader, mockProvider );

    when( mockHeadersXml.getAcceptableMediaTypes() ).thenReturn(
      new ArrayList<MediaType>() { {
        add( MediaType.APPLICATION_XML_TYPE );
      } }
    );

    when( mockHeadersJson.getAcceptableMediaTypes() ).thenReturn(
      new ArrayList<MediaType>() { {
        add( MediaType.APPLICATION_JSON_TYPE );
      } }
    );

    when( mockHeadersText.getAcceptableMediaTypes() ).thenReturn(
      new ArrayList<MediaType>() { {
        add( MediaType.TEXT_PLAIN_TYPE );
      } }
    );
  }

  @Test
  public void testExport_xml() throws MetaverseLocatorException {
    when( mockReader.exportFormat( anyString() ) ).thenReturn( TEST_XML );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    Response response = service.export( mockHeadersXml );
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_XML, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();
  }

  @Test
  public void testExport_json() throws MetaverseLocatorException {
    when( mockReader.exportFormat( anyString() ) ).thenReturn( TEST_JSON );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    Response response = service.export( mockHeadersJson );
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_JSON, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();
  }

  @Test
  public void testExport_csv() throws MetaverseLocatorException {
    when( mockReader.exportFormat( anyString() ) ).thenReturn( TEST_CSV );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    Response response = service.export( mockHeadersText );
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_CSV, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();
  }

  @Test
  public void testExport_MultipleCalls()throws MetaverseLocatorException {
    when( mockReader.exportFormat( anyString() ) ).thenReturn( TEST_XML );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    Response response = service.export( mockHeadersXml );

    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_XML, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();

    response = service.export( mockHeadersXml );

    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( TEST_XML, response.getEntity().toString() );
    verify( mockLocator, times( 1 ) ).startScan();

  }


  @Test
  public void testExport_NoLocators() throws MetaverseLocatorException {
    when( mockReader.exportFormat( anyString() ) ).thenReturn( "" );
    when( mockProvider.getDocumentLocators() ).thenReturn( null );

    Response response = service.export( mockHeadersXml );
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertNotNull( response.getEntity() );
    assertEquals( "", response.getEntity().toString() );

    verify( mockLocator, times( 0 ) ).startScan();
  }

  @Test
  public void testExport_NullReader() throws Exception {
    when( mockReader.exportFormat( anyString() ) ).thenReturn( TEST_XML );
    when( mockProvider.getDocumentLocators() ).thenReturn( locators );

    service = new MetaverseService( null, mockProvider );
    Response response = service.export( mockHeadersXml );
    assertNotNull( response );
    assertEquals( 500, response.getStatus() );
    assertEquals( Messages.getString( "ERROR.MetaverseReader.IsNull" ), response.getEntity().toString() );
  }

  @Test
  public void testExport_NullProvider() throws Exception {
    when( mockReader.exportFormat( anyString() ) ).thenReturn( "" );

    service = new MetaverseService( mockReader, null );
    Response response = service.export( mockHeadersXml );
    assertNotNull( response );
    assertEquals( 200, response.getStatus() );
    assertEquals( "", response.getEntity().toString() );
  }


}
