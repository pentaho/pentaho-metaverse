package com.pentaho.metaverse.analyzer.kettle.step.csvfileinput;


import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Collection;
import java.util.Set;

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

@RunWith( MockitoJUnitRunner.class )
public class CsvFileInputStepAnalyzerTest {

  private CsvFileInputStepAnalyzer csvFileInputStepAnalyzer;

  @Mock
  private CsvInputMeta mockCsvInputMeta;

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

    csvFileInputStepAnalyzer = new CsvFileInputStepAnalyzer();
    csvFileInputStepAnalyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_JOB, mockNamespace );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_nullInput() throws Exception {
    csvFileInputStepAnalyzer.analyze( null, null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockCsvInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyTextInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockCsvInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockCsvInputMeta.getFilePaths( Mockito.any( VariableSpace.class ) ) ).thenReturn( fileNames );

    IMetaverseNode result = csvFileInputStepAnalyzer.analyze( descriptor, mockCsvInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockCsvInputMeta, times( 1 ) ).getFilePaths( Mockito.any( VariableSpace.class ) );

    // make sure the step node is added as well as the file node
    verify( mockBuilder, times( 2 ) ).addNode( any( IMetaverseNode.class ) );

    // make sure there is a "readby" link added
    verify( mockBuilder, times( 1 ) ).addLink(
      any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_Fields() throws Exception {

    StepMeta meta = new StepMeta( "test", mockCsvInputMeta );
    StepMeta spyMeta = spy( meta );

    String[] fileNames = new String[]{ "MyTextInput.txt" };

    when( mockTransMeta.environmentSubstitute( any( String[].class ) ) ).thenReturn( fileNames );
    when( mockCsvInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockCsvInputMeta.getFilePaths( Mockito.any( VariableSpace.class ) ) ).thenReturn( fileNames );

    // set up the input fields
    TextFileInputField field1 = new TextFileInputField( "id", 0, 4 );
    TextFileInputField field2 = new TextFileInputField( "name", 1, 30 );
    TextFileInputField[] inputFields = new TextFileInputField[]{ field1, field2 };

    when( mockCsvInputMeta.getInputFields() ).thenReturn( inputFields );
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

    IMetaverseNode result = csvFileInputStepAnalyzer.analyze( descriptor, mockCsvInputMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    verify( mockCsvInputMeta, times( 1 ) ).getFilePaths( Mockito.any( VariableSpace.class ) );

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
  public void testGetSupportedSteps() {
    CsvFileInputStepAnalyzer analyzer = new CsvFileInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( CsvInputMeta.class ) );
  }

  @Test
  public void testCsvInputExternalResourceConsumer() throws Exception {
    CsvFileInputExternalResourceConsumer consumer = new CsvFileInputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", mockCsvInputMeta );
    StepMeta spyMeta = spy( meta );

    when( mockCsvInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( mockCsvInputMeta.getFilePaths( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( mockCsvInputMeta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( mockCsvInputMeta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockCsvInputMeta, mockRowMetaInterface, new String[]{ "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( CsvInputMeta.class, consumer.getMetaClass() );
  }

}
