package com.pentaho.metaverse.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SelectValuesStepAnalyzerTest {

  private static final String DEFAULT_STEP_NAME = "testStep";

  private SelectValuesStepAnalyzer analyzer;

  @Mock
  private IMetaverseBuilder builder;

  @Mock
  private SelectValuesMeta selectValuesMeta;

  @Mock
  private TransMeta transMeta;

  @Mock
  private RowMetaInterface prevRowMeta;

  @Mock
  private RowMetaInterface stepRowMeta;

  @Mock
  private INamespace namespace;

  @Mock
  IMetaverseComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( builder.getMetaverseObjectFactory() ).thenReturn( factory );
    when( namespace.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );
    when( namespace.getParentNamespace() ).thenReturn( namespace );
    when( namespace.getNamespaceId() ).thenReturn( "namespace" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
    when( descriptor.getChildNamespace( anyString(), anyString() ) ).thenReturn( namespace );
    when( descriptor.getParentNamespace() ).thenReturn( namespace );
    when( descriptor.getNamespaceId() ).thenReturn( "namespace" );

    analyzer = new SelectValuesStepAnalyzer();
    analyzer.setMetaverseBuilder( builder );
    descriptor = new MetaverseComponentDescriptor( DEFAULT_STEP_NAME, DictionaryConst.NODE_TYPE_TRANS, namespace );
  }

  @Test(expected = MetaverseAnalyzerException.class)
  public void testNullAnalyze() throws MetaverseAnalyzerException {

    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyze_noFields() throws Exception {

    StepMeta meta = new StepMeta( DEFAULT_STEP_NAME, selectValuesMeta );
    StepMeta spyMeta = spy( meta );

    when( selectValuesMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );

    IMetaverseNode result = analyzer.analyze( descriptor, selectValuesMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    // TODO verify( selectValuesMeta, times( 1 ) ).getFileName();

    // make sure the step node is added as well as the file node
    verify( builder, atLeastOnce() ).addNode( any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_SelectAlterFieldsRename() throws Exception {

    StepMeta meta = new StepMeta( DEFAULT_STEP_NAME, selectValuesMeta );
    StepMeta spyMeta = spy( meta );

    when( selectValuesMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );

    String[] fieldNames = { "field1", "field2" };
    String[] fieldRenames = { null, "field3" };

    // set up the input fields
    when( selectValuesMeta.getSelectName() ).thenReturn( fieldNames );
    when( selectValuesMeta.getSelectRename() ).thenReturn( fieldRenames );
    when( transMeta.getPrevStepFields( spyMeta ) ).thenReturn( prevRowMeta );
    when( transMeta.getStepFields( spyMeta ) ).thenReturn( stepRowMeta );
    when( stepRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( stepRowMeta.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "field1" )
          return new ValueMetaString( "field1" );
        if ( args[0] == "field2" )
          return new ValueMetaString( "field2" );
        return null;
      }
    } );
    when( prevRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( prevRowMeta.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "field1" )
          return new ValueMetaString( "field1" );
        if ( args[0] == "field2" )
          return new ValueMetaString( "field2" );
        return null;
      }
    } );

    IMetaverseNode result = analyzer.analyze( descriptor, selectValuesMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    // we should have "derives" links from input nodes to output nodes
    verify( builder, times( 1 ) )
        .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

    // we should have no "deletes" links from input nodes to output nodes
    verify( builder, never() )
        .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DELETES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testAnalyze_SelectAlterFieldsMetadataChange() throws Exception {

    StepMeta meta = new StepMeta( DEFAULT_STEP_NAME, selectValuesMeta );
    StepMeta spyMeta = spy( meta );

    when( selectValuesMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );

    String[] fieldNames = { "field1", "field2" };
    String[] fieldRenames = { null, null };
    int[] fieldLength = { SelectValuesStepAnalyzer.NOT_CHANGED, 5 };
    int[] fieldPrecision = { 2, SelectValuesStepAnalyzer.NOT_CHANGED };

    // set up the input fields
    when( selectValuesMeta.getSelectName() ).thenReturn( fieldNames );
    when( selectValuesMeta.getSelectRename() ).thenReturn( fieldRenames );
    when( selectValuesMeta.getSelectPrecision() ).thenReturn( fieldPrecision );
    when( selectValuesMeta.getSelectLength() ).thenReturn( fieldLength );
    when( transMeta.getPrevStepFields( spyMeta ) ).thenReturn( prevRowMeta );
    when( transMeta.getStepFields( spyMeta ) ).thenReturn( stepRowMeta );
    when( stepRowMeta.searchValueMeta( anyString() ) ).thenReturn( null );
    when( prevRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( prevRowMeta.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "field1" )
          return new ValueMetaString( "field1" );
        if ( args[0] == "field2" )
          return new ValueMetaString( "field2" );
        return null;
      }
    } );

    IMetaverseNode result = analyzer.analyze( descriptor, selectValuesMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    //verify( selectValuesMeta, times( 1 ) ).getFileName();

    // make sure there are "readby" links added (file, and each field)
    /*verify( builder, times( 1 + inputFields.length ) ).addLink(
        any( IMetaverseNode.class ), eq( DictionaryConst.LINK_READBY ), any( IMetaverseNode.class ) );*/

    // we should have "derives" links from input nodes to output nodes
    verify( builder, times( 2 ) )
        .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAnalyze_MetadataChange() throws Exception {

    StepMeta meta = new StepMeta( DEFAULT_STEP_NAME, selectValuesMeta );
    StepMeta spyMeta = spy( meta );

    when( selectValuesMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );

    String[] fieldNames = { "field1", "field2" };

    SelectMetadataChange testChange1 = new SelectMetadataChange( selectValuesMeta );
    testChange1.setName( "field1" );
    testChange1.setCurrencySymbol( "~" );

    SelectMetadataChange testChange2 = new SelectMetadataChange( selectValuesMeta );
    testChange2.setName( "field2" );
    testChange2.setRename( "field3" );

    // set up the input fields
    when( selectValuesMeta.getMeta() ).thenReturn( new SelectMetadataChange[] { testChange1, testChange2 } );
    when( transMeta.getPrevStepFields( spyMeta ) ).thenReturn( prevRowMeta );
    when( transMeta.getStepFields( spyMeta ) ).thenReturn( stepRowMeta );
    when( stepRowMeta.searchValueMeta( anyString() ) ).thenReturn( null );
    when( prevRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( prevRowMeta.searchValueMeta( Mockito.anyString() ) ).thenAnswer( new Answer<ValueMetaInterface>() {

      @Override public ValueMetaInterface answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        if ( args[0] == "field1" )
          return new ValueMetaString( "field1" );
        if ( args[0] == "field2" )
          return new ValueMetaString( "field2" );
        return null;
      }
    } );

    IMetaverseNode result = analyzer.analyze( descriptor, selectValuesMeta );
    assertNotNull( result );
    assertEquals( meta.getName(), result.getName() );

    // we should have 2 "derives" links from input fields to output fields
    verify( builder, times( 2 ) )
        .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

    // we should have 2 "creates" links for the derived fields
    verify( builder, times( 2 ) )
        .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_CREATES ), any( IMetaverseNode.class ) );

    // we should have no "deletes" links from input nodes to output nodes
    verify( builder, never() )
        .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DELETES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testGetSupportedSteps() {
    SelectValuesStepAnalyzer analyzer = new SelectValuesStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( SelectValuesMeta.class ) );
  }

}
