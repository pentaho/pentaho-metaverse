/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
 *
 */

package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.IGraphWriter;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.graph.GraphCsvWriter;
import com.pentaho.metaverse.graph.GraphMLWriter;
import com.pentaho.metaverse.graph.GraphSONWriter;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

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
