/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
public class ExternalResourceCacheTest {

  @Mock
  private Trans trans;
  @Mock
  private Trans trans2;

  @Mock
  private TransMeta transMeta1;
  @Mock
  private TransMeta transMeta2;

  @Mock
  private TransMeta transMeta;

  @Mock
  private BaseFileInputMeta meta;

  private StepMeta spyMeta;

  @Before
  public void setup() {
    ExternalResourceCache.getInstance().transMap = new ConcurrentHashMap();
    ExternalResourceCache.getInstance().resourceMap = new ConcurrentHashMap();
  }

  private void initMetas() {
    spyMeta = spy( new StepMeta( "test", meta ) );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( transMeta.getFilename() ).thenReturn( "my_file" );
    //when( transMeta.getName() ).thenReturn( "my_trans" );
    when( trans.getName() ).thenReturn( "my_trans" );
    when( trans.getFilename() ).thenReturn( "my_file" );

    when( trans.getTransMeta() ).thenReturn( transMeta );
    when( transMeta.getSteps() ).thenReturn( Arrays.asList( new StepMeta[] { spyMeta } ) );

    when( trans2.getTransMeta() ).thenReturn( transMeta2 );
    when( trans2.getName() ).thenReturn( "my_trans2" );
    when( trans2.getFilename() ).thenReturn( "my_file2" );
    when( transMeta2.getSteps() ).thenReturn( Arrays.asList( new StepMeta[] { spyMeta } ) );
  }

  @Test
  public void test_getInstance() {
    // verify that we have a singleton
    assertEquals( ExternalResourceCache.getInstance(), ExternalResourceCache.getInstance() );
  }

  @Test
  public void test_getUniqueId() {
    assertNull( ExternalResourceCache.getInstance().getUniqueId( null ) );
    // parent TransMeta is not yet mocked, we should get null back
    assertNull( ExternalResourceCache.getInstance().getUniqueId( meta.getParentStepMeta() ) );

    initMetas();

    // null repo
    assertEquals( System.getProperty( "user.dir" ) + File.separator + "my_file::test",
      ExternalResourceCache.getInstance().getUniqueId( meta.getParentStepMeta() ) );

    // mock the repo
    when( transMeta.getRepository() ).thenReturn( Mockito.mock( Repository.class ) );
    assertEquals( "null.null::test",
      ExternalResourceCache.getInstance().getUniqueId( meta.getParentStepMeta() ) );

    when( transMeta.getPathAndName() ).thenReturn( "path_and_name" );
    when( transMeta.getDefaultExtension() ).thenReturn( "ktr" );
    assertEquals( "path_and_name.ktr::test",
      ExternalResourceCache.getInstance().getUniqueId( meta.getParentStepMeta() ) );
  }

  @Test
  public void test_caching() {
    initMetas();

    assertNull( ExternalResourceCache.getInstance().get( null, null ) );
    assertEquals( 0, ExternalResourceCache.getInstance().transMap.size() );
    assertEquals( 0, ExternalResourceCache.getInstance().resourceMap.size() );

    assertNull( ExternalResourceCache.getInstance().get( trans, null ) );
    assertEquals( 0, ExternalResourceCache.getInstance().transMap.size() );
    assertEquals( 0, ExternalResourceCache.getInstance().resourceMap.size() );

    assertNull( ExternalResourceCache.getInstance().get( trans, meta ) );
    assertEquals( 1, ExternalResourceCache.getInstance().transMap.size() );
    assertEquals( 0, ExternalResourceCache.getInstance().resourceMap.size() );

    // the cache contains sets, make sure another copy of the same trans is not added
    assertNull( ExternalResourceCache.getInstance().get( trans, meta ) );
    assertEquals( 1, ExternalResourceCache.getInstance().transMap.size() );
    List<ExternalResourceCache.TransValues> cachedTransformations = IteratorUtils.toList(
      ExternalResourceCache.getInstance().transMap.values().iterator() );
    assertEquals( 1, cachedTransformations.size() );
    assertEquals( 1, cachedTransformations.get( 0 ).size() );
    assertTrue( cachedTransformations.get( 0 ).contains( trans ) );
    assertEquals( 0, ExternalResourceCache.getInstance().resourceMap.size() );

    assertNull( ExternalResourceCache.getInstance().get( trans2, meta ) );
    assertEquals( 1, ExternalResourceCache.getInstance().transMap.size() );
    cachedTransformations = IteratorUtils.toList(
      ExternalResourceCache.getInstance().transMap.values().iterator() );
    assertEquals( 1, cachedTransformations.size() );
    assertEquals( 2, cachedTransformations.get( 0 ).size() );
    assertTrue( cachedTransformations.get( 0 ).contains( trans ) );
    assertTrue( cachedTransformations.get( 0 ).contains( trans2 ) );
    assertEquals( 0, ExternalResourceCache.getInstance().resourceMap.size() );

    final ExternalResourceCache.ExternalResourceValues resources = ExternalResourceCache.getInstance()
      .newExternalResourceValues();
    resources.add( Mockito.mock( IExternalResourceInfo.class ) );

    // cache some resources
    ExternalResourceCache.getInstance().cache( trans, meta, resources );
    assertEquals( 1, ExternalResourceCache.getInstance().transMap.size() );
    cachedTransformations = IteratorUtils.toList(
      ExternalResourceCache.getInstance().transMap.values().iterator() );
    assertEquals( 1, cachedTransformations.size() );
    assertEquals( 2, cachedTransformations.get( 0 ).size() );
    assertTrue( cachedTransformations.get( 0 ).contains( trans ) );
    assertTrue( cachedTransformations.get( 0 ).contains( trans2 ) );
    assertEquals( 1, ExternalResourceCache.getInstance().resourceMap.size() );

    // remote trans from the cache
    ExternalResourceCache.getInstance().removeCachedResources( trans );
    // trans2 should still exist in the map
    assertEquals( 1, ExternalResourceCache.getInstance().transMap.size() );
    cachedTransformations = IteratorUtils.toList(
      ExternalResourceCache.getInstance().transMap.values().iterator() );
    assertEquals( 1, cachedTransformations.size() );
    assertEquals( 1, cachedTransformations.get( 0 ).size() );
    assertTrue( cachedTransformations.get( 0 ).contains( trans2 ) );
    // resources should not be removed as they are still being used by trans2
    assertEquals( 1, ExternalResourceCache.getInstance().resourceMap.size() );

    // remove trans2 from the cache
    ExternalResourceCache.getInstance().removeCachedResources( trans2 );
    // transMap and resourceMap should now both be empty
    assertEquals( 0, ExternalResourceCache.getInstance().transMap.size() );
    assertEquals( 0, ExternalResourceCache.getInstance().resourceMap.size() );
  }

