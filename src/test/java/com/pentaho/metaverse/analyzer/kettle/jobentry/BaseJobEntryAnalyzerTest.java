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

import com.pentaho.metaverse.analyzer.kettle.jobentry.BaseJobEntryAnalyzer;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BaseJobEntryAnalyzerTest {

  BaseJobEntryAnalyzer analyzer;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private JobEntryCopy mockEntry;

  @Mock
  private JobEntryInterface mockJobEntryInterface;

  @Mock IMetaverseComponentDescriptor mockDescriptor;

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

    analyzer = new BaseJobEntryAnalyzer() {

      @Override public Set<Class<? super JobEntryCopy>> getSupportedEntries() {
        return null;
      }

      @Override
      public IMetaverseNode analyze( IMetaverseComponentDescriptor descriptor, Object object )
          throws MetaverseAnalyzerException {

        return null;
      }
    };

    analyzer.setMetaverseBuilder( mockBuilder );
    when( mockEntry.getEntry() ).thenReturn( mockJobEntryInterface );
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullAnalyze() throws MetaverseAnalyzerException {

    analyzer.analyze( mockDescriptor, null );

  }

  public void testAnalyze() throws MetaverseAnalyzerException {

    IMetaverseNode node = analyzer.analyze( mockDescriptor, mockJobEntryInterface );
    assertNotNull( node );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testSetMetaverseBuilderNull() throws MetaverseAnalyzerException {

    analyzer.setMetaverseBuilder( null );
    analyzer.analyze( mockDescriptor, mockJobEntryInterface );

  }

}
