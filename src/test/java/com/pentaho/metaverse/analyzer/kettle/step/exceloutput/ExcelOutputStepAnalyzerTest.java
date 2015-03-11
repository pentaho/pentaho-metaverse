/*
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

package com.pentaho.metaverse.analyzer.kettle.step.exceloutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.exceloutput.ExcelField;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutput;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputData;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;

@RunWith( MockitoJUnitRunner.class )
public class ExcelOutputStepAnalyzerTest {

  private ExcelOutputStepAnalyzer analyzer;

  @Mock
  private StepMeta mockStepMeta;

  @Mock
  private ExcelOutput mockExcelOutput;

  @Mock
  private ExcelOutputMeta mockExcelOutputMeta;

  @Mock
  private ExcelOutputData mockExcelOutputData;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockRowMetaInterface;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private INamespace mockNamespace;

  @Mock
  private ExcelField mockField1;

  @Mock
  private ExcelField mockField2;

  private IMetaverseObjectFactory mockFactory;

  private IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    when( mockField1.getName() ).thenReturn( "Field 1" );
    when( mockField2.getName() ).thenReturn( "Field 2" );

    analyzer = new ExcelOutputStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );

    when( mockExcelOutput.getStepMetaInterface() ).thenReturn( mockExcelOutputMeta );
    when( mockExcelOutput.getStepDataInterface() ).thenReturn( mockExcelOutputData );
    when( mockExcelOutput.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockExcelOutputMeta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_nullInput() throws Exception {
    analyzer.analyze( null, null );
  }

  @Test
  public void testAnalyze_OutputSpecified() throws Exception {
    StepMeta meta = new StepMeta( "test", mockExcelOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockExcelOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( mockExcelOutputMeta.getFiles( any( VariableSpace.class ) ) ).thenReturn( new String[]{ "/tmp/out.xls" } );
    when( mockExcelOutputMeta.getOutputFields() ).thenReturn( new ExcelField[]{ } );

    when( mockBuilder.addNode( any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );
    when( mockBuilder.addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );

    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    IMetaverseNode result = analyzer.analyze( descriptor, mockExcelOutputMeta );

    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );
    verify( mockBuilder, times( 1 ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) );

    verify( mockBuilder, times( 0 ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_POPULATES ), any( IMetaverseNode.class ) );

    verify( mockBuilder, times( 0 ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze() throws Exception {
    StepMeta meta = new StepMeta( "test", mockExcelOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockExcelOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( mockExcelOutputMeta.getFiles( any( VariableSpace.class ) ) ).thenReturn( new String[]{ "/tmp/out.txt" } );

    ExcelField[] fields = new ExcelField[]{ mockField1, mockField2 };
    when( mockExcelOutputMeta.getOutputFields() ).thenReturn( fields );

    when( mockBuilder.addNode( any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );
    when( mockBuilder.addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );

    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[]{ "Field 1", "Field 2" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override
      public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "Field 1" ) {
          return new ValueMetaString( "Field 1" );
        }
        if ( args[0] == "Field 2" ) {
          return new ValueMetaString( "Field 2" );
        }
        return null;
      }
    } );

    IMetaverseNode result = analyzer.analyze( descriptor, mockExcelOutputMeta );

    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockBuilder, times( 2 + fields.length ) ).addNode( any( IMetaverseNode.class ) );
    verify( mockBuilder, times( 1 ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) );

    verify( mockBuilder, times( fields.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_POPULATES ), any( IMetaverseNode.class ) );

    verify( mockBuilder, times( fields.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetSupportedSteps() {
    ExcelOutputStepAnalyzer analyzer = new ExcelOutputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( ExcelOutputMeta.class ) );
  }

  @Test
  public void testExcelOutputExternalResourceConsumer() throws Exception {
    ExcelOutputExternalResourceConsumer consumer = new ExcelOutputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", mockExcelOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockExcelOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockExcelOutputMeta.getFileName() ).thenReturn( null );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( mockExcelOutputMeta.getFiles( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( mockExcelOutputMeta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( mockExcelOutputMeta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );


    when( mockExcelOutputMeta.getExtension() ).thenReturn( "xls" );

    assertFalse( consumer.getResourcesFromMeta( mockExcelOutputMeta ).isEmpty() );

    mockExcelOutputData.realFilename = "/path/to/row/file";
    when( mockExcelOutput.buildFilename() )
      .thenAnswer( new Answer<String>() {
        @Override
        public String answer( InvocationOnMock invocation ) throws Throwable {
          return ( mockExcelOutputData.realFilename + ".xls" );
        }
      } );

    resources = consumer.getResourcesFromRow( mockExcelOutput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    resources = consumer.getResourcesFromRow( mockExcelOutput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );

    assertEquals( ExcelOutputMeta.class, consumer.getMetaClass() );
  }
}
