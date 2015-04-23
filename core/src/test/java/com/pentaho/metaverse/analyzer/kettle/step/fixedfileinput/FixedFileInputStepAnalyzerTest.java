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

package com.pentaho.metaverse.analyzer.kettle.step.fixedfileinput;

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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fixedinput.FixedFileInputField;
import org.pentaho.di.trans.steps.fixedinput.FixedInput;
import org.pentaho.di.trans.steps.fixedinput.FixedInputMeta;

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

@RunWith( MockitoJUnitRunner.class )
public class FixedFileInputStepAnalyzerTest {

  private FixedFileInputStepAnalyzer fixedFileInputStepAnalyzer;

  @Mock
  private FixedInput mockFixedFileInput;

  @Mock
  private FixedInputMeta mockFixedFileInputMeta;

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

    fixedFileInputStepAnalyzer = new FixedFileInputStepAnalyzer();
    fixedFileInputStepAnalyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_JOB, mockNamespace );

    when( mockFixedFileInput.getStepMetaInterface() ).thenReturn( mockFixedFileInputMeta );
    when( mockFixedFileInput.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockFixedFileInputMeta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_nullInput() throws Exception {
    fixedFileInputStepAnalyzer.analyze( null, null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockFixedFileInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyTextInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockFixedFileInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockFixedFileInputMeta.getFilename() ).thenReturn( fileNames[0] );

    IMetaverseNode result = fixedFileInputStepAnalyzer.analyze( descriptor, mockFixedFileInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockFixedFileInputMeta, times( 1 ) ).getFilename();

    // make sure the step node is added as well as the file node
    verify( mockBuilder, times( 1 ) ).addNode( any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_Fields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockFixedFileInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyTextInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockFixedFileInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockFixedFileInputMeta.getFilename() ).thenReturn( fileNames[0] );

    // set up the input fields
    FixedFileInputField field1 = new FixedFileInputField();
    FixedFileInputField field2 = new FixedFileInputField();
    FixedFileInputField[] inputFields = new FixedFileInputField[]{ field1, field2 };

    when( mockFixedFileInputMeta.getFieldDefinition() ).thenReturn( inputFields );
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

    IMetaverseNode result = fixedFileInputStepAnalyzer.analyze( descriptor, mockFixedFileInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockFixedFileInputMeta, times( 1 ) ).getFilename();

    // make sure the step node, the file node, and the field nodes
    verify( mockBuilder, times( 3 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there are "uses" links added (file, and each field)
    verify( mockBuilder, times( inputFields.length ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

    // we should have "populates" links from input nodes to output nodes
    verify( mockBuilder, times( inputFields.length ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_POPULATES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetSupportedSteps() {
    FixedFileInputStepAnalyzer analyzer = new FixedFileInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( FixedInputMeta.class ) );
  }

  @Test
  public void testFixedFileInputExternalResourceConsumer() throws Exception {
    FixedFileInputExternalResourceConsumer consumer = new FixedFileInputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", mockFixedFileInputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockFixedFileInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockFixedFileInputMeta.getFilename() ).thenReturn( null );
    
    assertFalse( consumer.isDataDriven( mockFixedFileInputMeta ) );
    assertTrue( consumer.getResourcesFromMeta( mockFixedFileInputMeta ).isEmpty() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromRow( mockFixedFileInput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( FixedInputMeta.class, consumer.getMetaClass() );
  }
}
