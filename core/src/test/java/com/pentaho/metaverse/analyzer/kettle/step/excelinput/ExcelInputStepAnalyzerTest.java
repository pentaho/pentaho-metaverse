/*
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

package com.pentaho.metaverse.analyzer.kettle.step.excelinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.excelinput.ExcelInput;
import org.pentaho.di.trans.steps.excelinput.ExcelInputField;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;

@RunWith( MockitoJUnitRunner.class )
public class ExcelInputStepAnalyzerTest {

  private ExcelInputStepAnalyzer excelInputStepAnalyzer;

  @Mock
  private ExcelInput mockExcelInput;

  @Mock
  private ExcelInputMeta mockExcelInputMeta;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockRowMetaInterface;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private INamespace mockNamespace;

  private IMetaverseObjectFactory mockFactory;

  private IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {

    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    excelInputStepAnalyzer = new ExcelInputStepAnalyzer();
    excelInputStepAnalyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_JOB, mockNamespace );

    when( mockExcelInput.getStepMetaInterface() ).thenReturn( mockExcelInputMeta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_nullInput() throws Exception {
    excelInputStepAnalyzer.analyze( null, null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockExcelInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyExcelInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockExcelInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockExcelInputMeta.getFileName() ).thenReturn( fileNames );

    IMetaverseNode result = excelInputStepAnalyzer.analyze( descriptor, mockExcelInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockExcelInputMeta, times( 1 ) ).getFileName();

    // make sure the step node is added as well as the file node
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there is a "readby" link added
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_Fields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockExcelInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyExcelInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockExcelInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockExcelInputMeta.getFileName() ).thenReturn( fileNames );

    // set up the input fields
    ExcelInputField field1 = new ExcelInputField( "id", 0, 4 );
    ExcelInputField field2 = new ExcelInputField( "name", 1, 30 );
    ExcelInputField[] inputFields = new ExcelInputField[]{ field1, field2 };

    when( mockExcelInputMeta.getField() ).thenReturn( inputFields );
    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[]{ "id", "name" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override
      public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "id" ) {
          return new ValueMetaString( "id" );
        }
        if ( args[0] == "name" ) {
          return new ValueMetaString( "name" );
        }
        return null;
      }
    } );

    IMetaverseNode result = excelInputStepAnalyzer.analyze( descriptor, mockExcelInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockExcelInputMeta, times( 1 ) ).getFileName();

    // make sure the step node, the file node, and the field nodes
    verify( mockBuilder, times( 2 + inputFields.length ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there are "readby" and "uses" links added (file, and each field)
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );
    verify( mockBuilder, times( inputFields.length ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

    // we should have "populates" links from input nodes to output nodes
    verify( mockBuilder, times( inputFields.length ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_POPULATES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_FilenamesFromField() throws Exception {

    StepMeta meta = new StepMeta( "test", mockExcelInputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockExcelInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockExcelInputMeta.getFileName() ).thenReturn( null );
    when( mockExcelInputMeta.isAcceptingFilenames() ).thenReturn( true );
    when( mockExcelInputMeta.getAcceptingField() ).thenReturn( "inField" );

    // set up the input fields
    ExcelInputField field1 = new ExcelInputField( "id", 0, 4 );
    ExcelInputField field2 = new ExcelInputField( "name", 1, 30 );
    ExcelInputField[] inputFields = new ExcelInputField[]{ field1, field2 };

    when( mockExcelInputMeta.getField() ).thenReturn( inputFields );
    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[]{ "id", "name" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override
      public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "id" ) {
          return new ValueMetaString( "id" );
        }
        if ( args[0] == "name" ) {
          return new ValueMetaString( "name" );
        }
        return null;
      }
    } );

    RowMetaInterface mockPrevRowMeta = mock( RowMetaInterface.class );
    when( mockPrevRowMeta.getFieldNames() ).thenReturn( new String[]{ "inField" } );
    when( mockPrevRowMeta.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override
      public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "inField" ) {
          return new ValueMetaString( "inField" );
        }
        return null;
      }
    } );
    when( mockTransMeta.getPrevStepFields( spyMeta ) ).thenReturn( mockPrevRowMeta );

    IMetaverseNode result = excelInputStepAnalyzer.analyze( descriptor, mockExcelInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockExcelInputMeta, never() ).getFileName();
    verify( mockExcelInputMeta, times( 1 ) ).isAcceptingFilenames();
    verify( mockExcelInputMeta, times( 1 ) ).getAcceptingField();

    // make sure the step node and the field nodes have been added, but NOT the incoming stream field
    verify( mockBuilder, times( 1 + inputFields.length ) ).addNode( any( IMetaverseNode.class ) );

    // Verify there are no READBY links (usually from file to step)
    verify( mockBuilder, never() ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

    // make sure there "uses" links added (each field, and the filename stream field)
    verify( mockBuilder, times( inputFields.length + 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

    // we should have "populates" links from input nodes to output nodes
    verify( mockBuilder, times( inputFields.length ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_POPULATES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetSupportedSteps() {
    ExcelInputStepAnalyzer analyzer = new ExcelInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( ExcelInputMeta.class ) );
  }

  @Test
  public void testExcelInputExternalResourceConsumer() throws Exception {
    ExcelInputExternalResourceConsumer consumer = new ExcelInputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", mockExcelInputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockExcelInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockExcelInputMeta.getFileName() ).thenReturn( null );
    when( mockExcelInputMeta.isAcceptingFilenames() ).thenReturn( false );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( mockExcelInputMeta.getFilePaths( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( mockExcelInputMeta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( mockExcelInputMeta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );


    when( mockExcelInputMeta.isAcceptingFilenames() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( mockExcelInputMeta ) );
    assertTrue( consumer.getResourcesFromMeta( mockExcelInputMeta ).isEmpty() );
    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenReturn( "/path/to/row/file" );
    resources = consumer.getResourcesFromRow( mockExcelInput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockExcelInput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( ExcelInputMeta.class, consumer.getMetaClass() );
  }
}
