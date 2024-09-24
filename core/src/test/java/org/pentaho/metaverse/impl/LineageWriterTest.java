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
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionData;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.impl.model.ExecutionData;
import org.pentaho.metaverse.impl.model.ExecutionProfile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
    writer.setProfileOutputStream( mock( OutputStream.class ) );
    writer.outputExecutionProfile( holder );

    // We need the static mock for the null case because getProfileOutputStream() has the
    // side-effect of changing profileOutputStream to System.out if it is set to null.
    // Later on, outputExecutionProfile() calls IOUtils.closeQuietly on the output stream,
    // and closing System.out causes issues with other tests in some scenarios
    try( MockedStatic<IOUtils> mocked = mockStatic( IOUtils.class ) ) {
      writer.setProfileOutputStream( null );
      writer.outputExecutionProfile( holder );
    }
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
