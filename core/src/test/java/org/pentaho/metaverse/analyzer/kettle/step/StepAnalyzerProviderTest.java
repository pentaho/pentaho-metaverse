/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metaverse.analyzer.kettle.step.tableoutput.TableOutputStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class StepAnalyzerProviderTest {

  StepAnalyzerProvider provider;

  @Mock
  IStepAnalyzer mockStepAnalyzer;

  @Before
  public void setUp() throws Exception {
    provider = new StepAnalyzerProvider();
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
    List<IStepAnalyzer> analyzerSet = new ArrayList<IStepAnalyzer>() {{
      add( mockStepAnalyzer );
    }};
    provider.stepAnalyzers = analyzerSet;
    assertFalse( provider.getAnalyzers().isEmpty() );
  }

  @Test
  public void testGetAnalyzersForClass() throws Exception {
    Set<IStepAnalyzer> baseStepAnalyzerSet = Sets.newSet( mock( IStepAnalyzer.class ) );
    Set<IStepAnalyzer> tableOutputStepAnalyzerSet = Sets.newSet( mock( IStepAnalyzer.class ) );
    // Return the baseStepAnalyzer set if BaseStepMeta analyzers are requested
    provider.analyzerTypeMap.put( BaseStepMeta.class, baseStepAnalyzerSet );
    // Return the tableOutputStepAnalyzerSet set if TableOutputMeta analyzers are requested
    provider.analyzerTypeMap.put( TableOutputMeta.class, tableOutputStepAnalyzerSet );
    provider.addAnalyzer( mock( GenericStepMetaAnalyzer.class ) );
    provider.addAnalyzer( mock( TableOutputStepAnalyzer.class ) );

    List<IStepAnalyzer> analyzers = provider.getAnalyzers( new ArrayList() {{
      add( BaseStepMeta.class );
      add( TableOutputMeta.class );
    }} );
    assertEquals( 2, analyzers.size() );

    analyzers = provider.getAnalyzers( new HashSet() {{
      add( TableOutputMeta.class );
    }} );
    assertEquals( 1, analyzers.size() );
  }

  @Test
  public void testSetStepAnalyzersNull() throws Exception {
    assertNotNull( provider.stepAnalyzers );
    assertTrue( provider.stepAnalyzers.isEmpty() );
    provider.setStepAnalyzers( null );
    assertNull( provider.stepAnalyzers );
  }

  @Test
  public void testSetStepAnalyzers() throws Exception {
    assertNotNull( provider.stepAnalyzers );
    assertTrue( provider.stepAnalyzers.isEmpty() );
    List<IStepAnalyzer> analyzerSet = new ArrayList<IStepAnalyzer>() {{
      add( mockStepAnalyzer );
    }};
    provider.setStepAnalyzers( analyzerSet );
    assertFalse( provider.stepAnalyzers.isEmpty() );
  }

  @Test
  public void testLoadAnalyzerTypeMap() throws Exception {
    IStepAnalyzer baseStepAnalyzer = mock( IStepAnalyzer.class );
    when( baseStepAnalyzer.getSupportedSteps() ).thenReturn( Sets.newSet( BaseStepMeta.class ) );

    IStepAnalyzer tableOutputStepAnalyzer = mock( IStepAnalyzer.class );
    when( tableOutputStepAnalyzer.getSupportedSteps() ).thenReturn( Sets.newSet( TableOutputMeta.class ) );

    IStepAnalyzer tableOutputStepAnalyzer2 = mock( IStepAnalyzer.class );
    when( tableOutputStepAnalyzer2.getSupportedSteps() ).thenReturn( Sets.newSet( TableOutputMeta.class ) );

    provider.stepAnalyzers = Lists.newArrayList( baseStepAnalyzer, tableOutputStepAnalyzer, tableOutputStepAnalyzer2 );

    // Method under test
    provider.loadAnalyzerTypeMap();

    Set<IStepAnalyzer> baseStepAnalyzers = provider.analyzerTypeMap.get( BaseStepMeta.class );
    assertNotNull( baseStepAnalyzers );
    assertEquals( baseStepAnalyzers.size(), 1 );

    Set<IStepAnalyzer> tableOutputStepAnalyzers = provider.analyzerTypeMap.get( TableOutputMeta.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 2 );
  }

  @Test
  public void testRemoveAnalyzer() throws Exception {
    IStepAnalyzer baseStepAnalyzer = mock( IStepAnalyzer.class );
    when( baseStepAnalyzer.getSupportedSteps() ).thenReturn( Sets.newSet( BaseStepMeta.class ) );

    IStepAnalyzer tableOutputStepAnalyzer = mock( IStepAnalyzer.class );
    when( tableOutputStepAnalyzer.getSupportedSteps() ).thenReturn( Sets.newSet( TableOutputMeta.class ) );

    provider.setStepAnalyzers(
        Lists.newArrayList( baseStepAnalyzer, tableOutputStepAnalyzer ) );

    Set<IStepAnalyzer> tableOutputStepAnalyzers = provider.analyzerTypeMap.get( TableOutputMeta.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 1 );

    provider.removeAnalyzer( tableOutputStepAnalyzer );
    tableOutputStepAnalyzers = provider.analyzerTypeMap.get( TableOutputMeta.class );
    assertNull( tableOutputStepAnalyzers );

  }

  @Test
  public void testRemoveAnalyzer_multipleWithTheSameType() throws Exception {
    IStepAnalyzer baseStepAnalyzer = mock( IStepAnalyzer.class );
    when( baseStepAnalyzer.getSupportedSteps() ).thenReturn( Sets.newSet( BaseStepMeta.class ) );

    IStepAnalyzer tableOutputStepAnalyzer = mock( IStepAnalyzer.class );
    when( tableOutputStepAnalyzer.getSupportedSteps() ).thenReturn( Sets.newSet( TableOutputMeta.class ) );

    IStepAnalyzer tableOutputStepAnalyzer2 = mock( IStepAnalyzer.class );
    when( tableOutputStepAnalyzer2.getSupportedSteps() ).thenReturn( Sets.newSet( TableOutputMeta.class ) );

    provider.setStepAnalyzers(
        Lists.newArrayList( baseStepAnalyzer, tableOutputStepAnalyzer, tableOutputStepAnalyzer2 ) );

    Set<IStepAnalyzer> tableOutputStepAnalyzers = provider.analyzerTypeMap.get( TableOutputMeta.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 2 );

    provider.removeAnalyzer( tableOutputStepAnalyzer2 );
    tableOutputStepAnalyzers = provider.analyzerTypeMap.get( TableOutputMeta.class );
    assertNotNull( tableOutputStepAnalyzers );
    assertEquals( tableOutputStepAnalyzers.size(), 1 );
    assertEquals( tableOutputStepAnalyzer, tableOutputStepAnalyzers.iterator().next() );

  }

  @Test
  public void TestAddAndSetAnalyzers() {

    final IStepAnalyzer analyzer1 = mock( IStepAnalyzer.class );
    final  IStepAnalyzer analyzer2 = mock( IStepAnalyzer.class );
    List<IStepAnalyzer> analyzers = new ArrayList();
    analyzers.add( analyzer1 );
    analyzers.add( analyzer2 );

    provider.setStepAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );
    // verify that duplicate analyzers aren't added to the list
    provider.setStepAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );

    provider.setStepAnalyzers( null );
    assertTrue( provider.getAnalyzers().isEmpty() );

    // verify that "clonable" analyzers are added to the main analyzers list
    provider.setClonableStepAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );
    // and that duplicate clonable analyzers aren't added to the list
    provider.setClonableStepAnalyzers( analyzers );
    assertEquals( 2, provider.getAnalyzers().size() );

    provider.setClonableStepAnalyzers( null );
    assertTrue( provider.getAnalyzers().isEmpty() );
  }
}
