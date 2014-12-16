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
package com.pentaho.metaverse.analyzer.kettle.jobentry.transjob;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.MetaverseTransientNode;
import com.pentaho.metaverse.analyzer.kettle.jobentry.transjob.TransJobEntryAnalyzer;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class TransJobEntryAnalyzerTest {

  private static final String TEST_FILE_NAME = "file.ktr";

  private TransJobEntryAnalyzer analyzer = new TransJobEntryAnalyzer();

  private IMetaverseComponentDescriptor descriptor;

  @Mock
  private JobEntryTrans jobEntryTrans;

  @Mock
  private Job mockParentJob;

  @Mock
  private JobMeta mockParentJobMeta;

  @Mock
  private INamespace namespace;

  @Mock
  private IMetaverseBuilder metaverseBuilder;

  @Mock
  private IMetaverseObjectFactory objectFactory;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( objectFactory );
    when( objectFactory.createNodeObject( anyString(), anyString(),
      anyString() ) ).thenReturn( new MetaverseTransientNode( "name" ) );
    when( jobEntryTrans.getName() ).thenReturn( "job entry" );
    when( jobEntryTrans.getFilename() ).thenReturn( TEST_FILE_NAME );
    when( jobEntryTrans.getParentJob() ).thenReturn( mockParentJob );
    when( mockParentJob.getJobMeta() ).thenReturn( mockParentJobMeta );
    when( namespace.getParentNamespace() ).thenReturn( namespace );

    when( mockParentJobMeta.environmentSubstitute( anyString() ) ).thenAnswer( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return (String) invocation.getArguments()[0];
      }
    } );

    descriptor = new MetaverseComponentDescriptor( "job entry", DictionaryConst.NODE_TYPE_JOB_ENTRY, namespace );
    analyzer.setMetaverseBuilder( metaverseBuilder );

  }

  @Test
  public void testAnalyze() throws Exception {
    assertNotNull( analyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test
  public void testAnalyzeWithExistingFile() throws Exception {
    File testFile = new File( TEST_FILE_NAME );
    if ( !testFile.exists() ) {
      assertTrue( testFile.createNewFile() );
      FileOutputStream fos = new FileOutputStream( testFile );
      PrintStream ps = new PrintStream( fos );
      ps.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<transformation><info><shared_objects_file>shared.xml</shared_objects_file></info>"
        + "<step><name>Dummy</name>\n"
        + "<type>Dummy</type></step></transformation>" );
      ps.close();
      testFile.deleteOnExit();
    }
    assertNotNull( analyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test
  public void testAnalyzeNullFilename() throws Exception {
    when( jobEntryTrans.getFilename() ).thenReturn( null );
    assertNotNull( analyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNullParentJob() throws Exception {
    when( jobEntryTrans.getParentJob() ).thenReturn( null );
    assertNotNull( analyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNull() throws Exception {
    analyzer.analyze( null, null );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJob() throws Exception {
    when( jobEntryTrans.getParentJob() ).thenReturn( null );
    assertNotNull( analyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJobMeta() throws Exception {
    when( mockParentJob.getJobMeta() ).thenReturn( null );
    assertNotNull( analyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test
  public void testGetSupportedEntries() throws Exception {
    Set<Class<? extends JobEntryInterface>> supportedEntities = analyzer.getSupportedEntries();
    assertNotNull( supportedEntities );
    assertEquals( supportedEntities.size(), 1 );
    assertTrue( supportedEntities.contains( JobEntryTrans.class ) );
  }

}
