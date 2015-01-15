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

package com.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.kettle.IFieldMapping;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author mburgess
 */
@RunWith(MockitoJUnitRunner.class)
public class TableOutputStepAnalyzerTest {

  private TableOutputStepAnalyzer analyzer;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private TableOutputMeta mockTableOutputMeta;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockRowMetaInterface;

  @Mock
  DatabaseMeta mockDatabaseMeta;

  @Mock
  private INamespace mockNamespace;

  IComponentDescriptor descriptor;

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
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    when( mockNamespace.getNamespaceId() ).thenReturn( "namespaceID" );

    analyzer = new TableOutputStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );
    descriptor = spy( new MetaverseComponentDescriptor( "name", DictionaryConst.NODE_TYPE_TRANS, mockNamespace ) );
    when( descriptor.getParentNamespace() ).thenReturn( mockNamespace );
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullAnalyze() throws MetaverseAnalyzerException {

    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyzeTableOutputMetaDehydrated() throws MetaverseAnalyzerException, KettleStepException {

    StepMeta meta = new StepMeta( "test", mockTableOutputMeta );
    StepMeta spyMeta = spy( meta );

    // minimum mocking needed to not throw an exception
    when( mockTableOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    assertNotNull( analyzer.analyze( descriptor, mockTableOutputMeta ) );
  }

  @Test
  public void testAnalyzeTableOutputMetaHydrated() throws MetaverseAnalyzerException, KettleStepException {

    StepMeta meta = new StepMeta( "test", mockTableOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockTableOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    // additional hydration needed to get test lines code coverage
    when( mockDatabaseMeta.getDatabaseName() ).thenReturn( "testDatabase" );
    when( mockDatabaseMeta.getAccessTypeDesc() ).thenReturn( "Native" );
    when( mockTableOutputMeta.getDatabaseMeta() ).thenReturn( mockDatabaseMeta );
    when( mockTableOutputMeta.getTableName() ).thenReturn( "testTable" );
    when( mockTransMeta.getPrevStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[] { "test1", "test2" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "test1" )
          return new ValueMetaString( "test1" );
        if ( args[0] == "test2" )
          return new ValueMetaString( "test2" );
        return null;
      }
    } );

    assertNotNull( analyzer.analyze( descriptor, mockTableOutputMeta ) );
  }

  @Test
  public void testAnalyzeWithDbNodes() throws Exception {
    StepMeta meta = new StepMeta( "test", mockTableOutputMeta );
    StepMeta spyMeta = spy( meta );
    TableOutputStepAnalyzer spy = spy( analyzer );

    Map<String, IMetaverseNode> dbNodes = new HashMap<String, IMetaverseNode>();
    IMetaverseNode dbNode = mock( IMetaverseNode.class );
    dbNodes.put( "MyConnection", dbNode );

    when( spy.getDatabaseNodes() ).thenReturn( dbNodes );

    when( mockTableOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    // additional hydration needed to get test lines code coverage
    when( mockDatabaseMeta.getDatabaseName() ).thenReturn( "testDatabase" );
    when( mockDatabaseMeta.getName() ).thenReturn( "MyConnection" );
    when( mockDatabaseMeta.getAccessTypeDesc() ).thenReturn( "Native" );
    when( mockTableOutputMeta.getDatabaseMeta() ).thenReturn( mockDatabaseMeta );
    when( mockTableOutputMeta.getTableName() ).thenReturn( "testTable" );
    when( mockTableOutputMeta.getFieldStream() ).thenReturn(  new String[] { "test1", "test2" }  );
    when( mockTableOutputMeta.specifyFields() ).thenReturn( true );
    when( mockTransMeta.getPrevStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockTransMeta.getPrevStepNames( spyMeta ) ).thenReturn( new String[] { "prev step name" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[ 0 ] == "test1" )
          return new ValueMetaString( "test1" );
        if ( args[ 0 ] == "test2" )
          return new ValueMetaString( "test2" );
        return null;
      }
    } );
    assertNotNull( spy.analyze( descriptor, mockTableOutputMeta ) );

  }

  @Test
  public void testAnalyzeWithNoDbNodes() throws Exception {
    StepMeta meta = new StepMeta( "test", mockTableOutputMeta );
    StepMeta spyMeta = spy( meta );
    TableOutputStepAnalyzer spy = spy( analyzer );

    when( spy.getDatabaseNodes() ).thenReturn( null );

    when( mockTableOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    assertNotNull( spy.analyze( descriptor, mockTableOutputMeta ) );

  }

  @Test
  public void testGetSupportedSteps() {
    TableOutputStepAnalyzer analyzer = new TableOutputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TableOutputMeta.class ) );
  }

  @Test
  public void testGetFieldMappings() throws Exception {
    String[] streamFields = { "name", "age", "date of birth" };
    String[] tableFields = { "name", "age", "DOB" };

    when( mockTableOutputMeta.specifyFields() ).thenReturn( true );
    when( mockTableOutputMeta.getFieldStream() ).thenReturn( streamFields );
    when( mockTableOutputMeta.getFieldDatabase() ).thenReturn( tableFields );

    Set<IFieldMapping> mappings = analyzer.getFieldMappings( mockTableOutputMeta );
    assertEquals( 3, mappings.size() );
    for ( IFieldMapping mapping : mappings ) {
      if ( mapping.getSourceFieldName().equalsIgnoreCase( "date of birth" ) ) {
        assertEquals( "DOB", mapping.getTargetFieldName() );
      } else {
        // otherwise they are the same name
        assertEquals( mapping.getSourceFieldName(), mapping.getTargetFieldName() );
      }
    }
  }

  @Test
  public void testGetFieldMappings_fieldsNoSpecified() throws Exception {
    StepMeta meta = new StepMeta( "test", mockTableOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockTableOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getPrevStepNames( spyMeta ) ).thenReturn( new String[]{"prev step name"} );
    // additional hydration needed to get test lines code coverage
    when( mockDatabaseMeta.getDatabaseName() ).thenReturn( "testDatabase" );
    when( mockDatabaseMeta.getAccessTypeDesc() ).thenReturn( "Native" );
    when( mockTableOutputMeta.getDatabaseMeta() ).thenReturn( mockDatabaseMeta );
    when( mockTableOutputMeta.getTableName() ).thenReturn( "testTable" );
    when( mockTransMeta.getPrevStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[] { "test1", "test2" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "test1" )
          return new ValueMetaString( "test1" );
        if ( args[0] == "test2" )
          return new ValueMetaString( "test2" );
        return null;
      }
    } );

    when( mockTableOutputMeta.specifyFields() ).thenReturn( false );

    Set<IFieldMapping> mappings = analyzer.getFieldMappings( mockTableOutputMeta );
    assertEquals( 2, mappings.size() );
    for ( IFieldMapping mapping : mappings ) {
      assertEquals( mapping.getSourceFieldName(), mapping.getTargetFieldName() );
    }
  }
}
