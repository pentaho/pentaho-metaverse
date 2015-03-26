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

package com.pentaho.metaverse.analyzer.kettle.jobentry;

import com.pentaho.metaverse.analyzer.kettle.DatabaseConnectionAnalyzer;
import com.pentaho.metaverse.analyzer.kettle.IDatabaseConnectionAnalyzer;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BaseJobEntryAnalyzerTest {

  BaseJobEntryAnalyzer analyzer;

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

    BaseJobEntryAnalyzer baseAnalyzer =
        new BaseJobEntryAnalyzer() {

          @Override public Set<Class<? super JobEntryCopy>> getSupportedEntries() {
            return null;
          }

          @Override
          public IMetaverseNode analyze( IComponentDescriptor descriptor, Object object )
              throws MetaverseAnalyzerException {
            return null;
          }
        };
    analyzer = spy( baseAnalyzer );

    analyzer.setMetaverseBuilder( mockBuilder );
    when( mockEntry.getEntry() ).thenReturn( mockJobEntryInterface );
    when( mockJobEntryInterface.getPluginId() ).thenReturn( "Base job entry" );
    when( mockJobEntryInterface.getParentJob() ).thenReturn( mockJob );
    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
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
    when( mockJobEntryInterface.getUsedDatabaseConnections() ).thenReturn( dbs );
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
    when( mockDescriptor.getContext() ).thenThrow( Exception.class );
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
    when( analyzer.getConnectionAnalyzer() ).thenReturn( dbAnalyzer );
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
