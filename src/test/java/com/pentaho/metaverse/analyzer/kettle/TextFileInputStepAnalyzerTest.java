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

package com.pentaho.metaverse.analyzer.kettle;

import com.pentaho.dictionary.DictionaryConst;
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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;

@RunWith( MockitoJUnitRunner.class )
public class TextFileInputStepAnalyzerTest {

  private TextFileInputStepAnalyzer textFileInputStepAnalyzer;

  @Mock
  private TextFileInputMeta mockTextFileInputMeta;

  @Mock
  private TransMeta mockTransMeta;

  @Mock
  private RowMetaInterface mockRowMetaInterface;

  @Mock
  private IMetaverseBuilder mockBuilder;

  private IMetaverseObjectFactory mockFactory;

  @Mock
  private INamespace namespace;

  @Before
  public void setUp() throws Exception {

    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );

    textFileInputStepAnalyzer = new TextFileInputStepAnalyzer();
    textFileInputStepAnalyzer.setMetaverseBuilder( mockBuilder );
    textFileInputStepAnalyzer.setNamespace( namespace );
  }



  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_nullInput() throws Exception {
    textFileInputStepAnalyzer.analyze( null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockTextFileInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[] { "MyTextInput.txt" };

    when( mockTextFileInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockTextFileInputMeta.getFileName() ).thenReturn( fileNames );

    IMetaverseNode result = textFileInputStepAnalyzer.analyze( mockTextFileInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockTextFileInputMeta, times( 1 ) ).getFileName();

    // make sure the step node is added as well as the file node
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there is a "readby" link added
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_Fields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockTextFileInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[] { "MyTextInput.txt" };

    when( mockTextFileInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockTextFileInputMeta.getFileName() ).thenReturn( fileNames );

    // set up the input fields
    TextFileInputField field1 = new TextFileInputField( "id", 0, 4 );
    TextFileInputField field2 = new TextFileInputField( "name", 1, 30 );
    TextFileInputField[] inputFields = new TextFileInputField[]{ field1, field2 };

    when( mockTextFileInputMeta.getInputFields() ).thenReturn( inputFields );
    when( mockTransMeta.getStepFields( spyMeta ) ).thenReturn( mockRowMetaInterface );
    when( mockRowMetaInterface.getFieldNames() ).thenReturn( new String[] { "id", "name" } );
    when( mockRowMetaInterface.searchValueMeta( Mockito.anyString() ) ).thenAnswer(new Answer<ValueMetaInterface>(){

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if(args[0] == "id") return new ValueMetaString("id");
        if(args[0] == "name") return new ValueMetaString("name");
        return null;
      }
    });

    IMetaverseNode result = textFileInputStepAnalyzer.analyze( mockTextFileInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockTextFileInputMeta, times( 1 ) ).getFileName();

    // make sure the step node, the file node, and the field nodes
    verify( mockBuilder, times( 2 + inputFields.length ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there are "readby" links added (file, and each field)
    verify( mockBuilder, times( 1 + inputFields.length ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

    // we should have "populates" links from input nodes to output nodes
    verify( mockBuilder, times( inputFields.length ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_POPULATES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetSupportedSteps() {
    TextFileInputStepAnalyzer analyzer = new TextFileInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TextFileInputMeta.class ) );
  }

}
