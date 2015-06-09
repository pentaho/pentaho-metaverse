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

package com.pentaho.metaverse.analyzer.kettle.extensionpoints;

import org.pentaho.metaverse.api.IGraphWriter;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionEngine;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;
import com.pentaho.metaverse.impl.LineageWriter;
import com.pentaho.metaverse.impl.model.ExecutionProfile;
import com.tinkerpop.blueprints.Graph;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
