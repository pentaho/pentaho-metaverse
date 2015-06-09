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

package org.pentaho.metaverse.analyzer.kettle.step.textfileinput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class TextFileInputStepAnalyzerTest {

  private TextFileInputStepAnalyzer analyzer;

  @Mock
  private TextFileInput mockTextFileInput;
  @Mock
  private TextFileInputMeta meta;
  @Mock
  private StepMeta mockStepMeta;
  @Mock
  private TransMeta transMeta;
  @Mock
  private RowMetaInterface mockRowMetaInterface;
  @Mock
  private IMetaverseBuilder mockBuilder;
  @Mock
  private INamespace mockNamespace;

  private IMetaverseObjectFactory mockFactory;

    @Before
  public void setUp() throws Exception {

    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    analyzer = new TextFileInputStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );

    when( mockTextFileInput.getStepMetaInterface() ).thenReturn( meta );
    when( mockTextFileInput.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
  }

  @Test
  public void testGetUsedFields_fileNameFromField() throws Exception {
    when( meta.isAcceptingFilenames() ).thenReturn( true );
    when( meta.getAcceptingField() ).thenReturn( "filename" );
    when( meta.getAcceptingStepName() ).thenReturn( "previousStep" );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 1, usedFields.size() );
    StepField used = usedFields.iterator().next();
    assertEquals( "previousStep", used.getStepName() );
    assertEquals( "filename", used.getFieldName() );
  }

  @Test
  public void testGetUsedFields_isNotAcceptingFilenames() throws Exception {
    when( meta.isAcceptingFilenames() ).thenReturn( false );
    when( meta.getAcceptingField() ).thenReturn( "filename" );
    when( meta.getAcceptingStepName() ).thenReturn( "previousStep" );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testGetUsedFields_isAcceptingFilenamesButNoStepName() throws Exception {
    when( meta.isAcceptingFilenames() ).thenReturn( true );
    when( meta.getAcceptingField() ).thenReturn( "filename" );
    when( meta.getAcceptingStepName() ).thenReturn( null );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertNull( analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertFalse( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertTrue( analyzer.isInput() );
  }

  @Test
  public void testGetSupportedSteps() {
    TextFileInputStepAnalyzer analyzer = new TextFileInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TextFileInputMeta.class ) );
  }


  @Test
  public void testTextFileInputExternalResourceConsumer() throws Exception {
    TextFileInputExternalResourceConsumer consumer = new TextFileInputExternalResourceConsumer();

    StepMeta spyMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.getFileName() ).thenReturn( null );
    when( meta.isAcceptingFilenames() ).thenReturn( false );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( meta.getFilePaths( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );


    when( meta.isAcceptingFilenames() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( meta ) );
    assertTrue( consumer.getResourcesFromMeta( meta ).isEmpty() );
    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenReturn( "/path/to/row/file" );
    when( mockTextFileInput.getStepMetaInterface() ).thenReturn( meta );
    resources = consumer.getResourcesFromRow( mockTextFileInput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockTextFileInput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( TextFileInputMeta.class, consumer.getMetaClass() );
  }
}
