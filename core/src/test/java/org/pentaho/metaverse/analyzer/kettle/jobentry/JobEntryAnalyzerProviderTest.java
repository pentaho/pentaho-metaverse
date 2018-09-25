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

package org.pentaho.metaverse.analyzer.kettle.jobentry;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class JobEntryAnalyzerProviderTest {

  JobEntryAnalyzerProvider provider;

  @Mock
  IJobEntryAnalyzer mockJobEntryAnalyzer;

  @Before
  public void setUp() throws Exception {
    provider = new JobEntryAnalyzerProvider();
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetAnalyzersEmpty() throws Exception {
    assertNotNull( provider.getAnalyzers() );
    assertTrue( provider.getAnalyzers().isEmpty() );
  }

  @Test
  public void testGetAnalyzersNonEmpty() throws Exception {
    assertTrue( provider.getAnalyzers().isEmpty() );
    provider.jobEntryAnalyzers = new ArrayList<IJobEntryAnalyzer>() {{
      add( mockJobEntryAnalyzer );
    }};
    assertFalse( provider.getAnalyzers().isEmpty() );
  }

  @Test
  public void testGetAnalyzersForClass() throws Exception {
    Set<IJobEntryAnalyzer> baseJobEntryAnalyzerSet = Sets.newSet( mock( IJobEntryAnalyzer.class ) );
    Set<IJobEntryAnalyzer> jobEntryTransAnalyzerSet = Sets.newSet( mock( IJobEntryAnalyzer.class ) );
    // Return the baseJobEntryAnalyzer set if JobEntryInterface analyzers are requested
    provider.analyzerTypeMap.put( JobEntryInterface.class, baseJobEntryAnalyzerSet );
    // Return the jobEntryTransAnalyzerSet set if JobEntryTrans analyzers are requested
    provider.analyzerTypeMap.put( JobEntryTrans.class, jobEntryTransAnalyzerSet );

    List<IJobEntryAnalyzer> analyzers = provider.getAnalyzers( new ArrayList<Class<?>>() {{
      add( JobEntryInterface.class );
      add( JobEntryTrans.class );
    }} );
    assertEquals( analyzers.size(), 2 );

    analyzers = provider.getAnalyzers( new HashSet<Class<?>>() {{
      add( JobEntryTrans.class );
    }} );
    assertEquals( analyzers.size(), 1 );
  }

  @Test
  public void testsetJobEntryAnalyzersNull() throws Exception {
    assertNotNull( provider.jobEntryAnalyzers );
    assertTrue( provider.jobEntryAnalyzers.isEmpty() );
    provider.setJobEntryAnalyzers( null );
    assertNull( provider.jobEntryAnalyzers );
  }

  @Test
  public void testsetJobEntryAnalyzers() throws Exception {
    assertNotNull( provider.jobEntryAnalyzers );
    assertTrue( provider.jobEntryAnalyzers.isEmpty() );
    List<IJobEntryAnalyzer> analyzerSet = new ArrayList<IJobEntryAnalyzer>() {{
      add( mockJobEntryAnalyzer );
    }};
    provider.setJobEntryAnalyzers( analyzerSet );
    assertFalse( provider.jobEntryAnalyzers.isEmpty() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testLoadAnalyzerTypeMap() throws Exception {
    IJobEntryAnalyzer baseStepAnalyzer = mock( IJobEntryAnalyzer.class );
    when( baseStepAnalyzer.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryInterface.class ) );

    IJobEntryAnalyzer tableOutputStepAnalyzer = mock( IJobEntryAnalyzer.class );
    when( tableOutputStepAnalyzer.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryTrans.class ) );

    IJobEntryAnalyzer tableOutputStepAnalyzer2 = mock( IJobEntryAnalyzer.class );
    when( tableOutputStepAnalyzer2.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryTrans.class ) );

    provider.jobEntryAnalyzers =
        Lists.newArrayList( baseStepAnalyzer, tableOutputStepAnalyzer, tableOutputStepAnalyzer2 );

    // Method under test
    provider.loadAnalyzerTypeMap();

    Set<IJobEntryAnalyzer> baseStepAnalyzers = provider.analyzerTypeMap.get( JobEntryInterface.class );
    assertNotNull( baseStepAnalyzers );
    assertEquals( baseStepAnalyzers.size(), 1 );

    Set<IJobEntryAnalyzer> tableOutputStepAnalyzers = provider.analyzerTypeMap.get( JobEntryTrans.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 2 );
  }

  @Test
  public void testRemoveAnalyzer() throws Exception {
    IJobEntryAnalyzer baseStepAnalyzer = mock( IJobEntryAnalyzer.class );
    when( baseStepAnalyzer.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryInterface.class ) );

    IJobEntryAnalyzer jobEntryTransAnalyzer = mock( IJobEntryAnalyzer.class );
    when( jobEntryTransAnalyzer.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryTrans.class ) );

    provider.setJobEntryAnalyzers(
        Lists.newArrayList( baseStepAnalyzer, jobEntryTransAnalyzer ) );

    Set<IJobEntryAnalyzer> tableOutputStepAnalyzers = provider.analyzerTypeMap.get( JobEntryTrans.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 1 );

    provider.removeAnalyzer( jobEntryTransAnalyzer );
    tableOutputStepAnalyzers = provider.analyzerTypeMap.get( JobEntryTrans.class );
    assertNull( tableOutputStepAnalyzers );
  }

  @Test
  public void testRemoveAnalyzer_multipleWithTheSameType() throws Exception {
    IJobEntryAnalyzer baseStepAnalyzer = mock( IJobEntryAnalyzer.class );
    when( baseStepAnalyzer.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryInterface.class ) );

    IJobEntryAnalyzer jobEntryTransAnalyzer = mock( IJobEntryAnalyzer.class );
    when( jobEntryTransAnalyzer.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryTrans.class ) );

    IJobEntryAnalyzer jobEntryTransAnalyzer2 = mock( IJobEntryAnalyzer.class );
    when( jobEntryTransAnalyzer2.getSupportedEntries() ).thenReturn( Sets.newSet( JobEntryTrans.class ) );

    provider.setJobEntryAnalyzers(
        Lists.newArrayList( baseStepAnalyzer, jobEntryTransAnalyzer, jobEntryTransAnalyzer2 ) );

    Set<IJobEntryAnalyzer> tableOutputStepAnalyzers = provider.analyzerTypeMap.get( JobEntryTrans.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 2 );

    provider.removeAnalyzer( jobEntryTransAnalyzer2 );
    tableOutputStepAnalyzers = provider.analyzerTypeMap.get( JobEntryTrans.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 1 );
  }

  @Test
  public void TestAddAndSetAnalyzers() {

    final IJobEntryAnalyzer analyzer1 = mock( IJobEntryAnalyzer.class );
    final IJobEntryAnalyzer analyzer2 = mock( IJobEntryAnalyzer.class );
    List<IJobEntryAnalyzer> analyzers = new ArrayList();
    analyzers.add( analyzer1 );
    analyzers.add( analyzer2 );

    provider.setJobEntryAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );
    // verify that duplicate analyzers aren't added to the list
    provider.setJobEntryAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );

    provider.setJobEntryAnalyzers( null );
    assertNull( provider.getAnalyzers() );

    // verify that "clonable" analyzers are added to the main analyzers list
    provider.setClonableJobEntryAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );
    // and that duplicate clonable analyzers aren't added to the list
    provider.setClonableJobEntryAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );

    provider.setClonableJobEntryAnalyzers( null );
    assertNull( provider.getAnalyzers() );
  }
}
