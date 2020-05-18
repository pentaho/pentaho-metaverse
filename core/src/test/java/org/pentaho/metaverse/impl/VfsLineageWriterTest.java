/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.impl;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.graph.GraphCsvWriter;
import org.pentaho.metaverse.graph.GraphMLWriter;
import org.pentaho.metaverse.graph.GraphSONWriter;
import org.pentaho.metaverse.impl.model.ExecutionData;
import org.pentaho.metaverse.impl.model.ExecutionProfile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

/**
 * Created by wseyler on 4/1/15.
 */
public class VfsLineageWriterTest {

  private static String BAD_OUTPUT_FOLDER = "/target/outputfiles/doesnt_exist";
  private static String GOOD_OUTPUT_FOLDER = "/target/outputfiles";

  private static Random random = new Random();
  private VfsLineageWriter writer;

  private LineageHolder holder;

  private static final Date now = new Date();

  private static final String OS_NAME = System.getProperty( "os.name", "unknown" );

  @Before
  public void setUp() throws Exception {
    String basePath = new File( "." ).getCanonicalPath();

    writer = new VfsLineageWriter();
    writer = spy( writer );

    holder = new LineageHolder();
    IExecutionProfile profile = new ExecutionProfile();
    profile.setName( "test" );
    IExecutionData data = new ExecutionData();
    data.setStartTime( now );
    profile.setExecutionData( data );

    holder.setExecutionProfile( profile );

    BAD_OUTPUT_FOLDER =
      FilenameUtils.separatorsToSystem(
        "file://" + basePath + "/target/outputfiles/doesnt_exist" + random.nextInt() );
    GOOD_OUTPUT_FOLDER =
      FilenameUtils.separatorsToSystem(
        "file://" + basePath + "/target/outputfiles" + random.nextInt() );

    writer.setOutputFolder( GOOD_OUTPUT_FOLDER );
  }

  @After
  public void tearDown() throws FileSystemException {
    FileSystemManager fsManager = VFS.getManager();
    fsManager.resolveFile( BAD_OUTPUT_FOLDER ).deleteAll();
    fsManager.resolveFile( GOOD_OUTPUT_FOLDER ).deleteAll();
  }

  @Test
  public void testOutputLineageGraph() throws Exception {
    Graph g = new TinkerGraph();
    IMetaverseBuilder builder = new MetaverseBuilder( g );
    holder.setMetaverseBuilder( builder );

    writer.outputLineageGraph( holder );
  }

  @Test
  public void testGetSetGraphWriter() {
    IGraphWriter graphWriter = writer.getGraphWriter();
    assertNotNull( graphWriter );
    writer.setGraphWriter( null );
    assertNull( writer.getGraphWriter() );
  }

  @Test
  public void testGetSetOutputFolder() {
    assertEquals( GOOD_OUTPUT_FOLDER, writer.getOutputFolder() );
    writer.setOutputFolder( "./path/to/folder" );
    assertTrue( writer.getOutputFolder().replace( '\\', '/').endsWith( "/path/to/folder" ) );
  }

  @Test
  public void testOutputExecutionProfile() throws Exception {
    writer.outputExecutionProfile( holder );
  }

  @Test
  public void testGetProfileOutputStream() throws Exception {
    try ( OutputStream os = writer.getProfileOutputStream( holder ) ) {
      assertNotNull( os );
    }
  }

  @Test
  public void testCreateOutputStream() throws IOException {
    try ( OutputStream nullOS = writer.createOutputStream( null, null );
          OutputStream ktrOS = writer.createOutputStream( holder, "ktr" ) ) {
      assertNull( nullOS );
      assertNotNull( ktrOS );
    }

    writer.setOutputFolder( BAD_OUTPUT_FOLDER );

    try ( OutputStream nullOS = writer.createOutputStream( holder, null );
          OutputStream ktrOS = writer.createOutputStream( holder, "ktr" ) ) {
      assertNotNull( nullOS );
      assertNotNull( ktrOS );
    }
  }

  @Test
  public void testGetDateFolder() throws KettleFileException, FileSystemException {
    assertNotNull( writer.getDateFolder( null ) );
    FileObject folder = writer.getDateFolder( holder );
    assertNotNull( folder );
    assertTrue( folder.getName().getPath().endsWith( VfsLineageWriter.dateFolderFormat.format( now ) ) );
    if ( isWindows() ) {
      writer.setOutputFolder( "file:///c:/root" );
    } else {
      writer.setOutputFolder( "file://root" );
    }
    folder = writer.getDateFolder( holder );
    assertTrue( folder.getName().getPath().endsWith( "root" + "/" + VfsLineageWriter.dateFolderFormat.format( now ) ) );
  }

  @Test
  public void testSetGraphOutputStream() throws IOException {
    try ( OutputStream graphOutputStream = writer.getGraphOutputStream( null ) ) {
      assertNull( graphOutputStream );
    }

    IGraphWriter graphWriter = new GraphMLWriter();
    writer.setGraphWriter( graphWriter );
    try ( OutputStream graphOutputStream = writer.getGraphOutputStream( holder ) ) {
      assertNotNull( graphOutputStream );
    }

    graphWriter = new GraphSONWriter();
    writer.setGraphWriter( graphWriter );
    try ( OutputStream graphOutputStream = writer.getGraphOutputStream( holder ) ) {
      assertNotNull( graphOutputStream );
    }

    graphWriter = new GraphCsvWriter();
    writer.setGraphWriter( graphWriter );
    try ( OutputStream graphOutputStream = writer.getGraphOutputStream( holder ) ) {
      assertNotNull( graphOutputStream );
    }
  }

  @Test
  public void testGetOutputDirectoryAsFile() {
    holder.setId( "foobarbaz" );
    FileObject fo = writer.getOutputDirectoryAsFile( holder );
    assertThat( fo.getName().getPath(), endsWith( "foobarbaz" ) );

    //Skipping next test for windows because colon breaks it
    if ( !isWindows() ) {
      // Use the style of name created by DET for transient dataservice ktrs
      holder.setId( "transient:L1VzZXJzL21hdGNhbXBiZWxsL0Rvd25sb2Fkcy9Db25zdW1lckNvbXBsYWludHMua3Ry"
        + ":bG9jYWw6UHl0aG9uIEV4ZWN1dG9yIDI= - SQL - select "
        + "\"transient:L1VzZXJzL21hdGNhbXBiZWxsL0Rvd25sb2Fkcy9Db25zdW1lckNvbXBsYWludHMua3Ry:bG9jYW" );

      fo = writer.getOutputDirectoryAsFile( holder );
      assertThat( fo.getName().getPath(), endsWith(
        "transient-L1VzZXJzL21hdGNhbXBiZWxsL0Rvd25sb2Fkcy9Db25zdW1lckNvbXBsYWludHMua3Ry-bG9jYWw6UHl0aG9uIEV4ZWN1dG9yIDI= "
          + "- SQL - select \"transient-L1VzZXJzL21h" ) );

    }

    holder.setId( "invalidChar %  invalidChar" );
    fo = writer.getOutputDirectoryAsFile( holder );
    assertThat( fo.getName().getPath(), endsWith( "unknown_artifact" ) );
  }

  protected static boolean isWindows() {
    return OS_NAME.startsWith( "Windows" );
  }

}
