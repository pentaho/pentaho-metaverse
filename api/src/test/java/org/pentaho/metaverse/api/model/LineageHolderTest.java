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

package org.pentaho.metaverse.api.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.metaverse.api.IMetaverseBuilder;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
    lineageHolder = new LineageHolder();
    when( executionProfile.getPath() ).thenReturn( "profile/path" );
    lineageHolder = new LineageHolder( executionProfile, builder );
    assertEquals( "profile/path", lineageHolder.getId() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testAddSubTransOrJobInvalid() {
    lineageHolder = new LineageHolder();
    lineageHolder.addSubTransOrJob( "dummy" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testAddSubTransOrJobNull() {
    lineageHolder = new LineageHolder();
    lineageHolder.addSubTransOrJob( null );
  }

  @Test
  public void testSubTransOrJob() {
    lineageHolder = new LineageHolder();
    assertNotNull( lineageHolder.getSubTransAndJobs().size() );
    assertEquals( 0, lineageHolder.getSubTransAndJobs().size() );

    final Trans trans = Mockito.mock( Trans.class );
    final Job job = Mockito.mock( Job.class );
    lineageHolder.addSubTransOrJob( trans );
    lineageHolder.addSubTransOrJob( job );

    assertEquals( 2, lineageHolder.getSubTransAndJobs().size() );
    assertEquals( trans, lineageHolder.getSubTransAndJobs().get( 0 ) );
    assertEquals( job, lineageHolder.getSubTransAndJobs().get( 1 ) );
  }
}
