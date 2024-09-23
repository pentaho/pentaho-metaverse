/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.jobentry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.analyzer.kettle.DatabaseConnectionAnalyzer;
import org.pentaho.metaverse.api.testutils.MetaverseTestUtils;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


// Running this test with the "silent" runner to avoid an odd warning about an unused mock
// that isn't actually a mock.
@RunWith( MockitoJUnitRunner.Silent.class )
public class JobEntryAnalyzerTest {

  JobEntryAnalyzer analyzer;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private JobEntryCopy mockEntry;

  @Mock
  private JobEntryInterface mockJobEntryInterface;

  @Mock
  private IComponentDescriptor mockDescriptor;

  @Mock
  private Job mockJob;

  @Mock
  private JobMeta mockJobMeta;

  @Mock
  private DatabaseMeta mockDatabaseMeta;

  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
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

    JobEntryAnalyzer baseAnalyzer =
        new JobEntryAnalyzer() {

          @Override public Set<Class<? super JobEntryCopy>> getSupportedEntries() {
            return null;
          }

          @Override
          protected void customAnalyze( JobEntryInterface entry, IMetaverseNode rootNode )
            throws MetaverseAnalyzerException {
            // TODO Auto-generated method stub

          }
        };
    analyzer = spy( baseAnalyzer );

    analyzer.setMetaverseBuilder( mockBuilder );
    lenient().when( mockEntry.getEntry() ).thenReturn( mockJobEntryInterface );
    when( mockJobEntryInterface.getPluginId() ).thenReturn( "Base job entry" );
    when( mockJobEntryInterface.getParentJob() ).thenReturn( mockJob );
    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
    when( mockJobMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {

  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( mockDescriptor, null );
  }

  public void testAnalyze() throws MetaverseAnalyzerException {
    IMetaverseNode node = analyzer.analyze( mockDescriptor, mockJobEntryInterface );
    assertNotNull( node );
  }

  @Test
  public void testAnalyzeWithDatabaseMeta() throws MetaverseAnalyzerException {
    DatabaseMeta[] dbs = new DatabaseMeta[] { mockDatabaseMeta };
    lenient().when( mockJobEntryInterface.getUsedDatabaseConnections() ).thenReturn( dbs );
    assertNotNull( analyzer.analyze( mockDescriptor, mockJobEntryInterface ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testSetMetaverseBuilderNull() throws MetaverseAnalyzerException {
    analyzer.setMetaverseBuilder( null );
    analyzer.analyze( mockDescriptor, mockJobEntryInterface );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void addConnectionNodesWithNullStep() throws MetaverseAnalyzerException {
    analyzer.addConnectionNodes( mockDescriptor );
  }

  @Test
  public void testAddConnectionNodesException() throws MetaverseAnalyzerException {
    analyzer.jobEntryInterface = mock( JobEntryInterface.class );
    analyzer.connectionAnalyzer = mock( JobEntryDatabaseConnectionAnalyzer.class );
    doThrow( new MetaverseAnalyzerException() ).when( analyzer.connectionAnalyzer ).analyze( any(), any() );
    analyzer.addConnectionNodes( mockDescriptor );
  }

  @Test
  public void addConnectionNodes() throws MetaverseAnalyzerException {
    DatabaseMeta[] dbs = new DatabaseMeta[] { mockDatabaseMeta };
    analyzer.jobEntryInterface = mockJobEntryInterface;
    //analyzer.addConnectionNodes( mockDescriptor );
    DatabaseConnectionAnalyzer dbAnalyzer = new JobEntryDatabaseConnectionAnalyzer();
    dbAnalyzer.setMetaverseBuilder( mockBuilder );

    analyzer.setConnectionAnalyzer( dbAnalyzer );
    when( mockJobEntryInterface.getUsedDatabaseConnections() ).thenReturn( dbs );

    analyzer.addConnectionNodes( mockDescriptor );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testValidateStateNullEntry() throws MetaverseAnalyzerException {
    analyzer.validateState( mockDescriptor, null );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testValidateStateNullParentJob() throws MetaverseAnalyzerException {
    final Job parentJob = null;
    when( mockJobEntryInterface.getParentJob() ).thenReturn( parentJob );
    analyzer.validateState( mockDescriptor, mockJobEntryInterface );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testValidateStateNullParentJobMeta() throws MetaverseAnalyzerException {
    final JobMeta parentJobMeta = null;
    when( mockJob.getJobMeta() ).thenReturn( parentJobMeta );
    analyzer.validateState( mockDescriptor, mockJobEntryInterface );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testValidateStateNullMetaverseObjectFactory() throws MetaverseAnalyzerException {
    IMetaverseObjectFactory factory = null;
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.validateState( mockDescriptor, mockJobEntryInterface );
  }
}
