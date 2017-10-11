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

package org.pentaho.metaverse.analyzer.kettle.jobentry.job;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseTransientNode;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for JobJobEntryAnalyzer
 */
@RunWith( MockitoJUnitRunner.class )
public class JobJobEntryAnalyzerTest {

  private static final String TEST_FILE_NAME = "file.kjb";

  private JobJobEntryAnalyzer analyzer;

  private IComponentDescriptor descriptor;

  @Mock
  private JobEntryJob jobEntryJob;

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

  @Mock
  private JobMeta childJobMeta;

  private JobJobEntryAnalyzer spyAnalyzer;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( objectFactory );
    when( objectFactory.createNodeObject( anyString(), anyString(),
      anyString() ) ).thenReturn( new MetaverseTransientNode( "name" ) );
    when( jobEntryJob.getName() ).thenReturn( "job entry" );
    when( jobEntryJob.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( jobEntryJob.getFilename() ).thenReturn( TEST_FILE_NAME );
    when( jobEntryJob.getParentJob() ).thenReturn( mockParentJob );
    when( mockParentJob.getJobMeta() ).thenReturn( mockParentJobMeta );
    when( namespace.getParentNamespace() ).thenReturn( namespace );

    when( mockParentJobMeta.environmentSubstitute( anyString() ) ).thenAnswer( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return (String) invocation.getArguments()[0];
      }
    } );

    descriptor = new MetaverseComponentDescriptor( "job entry", DictionaryConst.NODE_TYPE_JOB_ENTRY, namespace );
    analyzer = new JobJobEntryAnalyzer();
    spyAnalyzer = spy( analyzer );
    spyAnalyzer.setMetaverseBuilder( metaverseBuilder );
    spyAnalyzer.setDescriptor( descriptor );
    doReturn( childJobMeta ).when( spyAnalyzer ).getSubJobMeta( anyString() );
  }

  @Test
  public void testAnalyze() throws Exception {
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryJob ) );
  }

  @Test
  public void testAnalyzeWithExistingFile() throws Exception {
    File testFile = new File( TEST_FILE_NAME );
    if ( !testFile.exists() ) {
      assertTrue( testFile.createNewFile() );
      FileOutputStream fos = new FileOutputStream( testFile );
      PrintStream ps = new PrintStream( fos );
      ps.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<job><info><shared_objects_file>shared.xml</shared_objects_file></info>"
        + "<entry><name>START</name>\n"
        + "<type>START</type></entry></job>" );
      ps.close();
      testFile.deleteOnExit();
    }
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryJob ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNullParentJob() throws Exception {
    when( jobEntryJob.getParentJob() ).thenReturn( null );
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryJob ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNull() throws Exception {
    spyAnalyzer.analyze( null, null );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJob() throws Exception {
    when( jobEntryJob.getParentJob() ).thenReturn( null );
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryJob ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJobMeta() throws Exception {
    when( mockParentJob.getJobMeta() ).thenReturn( null );
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithException() throws Exception {
    when( mockParentJobMeta.environmentSubstitute( anyString() ) ).thenThrow( new RuntimeException() );
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByNameThrowsException() throws Exception {
    Repository repo = mock( Repository.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryJob.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // Test exception
    when( repo.findDirectory( anyString() ) ).thenThrow( new KettleException() );
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByNameNoRepo() throws Exception {
    when( jobEntryJob.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // Test exception
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test
  public void testAnalyzeWithRepoByName() throws Exception {
    Repository repo = mock( Repository.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryJob.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // Test exception
    when( repo.findDirectory( anyString() ) ).thenReturn( repoDir );
    when( repo.loadJob( anyString(), eq( repoDir ), any( ProgressMonitorListener.class ), anyString() ) )
      .thenReturn( childJobMeta );
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByRefThrowsException() throws Exception {
    Repository repo = mock( Repository.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryJob.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // Test exception
    when( repo.loadJob( any( ObjectId.class ), anyString() ) ).thenThrow( new KettleException() );
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByRefNoRepo() throws Exception {
    when( jobEntryJob.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // Test exception
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test
  public void testAnalyzeWithRepoByRef() throws Exception {
    Repository repo = mock( Repository.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryJob.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // Test exception
    when( repo.loadJob( any( ObjectId.class ), anyString() ) ).thenReturn( childJobMeta );
    spyAnalyzer.analyze( descriptor, jobEntryJob );
  }

  @Test( expected = FileNotFoundException.class )
  public void testGetSubJobMetaFileNotFoundException() throws Exception {
    analyzer.getSubJobMeta( "/not/a/real/file" );
  }

  @Test
  public void testGetSupportedEntries() throws Exception {
    Set<Class<? extends JobEntryInterface>> supportedEntities = spyAnalyzer.getSupportedEntries();
    assertNotNull( supportedEntities );
    assertEquals( supportedEntities.size(), 1 );
    assertTrue( supportedEntities.contains( JobEntryJob.class ) );
  }

}
