/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.textfileoutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