  @Test
  public void test_ExternalResourceTransValues() {
    final Trans trans1 = new Trans();
    trans1.setTransMeta( transMeta1 );
    when( transMeta1.getName() ).thenReturn( "trans1file" );
    when( transMeta1.getFilename() ).thenReturn( "trans1file.ktr" );

    final Trans trans2 = new Trans();
    trans2.setTransMeta( transMeta2 );
    when( transMeta2.getName() ).thenReturn( "trans2file" );
    when( transMeta2.getFilename() ).thenReturn( "trans2file.ktr" );

    final Trans trans3 = new Trans();
    trans3.setTransMeta( transMeta1 );

    final ExternalResourceCache.Resources transValues = ExternalResourceCache.getInstance().new TransValues();
    assertEquals( 0, transValues.size() );
    transValues.add( trans1 );
    assertEquals( 1, transValues.size() );
    assertTrue( transValues.contains( trans1 ) );
    transValues.add( trans2 );
    assertEquals( 2, transValues.size() );
    assertTrue( transValues.contains( trans2 ) );
    transValues.add( trans3 );
    assertEquals( 2, transValues.size() );
    assertFalse( transValues.contains( trans3 ) );

    transValues.remove( trans1 );
    assertEquals( 1, transValues.size() );
    assertFalse( transValues.contains( trans1 ) );
    assertTrue( transValues.contains( trans2 ) );
    transValues.remove( trans2 );
    assertEquals( 0, transValues.size() );

    // make sure we're getting a copy, not the original
    assertFalse( transValues.getInternal() == transValues.getInternal() );
  }

  @Test
  public void test_ExternalResourceExternalResourceValues() {
    final IExternalResourceInfo resource1 = Mockito.mock( IExternalResourceInfo.class );
    final IExternalResourceInfo resource2 = Mockito.mock( IExternalResourceInfo.class );
    final ExternalResourceCache.Resources resourceValues = ExternalResourceCache.getInstance().new
      ExternalResourceValues();
    assertEquals( 0, resourceValues.size() );
    resourceValues.add( resource1 );
    assertEquals( 1, resourceValues.size() );
    assertTrue( resourceValues.contains( resource1 ) );
    resourceValues.add( resource2 );
    assertEquals( 2, resourceValues.size() );
    assertTrue( resourceValues.contains( resource2 ) );

    resourceValues.remove( resource1 );
    assertEquals( 1, resourceValues.size() );
    assertFalse( resourceValues.contains( resource1 ) );
    assertTrue( resourceValues.contains( resource2 ) );
    resourceValues.remove( resource2 );
    assertEquals( 0, resourceValues.size() );

    // make sure we're getting a copy, not the original
    assertFalse( resourceValues.getInternal() == resourceValues.getInternal() );
  }
}
