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

package org.pentaho.metaverse.analyzer.kettle.extensionpoints.trans;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.Trans;
import org.pentaho.metaverse.analyzer.kettle.extensionpoints.job.JobLineageHolderMap;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.model.IExecutionProfile;
import org.pentaho.metaverse.api.model.LineageHolder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransLineageHolderMap
 */
@RunWith( MockitoJUnitRunner.class )
public class TransLineageHolderMapTest {

  TransLineageHolderMap jobLineageHolderMap;

  // Will be a spy
  LineageHolder mockHolder;

  @Mock
  IMetaverseBuilder mockBuilder;

  @Mock
  IMetaverseBuilder defaultBuilder;

  @Mock
  IExecutionProfile mockExecutionProfile;

  @Before
  public void setUp() throws Exception {
    jobLineageHolderMap = TransLineageHolderMap.getInstance();
    mockHolder = spy( new LineageHolder() );
    jobLineageHolderMap.setDefaultMetaverseBuilder( defaultBuilder );
  }

  @After
  public void tearDown() {
    jobLineageHolderMap.setDefaultMetaverseBuilder( null );
  }

  @Test
  public void testGetSetInstance() throws Exception {
    assertNotNull( jobLineageHolderMap );
    TransLineageHolderMap.setInstance( jobLineageHolderMap );
    assertEquals( jobLineageHolderMap, TransLineageHolderMap.getInstance() );
  }

  @Test
  public void testGetPutLineageHolder() throws Exception {

    Trans trans = mock( Trans.class );

    LineageHolder holder = jobLineageHolderMap.getLineageHolder( trans );
    assertNotNull( holder ); // We always get a (perhaps empty) holder
    assertFalse( holder == mockHolder );
    assertNull( holder.getExecutionProfile() );
    assertNull( holder.getMetaverseBuilder() );

    mockHolder.setMetaverseBuilder( mockBuilder );
    mockHolder.setExecutionProfile( mockExecutionProfile );
    jobLineageHolderMap.putLineageHolder( trans, mockHolder );

    holder = jobLineageHolderMap.getLineageHolder( trans );
    assertNotNull( holder ); // We always get a (perhaps empty) holder
    assertTrue( holder == mockHolder );
    assertTrue( holder.getExecutionProfile() == mockExecutionProfile );
    assertTrue( holder.getMetaverseBuilder() == mockBuilder );
  }

  @Test
  public void testGetMetaverseBuilder() throws Exception {

    assertNull( jobLineageHolderMap.getMetaverseBuilder( null ) );

    Trans trans = mock( Trans.class );
    when( trans.getParentJob() ).thenReturn( null );
    when( trans.getParentTrans() ).thenReturn( null );

    mockHolder.setMetaverseBuilder( mockBuilder );
    jobLineageHolderMap.putLineageHolder( trans, mockHolder );

    IMetaverseBuilder builder = jobLineageHolderMap.getMetaverseBuilder( trans );
    assertNotNull( builder );

    Trans parentTrans = mock( Trans.class );
    when( parentTrans.getParentJob() ).thenReturn( null );
    when( parentTrans.getParentTrans() ).thenReturn( null );

    when( trans.getParentTrans() ).thenReturn( parentTrans );

    LineageHolder mockParentHolder = spy( new LineageHolder() );

    IMetaverseBuilder mockParentBuilder = mock( IMetaverseBuilder.class );
    jobLineageHolderMap.putLineageHolder( parentTrans, mockParentHolder );
    mockParentHolder.setMetaverseBuilder( null );

    assertEquals( defaultBuilder, jobLineageHolderMap.getMetaverseBuilder( trans ) );

    mockParentHolder.setMetaverseBuilder( mockParentBuilder );
    builder = jobLineageHolderMap.getMetaverseBuilder( trans );
    assertNotNull( builder );
  }
}
