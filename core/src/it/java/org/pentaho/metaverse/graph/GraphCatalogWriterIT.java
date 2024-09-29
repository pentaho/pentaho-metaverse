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


package org.pentaho.metaverse.graph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.metaverse.api.ICatalogLineageClient;
import org.pentaho.metaverse.api.ICatalogLineageClientProvider;
import org.pentaho.metaverse.api.model.catalog.FieldLevelRelationship;
import org.pentaho.metaverse.api.model.catalog.LineageDataResource;
import org.pentaho.metaverse.step.StepAnalyzerValidationIT;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;

public class GraphCatalogWriterIT extends StepAnalyzerValidationIT {
  @Mock ICatalogLineageClient mockCatalogLineageClient;
  @Captor ArgumentCaptor<List<LineageDataResource>> inputSourceCaptor;
  @Captor ArgumentCaptor<List<LineageDataResource>> outputSourcesCaptor;
  @Mock ICatalogLineageClientProvider mockCatalogLineageClientProvider;

  @Before
  public void setup() {
    Mockito.when( mockCatalogLineageClientProvider.getCatalogLineageClient( anyString(), anyString(), anyString(), anyString(), anyString(), anyString() ) )
      .thenReturn( mockCatalogLineageClient );
  }

  @Test
  public void testMultiSource() throws Exception {
    final String transNodeName = "CombineMultiSourceToTarget";
    initTest( transNodeName );

    GraphCatalogWriter graphCatalogWriter =
      new GraphCatalogWriter( "", "", "", "", "", "" );
    graphCatalogWriter.setCatalogLineageClientProvider( mockCatalogLineageClientProvider );

    graphCatalogWriter.outputGraphImpl( graph, null );

    Path personCsvPath = Paths.get("src", "it", "resources", "person.csv");
    Path personDetailsCsvPath = Paths.get("src", "it", "resources", "person_details.csv");

    LineageDataResource personCsv = new LineageDataResource( "person.csv" );
    personCsv.setPath( personCsvPath.toAbsolutePath().toString() );
    List<String> personFields = Arrays.asList( "first_name", "id", "last_name" );
    personCsv.setFields( personFields );
    LineageDataResource personDetailsCsv = new LineageDataResource( "person_details.csv" );
    personDetailsCsv.setPath( personDetailsCsvPath.toAbsolutePath().toString() );
    List<String> personDetailsFields = Arrays.asList( "gender", "ip_address", "id", "age", "email" );
    personDetailsCsv.setFields( personDetailsFields );
    LineageDataResource outputTarget = new LineageDataResource( "CombinedCsvToTextOut.csv" );
    outputTarget.setPath( "/Users/aramos/Documents/Hitachi/REPOS/R2D2-DEV/CatalogTestKTR/out/CombinedCsvToTextOut.csv" );
    List<String> outputFields = Arrays.asList( "GIVEN", "HOST", "SUR", "SPAN", "USERNAME", "LONG_LEGAL", "SSN", "SEX" );
    outputTarget.setFields( outputFields );
    addRelationship( personCsv, outputTarget, "first_name", "GIVEN" );
    addRelationship( personCsv, outputTarget, "last_name", "SUR" );
    addRelationship( personCsv, outputTarget, "id", "SSN" );
    addRelationship( personDetailsCsv, outputTarget, "ip_address", "HOST" );
    addRelationship( personDetailsCsv, outputTarget, "age", "SPAN" );
    addRelationship( personDetailsCsv, outputTarget, "email", "USERNAME" );
    addRelationship( personDetailsCsv, outputTarget, "gender", "SEX" );

    Mockito.verify( mockCatalogLineageClient ).processLineage( inputSourceCaptor.capture(), outputSourcesCaptor.capture() );
    List<LineageDataResource> inputSources = inputSourceCaptor.getValue();
    Assert.assertNotNull( "input sources must not be null", inputSources );
    listContainsExpectedResource( personCsv, inputSources );
    listContainsExpectedResource( personDetailsCsv, inputSources );
    List<LineageDataResource> outputSources = outputSourcesCaptor.getValue();
    Assert.assertNotNull( "output sources must not be null", outputSources );
    listContainsExpectedResource( outputTarget, outputSources );
  }

