/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse.api.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.AnalysisContext;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IConnectionAnalyzer;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import com.pentaho.metaverse.api.analyzer.kettle.DatabaseConnectionAnalyzer;
import com.pentaho.metaverse.api.testutils.MetaverseTestUtils;
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
import org.pentaho.di.core.ProgressMonitorListener;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
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

    when( analyzer.parentTransMeta.getPrevStepFields( eq( analyzer.parentStepMeta ),
      any( ProgressMonitorListener.class ) ) ).thenReturn( mockPrevFields );
    when( analyzer.parentTransMeta.getPrevStepNames( analyzer.parentStepMeta ) ).thenReturn( prevSteps );
    when( analyzer.parentTransMeta.getStepFields( eq( analyzer.parentStepMeta ),
      any( ProgressMonitorListener.class ) ) ).thenReturn( mockStepFields );
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void addConnectionNodesWithNullStep() throws MetaverseAnalyzerException {
    analyzer.addConnectionNodes( mockDescriptor );
  }

  @Test
  public void testAddConnectionNodesException() throws MetaverseAnalyzerException {
    analyzer.baseStepMeta = mock( BaseStepMeta.class );
    BaseStepAnalyzer mockAnalyzer = spy( analyzer );
    when( mockDescriptor.getContext() ).thenThrow( Exception.class );
    mockAnalyzer.addConnectionNodes( mockDescriptor );
  }

  @Test
  public void testLoadInputAndOutputStreamFields() throws KettleStepException {
    analyzer.loadInputAndOutputStreamFields( mockStepMeta );
    assertNotNull( analyzer.prevFields );
    assertNotNull( analyzer.stepFields );
  }

  @Test
  public void testLoadInputAndOutputStreamFieldsWithException() throws KettleStepException {
    when( analyzer.parentTransMeta.getPrevStepFields( eq( analyzer.parentStepMeta ),
      any( ProgressMonitorListener.class ) ) ).thenThrow( KettleStepException.class );
    when( analyzer.parentTransMeta.getStepFields( eq( analyzer.parentStepMeta ),
      any( ProgressMonitorListener.class ) ) ).thenThrow( KettleStepException.class );
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

    when( mockTransMeta.getPrevStepFields( eq( mockStepMeta.getParentStepMeta() ),
      any( ProgressMonitorListener.class ) ) )
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
  public void testAddCreatedFieldNodesWithException() throws KettleStepException {
    analyzer = new BaseStepAnalyzer() {

      {
        stepFields = mockStepFields;
      }

      @Override
      protected boolean fieldNameExistsInInput( String fieldName ) {
        throw new RuntimeException();
      }

      @Override
      public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
        return null;
      }
    };
    List<ValueMetaInterface> valueMetaList = new ArrayList<ValueMetaInterface>( 1 );
    valueMetaList.add( mock( ValueMetaInterface.class ) );
    when( mockStepFields.getValueMetaList() ).thenReturn( valueMetaList );
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
  public void testAddConnectionNodes() throws MetaverseAnalyzerException {
    DatabaseMeta[] dbs = new DatabaseMeta[]{ mockDatabaseMeta };
    when( mockStepMetaInterface.getUsedDatabaseConnections() ).thenReturn( dbs );
    when( mockStepMeta.getUsedDatabaseConnections() ).thenReturn( dbs );
    analyzer.baseStepMeta = mockStepMeta;
    DatabaseConnectionAnalyzer dbAnalyzer = new StepDatabaseConnectionAnalyzer();
    dbAnalyzer.setMetaverseBuilder( mockBuilder );
    analyzer.setConnectionAnalyzer( dbAnalyzer );
    analyzer.addConnectionNodes( mockDescriptor );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAddConnectionNodesNullStepMeta() throws MetaverseAnalyzerException {
    analyzer.addConnectionNodes( mockDescriptor );
  }

  @Test
  public void testProcessChangeRecordNullDescriptor() {
    when( changeRecord.hasDelta() ).thenReturn( true );
    analyzer.processFieldChangeRecord( null, mock( IMetaverseNode.class ), changeRecord );
  }

  @Test
  public void testProcessChangeRecordNullRecord() {
    analyzer.processFieldChangeRecord( mockDescriptor, mock( IMetaverseNode.class ), null );
  }

  @Test
  public void testProcessChangeRecord() {
    when( changeRecord.hasDelta() ).thenReturn( true );
    when( changeRecord.getChangedEntityName() ).thenReturn( "myField" );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );
    when( rootNode.getLogicalId() ).thenReturn( "{}" );
    analyzer.rootNode = rootNode;

    analyzer.processFieldChangeRecord( mockDescriptor, mock( IMetaverseNode.class ), changeRecord );
  }

  @Test
  public void testGetPrevStepFieldOriginDescriptorNullDescriptor() {
    assertNull( analyzer.getPrevStepFieldOriginDescriptor( null, "Name" ) );
    // Call protected version
    assertNull( analyzer.getPrevStepFieldOriginDescriptor( null, "Name", mock( RowMetaInterface.class ) ) );
    assertNull( analyzer.getPrevStepFieldOriginDescriptor( mock( IComponentDescriptor.class ), "Name", null ) );
  }

  @Test
  public void testGetPrevStepFieldOriginDescriptor() {
    when( mockPrevFields.getFieldNames() ).thenReturn( new String[]{ "field1", "field2" } );
    analyzer.prevFields = Collections.singletonMap( "previous step name", mockPrevFields );
    analyzer.getPrevStepFieldOriginDescriptor( mockDescriptor, "field1" );
  }

  @Test
  public void testGetStepFieldOriginDescriptorNullDescriptor() throws Exception {
    assertNull( analyzer.getStepFieldOriginDescriptor( null, "Name" ) );
  }

  @Test
  public void testGetStepFieldOriginDescriptor() throws Exception {
    when( mockStepFields.getFieldNames() ).thenReturn( new String[]{ "field1", "field2" } );
    analyzer.stepFields = mockStepFields;
    IMetaverseNode rootNode = mock( IMetaverseNode.class );
    when( rootNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) ).thenReturn( "{}" );
    analyzer.rootNode = rootNode;
    assertNotNull( analyzer.getStepFieldOriginDescriptor( mockDescriptor, "field1" ) );
  }

  @Test
  public void testGetFieldMappings() throws Exception {
    assertNull( analyzer.getFieldMappings( mockStepMeta ) );
  }

  @Test
  public void testGetInputFieldsWithException() {
    analyzer = new BaseStepAnalyzer() {

      @Override
      public void validateState( IComponentDescriptor descriptor, BaseStepMeta object ) throws MetaverseAnalyzerException {
        throw new MetaverseAnalyzerException( "expected exception" );
      }

      @Override
      public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
        return null;
      }
    };
    assertNull( analyzer.getInputFields( null ) );
  }

  @Test
  public void testGetOutputFieldsWithException() {
    analyzer = new BaseStepAnalyzer() {

      @Override
      public void validateState( IComponentDescriptor descriptor, BaseStepMeta object ) throws MetaverseAnalyzerException {
        throw new MetaverseAnalyzerException( "expected exception" );
      }

      @Override
      public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
        return null;
      }
    };
    assertNull( analyzer.getOutputFields( null ) );
  }

  @Test
  public void testGetPassthruFieldMappings() throws Exception {
    assertTrue( analyzer.getPassthruFieldMappings( mockStepMeta ).isEmpty() );
    when( mockPrevFields.getFieldNames() ).thenReturn( new String[]{ "field1", "field2" } );
    assertEquals( 2, analyzer.getPassthruFieldMappings( mockStepMeta ).size() );
  }

  @Test
  public void testGetSetConnectionAnalyzer() throws Exception {
    assertNull( analyzer.getConnectionAnalyzer() );
    IConnectionAnalyzer connectionAnalyzer = mock( IConnectionAnalyzer.class );
    analyzer.setConnectionAnalyzer( connectionAnalyzer );
    assertEquals( connectionAnalyzer, analyzer.getConnectionAnalyzer() );
  }

  @Test
  public void testGetSupportedSteps() {
    assertNull( analyzer.getSupportedSteps() );
  }

  @Test
  public void testGetChangeRecords() throws Exception {
    assertNull( analyzer.getChangeRecords( mockStepMeta ) );
  }

  @Test
  public void testCreateFieldNode() {
    IMetaverseNode rootNode = mock( IMetaverseNode.class );
    when( rootNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) ).thenReturn( "{}" );
    analyzer.rootNode = rootNode;

    final ValueMetaInterface vmi = new ValueMetaInteger( "testInt" );
    analyzer.createFieldNode( mockDescriptor.getContext(), vmi );
  }

  @Test
  public void testFieldNameExistsInInput() {
    when( mockPrevFields.getFieldNames() ).thenReturn( new String[]{ "field1" } );
    when( mockPrevFields.searchValueMeta( anyString() ) ).thenAnswer(
      new Answer<ValueMetaInterface>() {

        @Override
        public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
          Object[] args = invocation.getArguments();
          String fieldName = (String) args[0];
          if ( "field1".equals( fieldName ) ) {
            return new ValueMetaString( "field1" );
          }
          return null;
        }
      } );
    analyzer.prevFields = Collections.singletonMap( "field1", mockPrevFields );

    assertFalse( analyzer.fieldNameExistsInInput( "noField" ) );
    assertTrue( analyzer.fieldNameExistsInInput( "field1" ) );
  }
}
