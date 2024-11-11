/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.api.analyzer.kettle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.metaverse.api.IMetaverseConfig;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
  private BaseFileInputMeta meta1;

  @Mock
  private BaseFileInputMeta meta2;

  private StepMeta spyMeta1;

  private StepMeta spyMeta2;

  private ExternalResourceCache testInstance;

  private void initMetas() {
    spyMeta1 = spy( new StepMeta( "test", meta1) );
    when( spyMeta1.getParentTransMeta() ).thenReturn( transMeta );
    when( meta1.getParentStepMeta() ).thenReturn(spyMeta1);
    when( transMeta.getFilename() ).thenReturn( "my_file" );

    spyMeta2 = spy( new StepMeta( "test2", meta2) );
    when( spyMeta2.getParentTransMeta() ).thenReturn( transMeta2 );
    when( meta2.getParentStepMeta() ).thenReturn(spyMeta2);
    when( transMeta2.getFilename() ).thenReturn( "my_file2" );

    lenient().when( trans.getName() ).thenReturn( "my_trans" );
    lenient().when( trans.getFilename() ).thenReturn( "my_file" );

    when( trans.getTransMeta() ).thenReturn( transMeta );
    when( transMeta.getSteps() ).thenReturn( Arrays.asList( new StepMeta[] {spyMeta1} ) );

    when( trans2.getTransMeta() ).thenReturn( transMeta2 );
    lenient().when( trans2.getName() ).thenReturn( "my_trans2" );
    lenient().when( trans2.getFilename() ).thenReturn( "my_file2" );
    when( transMeta2.getSteps() ).thenReturn( Arrays.asList( new StepMeta[] {spyMeta2} ) );
  }

  @Before
  public void setup() {
    testInstance = new ExternalResourceCache(null );
  }

  @Test
  public void test_getInstance() {
    assertNotNull( ExternalResourceCache.getInstance() );
    // verify that we have a singleton
    assertEquals( ExternalResourceCache.getInstance(), ExternalResourceCache.getInstance() );
  }

  @Test
  public void test_getUniqueId() {
    assertNull( testInstance.getUniqueId( null ) );
    // parent TransMeta is not yet mocked, we should get null back
    assertNull( testInstance.getUniqueId( meta1.getParentStepMeta() ) );

    initMetas();

    // null repo
    assertEquals( System.getProperty( "user.dir" ) + File.separator + "my_file::test",
      testInstance.getUniqueId( meta1.getParentStepMeta() ) );

    // mock the repo
    when( transMeta.getRepository() ).thenReturn( Mockito.mock( Repository.class ) );
    assertEquals( "null.null::test",
      testInstance.getUniqueId( meta1.getParentStepMeta() ) );

    when( transMeta.getPathAndName() ).thenReturn( "path_and_name" );
    when( transMeta.getDefaultExtension() ).thenReturn( "ktr" );
    assertEquals( "path_and_name.ktr::test",
      testInstance.getUniqueId( meta1.getParentStepMeta() ) );
  }

  @Test
  public void test_caching() {
    initMetas();

    assertNull( testInstance.get( null ) );
    assertEquals( 0, testInstance.resourceCache.size() );

    assertNull( testInstance.get( meta1) );
    assertEquals( 0, testInstance.resourceCache.size() );

    assertNull( testInstance.get( meta2) );
    assertEquals( 0, testInstance.resourceCache.size() );

    final ExternalResourceCache.ExternalResourceValues resources1 = testInstance
      .newExternalResourceValues();
    resources1.add( Mockito.mock( IExternalResourceInfo.class ) );

    final ExternalResourceCache.ExternalResourceValues resources2 = testInstance
      .newExternalResourceValues();
    resources2.add( Mockito.mock( IExternalResourceInfo.class ) );

    // cache some resources1
    testInstance.cache( meta1, resources1 );
    assertEquals( 1, testInstance.resourceCache.size() );

    // cache some resources2
    testInstance.cache( meta2, resources2 );
    assertEquals( 2, testInstance.resourceCache.size() );

    // remote trans from the cache
    testInstance.removeCachedResources( trans );
    assertEquals( 1, testInstance.resourceCache.size() );
    assertNull( testInstance.get( meta1 ) );
    assertEquals( resources2, testInstance.get( meta2 ) );

    // remove trans2 from the cache
    testInstance.removeCachedResources( trans2 );
    assertNull( testInstance.get( meta2 ) );
    assertEquals( 0, testInstance.resourceCache.size() );
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

  @Test
  public void test_caching_timeout() throws Exception {
    long timeoutInSeconds = 1;
    IMetaverseConfig metaverseConfig = mock( IMetaverseConfig.class );
    when( metaverseConfig.getExternalResourceCacheExpireTime() ).thenReturn( Long.toString( timeoutInSeconds ) );
    testInstance = new ExternalResourceCache( metaverseConfig );

    initMetas();

    final ExternalResourceCache.ExternalResourceValues resources1 = testInstance
      .newExternalResourceValues();
    resources1.add( Mockito.mock( IExternalResourceInfo.class ) );

    final ExternalResourceCache.ExternalResourceValues resources2 = testInstance
      .newExternalResourceValues();
    resources2.add( Mockito.mock( IExternalResourceInfo.class ) );

    // verify nothing in cache
    assertNull( testInstance.get( meta1 ) );
    assertNull( testInstance.get( meta2 ) );

    // add items
    testInstance.cache( meta1, resources1 );
    testInstance.cache( meta2, resources2 );
    assertEquals( resources1, testInstance.get( meta1 ) );
    assertEquals( resources2, testInstance.get( meta2 ) );

    // sleep to test time eviction policy
    Thread.sleep( 11 * timeoutInSeconds * 1000 / 10 ); // eviction policy 1.1  * timeout

    // verify
    assertNull( testInstance.get( meta1 ) );
    assertNull( testInstance.get( meta2 ) );
  }

  @Test
  public void test_GetCacheExpireTime() throws Exception {

    // TEST1 : config null
    assertEquals( ExternalResourceCache.DEFAULT_TIMEOUT_SECONDS, testInstance.getCacheExpireTime( null ) );

    // TEST2: config expireTime value is null
    IMetaverseConfig metaverseConfig1 = mock( IMetaverseConfig.class );
    when( metaverseConfig1.getExternalResourceCacheExpireTime() ).thenReturn( null );
    assertEquals( ExternalResourceCache.DEFAULT_TIMEOUT_SECONDS, testInstance.getCacheExpireTime( metaverseConfig1 ) );

    // TEST3:
    long expireTime = 423L;
    IMetaverseConfig metaverseConfig2 = mock( IMetaverseConfig.class );
    when( metaverseConfig2.getExternalResourceCacheExpireTime() ).thenReturn( Long.toString( expireTime ) );
    assertEquals( expireTime, testInstance.getCacheExpireTime( metaverseConfig2 ) );

  }
}
