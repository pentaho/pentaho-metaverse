/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.jobentry.IJobEntryAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.jobentry.IJobEntryAnalyzerProvider;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IDocument;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @See com.pentaho.analyzer.kettle.MetaverseDocumentAnalyzerTest for base JobAnalyzer tests. Tests here
 * are specific to the JobAnalyzer.
 */
@RunWith(MockitoJUnitRunner.class)
public class JobAnalyzerTest {

  private JobAnalyzer analyzer;

  @Mock
  private JobMeta mockContent;

  @Mock
  private JobEntryCopy mockJobEntry;

  @Mock
  private JobEntryInterface mockJobEntryInterface;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private IDocument mockJobDoc;

  @Mock
  private INamespace namespace;

  private MetaverseComponentDescriptor descriptor;

  @Mock
  private Job mockJob;

  @Mock
  private IJobEntryAnalyzerProvider jobEntryAnalyzerProvider;

  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }

  }

  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {

    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );

    analyzer = new JobAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );
    when( namespace.getParentNamespace() ).thenReturn( namespace );

    when( mockJobDoc.getType() ).thenReturn( DictionaryConst.NODE_TYPE_JOB );
    when( mockJobDoc.getContent() ).thenReturn( mockContent );
    when( mockJobDoc.getNamespace() ).thenReturn( namespace );

    when( mockContent.nrJobEntries() ).thenReturn( 1 );
    when( mockContent.getJobEntry( 0 ) ).thenReturn( mockJobEntry );

    when( mockJobEntry.getEntry() ).thenReturn( mockJobEntryInterface );
    when( mockJobEntryInterface.getParentJob() ).thenReturn( mockJob );

    when( mockJob.getJobMeta() ).thenReturn( mockContent );

    when( mockContent.listVariables() ).thenReturn( new String[] { } );
    final String PARAM = "param1";
    when( mockContent.listParameters() ).thenReturn( new String[] { PARAM } );
    when( mockContent.getParameterDefault( PARAM ) ).thenReturn( "default" );

    descriptor = new MetaverseComponentDescriptor( "name", DictionaryConst.NODE_TYPE_JOB, namespace );
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAnalyzerJobWithEntries() throws MetaverseAnalyzerException {

    // increases line code coverage by adding entries to the job
    IMetaverseNode node = analyzer.analyze( descriptor, mockJobDoc );
    assertNotNull( node );
  }

  @Test
  public void testAnalyzerJobWithEntriesAndHop() throws MetaverseAnalyzerException {

    JobEntryCopy mockToEntryMeta = mock( JobEntryCopy.class );
    when( mockToEntryMeta.getEntry() ).thenReturn( mockJobEntryInterface );
    when( mockToEntryMeta.getParentJobMeta() ).thenReturn( mockContent );

    when( mockContent.nrJobEntries() ).thenReturn( 2 );
    when( mockContent.getJobEntry( 0 ) ).thenReturn( mockJobEntry );
    when( mockContent.getJobEntry( 1 ) ).thenReturn( mockToEntryMeta );
    when( mockContent.nrJobHops() ).thenReturn( 1 );
    final JobHopMeta hop = new JobHopMeta( mockJobEntry, mockToEntryMeta );
    when( mockContent.getJobHop( 0 ) ).thenReturn( hop );

    IMetaverseNode node = analyzer.analyze( descriptor, mockJobDoc );
    assertNotNull( node );
  }

  @Test
  public void testAnalyzerWithEntriesGenericAnalyzer() throws MetaverseAnalyzerException {

    analyzer.setJobEntryAnalyzerProvider( jobEntryAnalyzerProvider );
    final Set<IJobEntryAnalyzer> jobEntryAnalyzers = null;
    when( jobEntryAnalyzerProvider.getAnalyzers( any( Set.class ) ) ).thenReturn( jobEntryAnalyzers );
    IMetaverseNode node = analyzer.analyze( descriptor, mockJobDoc );
    assertNotNull( node );
  }

  @Test
  public void testAnalyzerWithEntriesSpecificAnalyzer() throws MetaverseAnalyzerException {

    analyzer.setJobEntryAnalyzerProvider( jobEntryAnalyzerProvider );
    final Set<IJobEntryAnalyzer> jobEntryAnalyzers = null;
    when( jobEntryAnalyzerProvider.getAnalyzers( any( Set.class ) ) ).thenReturn( new HashSet<IJobEntryAnalyzer>() {{
      add(
          mock( IJobEntryAnalyzer.class )
      );
    }} );
    IMetaverseNode node = analyzer.analyze( descriptor, mockJobDoc );
    assertNotNull( node );
  }

  @Test
  public void testAnalyzerJobWithFullMetadata() throws MetaverseAnalyzerException {

    when( mockContent.getDescription() ).thenReturn( "I am a description" );
    when( mockContent.getExtendedDescription() ).thenReturn( "I am an extended description" );
    when( mockContent.getJobversion() ).thenReturn( "1.0" );
    Date now = Calendar.getInstance().getTime();
    when( mockContent.getCreatedDate() ).thenReturn( now );
    when( mockContent.getCreatedUser() ).thenReturn( "joe" );
    when( mockContent.getModifiedDate() ).thenReturn( now );
    when( mockContent.getModifiedUser() ).thenReturn( "suzy" );
    when( mockContent.getJobstatus() ).thenReturn( 1 ); // Production

    IMetaverseNode node = analyzer.analyze( descriptor, mockJobDoc );
    assertNotNull( node );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithBadXML() throws MetaverseAnalyzerException {
    IDocument newMockJobDoc = mock( IDocument.class );
    when( newMockJobDoc.getType() ).thenReturn( DictionaryConst.NODE_TYPE_JOB );
    when( newMockJobDoc.getContent() ).thenReturn(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<job>This is not a valid JobMeta doc!" );
    analyzer.analyze( descriptor, newMockJobDoc );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzerJobWithParamException() throws Exception {

    when( mockContent.getParameterDefault( anyString() ) ).thenThrow( UnknownParamException.class );
    // increases line code coverage by adding step to transformation
    IMetaverseNode node = analyzer.analyze( descriptor, mockJobDoc );
    assertNotNull( node );
  }

  @Test
  public void testSetGetJobEntryAnalyzerProvider() {
    analyzer.setJobEntryAnalyzerProvider( jobEntryAnalyzerProvider );
    assertEquals( analyzer.getJobEntryAnalyzerProvider(), jobEntryAnalyzerProvider );
    analyzer.setJobEntryAnalyzerProvider( null );
    assertNull( analyzer.getJobEntryAnalyzerProvider() );
  }

  @Test
  public void testGetSupportedTypes() {
    Set<String> types = analyzer.getSupportedTypes();
    assertTrue( types == JobAnalyzer.defaultSupportedTypes );
  }
}
