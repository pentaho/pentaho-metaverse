package com.pentaho.metaverse.analyzer.kettle.jobentry.transjob;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.MetaverseTransientNode;
import com.pentaho.metaverse.analyzer.kettle.jobentry.transjob.TransJobEntryAnalyzer;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class TransJobEntryAnalyzerTest {

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

  @Before
  public void setUp() throws Exception {
    when( metaverseBuilder.getMetaverseObjectFactory() ).thenReturn( objectFactory );
    when( objectFactory.createNodeObject( anyString(), anyString(),
        anyString() ) ).thenReturn( new MetaverseTransientNode( "name" ) );
    when( jobEntryTrans.getName() ).thenReturn( "job entry" );
    when( jobEntryTrans.getFilename() ).thenReturn( "file.ktr" );
    when( jobEntryTrans.getParentJob() ).thenReturn( mockParentJob );
    when( mockParentJob.getJobMeta() ).thenReturn( mockParentJobMeta );
    when( namespace.getParentNamespace() ).thenReturn( namespace );
//    when( namespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );

    when( mockParentJobMeta.environmentSubstitute( anyString() ) ).thenAnswer( new Answer<String>() {
      @Override public String answer( InvocationOnMock invocation ) throws Throwable {
        return (String)invocation.getArguments()[0];
      }
    });

    descriptor = new MetaverseComponentDescriptor( "job entry", DictionaryConst.NODE_TYPE_JOB_ENTRY, namespace );
    analyzer.setMetaverseBuilder( metaverseBuilder );

  }

  @Test
  public void testAnalyze() throws Exception {
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
