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


package org.pentaho.metaverse.analyzer.kettle;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.jobentry.IJobEntryAnalyzerProvider;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @See com.pentaho.analyzer.kettle.MetaverseDocumentAnalyzerTest for base JobAnalyzer tests. Tests here
 * are specific to the JobAnalyzer.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
    lenient().when( namespace.getParentNamespace() ).thenReturn( namespace );

    lenient().when( mockJobDoc.getType() ).thenReturn( DictionaryConst.NODE_TYPE_JOB );
    when( mockJobDoc.getContent() ).thenReturn( mockContent );
    when( mockJobDoc.getNamespace() ).thenReturn( namespace );

    when( mockContent.nrJobEntries() ).thenReturn( 1 );
    when( mockContent.getJobEntry( 0 ) ).thenReturn( mockJobEntry );

    when( mockJobEntry.getEntry() ).thenReturn( mockJobEntryInterface );
    when( mockJobEntryInterface.getParentJob() ).thenReturn( mockJob );

    when( mockJob.getJobMeta() ).thenReturn( mockContent );

    lenient().when( mockContent.listVariables() ).thenReturn( new String[] { } );
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
    lenient().when( mockToEntryMeta.getParentJobMeta() ).thenReturn( mockContent );

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
    final List<IJobEntryAnalyzer> jobEntryAnalyzers = null;
    when( jobEntryAnalyzerProvider.getAnalyzers( any( Collection.class ) ) ).thenReturn( jobEntryAnalyzers );
    IMetaverseNode node = analyzer.analyze( descriptor, mockJobDoc );
    assertNotNull( node );
  }

  @Test
  public void testAnalyzerWithEntriesSpecificAnalyzer() throws MetaverseAnalyzerException {

    analyzer.setJobEntryAnalyzerProvider( jobEntryAnalyzerProvider );
    final Set<IJobEntryAnalyzer> jobEntryAnalyzers = null;
    when( jobEntryAnalyzerProvider.getAnalyzers( any( Collection.class ) ) ).thenReturn( new ArrayList<IJobEntryAnalyzer>() {{
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
    lenient().when( newMockJobDoc.getType() ).thenReturn( DictionaryConst.NODE_TYPE_JOB );
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
