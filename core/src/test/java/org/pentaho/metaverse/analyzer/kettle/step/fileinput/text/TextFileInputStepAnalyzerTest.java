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


package org.pentaho.metaverse.analyzer.kettle.step.fileinput.text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInput;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TextFileInputStepAnalyzerTest extends ClonableStepAnalyzerTest {

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
  @Mock
  private FileInputList fileInputList;

  private IMetaverseObjectFactory mockFactory;

  @Before
  public void setUp() throws Exception {

    mockFactory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    lenient().when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    analyzer = new TextFileInputStepAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );

    lenient().when( mockTextFileInput.getStepMetaInterface() ).thenReturn( meta );
    lenient().when( mockTextFileInput.getStepMeta() ).thenReturn( mockStepMeta );
    lenient().when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
  }

  @Test
  public void testGetUsedFields_fileNameFromField() throws Exception {
    lenient().when( meta.isAcceptingFilenames() ).thenReturn( true );
    lenient().when( meta.getAcceptingField() ).thenReturn( "filename" );
    lenient().when( meta.getAcceptingStepName() ).thenReturn( "previousStep" );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 1, usedFields.size() );
    StepField used = usedFields.iterator().next();
    assertEquals( "previousStep", used.getStepName() );
    assertEquals( "filename", used.getFieldName() );
  }

  @Test
  public void testGetUsedFields_isNotAcceptingFilenames() throws Exception {
    lenient().when( meta.isAcceptingFilenames() ).thenReturn( false );
    lenient().when( meta.getAcceptingField() ).thenReturn( "filename" );
    lenient().when( meta.getAcceptingStepName() ).thenReturn( "previousStep" );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testGetUsedFields_isAcceptingFilenamesButNoStepName() throws Exception {
    lenient().when( meta.isAcceptingFilenames() ).thenReturn( true );
    lenient().when( meta.getAcceptingField() ).thenReturn( "filename" );
    lenient().when( meta.getAcceptingStepName() ).thenReturn( null );
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
    when( meta.writesToFile() ).thenReturn( true );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( meta.getFilePaths( false) ).thenReturn( filePaths );

    when( meta.isAcceptingFilenames() ).thenReturn( true );
    Collection<IExternalResourceInfo>  resources = consumer.getResourcesFromMeta( meta );
    assertTrue( resources.isEmpty() );

    when( meta.isAcceptingFilenames() ).thenReturn( false );
    resources = consumer.getResourcesFromMeta( meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );

    when( meta.isAcceptingFilenames() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( meta ) );
    resources = consumer.getResourcesFromMeta( meta );
    // call to getResourcesFromMeta does not cache resources, only calls to getResourcesFromRow do
    assertTrue( resources.isEmpty() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.any(), Mockito.any() ) )
      .thenReturn( "/path/to/row/file" );
    when( mockTextFileInput.environmentSubstitute( "/path/to/row/file" ) ).thenReturn( "/path/to/row/file" );
    when( mockTextFileInput.getStepMetaInterface() ).thenReturn( meta );
    resources = consumer.getResourcesFromRow( mockTextFileInput, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any(), Mockito.any(), Mockito.any() ) ).thenThrow( KettleValueException.class );
    resources = consumer.getResourcesFromRow( mockTextFileInput, mockRowMetaInterface, new String[]{ "id", "name" } );
    // even when the call to getString throws an exception, we can still get resources from the cache
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    assertEquals( TextFileInputMeta.class, consumer.getMetaClass() );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new TextFileInputStepAnalyzer();
  }
}
