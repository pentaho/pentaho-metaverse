/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.dictionary.DictionaryConst;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BaseRuntimeExtensionPointTest {

  BaseRuntimeExtensionPoint extensionPoint;

  final AtomicBoolean called = new AtomicBoolean( false );

  @Before
  public void setUp() throws Exception {
    extensionPoint = new BaseRuntimeExtensionPoint() {

      @Override
      public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
        // trivial isRuntimeEnabled logic
        if ( isRuntimeEnabled() ) {
          called.set( true );
        } else {
          called.set( false );
        }
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
    IExecutionEngine engineInfo = BaseRuntimeExtensionPoint.getExecutionEngineInfo();
    assertNotNull( engineInfo );
    assertEquals( DictionaryConst.EXECUTION_ENGINE_NAME, engineInfo.getName() );
    assertEquals( DictionaryConst.EXECUTION_ENGINE_DESCRIPTION, engineInfo.getDescription() );
  }

  @Test
  public void testGetSetRuntimeEnabled() throws Exception {
    // Ensure runtime is disabled by default
    assertFalse( extensionPoint.isRuntimeEnabled() );
    extensionPoint.callExtensionPoint( null, null );
    assertFalse( called.get() );

    extensionPoint.setRuntimeEnabled( true );
    assertTrue( extensionPoint.isRuntimeEnabled() );
    extensionPoint.callExtensionPoint( null, null );
    assertTrue( called.get() );

    // Test string setting
    extensionPoint.setRuntimeEnabled( "on" );
    assertTrue( extensionPoint.isRuntimeEnabled() );
    extensionPoint.setRuntimeEnabled( "ON" );
    assertTrue( extensionPoint.isRuntimeEnabled() );
    extensionPoint.setRuntimeEnabled( "yes" );
    assertFalse( extensionPoint.isRuntimeEnabled() );
    extensionPoint.setRuntimeEnabled( "true" );
    assertFalse( extensionPoint.isRuntimeEnabled() );
    extensionPoint.setRuntimeEnabled( "off" );
    assertFalse( extensionPoint.isRuntimeEnabled() );
    extensionPoint.callExtensionPoint( null, null );
    assertFalse( called.get() );
  }

}
