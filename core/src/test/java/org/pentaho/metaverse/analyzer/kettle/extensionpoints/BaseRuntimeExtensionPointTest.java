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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints;

import com.tinkerpop.blueprints.Graph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionEngine;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import org.pentaho.metaverse.impl.LineageWriter;
import org.pentaho.metaverse.impl.model.ExecutionProfile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BaseRuntimeExtensionPointTest {

  BaseRuntimeExtensionPoint extensionPoint;

  @Before
  public void setUp() throws Exception {
    extensionPoint = new BaseRuntimeExtensionPoint() {
      @Override
      public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
        // Noop
      }
    };
  }

  @Test
  public void testWriteLineageHolder() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    assertEquals( 0, baos.size() );
    PrintStream stringStream = new PrintStream( baos );

    IGraphWriter graphWriter = mock( IGraphWriter.class );

    IExecutionProfile executionProfile = new ExecutionProfile();
    executionProfile.setName( "testName" );
    IMetaverseBuilder builder = mock( IMetaverseBuilder.class );
    LineageHolder holder = new LineageHolder( executionProfile, builder );
    LineageWriter lineageWriter = new LineageWriter();
    lineageWriter.setProfileOutputStream( stringStream );
    lineageWriter.setGraphOutputStream( System.out );
    lineageWriter.setOutputStrategy( "all" );

    lineageWriter.setGraphWriter( graphWriter );

    extensionPoint.setLineageWriter( lineageWriter );
    extensionPoint.writeLineageInfo( holder );
    assertNotEquals( 0, baos.size() );
    assertTrue( baos.toString().contains( "testName" ) );

    verify( graphWriter, times( 1 ) ).outputGraph( any( Graph.class ), any( OutputStream.class ) );
  }

  @Test
  public void testGetExecutionEngineInfo() {
    IExecutionEngine engineInfo = extensionPoint.getExecutionEngineInfo();
    assertNotNull( engineInfo );
    assertEquals( BaseRuntimeExtensionPoint.EXECUTION_ENGINE_NAME, engineInfo.getName() );
    assertEquals( BaseRuntimeExtensionPoint.EXECUTION_ENGINE_DESCRIPTION, engineInfo.getDescription() );
  }
}
