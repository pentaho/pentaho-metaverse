package org.pentaho.metaverse.analyzer.kettle.jobentry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseTransientNode;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class GenericJobEntryMetaAnalyzerTest {

  private GenericJobEntryMetaAnalyzer analyzer = new GenericJobEntryMetaAnalyzer();

  private IComponentDescriptor descriptor;

  @Mock
  private JobEntryInterface mockJobEntry;

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
    when( mockJobEntry.getName() ).thenReturn( "job entry" );
    when( mockJobEntry.getParentJob() ).thenReturn( mockParentJob );
    when( mockParentJob.getJobMeta() ).thenReturn( mockParentJobMeta );

    descriptor = new MetaverseComponentDescriptor( "job entry", DictionaryConst.NODE_TYPE_JOB_ENTRY, namespace );
    analyzer.setMetaverseBuilder( metaverseBuilder );

  }

  @Test
  public void testAnalyze() throws Exception {
    assertNotNull( analyzer.analyze( descriptor, mockJobEntry ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeNull() throws Exception {
    analyzer.analyze( null, null );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJob() throws Exception {
    when( mockJobEntry.getParentJob() ).thenReturn( null );
    assertNotNull( analyzer.analyze( descriptor, mockJobEntry ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentJobMeta() throws Exception {
    when( mockParentJob.getJobMeta() ).thenReturn( null );
    assertNotNull( analyzer.analyze( descriptor, mockJobEntry ) );
  }

  @Test
  public void testGetSupportedEntries() throws Exception {
    assertNull( analyzer.getSupportedEntries() );
  }
}
