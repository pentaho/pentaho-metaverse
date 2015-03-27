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
 *
 */

package com.pentaho.metaverse.analyzer.kettle.step.transexecutor;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IAnalysisContext;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
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
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_fileName_subTransNotFoundOnFileSystem() throws Exception {
    // we should get an exception if the sub transformation isn't found on the filesystem
    when( meta.getFileName() ).thenReturn( "target/outputfiles/TransExecutorStepAnalyzerTest/subTrans.ktr" );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
  }

  @Test
  public void testAnalyze_fileName() throws Exception {
    String filePath = "src/it/resources/repo/validation/transformation-executor/trans-executor-child.ktr";
    doReturn( childTransMeta ).when( spyAnalyzer ).getSubTransMeta( anyString() );
    when( meta.getFileName() ).thenReturn( filePath );

    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
    assertNotNull( node );
    assertTrue( node.getProperty( TransExecutorStepAnalyzer.TRANSFORMATION_TO_EXECUTE )
      .toString().contains( filePath ) );

    // we should have one link created that "executes" the sub transformation
    verify( builder, times( 1 ) ).addLink( eq( node ), eq( DictionaryConst.LINK_EXECUTES ),
      any( IMetaverseNode.class ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_repoFile_ErrorFindingRepoDir() throws Exception {
    // we should get an exception if the sub transformation isn't found on the filesystem
    when( meta.getDirectoryPath() ).thenReturn( "/home/admin" );
    when( meta.getTransName() ).thenReturn( "my.ktr" );
    Repository repo = mock( Repository.class );
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( repo.findDirectory( anyString() ) ).thenThrow( new KettleException() );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_repoFile_NoRepo() throws Exception {
    // we should get an exception if the sub transformation isn't found in the repo
    when( meta.getDirectoryPath() ).thenReturn( "/home/admin" );
    when( meta.getTransName() ).thenReturn( "my.ktr" );
    Repository repo = null;
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
  }

  @Test
  public void testAnalyze_repoFile() throws Exception {
    when( meta.getDirectoryPath() ).thenReturn( "/home/admin" );
    when( meta.getTransName() ).thenReturn( "my" );
    Repository repo = mock( Repository.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );
    when( repo.findDirectory( anyString() ) ).thenReturn( repoDir );
    when( repo.loadTransformation( anyString(), eq ( repoDir ), any( ProgressMonitorListener.class ),
      anyBoolean(), anyString() ) ).thenReturn( childTransMeta );
    when( parentTransMeta.getRepository() ).thenReturn( repo );

    doReturn( childTransMeta ).when( spyAnalyzer ).getSubTransMeta( anyString() );
    when( childTransMeta.getPathAndName() ).thenReturn( "/home/admin/my" );
    when( childTransMeta.getDefaultExtension() ).thenReturn( "ktr" );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
    assertNotNull( node );
    assertTrue( node.getProperty( TransExecutorStepAnalyzer.TRANSFORMATION_TO_EXECUTE )
      .toString().contains( "/home/admin/my.ktr" ) );

    // we should have one link created that "executes" the sub transformation
    verify( builder, times( 1 ) ).addLink( eq( node ), eq( DictionaryConst.LINK_EXECUTES ),
      any( IMetaverseNode.class ) );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_repoRef_ErrorFindingRepoObject() throws Exception {
    // we should get an exception if the sub transformation isn't found in the repo
    when( meta.getTransObjectId() ).thenReturn( mock( ObjectId.class ) );
    Repository repo = mock( Repository.class );
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( repo.loadTransformation( any( ObjectId.class ), anyString() ) ).thenThrow( new KettleException() );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testAnalyze_repoRepo_NoRepo() throws Exception {
    // we should get an exception if the sub transformation isn't found on the filesystem
    when( meta.getTransObjectId() ).thenReturn( mock( ObjectId.class ) );
    Repository repo = null;
    when( parentTransMeta.getRepository() ).thenReturn( repo );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
  }

  @Test
  public void testAnalyze_repoRef() throws Exception {
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
    IMetaverseNode node = spyAnalyzer.analyze( descriptor, meta );
    assertNotNull( node );
    assertTrue( node.getProperty( TransExecutorStepAnalyzer.TRANSFORMATION_TO_EXECUTE )
      .toString().contains( "/home/admin/my.ktr" ) );

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
  public void testAddResultRowsFields() throws Exception {
    when( meta.getOutputRowsSourceStep() ).thenReturn( "outputRowsStepName" );
    when( meta.getOutputRowsField() ).thenReturn( resultsFieldNames );
    when( meta.getOutputRowsType() ).thenReturn( new int[]{ 2, 2 } );
    IMetaverseNode node = mock( IMetaverseNode.class );
    when( spyAnalyzer.getRootNode() ).thenReturn( node );

    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    spyAnalyzer.addResultRowsFields( meta, childTransMeta, childTransNode, descriptor );
    verify( builder, times( resultsFieldNames.length ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_CREATES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testAddResultRowsFields_noOutputRowsSourceStep() throws Exception {
    when( meta.getOutputRowsSourceStep() ).thenReturn( null );
    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_CREATES ), any( IMetaverseNode.class ) );
  }

  @Test
  public void testAddUsesLinks() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );

    Map<String, RowMetaInterface> prevFields = new HashMap<String, RowMetaInterface>();
    prevFields.put( "previousStep", inputRowMeta );
    List<ValueMetaInterface> inputValueMetas = new ArrayList<ValueMetaInterface>();
    ValueMetaInterface field1 = mock( ValueMetaInterface.class );
    ValueMetaInterface field2 = mock( ValueMetaInterface.class );
    when( field1.getName() ).thenReturn( "one" );
    when( field2.getName() ).thenReturn( "two" );
    inputValueMetas.add( field1 );
    inputValueMetas.add( field2 );
    when( inputRowMeta.getValueMetaList() ).thenReturn( inputValueMetas );
    doReturn( prevFields ).when( spyAnalyzer ).getPrevFields();

    spyAnalyzer.addUsesLinks( meta, childTransMeta, childTransNode, descriptor );

    verify( builder, times( inputValueMetas.size() ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

    verify( builder, times( inputValueMetas.size() ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DELETES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testAddUsesLinks_noPrevFields() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );

    Map<String, RowMetaInterface> prevFields = new HashMap<String, RowMetaInterface>();
    doReturn( prevFields ).when( spyAnalyzer ).getPrevFields();

    spyAnalyzer.addUsesLinks( meta, childTransMeta, childTransNode, descriptor );

    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_USES ), any( IMetaverseNode.class ) );

    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DELETES ), any( IMetaverseNode.class ) );

  }


  @Test
  public void testConnectResultFieldToSubTrans() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );
    when( rootNode.getName() ).thenReturn( resultsFieldNames[ 1 ] );

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
    childTransSteps.add( rowsFromResult );

    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    when( parentTransMeta.getStepFields( any( StepMeta.class ) ) ).thenReturn( rowMetaInterface );
    when( rowMetaInterface.getFieldNames() ).thenReturn( resultsFieldNames );
    when( childTransMeta.getSteps() ).thenReturn( childTransSteps );

    spyAnalyzer.connectResultFieldToSubTrans( rootNode, childTransMeta, childTransNode, descriptor );

    verify( builder, times( 1 ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testConnectResultFieldToSubTrans_noChildTransSteps() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );

    when( childTransMeta.getSteps() ).thenReturn( null );

    spyAnalyzer.connectResultFieldToSubTrans( rootNode, childTransMeta, childTransNode, descriptor );

    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }


  @Test
  public void testConnectUsedFieldToSubTrans() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );
    when( rootNode.getName() ).thenReturn( resultsFieldNames[1] );

    List<StepMeta> childTransSteps = new ArrayList<StepMeta>();

    StepMeta dummy = mock( StepMeta.class );
    when( dummy.getStepMetaInterface() ).thenReturn( mock( DummyTransMeta.class ) );
    childTransSteps.add( dummy );

    StepMeta rowsFromResult = mock( StepMeta.class );
    RowsFromResultMeta mockRowsFromResultMeta = mock( RowsFromResultMeta.class );
    when( rowsFromResult.getStepMetaInterface() ).thenReturn( mockRowsFromResultMeta );
    childTransSteps.add( rowsFromResult );

    when( mockRowsFromResultMeta.getFieldname() ).thenReturn( resultsFieldNames );

    when( childTransMeta.getSteps() ).thenReturn( childTransSteps );

    spyAnalyzer.connectUsedFieldToSubTrans( rootNode, childTransMeta, childTransNode, descriptor );

    verify( builder, times( 1 ) )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

  @Test
  public void testConnectUsedFieldToSubTrans_noChildTransSteps() throws Exception {
    IMetaverseNode childTransNode = mock( IMetaverseNode.class );
    IMetaverseNode rootNode = mock( IMetaverseNode.class );

    when( childTransMeta.getSteps() ).thenReturn( null );

    spyAnalyzer.connectUsedFieldToSubTrans( rootNode, childTransMeta, childTransNode, descriptor );

    verify( builder, never() )
      .addLink( any( IMetaverseNode.class ), eq( DictionaryConst.LINK_DERIVES ), any( IMetaverseNode.class ) );

  }

}
