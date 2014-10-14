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

package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
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
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: RFellows Date: 9/22/14
 */
@RunWith(MockitoJUnitRunner.class)
public class TextFileOutputStepAnalyzerTest {

  private TextFileOutputStepAnalyzer analyzer;

  @Mock
  private TextFileOutputMeta mockTextFileOutputMeta;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockRowMetaInterface;

  @Mock
  private IMetaverseBuilder mockBuilder;

  @Mock
  private INamespace mockNamespace;

  @Mock
  private TextFileField mockField1;

  @Mock
  private TextFileField mockField2;

  private IMetaverseObjectFactory mockFactory;

  private IMetaverseComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
//    when( mockNamespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( mockNamespace );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    when( mockField1.getName() ).thenReturn( "Field 1" );
    when( mockField2.getName() ).thenReturn( "Field 2" );

    analyzer = new TextFileOutputStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testAnalyze_nullInput() throws Exception {
    analyzer.analyze( null, null );
  }

  @Test
  public void testAnalyze_NoFields() throws Exception {
    StepMeta meta = new StepMeta( "test", mockTextFileOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockTextFileOutputMeta.isFileNameInField() ).thenReturn( false );
    when( mockTextFileOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( mockTextFileOutputMeta.getFiles( any( VariableSpace.class ) ) ).thenReturn( new String[] { "/tmp/out.txt" } );
    when( mockTextFileOutputMeta.getOutputFields() ).thenReturn( new TextFileField[]{} );

    when( mockBuilder.addNode( any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );
    when( mockBuilder.addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );

    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    IMetaverseNode result = analyzer.analyze( descriptor, mockTextFileOutputMeta );

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
    StepMeta meta = new StepMeta( "test", mockTextFileOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockTextFileOutputMeta.isFileNameInField() ).thenReturn( false );
    when( mockTextFileOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( mockTextFileOutputMeta.getFiles( any( VariableSpace.class ) ) ).thenReturn( new String[] { "/tmp/out.txt" } );

    TextFileField[] fields = new TextFileField[]{ mockField1, mockField2 };
    when( mockTextFileOutputMeta.getOutputFields() ).thenReturn( fields );

    when( mockBuilder.addNode( any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );
    when( mockBuilder.addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );

    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[] { "Field 1", "Field 2" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[ 0 ] == "Field 1" )
          return new ValueMetaString( "Field 1" );
        if ( args[ 0 ] == "Field 2" )
          return new ValueMetaString( "Field 2" );
        return null;
      }
    } );

    IMetaverseNode result = analyzer.analyze( descriptor, mockTextFileOutputMeta );

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
  public void testAnalyze_FileFromStreamField() throws Exception {
    StepMeta meta = new StepMeta( "test", mockTextFileOutputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockTextFileOutputMeta.isFileNameInField() ).thenReturn( true );
    when( mockTextFileOutputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( mockTextFileOutputMeta.getFileNameField() ).thenReturn( "filename" );

    TextFileField[] fields = new TextFileField[]{ mockField1, mockField2 };
    when( mockTextFileOutputMeta.getOutputFields() ).thenReturn( fields );

    when( mockBuilder.addNode( any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );
    when( mockBuilder.addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) ) ).thenReturn( mockBuilder );

    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockTransMeta.getPrevStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    String[] incomingFields = new String[] { "Field 1", "Field 2", "filename" };
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( incomingFields );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[ 0 ] == "Field 1" )
          return new ValueMetaString( "Field 1" );
        if ( args[ 0 ] == "Field 2" )
          return new ValueMetaString( "Field 2" );
        if ( args[ 0 ] == "filename" )
          return new ValueMetaString( "filename" );
        return null;
      }
    } );

    IMetaverseNode result = analyzer.analyze( descriptor, mockTextFileOutputMeta );

    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    // we shouldn't try to get the files when it should be coming from the stream
    verify( mockTextFileOutputMeta, times( 0 ) ).getFiles( any( VariableSpace.class ) );

    verify( mockBuilder, times( 2 + fields.length ) ).addNode( any( IMetaverseNode.class ) );

    // don't know what we are writing to until runtime, make sure no link is created yet
    verify( mockBuilder, times( 0 ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_WRITESTO ), any( IMetaverseNode.class ) );

    verify( mockBuilder, times( fields.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_POPULATES ), any( IMetaverseNode.class ) );

    verify( mockBuilder, times( incomingFields.length ) ).addLink( any( IMetaverseNode.class ),
      eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testGetSupportedSteps() {
    TextFileOutputStepAnalyzer analyzer = new TextFileOutputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TextFileOutputMeta.class ) );
  }
}