  private void listContainsExpectedResource( LineageDataResource resource, List<LineageDataResource> sourceList ) {
    sourceList.forEach( this::cleanVertexIds );
    Assert.assertTrue( String.format( "did not find %s resource", resource.getName() ), sourceList.contains( resource ) );
    List<FieldLevelRelationship> personCsvRelationshipsComputed = sourceList.stream().filter( r -> r.getName().equals( resource.getName() ) ).findFirst().get()
      .getFieldLevelRelationships();
    Assert.assertTrue( String.format( "field relationships in %s resource incorrect", resource.getName() ),
      personCsvRelationshipsComputed.containsAll( resource.getFieldLevelRelationships() )
        && ( personCsvRelationshipsComputed.size() == resource.getFieldLevelRelationships().size() ) );
  }

  private void cleanVertexIds( LineageDataResource r ) {
    r.setVertexId( null );

    for ( FieldLevelRelationship relationship : r.getFieldLevelRelationships() ) {
      LineageDataResource input = relationship.getInputSourceResource();
      LineageDataResource output = relationship.getOutputTargetResource();
      if ( null != input ) {
        input.setVertexId( null );
        relationship.setInputSourceResource( input );
      }
      if ( null != output ) {
        output.setVertexId( null );
        relationship.setOutputTargetResource( output );
      }
    }
  }

  @Test
  public void testFileToDb() throws Exception {
    final String transNodeName = "file_to_db";
    initTest( transNodeName );

    GraphCatalogWriter graphCatalogWriter =
      new GraphCatalogWriter( "", "", "", "", "", "" );
    graphCatalogWriter.setCatalogLineageClientProvider( mockCatalogLineageClientProvider );

    graphCatalogWriter.outputGraphImpl( graph, null );

    Path personCsvPath = Paths.get("src", "it", "resources", "person.csv");
    LineageDataResource personCsv = new LineageDataResource( "person.csv" );
    personCsv.setPath( personCsvPath.toAbsolutePath().toString() );
    List<String> personFields = Arrays.asList( "first_name", "id", "last_name" );
    personCsv.setFields( personFields );
    LineageDataResource outputTarget = new LineageDataResource( "silly_test" );
    outputTarget.setDbName( "hibernate" );
    outputTarget.setDbSchema( "public" );
    outputTarget.setDbHost( "192.168.50.68" );
    outputTarget.setDbPort( "5432" );
    List<String> outputFields = Arrays.asList( "SSN", "NAME1", "NAME2" );
    outputTarget.setFields( outputFields );
    addRelationship( personCsv, outputTarget, "first_name", "NAME1" );
    addRelationship( personCsv, outputTarget, "last_name", "NAME2" );
    addRelationship( personCsv, outputTarget, "id", "SSN" );

    Mockito.verify( mockCatalogLineageClient ).processLineage( inputSourceCaptor.capture(), outputSourcesCaptor.capture() );
    List<LineageDataResource> inputSources = inputSourceCaptor.getValue();
    listContainsExpectedResource( personCsv, inputSources );
    List<LineageDataResource> outputSources = outputSourcesCaptor.getValue();
    Assert.assertNotNull( "output sources must not be null", outputSources );
    listContainsExpectedResource( outputTarget, outputSources );
  }

  private void addRelationship( LineageDataResource input, LineageDataResource output, String inputField, String outputField ) {
    FieldLevelRelationship r1 = new FieldLevelRelationship();
    r1.setInputSourceResource( input );
    r1.setOutputTargetResource( output );
    r1.setInputSourceResourceField( inputField );
    r1.setOutputTargetResourceField( outputField );
    input.addFieldLevelRelationship( r1 );
    output.addFieldLevelRelationship( r1 );
  }
}