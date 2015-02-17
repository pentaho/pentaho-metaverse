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

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.analyzer.kettle.DatabaseConnectionAnalyzerProvider;
import com.pentaho.metaverse.impl.AnalysisContext;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author mburgess
 */
@RunWith( MockitoJUnitRunner.class )
public class BaseStepAnalyzerTest {

  BaseStepAnalyzer analyzer;

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

  @Mock
  INamespace namespace;

  @Mock
  IComponentDescriptor mockDescriptor;

  @Mock
  ComponentDerivationRecord changeRecord;

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

    analyzer = new BaseStepAnalyzer() {
      @Override
      public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
        return null;
      }
    };
    analyzer.setMetaverseBuilder( mockBuilder );
    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( mockDescriptor.getNamespace() ).thenReturn( namespace );
    when( mockDescriptor.getParentNamespace() ).thenReturn( namespace );
    when( mockDescriptor.getNamespaceId() ).thenReturn( "namespace" );
    when( mockDescriptor.getContext() ).thenReturn( new AnalysisContext( DictionaryConst.CONTEXT_DEFAULT, null ) );

    when( mockStepMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( parentStepMeta.getName() ).thenReturn( "parentStepMeta" );

    analyzer.parentStepMeta = parentStepMeta;
    analyzer.parentTransMeta = mockTransMeta;

    String[] prevSteps = { "previous step name" };

    when( analyzer.parentTransMeta.getPrevStepFields( analyzer.parentStepMeta ) ).thenReturn( mockPrevFields );
    when( analyzer.parentTransMeta.getPrevStepNames( analyzer.parentStepMeta ) ).thenReturn( prevSteps );
    when( analyzer.parentTransMeta.getStepFields( analyzer.parentStepMeta ) ).thenReturn( mockStepFields );
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void addDatabaseConnectionNodesWithNullStep() throws MetaverseAnalyzerException {
    analyzer.addDatabaseConnectionNodes( mockDescriptor );
  }

  @Test
  public void testAddDatabaseConnectionNodesException() throws MetaverseAnalyzerException {
    analyzer.baseStepMeta = mock( BaseStepMeta.class );
    BaseStepAnalyzer mockAnalyzer = spy( analyzer );
    when( mockDescriptor.getContext() ).thenThrow( Exception.class );
    mockAnalyzer.addDatabaseConnectionNodes( mockDescriptor );
  }

  @Test
  public void testLoadInputAndOutputStreamFields() throws KettleStepException {
    analyzer.loadInputAndOutputStreamFields( mockStepMeta );
    assertNotNull( analyzer.prevFields );
    assertNotNull( analyzer.stepFields );
  }

  @Test
  public void testLoadInputAndOutputStreamFieldsWithException() throws KettleStepException {
    when( analyzer.parentTransMeta.getPrevStepFields( analyzer.parentStepMeta ) )
      .thenThrow( KettleStepException.class );
    when( analyzer.parentTransMeta.getStepFields( analyzer.parentStepMeta ) ).thenThrow( KettleStepException.class );
    analyzer.loadInputAndOutputStreamFields( mockStepMeta );
    assertNull( analyzer.prevFields );
    assertNull( analyzer.stepFields );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( mockDescriptor, null );
  }

  @Test
  public void testAnalyze() throws MetaverseAnalyzerException {
    assertNotNull( analyzer.analyze( mockDescriptor, mockStepMeta ) );
  }

  @Test
  public void testAnalyzeWithDatabaseMeta() throws MetaverseAnalyzerException {
    DatabaseMeta[] dbs = new DatabaseMeta[]{ mockDatabaseMeta };
    when( mockStepMetaInterface.getUsedDatabaseConnections() ).thenReturn( dbs );
    when( mockStepMeta.getUsedDatabaseConnections() ).thenReturn( dbs );
    assertNotNull( analyzer.analyze( mockDescriptor, mockStepMeta ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testSetMetaverseBuilderNull() throws MetaverseAnalyzerException {
    analyzer.setMetaverseBuilder( null );
    analyzer.analyze( mockDescriptor, mockStepMeta );
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
    IMetaverseNode node = analyzer.analyze( mockDescriptor, mockStepMeta );
    assertNotNull( node );
  }

  @Test
  public void testAnalyzeWithDeletedFields() throws MetaverseAnalyzerException, KettleStepException {

    final ValueMetaInterface vmi = new ValueMetaInteger( "testInt" );
    vmi.setOrigin( "SomeoneElse" );

    when( mockTransMeta.getPrevStepFields( mockStepMeta.getParentStepMeta() ) )
      .thenAnswer( new Answer<RowMetaInterface>() {

        @Override
        public RowMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
          Object[] args = invocation.getArguments();
          RowMeta rowMeta = new RowMeta();
          rowMeta.addValueMeta( vmi );
          return rowMeta;
        }
      } );
    when( mockStepFields.getFieldNames() ).thenReturn( new String[]{ "outfield" } );
    IMetaverseNode node = analyzer.analyze( mockDescriptor, mockStepMeta );
    assertNotNull( node );

    // make sure there is a "deletes" link added
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DELETES ), any( IMetaverseNode.class ) );

    // make sure there is not a "derives" link added
    verify( mockBuilder, never() ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testAddCreatedFieldNodesWithNoFields() throws KettleStepException {
    when( mockStepFields.getValueMetaList() ).thenReturn( null );
    analyzer.addCreatedFieldNodes( mockDescriptor );
  }

  @Test
  public void testAddDeletedFieldLinksWithNoFields() throws KettleStepException {
    when( mockStepFields.getValueMetaList() ).thenReturn( null );
    analyzer.addDeletedFieldLinks( mockDescriptor );
  }

  @Test
  public void testAddDeletedFieldLinks() throws KettleStepException {
    when( mockStepFields.getValueMetaList() ).thenReturn( null );
    analyzer.addDeletedFieldLinks( mockDescriptor );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentStepMeta() throws MetaverseAnalyzerException {
    when( mockStepMeta.getParentStepMeta() ).thenReturn( null );
    analyzer.analyze( mockDescriptor, mockStepMeta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullMetaverseObjectFactory() throws MetaverseAnalyzerException {
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( null );
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.analyze( mockDescriptor, mockStepMeta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyzeWithNullParentTransMeta() throws MetaverseAnalyzerException {
    when( parentStepMeta.getParentTransMeta() ).thenReturn( null );
    analyzer.analyze( mockDescriptor, mockStepMeta );
  }

  @Test
  public void testAddDatabaseConnectionNodes() throws MetaverseAnalyzerException {
    DatabaseMeta[] dbs = new DatabaseMeta[]{ mockDatabaseMeta };
    when( mockStepMetaInterface.getUsedDatabaseConnections() ).thenReturn( dbs );
    when( mockStepMeta.getUsedDatabaseConnections() ).thenReturn( dbs );
    analyzer.setDatabaseConnectionAnalyzerProvider( new DatabaseConnectionAnalyzerProvider() );
    analyzer.baseStepMeta = mockStepMeta;
    analyzer.addDatabaseConnectionNodes( mockDescriptor );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAddDatabaseConnectionNodesNullStepMeta() throws MetaverseAnalyzerException {
    analyzer.addDatabaseConnectionNodes( mockDescriptor );
  }

  @Test
  public void testSetDatabaseConnectionAnalyzerProvider() {
    analyzer.setDatabaseConnectionAnalyzerProvider( new DatabaseConnectionAnalyzerProvider() );
    assertNotNull( analyzer.getDatabaseConnectionAnalyzers() );
  }

  @Test
  public void testProcessChangeRecordNullDescriptor() {
    when( changeRecord.hasDelta() ).thenReturn( true );
    analyzer.processFieldChangeRecord( null, mock( IMetaverseNode.class ), changeRecord );
//    verify( mockDescriptor, never() ).getChildNamespace( anyString(), anyString() );
  }

  @Test
  public void testProcessChangeRecordNullRecord() {
    analyzer.processFieldChangeRecord( mockDescriptor, mock( IMetaverseNode.class ), null );
  }

  @Test
  public void testGetPrevStepFieldOriginDescriptorNullDescriptor() {
    assertNull( analyzer.getPrevStepFieldOriginDescriptor( null, "Name" ) );
  }

  @Test
  public void testGetStepFieldOriginDescriptorNullDescriptor() {
    assertNull( analyzer.getStepFieldOriginDescriptor( null, "Name" ) );
  }

  @Test
  public void testGetFieldMappings() throws Exception {
    assertNull( analyzer.getFieldMappings( mockStepMeta ) );
  }

  @Test
  public void testGetSupportedSteps() {
    assertNull( analyzer.getSupportedSteps() );
  }
}
