/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.impl;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Created by rfellows on 7/6/15.
 */
public class VfsLineageCollectorTest {

  VfsLineageCollector collector;

  @Before
  public void setUp() throws Exception {
    collector = new VfsLineageCollector();
    String basePath = new File( "." ).getCanonicalPath();
    collector.setOutputFolder( FilenameUtils.separatorsToSystem( "file://" + basePath
        + "/src/test/resources/pentaho-lineage-output" ) );
  }

  @Test
  public void testListArtifacts() throws Exception {
    List<String> artifacts = collector.listArtifacts();
    assertNotNull( artifacts );
    assertEquals( 6, artifacts.size() );
  }

  @Test
  public void testListArtifacts_startingDate() throws Exception {
    List<String> artifacts = collector.listArtifacts( "20150706" );
    assertNotNull( artifacts );
    assertEquals( 6, artifacts.size() );
  }

  @Test
  public void testListArtifacts_startingDate2() throws Exception {
    // Find any artifacts from July, 7 2015 - There should only be 2
    List<String> artifacts = collector.listArtifacts( "20150707" );
    assertNotNull( artifacts );
    assertEquals( 2, artifacts.size() );
  }

  @Test
  public void testListArtifacts_startingDateNoResults() throws Exception {
    // Find any artifacts from July, 7 2016 - There should be 0 results
    List<String> artifacts = collector.listArtifacts( "20160707" );
    assertNotNull( artifacts );
    assertEquals( 0, artifacts.size() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testListArtifacts_InvalidStartingDate() throws Exception {
    List<String> artifacts = collector.listArtifacts( "20159999" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testListArtifacts_InvalidStartingDate_NoArtifactsToList() throws Exception {
    // Even if there are no artifacts available, we should still report throw the error
    collector.setOutputFolder( "file:///target/test/resources/invalidFolder" );
    List<String> artifacts = collector.listArtifacts( "20159999" );
  }

  @Test
  public void testCompressArtifacts() throws Exception {
    List<String> artifacts = collector.listArtifacts();
    File output = new File( "target/outputfiles/compressedFiles.zip" );
    output.mkdirs();
    if ( output.exists() ) {
      output.delete();
    }
    FileOutputStream fos = spy( new FileOutputStream( output ) );
    collector.compressArtifacts( artifacts, fos );
    byte[] emptyByteArray = new byte[0];

    // should be called at least once per file
    verify( fos, atLeast( 6 ) ).write( any( emptyByteArray.getClass() ), anyInt(), anyInt() );

  }

  @Test
  public void testGetArtifactsForFile_mergeJoin() throws Exception {
    // we have artifacts for merge_join on 2 days (execution profile + graph for each day)
    List<String> artifacts = collector.listArtifactsForFile( "validation/merge_join.ktr" );
    assertEquals( 4, artifacts.size() );
  }

  @Test
  public void testGetArtifactsForFile_stringsCut() throws Exception {
    // we have artifacts for strings_cut on 1 day (execution profile + graph)
    List<String> artifacts = collector.listArtifactsForFile( "validation/strings_cut.ktr" );
    assertEquals( 2, artifacts.size() );
  }

  @Test
  public void testGetArtifactsForFile_noArtifacts() throws Exception {
    List<String> artifacts = collector.listArtifactsForFile( "repo/validation/XYZ.ktr" );
    assertEquals( 0, artifacts.size() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetArtifactsForFile_noArtifacts_InvalidDate() throws Exception {
    List<String> artifacts = collector.listArtifactsForFile( "repo/validation/XYZ.ktr", "20159999" );
  }
}
