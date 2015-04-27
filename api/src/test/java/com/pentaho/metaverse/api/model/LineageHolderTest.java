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

package com.pentaho.metaverse.api.model;

import com.pentaho.metaverse.api.IMetaverseBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class LineageHolderTest {

  LineageHolder lineageHolder;

  @Mock
  IExecutionProfile executionProfile;
  @Mock
  IMetaverseBuilder builder;
  @Mock
  Future lineageTask;


  @Test
  public void testEmptyConstructor() throws Exception {
    lineageHolder = new LineageHolder();
    assertNull( lineageHolder.getExecutionProfile() );
    assertNull( lineageHolder.getMetaverseBuilder() );
    assertNull( lineageHolder.getLineageTask() );
    assertNull( lineageHolder.getId() );
  }

  @Test
  public void testSetters() throws Exception {
    lineageHolder = new LineageHolder();

    lineageHolder.setExecutionProfile( executionProfile );
    assertEquals( executionProfile, lineageHolder.getExecutionProfile() );

    lineageHolder.setLineageTask( lineageTask );
    assertEquals( lineageTask, lineageHolder.getLineageTask() );

    lineageHolder.setMetaverseBuilder( builder );
    assertEquals( builder, lineageHolder.getMetaverseBuilder() );

    lineageHolder.setId( "ID" );
    assertEquals( "ID", lineageHolder.getId() );
  }

  @Test
  public void testConstructor() throws Exception {
    lineageHolder = new LineageHolder( executionProfile, builder );
    assertEquals( executionProfile, lineageHolder.getExecutionProfile() );
    assertEquals( builder, lineageHolder.getMetaverseBuilder() );
  }

  @Test
  public void testGetIdFromExecutionProfile() throws Exception {
    when( executionProfile.getPath() ).thenReturn( "profile/path" );
    lineageHolder = new LineageHolder( executionProfile, builder );
    assertEquals( "profile/path", lineageHolder.getId() );
  }
}