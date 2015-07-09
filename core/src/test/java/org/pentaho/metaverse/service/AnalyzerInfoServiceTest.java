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

package org.pentaho.metaverse.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.metaverse.analyzer.kettle.jobentry.transjob.TransJobEntryAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.mergejoin.MergeJoinStepAnalyzer;
import org.pentaho.metaverse.analyzer.kettle.step.stringscut.StringsCutStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzerProvider;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepAnalyzerProvider;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class AnalyzerInfoServiceTest {

  AnalyzerInfoService service;

  @Mock
  IJobEntryAnalyzerProvider jobEntryAnalyzerProvider;
  @Mock
  IStepAnalyzerProvider stepAnalyzerProvider;

  @Before
  public void setUp() throws Exception {
    service = new AnalyzerInfoService();
    service.setJobEntryAnalyzerProvider( jobEntryAnalyzerProvider );
    service.setStepAnalyzerProvider( stepAnalyzerProvider );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    List<IStepAnalyzer> analyzers = new ArrayList<>();
    final StringsCutStepAnalyzer stringsCutStepAnalyzer = new StringsCutStepAnalyzer();
    final MergeJoinStepAnalyzer mergeJoinStepAnalyzer = new MergeJoinStepAnalyzer();
    analyzers.add( stringsCutStepAnalyzer );
    analyzers.add( mergeJoinStepAnalyzer );

    when( stepAnalyzerProvider.getAnalyzers() ).thenReturn( analyzers );

    Response supportedSteps = service.getSupportedSteps();

    assertEquals( Response.Status.OK.getStatusCode(), supportedSteps.getStatus() );
    assertNotNull( supportedSteps.getEntity() );
    assertTrue( supportedSteps.getEntity() instanceof List );
    List<AnalyzerInfo> responseList = (List<AnalyzerInfo>) supportedSteps.getEntity();
    assertEquals( analyzers.size(), responseList.size() );

    // should be sorted based on meta name
    assertEquals( mergeJoinStepAnalyzer.getSupportedSteps().iterator().next().getSimpleName(), responseList.get( 0 ).getMeta() );
    assertEquals( stringsCutStepAnalyzer.getSupportedSteps().iterator().next().getSimpleName(), responseList.get( 1 ).getMeta() );

  }

  @Test
  public void testGetSupportedEntries() throws Exception {
    List<IJobEntryAnalyzer> analyzers = new ArrayList<>();
    final TransJobEntryAnalyzer transJobEntryAnalyzer = new TransJobEntryAnalyzer();
    analyzers.add( transJobEntryAnalyzer );

    when( jobEntryAnalyzerProvider.getAnalyzers() ).thenReturn( analyzers );

    Response supportedJobEntries = service.getSupportedJobEntries();

    assertEquals( Response.Status.OK.getStatusCode(), supportedJobEntries.getStatus() );
    assertNotNull( supportedJobEntries.getEntity() );
    assertTrue( supportedJobEntries.getEntity() instanceof List );
    List<AnalyzerInfo> responseList = (List<AnalyzerInfo>) supportedJobEntries.getEntity();
    assertEquals( analyzers.size(), responseList.size() );

    // should be sorted based on meta name
    assertEquals( transJobEntryAnalyzer.getSupportedEntries().iterator().next().getSimpleName(), responseList.get( 0 ).getMeta() );

  }
}