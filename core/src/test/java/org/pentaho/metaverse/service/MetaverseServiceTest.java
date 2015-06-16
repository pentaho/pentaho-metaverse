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

package org.pentaho.metaverse.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.metaverse.api.IDocumentLocator;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.messages.Messages;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
      new ArrayList<MediaType>() {
        {
          add( MediaType.APPLICATION_XML_TYPE );
        }
      }
    );

    when( mockHeadersJson.getAcceptableMediaTypes() ).thenReturn(
      new ArrayList<MediaType>() {
        {
          add( MediaType.APPLICATION_JSON_TYPE );
        }
      }
    );

    when( mockHeadersText.getAcceptableMediaTypes() ).thenReturn(
      new ArrayList<MediaType>() {
        {
          add( MediaType.TEXT_PLAIN_TYPE );
        }
      }
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
  public void testExport_MultipleCalls() throws MetaverseLocatorException {
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

  @Test
  public void testPrepareMetaverse() {
    service = new MetaverseService( mockReader, mockProvider );
    service.prepareMetaverse();
  }

  @Test
  public void testPrepareMetaverse_Exception() {
    service = new MetaverseService( mockReader, mockProvider );
    // Set up for exception
    when( mockProvider.getDocumentLocators() ).thenThrow( MetaverseLocatorException.class );
    service.prepareMetaverse();
  }

  @Test
  public void testPrepareMetaverse_InterruptedException() {
    service = new MetaverseService( mockReader, mockProvider );
    // Set up for exception
    when( mockProvider.getDocumentLocators() ).thenThrow( InterruptedException.class );
    service.prepareMetaverse();
  }

  @Test
  public void testPrepareMetaverse_ExecutionException() {
    service = new MetaverseService( mockReader, mockProvider );
    // Set up for exception
    when( mockProvider.getDocumentLocators() ).thenThrow( ExecutionException.class );
    service.prepareMetaverse();
  }
}
