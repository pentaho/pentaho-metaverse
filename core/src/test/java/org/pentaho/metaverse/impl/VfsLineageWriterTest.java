/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.util.Date;
import java.util.Random;

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

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * Created by wseyler on 4/1/15.
 */
public class VfsLineageWriterTest {

  private static String BAD_OUTPUT_FOLDER = "/target/outputfiles/doesnt_exist";
  private static String GOOD_OUTPUT_FOLDER = "/target/outputfiles";

  private static String BAD_OUTPUT_FOLDER_DEFAULT = "/target/outputfiles/doesnt_exist";
  private static String GOOD_OUTPUT_FOLDER_DEFAULT = "/target/outputfiles";
  private static Random random = new Random();
  private VfsLineageWriter writer;

  private LineageHolder holder;

  private static final Date now = new Date();

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
        FilenameUtils.separatorsToSystem( "file://" + basePath + BAD_OUTPUT_FOLDER_DEFAULT + random.nextInt() );
    GOOD_OUTPUT_FOLDER =
        FilenameUtils.separatorsToSystem( "file://" + basePath + GOOD_OUTPUT_FOLDER_DEFAULT + random.nextInt() );

    writer.setOutputFolder( GOOD_OUTPUT_FOLDER );
  }

  /**
   * @throws FileSystemException
   * @throws java.lang.Exception
   */
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
  public void testGetSetGraphWriter() throws Exception {
    IGraphWriter graphWriter = writer.getGraphWriter();
    assertNotNull( graphWriter );
    writer.setGraphWriter( null );
    assertNull( writer.getGraphWriter() );
  }

  @Test
  public void testGetSetOutputFolder() throws Exception {
    assertEquals( GOOD_OUTPUT_FOLDER, writer.getOutputFolder() );
    writer.setOutputFolder( "./path/to/folder" );
    assertTrue( writer.getOutputFolder().endsWith( "/path/to/folder" ) );
  }

  @Test
  public void testOutputExecutionProfile() throws Exception {
    writer.outputExecutionProfile( holder );
  }

  @Test
  public void testGetProfileOutputStream() throws Exception {
    assertNotNull( writer.getProfileOutputStream( holder ) );
  }

  @Test
  public void testCreateOutputStream() throws FileSystemException {
    assertNull( writer.createOutputStream( null, null ) );
    assertNotNull( writer.createOutputStream( holder, "ktr" ) );
    writer.setOutputFolder( BAD_OUTPUT_FOLDER );
    assertNotNull( writer.createOutputStream( holder, null ) );
    assertNotNull( writer.createOutputStream( holder, "ktr" ) );
  }

  @Test
  public void testGetDateFolder() throws KettleFileException, FileSystemException {
    assertNotNull( writer.getDateFolder( null ) );
    FileObject folder = writer.getDateFolder( holder );
    assertNotNull( folder );
    assertTrue( folder.getName().getPath().endsWith( VfsLineageWriter.dateFolderFormat.format( now ) ) );
    writer.setOutputFolder( "file://root" );
    folder = writer.getDateFolder( holder );
    assertTrue( folder.getName().getPath().endsWith( "root" + "/" + VfsLineageWriter.dateFolderFormat.format( now ) ) );
  }

  @Test
  public void testSetGraphOutputStream() {
    assertNull( writer.getGraphOutputStream( null ) );
    IGraphWriter graphWriter = new GraphMLWriter();
    writer.setGraphWriter( graphWriter );
    assertNotNull( writer.getGraphOutputStream( holder ) );
    graphWriter = new GraphSONWriter();
    writer.setGraphWriter( graphWriter );
    assertNotNull( writer.getGraphOutputStream( holder ) );
    graphWriter = new GraphCsvWriter();
    writer.setGraphWriter( graphWriter );
    assertNotNull( writer.getGraphOutputStream( holder ) );
  }
}
