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
 */

package com.pentaho.metaverse.impl;

import com.pentaho.metaverse.api.IGraphWriter;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.model.IExecutionData;
import com.pentaho.metaverse.api.model.IExecutionProfile;
import com.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.impl.model.ExecutionData;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by mburgess on 4/2/15.
 */
public class LineageWriterTest {

  private LineageWriter writer;

  private LineageHolder holder;

  private static final Date now = new Date();

  @Before
  public void setUp() throws Exception {
    LineageWriter fslw = new LineageWriter();
    writer = spy( fslw );

    holder = new LineageHolder();
    IExecutionProfile profile = new ExecutionProfile();
    profile.setName( "test" );
    IExecutionData data = new ExecutionData();
    data.setStartTime( now );
    profile.setExecutionData( data );

    holder.setExecutionProfile( profile );
  }

  @Test
  public void testOutputExecutionProfile() throws Exception {
    writer.setProfileOutputStream( System.out );
    writer.outputExecutionProfile( holder );
    writer.setProfileOutputStream( null );
    writer.outputExecutionProfile( holder );
  }

  @Test
  public void testOutputLineageGraph() throws Exception {
    Graph g = new TinkerGraph();
    IMetaverseBuilder builder = new MetaverseBuilder( g );
    holder.setMetaverseBuilder( builder );
    writer.setGraphOutputStream( System.out );
    IGraphWriter graphWriter = mock( IGraphWriter.class );
    writer.setGraphWriter( graphWriter );
    writer.outputLineageGraph( holder );
  }

  @Test( expected = IOException.class )
  public void testOutputLineageGraphNoOutputStream() throws Exception {
    Graph g = new TinkerGraph();
    IMetaverseBuilder builder = new MetaverseBuilder( g );
    holder.setMetaverseBuilder( builder );

    writer.outputLineageGraph( holder );
  }

  @Test
  public void testGetSetGraphWriter() throws Exception {
    assertNull( writer.getGraphWriter() );
    IGraphWriter graphWriter = mock( IGraphWriter.class );
    writer.setGraphWriter( graphWriter );
    assertEquals( graphWriter, writer.getGraphWriter() );
  }

  @Test
  public void testGetSetProfileOutputStream() throws Exception {
    assertNotNull( writer.getProfileOutputStream() );
  }

  @Test
  public void testGetSetGraphOutputStream() {
    assertEquals( System.out, writer.getGraphOutputStream() );
    writer.setGraphOutputStream( null );
    assertEquals( System.out, writer.getGraphOutputStream() );
    OutputStream outStream = mock( OutputStream.class );
    writer.setGraphOutputStream( outStream );
    assertEquals( outStream, writer.getGraphOutputStream() );
  }
}
