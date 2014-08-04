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

import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.*;
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
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author mburgess
 */
@RunWith( MockitoJUnitRunner.class )
public class KettleBaseStepAnalyzerTest {

  KettleBaseStepAnalyzer analyzer;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  TransMeta mockTransMeta;

  @Mock
  protected BaseStepMeta mockStepMeta;

  @Mock
  protected StepMeta parentStepMeta;

  @Mock
  DatabaseMeta mockDatabaseMeta;

  @Mock
  StepMetaInterface mockStepMetaInterface;

  @Mock
  RowMetaInterface mockPrevFields;

  @Mock
  RowMetaInterface mockStepFields;

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

    analyzer = new KettleBaseStepAnalyzer() {
      @Override
      public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
        return null;
      }
    };
    analyzer.setMetaverseBuilder( mockBuilder );

    // set random StepMetaInterface
    when( mockStepMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    analyzer.parentStepMeta = parentStepMeta;
    analyzer.parentTransMeta = mockTransMeta;
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetMetaverseBuilder() {
    assertNotNull( analyzer.metaverseBuilder );
  }

  @Test
  public void testAddSelfNode() throws MetaverseAnalyzerException {
    assertNotNull( analyzer.addSelfNode() );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAddSelfNodeWithException() throws MetaverseAnalyzerException {
    analyzer.parentStepMeta = null;
    assertNotNull( analyzer.addSelfNode() );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void addDatabaseConnectionNodesWithNullStep() throws MetaverseAnalyzerException {
    analyzer.addDatabaseConnectionNodes();
  }

  @Test
  public void testGetDatabaseConnectionAnalyzer() {
    assertNotNull( analyzer.getDatabaseConnectionAnalyzer() );
  }

  @Test
  public void testAddDatabaseConnectionNodesNullDatabaseConnectionAnalyzer() {
    KettleBaseStepAnalyzer spyAnalyzer = spy( analyzer );
    when( spyAnalyzer.getDatabaseConnectionAnalyzer() ).thenReturn( null );
    assertNotNull( analyzer.getDatabaseConnectionAnalyzer() );
  }

  @Test
  public void testLoadInputAndOutputStreamFields() throws KettleStepException {
    when( analyzer.parentTransMeta.getPrevStepFields( analyzer.parentStepMeta ) ).thenReturn( mockPrevFields );
    when( analyzer.parentTransMeta.getStepFields( analyzer.parentStepMeta ) ).thenReturn( mockStepFields );
    analyzer.loadInputAndOutputStreamFields();
    assertNotNull( analyzer.prevFields );
    assertNotNull( analyzer.stepFields );
  }

  @Test
  public void testLoadInputAndOutputStreamFieldsWithException() throws KettleStepException {
    when( analyzer.parentTransMeta.getPrevStepFields( analyzer.parentStepMeta ) )
        .thenThrow( KettleStepException.class );
    when( analyzer.parentTransMeta.getStepFields( analyzer.parentStepMeta ) ).thenThrow( KettleStepException.class );
    analyzer.loadInputAndOutputStreamFields();
    assertNull( analyzer.prevFields );
    assertNull( analyzer.stepFields );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( null );
  }

  @Test
  public void testAnalyze() throws MetaverseAnalyzerException {
    assertNotNull( analyzer.analyze( mockStepMeta ) );
  }

  @Test
  public void testAnalyzeWithDatabaseMeta() throws MetaverseAnalyzerException {
    DatabaseMeta[] dbs = new DatabaseMeta[] { mockDatabaseMeta };
    when( mockStepMetaInterface.getUsedDatabaseConnections() ).thenReturn( dbs );
    when( mockStepMeta.getUsedDatabaseConnections() ).thenReturn( dbs );
    assertNotNull( analyzer.analyze( mockStepMeta ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testSetMetaverseBuilderNull() throws MetaverseAnalyzerException {
    analyzer.setMetaverseBuilder( null );
    analyzer.analyze( mockStepMeta );
  }

  @Test
  public void testAnalyzeWithNewFields() throws MetaverseAnalyzerException, KettleStepException {

    when( mockTransMeta.getStepFields( mockStepMeta.getParentStepMeta() ) ).thenAnswer( new Answer<RowMetaInterface>() {

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

  @Test
  public void testAddCreatedFieldNodesWithNoFields() throws KettleStepException {
    when( mockTransMeta.getStepFields( parentStepMeta ) ).thenReturn( mockStepFields );
    when( mockStepFields.getValueMetaList() ).thenReturn( null );
    analyzer.addCreatedFieldNodes();
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentStepMeta() throws MetaverseAnalyzerException {
    when( mockStepMeta.getParentStepMeta() ).thenReturn( null );
    analyzer.analyze( mockStepMeta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullMetaverseObjectFactory() throws MetaverseAnalyzerException {
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( null );
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.analyze( mockStepMeta );
  }

  @Test
  public void testGetSupportedSteps() {
    assertNull( analyzer.getSupportedSteps() );
  }
}
