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


package org.pentaho.metaverse.impl;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
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
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

/**
 * Created by mburgess on 4/1/15.
 */
public class FileSystemLineageWriterTest {

  private static final String BAD_OUTPUT_FOLDER = "target/outputfiles/doesnt_exist";

  private FileSystemLineageWriter writer;

  private LineageHolder holder;

  private static final Date now = new Date();

  @Before
  public void setUp() throws Exception {
    FileSystemLineageWriter fslw = new FileSystemLineageWriter();
    writer = spy( fslw );

    holder = new LineageHolder();
    IExecutionProfile profile = new ExecutionProfile();
    profile.setName( "test" );
    IExecutionData data = new ExecutionData();
    data.setStartTime( now );
    profile.setExecutionData( data );

    holder.setExecutionProfile( profile );

    writer.setOutputFolder( "target/outputfiles" );
  }

  @Test
  public void testOutputExecutionProfile() throws Exception {
    writer.outputExecutionProfile( holder );
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
    assertEquals( "target/outputfiles", writer.getOutputFolder() );
    writer.setOutputFolder( "/path/to/folder" );
    assertEquals( "/path/to/folder", writer.getOutputFolder() );
  }

  @Test
  public void testGetSetProfileOutputStream() throws Exception {
    assertNotNull( writer.getProfileOutputStream( holder ) );
  }

  @Test
  public void testCreateOutputStream() {
    assertNull( writer.createOutputStream( null, null ) );
    assertNotNull( writer.createOutputStream( holder, "ktr" ) );
    new File(BAD_OUTPUT_FOLDER).delete();
    writer.setOutputFolder( BAD_OUTPUT_FOLDER );
    assertNotNull( writer.createOutputStream( holder, null ) );
    assertNotNull( writer.createOutputStream( holder, "ktr" ) );
  }

  @Test
  public void testGetDateFolder() {
    assertNotNull( writer.getDateFolder( null, null ) );
    File folder = writer.getDateFolder( null, holder );
    assertNotNull( folder );
    assertTrue( folder.getPath().startsWith( writer.dateFolderFormat.format( now ) ) );
    folder = writer.getDateFolder( "root", holder );
    assertTrue( folder.getPath().startsWith( "root" + File.separator + writer.dateFolderFormat.format( now ) ) );
  }

  @Test
  public void testGetSetGraphOutputStream() {
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

  @Test
  public void testIsWindows() throws Exception {
    String os = System.getProperty( "os.name" ).toLowerCase();
    boolean expected = os.contains( "win" );
    assertEquals( expected, writer.isWindows() );
  }

  @Test
  public void testReplaceColonInPath() throws Exception {
    String path = "C:\\Users\\joe\\folder\\to\\use\\file.ktr";
    String expected = "C\\Users\\joe\\folder\\to\\use\\file.ktr";
    String replaced = writer.replaceColonInPath( path );
    assertEquals( expected, replaced );
  }
}
