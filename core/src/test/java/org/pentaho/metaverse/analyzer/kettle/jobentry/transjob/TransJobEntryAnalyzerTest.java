/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.jobentry.transjob;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TransJobEntryAnalyzerTest {

  private static final String TEST_FILE_NAME = "file.ktr";

  private TransJobEntryAnalyzer analyzer;

  private IComponentDescriptor descriptor;

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

  @Mock
  private TransMeta childTransMeta;

  @Mock
  private IComponentDescriptor documentDescriptor;

  private TransJobEntryAnalyzer spyAnalyzer;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( objectFactory );
    when( objectFactory.createNodeObject( Mockito.<String>any(), any(), any() ) ).thenReturn( new MetaverseTransientNode( "name" ) );
    when( jobEntryTrans.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( jobEntryTrans.getFilename() ).thenReturn( TEST_FILE_NAME );
    when( jobEntryTrans.getParentJob() ).thenReturn( mockParentJob );
    when( mockParentJob.getJobMeta() ).thenReturn( mockParentJobMeta );
    when( namespace.getParentNamespace() ).thenReturn( namespace );

    when( mockParentJobMeta.environmentSubstitute( Mockito.<String>any() ) ).thenAnswer( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return (String) invocation.getArguments()[0];
      }
    } );

    descriptor = new MetaverseComponentDescriptor( "job entry", DictionaryConst.NODE_TYPE_JOB_ENTRY, namespace );
    analyzer = new TransJobEntryAnalyzer();
    spyAnalyzer = spy( analyzer );
    spyAnalyzer.setMetaverseBuilder( metaverseBuilder );
    spyAnalyzer.setDescriptor( descriptor );
    doReturn( childTransMeta ).when( spyAnalyzer ).getSubTransMeta( anyString() );
    doReturn( documentDescriptor ).when( spyAnalyzer ).getDocumentDescriptor();
  }

  @Test
  public void testAnalyze() throws Exception {
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryTrans ) );
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
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNullParentJob() throws Exception {
    when( jobEntryTrans.getParentJob() ).thenReturn( null );
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNull() throws Exception {
    spyAnalyzer.analyze( null, null );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJob() throws Exception {
    when( jobEntryTrans.getParentJob() ).thenReturn( null );
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJobMeta() throws Exception {
    when( mockParentJob.getJobMeta() ).thenReturn( null );
    assertNotNull( spyAnalyzer.analyze( descriptor, jobEntryTrans ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithException() throws Exception {
    when( mockParentJobMeta.environmentSubstitute( anyString() ) ).thenThrow( new RuntimeException() );
    spyAnalyzer.analyze( descriptor, jobEntryTrans );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByNameThrowsException() throws Exception {
    Repository repo = mock( Repository.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryTrans.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // Test exception
    when( repo.findDirectory( Mockito.<String>any() ) ).thenThrow( new KettleException() );
    spyAnalyzer.analyze( descriptor, jobEntryTrans );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByNameNoRepo() throws Exception {
    when( jobEntryTrans.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // Test exception
    spyAnalyzer.analyze( descriptor, jobEntryTrans );
  }

  @Test
  public void testAnalyzeWithRepoByName() throws Exception {
    Repository repo = mock( Repository.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryTrans.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    when( repo.findDirectory( Mockito.<String>any() ) ).thenReturn( repoDir );
    when( repo.loadTransformation( any(), eq( repoDir ), any(), anyBoolean(), any() ) ).thenReturn( childTransMeta );
    spyAnalyzer.analyze( descriptor, jobEntryTrans );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByRefThrowsException() throws Exception {
    Repository repo = mock( Repository.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryTrans.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // Test exception
    when( repo.loadTransformation( any(), any() ) ).thenThrow( new KettleException() );
    spyAnalyzer.analyze( descriptor, jobEntryTrans );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithRepoByRefNoRepo() throws Exception {
    when( jobEntryTrans.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // Test exception
    spyAnalyzer.analyze( descriptor, jobEntryTrans );
  }

  @Test
  public void testAnalyzeWithRepoByRef() throws Exception {
    Repository repo = mock( Repository.class );
    when( mockParentJobMeta.getRepository() ).thenReturn( repo );
    when( jobEntryTrans.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    when( repo.loadTransformation( any(), any() ) ).thenReturn( childTransMeta );
    when( childTransMeta.getPathAndName() ).thenReturn( "/path/to/test" );
    when( childTransMeta.getDefaultExtension() ).thenReturn( "ktr" );
    spyAnalyzer.analyze( descriptor, jobEntryTrans );
  }

  @Test( expected = FileNotFoundException.class )
  public void testGetSubJobMetaFileNotFoundException() throws Exception {
    analyzer.getSubTransMeta( "/not/a/real/file" );
  }

  @Test
  public void testGetSupportedEntries() throws Exception {
    Set<Class<? extends JobEntryInterface>> supportedEntities = spyAnalyzer.getSupportedEntries();
    assertNotNull( supportedEntities );
    assertEquals( supportedEntities.size(), 1 );
    assertTrue( supportedEntities.contains( JobEntryTrans.class ) );
  }

}
