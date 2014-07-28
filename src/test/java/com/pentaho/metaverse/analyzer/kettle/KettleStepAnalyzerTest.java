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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import com.pentaho.metaverse.testutils.MetaverseTestUtils;

/**
 * @author mburgess
 */
@RunWith(MockitoJUnitRunner.class)
public class KettleStepAnalyzerTest {

  KettleStepAnalyzer analyzer;

  @Mock
  private IMetaverseBuilder mockBuilder;

  private IMetaverseObjectFactory factory;

  @Mock
  TransMeta mockTransMeta;

  @Mock
  private StepMeta mockStepMeta;

  @Mock
  DatabaseMeta mockDatabaseMeta;

  @Mock
  StepMetaInterface mockStepMetaInterface;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {

    factory = MetaverseTestUtils.getMetaverseObjectFactory();

    analyzer = new KettleStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.setMetaverseObjectFactory( factory );

    // set random StepMetaInterface
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockStepMetaInterface );
    when( mockStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetMetaverseBuilder() {

    assertNotNull( analyzer.metaverseBuilder );

  }

  @Test
  public void testSetMetaverseObjectFactory() {

    assertNotNull( analyzer.metaverseObjectFactory );

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullAnalyze() throws MetaverseAnalyzerException {

    analyzer.analyze( null );

  }

  @Test
  public void testAnalyze() throws MetaverseAnalyzerException {

    IMetaverseNode node = analyzer.analyze( mockStepMeta );
    assertNotNull( node );

  }

  @Test
  public void testAnalyzeWithDatabaseMeta() throws MetaverseAnalyzerException {

    when( mockStepMetaInterface.getUsedDatabaseConnections() ).thenReturn( new DatabaseMeta[] { mockDatabaseMeta } );

    IMetaverseNode node = analyzer.analyze( mockStepMeta );
    assertNotNull( node );

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testAnalyzeNullStepMetaInterface() throws MetaverseAnalyzerException {

    when( mockStepMeta.getStepMetaInterface() ).thenReturn( null );
    analyzer.analyze( mockStepMeta );

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testSetMetaverseBuilderNull() throws MetaverseAnalyzerException {

    analyzer.setMetaverseBuilder( null );
    analyzer.analyze( mockStepMeta );

  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testSetObjectFactoryNull() throws MetaverseAnalyzerException {

    analyzer.setMetaverseObjectFactory( null );
    analyzer.analyze( mockStepMeta );

  }

  @Test
  public void testAnalyzeWithNewFields() throws MetaverseAnalyzerException, KettleStepException {

    when( mockTransMeta.getStepFields( mockStepMeta ) ).thenAnswer( new Answer<RowMetaInterface>() {

      @Override
      public RowMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        RowMeta rowMeta = new RowMeta();
        rowMeta.addValueMeta( new ValueMetaInteger( "testInt" ) );
        rowMeta.addValueMeta( new ValueMetaString( "testString" ) );
        return rowMeta;
      }
    } );
    IMetaverseNode node = analyzer.analyze( mockStepMeta );
    assertNotNull( node );

  }
}
