/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle.step.transexecutor;

import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResultMeta;
import org.pentaho.di.trans.steps.rowstoresult.RowsToResultMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 4/2/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class TransExecutorStepAnalyzerTest {

  private TransExecutorStepAnalyzer analyzer;

  @Mock
  private TransExecutorMeta meta;

  @Mock
  IMetaverseNode node;

  @Mock
  private IMetaverseBuilder builder;
  @Mock
  private INamespace namespace;
  @Mock
  private INamespace parentNamespace;
  @Mock
  private IComponentDescriptor descriptor;
  @Mock
  private StepMeta parentStepMeta;
  @Mock
  private TransMeta parentTransMeta;
  @Mock
  private RowMetaInterface inputRowMeta;
  @Mock
  private RowMetaInterface outputRowMeta;
  @Mock
  private StepMeta nextStepMeta_Results;
  @Mock
  private RowMetaInterface nextStepMeta_Results_input;
  @Mock
  private IAnalysisContext analysisContext;

  @Mock
  private TransMeta childTransMeta;

  private String[] nextStepNames = new String[] { "results" };
  private String[] resultsFieldNames = new String[] { "one", "two" };
  private TransExecutorStepAnalyzer spyAnalyzer;

  @Before
  public void setUp() throws Exception {

    when( builder.getMetaverseObjectFactory() ).thenReturn( MetaverseTestUtils.getMetaverseObjectFactory() );

    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );

    when( parentTransMeta.getNextStepNames( parentStepMeta ) ).thenReturn( nextStepNames );
    when( parentTransMeta.findStep( nextStepNames[ 0 ] ) ).thenReturn( nextStepMeta_Results );
    when( parentTransMeta.getPrevStepFields( eq( nextStepMeta_Results ), any( ProgressMonitorListener.class ) ) )
      .thenReturn( nextStepMeta_Results_input );

    when( nextStepMeta_Results_input.getFieldNames() ).thenReturn( resultsFieldNames );

    when( parentTransMeta.environmentSubstitute( anyString() ) ).then( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocationOnMock ) throws Throwable {
        return invocationOnMock.getArguments()[ 0 ].toString();
      }
    } );

    when( childTransMeta.getName() ).thenReturn( "child" );
    when( descriptor.getNamespace() ).thenReturn( namespace );
    when( namespace.getParentNamespace() ).thenReturn( parentNamespace );

    when( descriptor.getContext() ).thenReturn( analysisContext );

    analyzer = new TransExecutorStepAnalyzer();
    spyAnalyzer = spy( analyzer );
    spyAnalyzer.setMetaverseBuilder( builder );
    spyAnalyzer.setParentTransMeta( parentTransMeta );
    spyAnalyzer.setParentStepMeta( parentStepMeta );
    spyAnalyzer.setDescriptor( descriptor );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testCustomAnalyze_fileName_subTransNotFoundOnFileSystem() throws Exception {
    // we should get an exception if the sub transformation isn't found on the filesystem
    when( meta.getFileName() ).thenReturn( "target/outputfiles/TransExecutorStepAnalyzerTest/subTrans.ktr" );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    spyAnalyzer.customAnalyze( meta, node );
  }

  @Test
  public void testCustomAnalyze_fileName() throws Exception {
    String filePath = "src/it/resources/repo/validation/transformation-executor/trans-executor-child.ktr";
    doReturn( childTransMeta ).when( spyAnalyzer ).getSubTransMeta( anyString() );
    when( meta.getFileName() ).thenReturn( filePath );

    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( meta.getExecutionResultTargetStep() ).thenReturn( "executionResultsTargetStep" );
    when( meta.getOutputRowsSourceStep() ).thenReturn( "outputRowsSourceStep" );
    when( meta.getResultFilesTargetStep() ).thenReturn( "resultFilesTargetStep" );

    // don't bother running the connectX methods, we'll test those later
    doNothing().when( spyAnalyzer ).connectToSubTransInputFields(
      eq( meta ), any( TransMeta.class ), any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );
    doNothing().when( spyAnalyzer ).connectToSubTransOutputFields(
      eq( meta ), any( TransMeta.class ), any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

    spyAnalyzer.customAnalyze( meta, node );

    verify( node ).setProperty( eq( TransExecutorStepAnalyzer.TRANSFORMATION_TO_EXECUTE ),
      Mockito.contains( filePath ) );

    // we should have one link created that "executes" the sub transformation
    verify( builder, times( 1 ) ).addLink( eq( node ), eq( DictionaryConst.LINK_EXECUTES ),
      any( IMetaverseNode.class ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testCustomAnalyze_repoFile_ErrorFindingRepoDir() throws Exception {
    // we should get an exception if the sub transformation isn't found on the filesystem
    when( meta.getDirectoryPath() ).thenReturn( "/home/admin" );
    when( meta.getTransName() ).thenReturn( "my.ktr" );
    Repository repo = mock( Repository.class );
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( repo.findDirectory( anyString() ) ).thenThrow( new KettleException() );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    spyAnalyzer.customAnalyze( meta, node );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testCustomAnalyze_repoFile_NoRepo() throws Exception {
    // we should get an exception if the sub transformation isn't found in the repo
    when( meta.getDirectoryPath() ).thenReturn( "/home/admin" );
    when( meta.getTransName() ).thenReturn( "my.ktr" );
    Repository repo = null;
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    spyAnalyzer.customAnalyze( meta, node );
  }

  @Test
  public void testCustomAnalyze_repoFile() throws Exception {
    when( meta.getDirectoryPath() ).thenReturn( "/home/admin" );
    when( meta.getTransName() ).thenReturn( "my" );
    Repository repo = mock( Repository.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );
    when( repo.findDirectory( anyString() ) ).thenReturn( repoDir );
    when( repo.loadTransformation( anyString(), eq( repoDir ), any( ProgressMonitorListener.class ),
      anyBoolean(), anyString() ) ).thenReturn( childTransMeta );
    when( parentTransMeta.getRepository() ).thenReturn( repo );

    doReturn( childTransMeta ).when( spyAnalyzer ).getSubTransMeta( anyString() );
    when( childTransMeta.getPathAndName() ).thenReturn( "/home/admin/my" );
    when( childTransMeta.getDefaultExtension() ).thenReturn( "ktr" );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );


    // don't bother running the connectX methods, we'll test those later
    doNothing().when( spyAnalyzer ).connectToSubTransInputFields(
      eq( meta ), any( TransMeta.class ), any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );
    doNothing().when( spyAnalyzer ).connectToSubTransOutputFields(
      eq( meta ), any( TransMeta.class ), any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

    spyAnalyzer.customAnalyze( meta, node );

    verify( node ).setProperty( eq( TransExecutorStepAnalyzer.TRANSFORMATION_TO_EXECUTE ),
      Mockito.contains( "/home/admin/my.ktr" ) );

    // we should have one link created that "executes" the sub transformation
    verify( builder, times( 1 ) ).addLink( eq( node ), eq( DictionaryConst.LINK_EXECUTES ),
      any( IMetaverseNode.class ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testCustomAnalyze_repoRef_ErrorFindingRepoObject() throws Exception {
    // we should get an exception if the sub transformation isn't found in the repo
    when( meta.getTransObjectId() ).thenReturn( mock( ObjectId.class ) );
    Repository repo = mock( Repository.class );
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( repo.loadTransformation( any( ObjectId.class ), anyString() ) ).thenThrow( new KettleException() );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    spyAnalyzer.customAnalyze( meta, node );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testCustomAnalyze_repoRepo_NoRepo() throws Exception {
    // we should get an exception if the sub transformation isn't found on the filesystem
    when( meta.getTransObjectId() ).thenReturn( mock( ObjectId.class ) );
    Repository repo = null;
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    spyAnalyzer.customAnalyze( meta, node );
  }

  @Test
  public void testCustomAnalyze_repoRef() throws Exception {
    when( meta.getTransObjectId() ).thenReturn( mock( ObjectId.class ) );
    Repository repo = mock( Repository.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );
    when( repo.findDirectory( anyString() ) ).thenReturn( repoDir );
    when( repo.loadTransformation( any( ObjectId.class ), anyString() ) ).thenReturn( childTransMeta );
    when( parentTransMeta.getRepository() ).thenReturn( repo );

    doReturn( childTransMeta ).when( spyAnalyzer ).getSubTransMeta( anyString() );
    when( childTransMeta.getPathAndName() ).thenReturn( "/home/admin/my" );
    when( childTransMeta.getDefaultExtension() ).thenReturn( "ktr" );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );

    // don't bother running the connectX methods, we'll test those later
    doNothing().when( spyAnalyzer ).connectToSubTransInputFields(
      eq( meta ), any( TransMeta.class ), any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );
    doNothing().when( spyAnalyzer ).connectToSubTransOutputFields(
      eq( meta ), any( TransMeta.class ), any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

    spyAnalyzer.customAnalyze( meta, node );

    verify( node ).setProperty( eq( TransExecutorStepAnalyzer.TRANSFORMATION_TO_EXECUTE ),
      Mockito.contains( "/home/admin/my.ktr" ) );

    // we should have one link created that "executes" the sub transformation
    verify( builder, times( 1 ) ).addLink( eq( node ), eq( DictionaryConst.LINK_EXECUTES ),
      any( IMetaverseNode.class ) );
  }
  @Test
  public void testGetSupportedSteps() {
    TransExecutorStepAnalyzer analyzer = new TransExecutorStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TransExecutorMeta.class ) );
  }

  @Test
  public void testConnectToSubTransOutputFields() throws Exception {
    when( meta.getOutputRowsSourceStep() ).thenReturn( "outputRowsStepName" );
    String[] outputFields = new String[]{ "first", "last" };
    when( meta.getOutputRowsField() ).thenReturn( outputFields );
    IMetaverseNode outNode = mock( IMetaverseNode.class );
    StepNodes outputs = new StepNodes();
    outputs.addNode( "outputRowsStepName", "first", outNode );
    outputs.addNode( "outputRowsStepName", "last", outNode );

    doReturn( outputs ).when( spyAnalyzer ).getOutputs();

    // we'll test this in it's own test
    doNothing().when( spyAnalyzer ).linkResultFieldToSubTrans( any( IMetaverseNode.class ), any( TransMeta.class ),
      any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    spyAnalyzer.connectToSubTransOutputFields( meta, childTransMeta, childTransNode, descriptor );

    verify( spyAnalyzer, times( outputFields.length ) ).linkResultFieldToSubTrans( any( IMetaverseNode.class ),
      any( TransMeta.class ),
      any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

  }

  @Test
  public void testConnectToSubTransInputFields() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode outNode = mock( IMetaverseNode.class );

    StepNodes inputs = new StepNodes();
    inputs.addNode( "previousStep", "first", outNode );
    inputs.addNode( "previousStep", "last", outNode );
    doReturn( inputs ).when( spyAnalyzer ).getInputs();

    doNothing().when( spyAnalyzer).linkUsedFieldToSubTrans( any( IMetaverseNode.class ), any( TransMeta.class ),
      any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

    spyAnalyzer.connectToSubTransInputFields( meta, childTransMeta, childTransNode, descriptor );

    verify( spyAnalyzer, times( inputs.getFieldNames().size() ) ).linkUsedFieldToSubTrans( any( IMetaverseNode.class ),
      any( TransMeta.class ),
      any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

  }

  @Test
  public void testConnectToSubTransInputFields_noPrevFields() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );

    StepNodes inputs = new StepNodes();
    doReturn( inputs ).when( spyAnalyzer ).getInputs();

    doNothing().when( spyAnalyzer).linkUsedFieldToSubTrans( any( IMetaverseNode.class ), any( TransMeta.class ),
      any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

    spyAnalyzer.connectToSubTransInputFields( meta, childTransMeta, childTransNode, descriptor );

    verify( spyAnalyzer, never() ).linkUsedFieldToSubTrans( any( IMetaverseNode.class ),
      any( TransMeta.class ),
      any( IMetaverseNode.class ), any( IComponentDescriptor.class ) );

  }


  @Test
  public void testLinkResultFieldToSubTrans() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode fieldNode = mock( IMetaverseNode.class );
    when( fieldNode.getName() ).thenReturn( resultsFieldNames[ 1 ] );

    List<StepMeta> childTransSteps = new ArrayList<StepMeta>();

    StepMeta dummy = mock( StepMeta.class );
    when( dummy.getStepMetaInterface() ).thenReturn( mock( DummyTransMeta.class ) );
    childTransSteps.add( dummy );

    StepMeta parentStepMeta = mock( StepMeta.class );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );

    StepMeta rowsFromResult = mock( StepMeta.class );
    RowsToResultMeta mockRowsToResultMeta = mock( RowsToResultMeta.class );
    when( rowsFromResult.getStepMetaInterface() ).thenReturn( mockRowsToResultMeta );
    when( mockRowsToResultMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( mockRowsToResultMeta.getName() ).thenReturn( "stepName" );
    childTransSteps.add( rowsFromResult );

    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    when( parentTransMeta.getStepFields( any( StepMeta.class ) ) ).thenReturn( rowMetaInterface );
    when( rowMetaInterface.getFieldNames() ).thenReturn( resultsFieldNames );
    when( childTransMeta.getSteps() ).thenReturn( childTransSteps );

    IMetaverseNode subFieldNode = mock( IMetaverseNode.class );
    doReturn( subFieldNode ).when( spyAnalyzer ).createFieldNode( any( IComponentDescriptor.class ),
      any( ValueMetaInterface.class ),
      eq( StepAnalyzer.NONE ),
      eq( false ) );

    spyAnalyzer.linkResultFieldToSubTrans( fieldNode, childTransMeta, childTransNode, descriptor );

    verify( spyAnalyzer ).createFieldNode( any( IComponentDescriptor.class ),
      any( ValueMetaInterface.class ),
      eq( StepAnalyzer.NONE ),
      eq( false ) );

    verify( builder )
      .addLink( eq( subFieldNode ), eq( DictionaryConst.LINK_DERIVES ), eq( fieldNode ) );

  }

  @Test
  public void testConnectResultFieldToSubTrans_noChildTransSteps() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );

    when( childTransMeta.getSteps() ).thenReturn( null );

    spyAnalyzer.linkResultFieldToSubTrans( rootNode, childTransMeta, childTransNode, descriptor );

    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testLinkUsedFieldToSubTrans() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode originalFieldNode = mock( IMetaverseNode.class );
    when( originalFieldNode.getName() ).thenReturn( resultsFieldNames[ 1 ] );

    List<StepMeta> childTransSteps = new ArrayList<StepMeta>();

    StepMeta dummy = mock( StepMeta.class );
    when( dummy.getStepMetaInterface() ).thenReturn( mock( DummyTransMeta.class ) );
    childTransSteps.add( dummy );

    StepMeta rowsFromResult = mock( StepMeta.class );
    RowsFromResultMeta mockRowsFromResultMeta = mock( RowsFromResultMeta.class );
    when( rowsFromResult.getStepMetaInterface() ).thenReturn( mockRowsFromResultMeta );
    when( rowsFromResult.getName() ).thenReturn( "stepName" );
    childTransSteps.add( rowsFromResult );

    when( mockRowsFromResultMeta.getFieldname() ).thenReturn( resultsFieldNames );

    StepMeta rowsParentStepMeta = mock( StepMeta.class );
    TransMeta rowsParentTransMeta = mock( TransMeta.class );
    RowMetaInterface rmiRows = mock( RowMetaInterface.class );
    when( mockRowsFromResultMeta.getParentStepMeta() ).thenReturn( rowsParentStepMeta );
    when( rowsParentStepMeta.getParentTransMeta() ).thenReturn( rowsParentTransMeta );
    when( rowsParentTransMeta.getStepFields( rowsFromResult ) ).thenReturn( rmiRows );

    when( rmiRows.getFieldNames() ).thenReturn( resultsFieldNames );
    ValueMetaInterface vmi = mock( ValueMetaInterface.class );
    when( rmiRows.getValueMeta( anyInt() ) ).thenReturn( vmi );

    when( childTransMeta.getSteps() ).thenReturn( childTransSteps );

    IMetaverseNode subFieldNode = mock( IMetaverseNode.class );
    doReturn( subFieldNode ).when( spyAnalyzer ).createFieldNode( any( IComponentDescriptor.class ),
      any( ValueMetaInterface.class ),
      eq( "stepName" ), eq( false ) );

    spyAnalyzer.linkUsedFieldToSubTrans( originalFieldNode, childTransMeta, childTransNode, descriptor );

    verify( spyAnalyzer ).createFieldNode( any( IComponentDescriptor.class ),
      any( ValueMetaInterface.class ),
      eq( "stepName" ), eq( false ) );

    verify( builder )
      .addLink( eq( originalFieldNode ), eq( DictionaryConst.LINK_DERIVES ), eq( subFieldNode ) );

  }

  @Test
  public void testConnectUsedFieldToSubTrans_noChildTransSteps() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );

    when( childTransMeta.getSteps() ).thenReturn( null );

    spyAnalyzer.linkUsedFieldToSubTrans( rootNode, childTransMeta, childTransNode, descriptor );

    verify( spyAnalyzer, never() ).createFieldNode( any( IComponentDescriptor.class ),
      any( ValueMetaInterface.class ),
      anyString(),
      eq( false ) );

    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testConnectUsedFieldToSubTrans_noChildRowsFromResultStep() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );
    List<StepMeta> childTransSteps = new ArrayList<StepMeta>();

    StepMeta dummy = mock( StepMeta.class );
    when( dummy.getStepMetaInterface() ).thenReturn( mock( DummyTransMeta.class ) );
    childTransSteps.add( dummy );

    when( childTransMeta.getSteps() ).thenReturn( childTransSteps );

    spyAnalyzer.linkUsedFieldToSubTrans( rootNode, childTransMeta, childTransNode, descriptor );

    verify( spyAnalyzer, never() ).createFieldNode( any( IComponentDescriptor.class ),
      any( ValueMetaInterface.class ),
      anyString(),
      eq( false ) );

    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testGetOutputRowMetaInterfaces() throws Exception {
    Map<String, RowMetaInterface> rowMetaInterfaces = spyAnalyzer.getOutputRowMetaInterfaces( meta );
    assertTrue( MapUtils.isNotEmpty( rowMetaInterfaces ) );
  }

  @Test
  public void testIsPassthrough() throws Exception {
    StepField stepField = new StepField( "previousStep", "one" );
    assertFalse( spyAnalyzer.isPassthrough( stepField ) );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    IMetaverseNode outNode = mock( IMetaverseNode.class );
    StepNodes inputs = new StepNodes();
    inputs.addNode( "previousStep", "one", outNode );
    inputs.addNode( "previousStep", "two", outNode );
    doReturn( inputs ).when( spyAnalyzer ).getInputs();
    Set<StepField> usedFields = spyAnalyzer.getUsedFields( meta );
    assertEquals( inputs.getFieldNames(), usedFields );
  }
}
