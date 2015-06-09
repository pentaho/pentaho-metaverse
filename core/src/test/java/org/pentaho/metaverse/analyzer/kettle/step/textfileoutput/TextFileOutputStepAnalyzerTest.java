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

package org.pentaho.metaverse.analyzer.kettle.step.textfileoutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class TextFileOutputStepAnalyzerTest {

  private TextFileOutputStepAnalyzer analyzer;

  @Mock TextFileOutputMeta meta;

  @Mock IMetaverseNode node;
  @Mock INamespace mockNamespace;
  IComponentDescriptor descriptor;

  StepNodes inputs;

  @Before
  public void setUp() throws Exception {
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );

    analyzer = spy( new TextFileOutputStepAnalyzer() );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( MetaverseTestUtils.getMetaverseObjectFactory() );

    inputs = new StepNodes();
    inputs.addNode( "previousStep", "first", node );
    inputs.addNode( "previousStep", "last", node );
    inputs.addNode( "previousStep", "age", node );
    inputs.addNode( "previousStep", "filename", node );
    doReturn( inputs ).when( analyzer ).getInputs();
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertNull( analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertTrue( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertFalse( analyzer.isInput() );
  }

  @Test
  public void testCreateResourceNode() throws Exception {
    IExternalResourceInfo res = mock( IExternalResourceInfo.class );
    when( res.getName() ).thenReturn( "file:///Users/home/tmp/xyz.ktr" );
    IMetaverseNode resourceNode = analyzer.createResourceNode( res );
    assertNotNull( resourceNode );
    assertEquals( DictionaryConst.NODE_TYPE_FILE, resourceNode.getType() );
  }

  @Test
  public void testGetUsedFields_fileNameInField() throws Exception {
    when( meta.isFileNameInField() ).thenReturn( true );
    when( meta.getFileNameField() ).thenReturn( "filename" );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 1, usedFields.size() );
  }

  @Test
  public void testGetUsedFields_fileDefinedInMeta() throws Exception {
    when( meta.isFileNameInField() ).thenReturn( false );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testGetSupportedSteps() {
    TextFileOutputStepAnalyzer analyzer = new TextFileOutputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TextFileOutputMeta.class ) );
  }

  @Test
  public void testGetOutputResourceFields() throws Exception {
    TextFileField[] outputFields = new TextFileField[2];
    TextFileField field1 = mock( TextFileField.class );
    TextFileField field2 = mock( TextFileField.class );
    outputFields[0] = field1;
    outputFields[1] = field2;

    when( field1.getName() ).thenReturn( "field1" );
    when( field2.getName() ).thenReturn( "field2" );

    when( meta.getOutputFields() ).thenReturn( outputFields );

    Set<String> outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( outputFields.length, outputResourceFields.size() );
    for ( TextFileField outputField : outputFields ) {
      assertTrue( outputResourceFields.contains( outputField.getName() ) );
    }
  }

}
