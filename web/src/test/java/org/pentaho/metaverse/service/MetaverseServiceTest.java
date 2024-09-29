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


package org.pentaho.metaverse.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.metaverse.api.IDocumentLocator;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.ILineageCollector;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.api.MetaverseLocatorException;
import org.pentaho.metaverse.api.model.LineageRequest;
import org.pentaho.metaverse.impl.MetaverseCompletionService;
import org.pentaho.metaverse.messages.Messages;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
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

  @Mock
  private ILineageCollector mockCollector;

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
  public void testPrepareMetaverse_Exception() throws MetaverseLocatorException {
    service = new MetaverseService( mockReader, mockProvider );

    // Set up for exception
    IDocumentLocator mockLocator = mock( IDocumentLocator.class );
    doThrow( new MetaverseLocatorException() ).when( mockLocator ).startScan();
    when( mockProvider.getDocumentLocators() ).thenReturn( Set.of( mockLocator ) );

    service.prepareMetaverse();
  }

  @Test
  public void testPrepareMetaverse_InterruptedException() throws ExecutionException, InterruptedException {
    try( MockedStatic<MetaverseCompletionService> mockCompletionService = mockStatic( MetaverseCompletionService.class ) ) {
      service = new MetaverseService( mockReader, mockProvider );

      // Set up for exception
      MetaverseCompletionService mockCompletionServiceInstance = mock( MetaverseCompletionService.class );
      doThrow( new InterruptedException() ).when( mockCompletionServiceInstance ).waitTillEmpty();
      mockCompletionService.when( MetaverseCompletionService::getInstance ).thenReturn( mockCompletionServiceInstance );

      service.prepareMetaverse();
    }
  }

  @Test
  public void testPrepareMetaverse_ExecutionException() throws ExecutionException, InterruptedException {
    try( MockedStatic<MetaverseCompletionService> mockCompletionService = mockStatic( MetaverseCompletionService.class ) ) {
      service = new MetaverseService( mockReader, mockProvider );

      // Set up for exception
      MetaverseCompletionService mockCompletionServiceInstance = mock( MetaverseCompletionService.class );
      doThrow( ExecutionException.class ).when( mockCompletionServiceInstance ).waitTillEmpty();
      mockCompletionService.when( MetaverseCompletionService::getInstance ).thenReturn( mockCompletionServiceInstance );

      service.prepareMetaverse();
    }
  }

  @Test
  public void testDownload() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    when( mockCollector.listArtifacts(any(), any()) ).thenReturn( paths );

    Response response = service.download();
    assertNotNull( response );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "application/zip", response.getHeaderString( "Content-Type" ) );
    assertEquals( "inline; filename=pentaho-lineage.zip", response.getHeaderString( "Content-Disposition" ) );

    verify( mockCollector ).listArtifacts( null, null );
  }

  @Test
  public void testDownload_nullLineageCollector() throws Exception {
    service.setLineageCollector( null );

    List<String> paths = new ArrayList<>();

    Response response = service.download();
    assertNotNull( response );
    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testDownload_startingDate() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    String dateString = "20150707";
    when( mockCollector.listArtifacts( eq( dateString ), any() ) ).thenReturn( paths );

    Response response = service.download( dateString );
    assertNotNull( response );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "application/zip", response.getHeaderString( "Content-Type" ) );
    assertEquals( "inline; filename=pentaho-lineage_" + dateString + ".zip",
      response.getHeaderString( "Content-Disposition" ) );

    verify( mockCollector ).listArtifacts( eq( dateString ), any() );
  }

  @Test
  public void testDownload_startingDate_endingDate() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    String dateString = "20150707";
    String endingDate = "20151030";
    when( mockCollector.listArtifacts( dateString, endingDate ) ).thenReturn( paths );

    Response response = service.download( dateString, endingDate );
    assertNotNull( response );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "application/zip", response.getHeaderString( "Content-Type" ) );
    assertEquals( "inline; filename=pentaho-lineage_" + dateString + "-" + endingDate + ".zip",
      response.getHeaderString( "Content-Disposition" ) );

    verify( mockCollector ).listArtifacts( eq( dateString ), eq( endingDate ) );
  }

  @Test
  public void testDownload_startingDate_nullLineageCollector() throws Exception {
    service.setLineageCollector( null );

    String dateString = "20150707";

    Response response = service.download( dateString );
    assertNotNull( response );
    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
  }

  @Test( expected = BadRequestException.class )
  public void testDownload_invalidStartingDate() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    String dateString = "20159999";
    when( mockCollector.listArtifacts( dateString, null ) ).thenThrow( new IllegalArgumentException( "bad" ) );

    Response response = service.download( dateString );
  }

  @Test
  public void testDownload_forSpecificKtr() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    String path = "/tmp/test/some.ktr";
    when( mockCollector.listArtifactsForFile( eq( path ), any(), any() ) ).thenReturn( paths );

    LineageRequest request = new LineageRequest();
    request.setPath( path );
    Response response = service.downloadFile( request );
    assertNotNull( response );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "application/zip", response.getHeaderString( "Content-Type" ) );
    assertEquals( "inline; filename=some.ktr_lineage.zip", response.getHeaderString( "Content-Disposition" ) );

    verify( mockCollector ).listArtifactsForFile( path, null, null );
  }

  @Test
  public void testDownload_forSpecificKtr_nullLineageCollector() throws Exception {
    service.setLineageCollector( null );

    String path = "/tmp/test/some.ktr";

    LineageRequest request = new LineageRequest();
    request.setPath( path );
    Response response = service.downloadFile( request );
    assertNotNull( response );
    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testDownload_forSpecificKtr_startingDate() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    String path = "/tmp/test/some.ktr";
    String dateString = "20150707";
    when( mockCollector.listArtifactsForFile( eq( path ), eq( dateString ), any() ) ).thenReturn( paths );

    LineageRequest request = new LineageRequest();
    request.setPath( path );
    Response response = service.downloadFile( request, dateString );
    assertNotNull( response );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "application/zip", response.getHeaderString( "Content-Type" ) );
    assertEquals( "inline; filename=some.ktr_lineage_" + dateString + ".zip",
      response.getHeaderString( "Content-Disposition" ) );

    verify( mockCollector ).listArtifactsForFile( path, dateString, null );
  }

  @Test
  public void testDownload_forSpecificKtr_startingDate_endingDate() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    String path = "/tmp/test/some.ktr";
    String dateString = "20150707";
    String endingDate = "20151030";
    when( mockCollector.listArtifactsForFile( path, dateString, endingDate ) ).thenReturn( paths );

    LineageRequest request = new LineageRequest();
    request.setPath( path );
    Response response = service.downloadFile( request, dateString, endingDate );
    assertNotNull( response );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "application/zip", response.getHeaderString( "Content-Type" ) );
    assertEquals( "inline; filename=some.ktr_lineage_" + dateString + "-" + endingDate + ".zip",
      response.getHeaderString( "Content-Disposition" ) );

    verify( mockCollector ).listArtifactsForFile( path, dateString, endingDate );
  }

  @Test( expected = BadRequestException.class )
  public void testDownload_forSpecificKtr_invalidDate() throws Exception {
    service.setLineageCollector( mockCollector );

    List<String> paths = new ArrayList<>();
    String path = "/tmp/test/some.ktr";
    String dateString = "20159999";
    when( mockCollector.listArtifactsForFile( path, dateString, null ) ).thenThrow( new IllegalArgumentException( "error" ) );

    LineageRequest request = new LineageRequest();
    request.setPath( path );
    Response response = service.downloadFile( request, dateString );
  }
}
