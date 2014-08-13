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
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.impl.MetaverseNamespace;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 *  @See com.pentaho.analyzer.kettle.MetaverseDocumentAnalyzerTest for base JobAnalyzer tests. Tests here
 *  are specific to the JobAnalyzer.
 */
@RunWith( MockitoJUnitRunner.class )
public class JobAnalyzerTest {

  private IDocumentAnalyzer analyzer;

  @Mock
  private JobMeta mockContent;

  @Mock
  private JobEntryCopy mockJobEntry;

  @Mock
  private JobEntryInterface mockJobEntryInterface;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private IMetaverseDocument mockJobDoc;

  @Mock
  private MetaverseNamespace namespace;

  private MetaverseComponentDescriptor descriptor;

  @Mock
  private Job mockJob;


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
    when(namespace.getChildNamespace( anyString(), anyString() )).thenReturn( namespace );
    when(namespace.getParentNamespace() ).thenReturn( namespace );

    when( mockJobDoc.getType() ).thenReturn( DictionaryConst.NODE_TYPE_JOB );
    when( mockJobDoc.getContent() ).thenReturn( mockContent );
    when( mockJobDoc.getNamespace() ).thenReturn( namespace );

    when( mockContent.nrJobEntries() ).thenReturn( 1 );
    when( mockContent.getJobEntry( 0 ) ).thenReturn( mockJobEntry );

    when( mockJobEntry.getEntry()).thenReturn( mockJobEntryInterface );
    when (mockJobEntryInterface.getParentJob()).thenReturn( mockJob );

    when (mockJob.getJobMeta()).thenReturn( mockContent );

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


}
