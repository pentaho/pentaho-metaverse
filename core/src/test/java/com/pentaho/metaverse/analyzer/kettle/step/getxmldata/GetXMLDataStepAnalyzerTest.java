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

package com.pentaho.metaverse.analyzer.kettle.step.getxmldata;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.getxmldata.GetXMLData;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataField;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataMeta;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by mburgess on 4/24/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class GetXMLDataStepAnalyzerTest {

  private GetXMLDataStepAnalyzer getXMLDataStepAnalyzer;

  @Mock
  private GetXMLData mockGetXMLData;

  @Mock
  private GetXMLDataMeta mockGetXMLDataMeta;

  @Mock
  private StepMeta mockStepMeta;

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

    getXMLDataStepAnalyzer = new GetXMLDataStepAnalyzer();
    getXMLDataStepAnalyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_JOB, mockNamespace );

    when( mockGetXMLData.getStepMetaInterface() ).thenReturn( mockGetXMLDataMeta );
    when( mockGetXMLData.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockGetXMLDataMeta );

  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_nullInput() throws Exception {
    getXMLDataStepAnalyzer.analyze( null, null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockGetXMLDataMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyTextInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockGetXMLDataMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockGetXMLDataMeta.getFileName() ).thenReturn( fileNames );

    IMetaverseNode result = getXMLDataStepAnalyzer.analyze( descriptor, mockGetXMLDataMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockGetXMLDataMeta, times( 1 ) ).getFileName();

    // make sure the step node is added as well as the file node
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there is a "readby" link added
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_Fields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockGetXMLDataMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyTextInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockGetXMLDataMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockGetXMLDataMeta.getFileName() ).thenReturn( fileNames );

    // set up the input fields
    GetXMLDataField field1 = new GetXMLDataField( "id" );
    GetXMLDataField field2 = new GetXMLDataField( "name" );
    GetXMLDataField[] inputFields = new GetXMLDataField[]{ field1, field2 };

    when( mockGetXMLDataMeta.getInputFields() ).thenReturn( inputFields );
    when( mockTransMeta.getStepFields( eq( spyMeta ), Mockito.any( ProgressMonitorListener.class ) ) )
      .thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[]{ "id", "name" } );
    when( mockRowMetaInterface.searchValueMeta( anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

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

    IMetaverseNode result = getXMLDataStepAnalyzer.analyze( descriptor, mockGetXMLDataMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockGetXMLDataMeta, times( 1 ) ).getFileName();

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

    StepMeta meta = new StepMeta( "test", mockGetXMLDataMeta );
    StepMeta spyMeta = spy( meta );

    when( mockGetXMLDataMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockGetXMLDataMeta.getFileName() ).thenReturn( null );
    when( mockGetXMLDataMeta.isInFields() ).thenReturn( true );
    when( mockGetXMLDataMeta.getIsAFile() ).thenReturn( true );
    when( mockGetXMLDataMeta.isReadUrl() ).thenReturn( false );
    when( mockGetXMLDataMeta.getXMLField() ).thenReturn( "inField" );

    // set up the input fields
    GetXMLDataField field1 = new GetXMLDataField( "id" );
    GetXMLDataField field2 = new GetXMLDataField( "name" );
    GetXMLDataField[] inputFields = new GetXMLDataField[]{ field1, field2 };

    when( mockGetXMLDataMeta.getInputFields() ).thenReturn( inputFields );
    when( mockTransMeta.getStepFields( eq( spyMeta ), Mockito.any( ProgressMonitorListener.class ) ) )
      .thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[]{ "id", "name" } );
    when( mockRowMetaInterface.searchValueMeta( anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override
      public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        ValueMetaInterface vmi = null;
        if ( args[0] == "id" ) {
          vmi = new ValueMetaString( "id" );
          vmi.setOrigin( "test" );
        }
        if ( args[0] == "name" ) {
          vmi = new ValueMetaString( "name" );
          vmi.setOrigin( "test" );
        }
        return vmi;
      }
    } );

    RowMetaInterface mockPrevRowMeta = mock( RowMetaInterface.class );
    when( mockPrevRowMeta.getFieldNames() ).thenReturn( new String[]{ "inField" } );
    when( mockPrevRowMeta.searchValueMeta( anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override
      public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "inField" ) {
          return new ValueMetaString( "inField" );
        }
        return null;
      }
    } );
    when( mockTransMeta.getPrevStepFields( eq( spyMeta ), Mockito.any( ProgressMonitorListener.class ) ) )
      .thenReturn( mockPrevRowMeta );
    when( mockTransMeta.getPrevStepNames( spyMeta ) ).thenReturn( new String[]{ "prevStep" } );

    IMetaverseNode result = getXMLDataStepAnalyzer.analyze( descriptor, mockGetXMLDataMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockGetXMLDataMeta, never() ).getFileName();
    verify( mockGetXMLDataMeta, times( 1 ) ).isInFields();
    verify( mockGetXMLDataMeta, times( 1 ) ).getXMLField();
    verify( mockGetXMLDataMeta, times( 1 ) ).isReadUrl();

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
    GetXMLDataStepAnalyzer analyzer = new GetXMLDataStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( GetXMLDataMeta.class ) );
  }

  @Test
  public void testGetXMLDataExternalResourceConsumer() throws Exception {
    GetXMLDataExternalResourceConsumer consumer = new GetXMLDataExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", mockGetXMLDataMeta );
    StepMeta spyMeta = spy( meta );

    when( mockGetXMLDataMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );


    when( mockGetXMLDataMeta.isInFields() ).thenReturn( false );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( mockGetXMLDataMeta.getFileName() ).thenReturn( filePaths );
    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( mockGetXMLDataMeta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( mockGetXMLDataMeta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );


    when( mockGetXMLDataMeta.isInFields() ).thenReturn( true );
    when( mockGetXMLDataMeta.getIsAFile() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( mockGetXMLDataMeta ) );
    assertTrue( consumer.getResourcesFromMeta( mockGetXMLDataMeta ).isEmpty() );
    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), anyString(), anyString() ) )
      .thenReturn( "/path/to/row/file" );
    resources = consumer.getResourcesFromRow( mockGetXMLData, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), anyString(), anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockGetXMLData, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( GetXMLDataMeta.class, consumer.getMetaClass() );
  }

}
