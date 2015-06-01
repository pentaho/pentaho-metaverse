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

package com.pentaho.metaverse.analyzer.kettle.step.mongodbinput;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.dictionary.MetaverseTransientNode;
import com.pentaho.metaverse.api.IAnalysisContext;
import com.pentaho.metaverse.api.IComponentDescriptor;
import com.pentaho.metaverse.api.IConnectionAnalyzer;
import com.pentaho.metaverse.api.IMetaverseBuilder;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.IMetaverseObjectFactory;
import com.pentaho.metaverse.api.INamespace;
import com.pentaho.metaverse.api.MetaverseComponentDescriptor;
import com.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.model.MongoDbResourceInfo;
import com.pentaho.metaverse.testutils.MetaverseTestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
import org.pentaho.mongo.wrapper.field.MongoField;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * User: RFellows Date: 3/6/15
 */
@RunWith( MockitoJUnitRunner.class )
public class MongoDbInputStepAnalyzerTest {
  MongoDbInputStepAnalyzer analyzer;

  @Mock IMetaverseNode node;

  @Mock
  private IMetaverseBuilder mockBuilder;
  @Mock
  private MongoDbInputMeta meta;
  @Mock
  private INamespace mockNamespace;
  @Mock
  private StepMeta parentStepMeta;
  @Mock
  private TransMeta mockTransMeta;

  IComponentDescriptor descriptor;

  @BeforeClass
  public static void init() throws Exception {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    Encr.init( "Kettle" );
  }

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );
    analyzer = spy( new MongoDbInputStepAnalyzer() );
    analyzer.setConnectionAnalyzer( mock( IConnectionAnalyzer.class ) );
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.setBaseStepMeta( meta );
    analyzer.setRootNode( node );
    analyzer.setParentTransMeta( mockTransMeta );
    analyzer.setParentStepMeta( parentStepMeta );

    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );

    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( parentStepMeta.getName() ).thenReturn( "test" );
    when( parentStepMeta.getStepID() ).thenReturn( "MongoDbInput" );

  }

  @Test
  public void testCustomAnalyze() throws Exception {
    when( meta.getJsonQuery() ).thenReturn( "{test:test}" );
    when( meta.getCollection() ).thenReturn( "myCollection" );
    when( meta.getQueryIsPipeline() ).thenReturn( true );
    IMetaverseNode node = new MetaverseTransientNode( "new node" );
    analyzer.customAnalyze( meta, node );
    assertNotNull( node );

    assertEquals( "{test:test}", node.getProperty( DictionaryConst.PROPERTY_QUERY ) );
    assertEquals( "myCollection", node.getProperty( MongoDbInputStepAnalyzer.COLLECTION ) );
    assertTrue( (Boolean) node.getProperty( MongoDbInputStepAnalyzer.AGG_PIPELINE ) );
  }

  @Test
  public void testCustomAnalyze_jsonOutput() throws Exception {
    when( meta.getOutputJson() ).thenReturn( true );
    when( meta.getCollection() ).thenReturn( "myCollection" );

    IMetaverseNode node = new MetaverseTransientNode( "new node" );
    analyzer.customAnalyze( meta, node );
    assertNotNull( node );

    assertEquals( "myCollection", node.getProperty( MongoDbInputStepAnalyzer.COLLECTION ) );
    assertTrue( (Boolean) node.getProperty( MongoDbInputStepAnalyzer.OUTPUT_JSON ) );
    assertNull( node.getProperty( DictionaryConst.PROPERTY_QUERY ) );
    assertNull( node.getProperty( MongoDbInputStepAnalyzer.AGG_PIPELINE ) );
  }

  @Test
  public void testCreateTableNode() throws Exception {
    IConnectionAnalyzer connectionAnalyzer = mock( IConnectionAnalyzer.class );

    doReturn( connectionAnalyzer ).when( analyzer ).getConnectionAnalyzer();
    IMetaverseNode connNode = mock( IMetaverseNode.class );
    when( connectionAnalyzer.analyze( any( IComponentDescriptor.class ), anyObject() ) ).thenReturn( connNode );

    MongoDbResourceInfo resourceInfo = mock( MongoDbResourceInfo.class );
    when( resourceInfo.getCollection() ).thenReturn( "myCollection" );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();
    when( connectionNode.getLogicalId() ).thenReturn( "CONNECTION_ID" );

    IMetaverseNode resourceNode = analyzer.createTableNode( resourceInfo );
    assertEquals( "myCollection", resourceNode.getProperty( MongoDbInputStepAnalyzer.COLLECTION ) );
    assertEquals( "myCollection", resourceNode.getName() );
    assertEquals( "CONNECTION_ID", resourceNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
  }

  @Test
  public void testCreateOutputFieldNode() throws Exception {

    IAnalysisContext context = mock( IAnalysisContext.class );
    ValueMetaInterface vmi = new ValueMeta( "field1" );
    MongoField mongoField1 = new MongoField();
    mongoField1.m_fieldName = "field1";
    mongoField1.m_fieldPath = "$.field1";
    mongoField1.m_arrayIndexInfo = "range";
    mongoField1.m_occurenceFraction = "occurence";
    mongoField1.m_indexedVals = Arrays.asList( new String[] { "one", "two" } );
    mongoField1.m_disparateTypes = true;
    mongoField1.m_kettleType = "ValueMetaString";
    mongoField1.m_outputIndex = 0;
    List<MongoField> mongoFields = Arrays.asList( mongoField1 );
    when( meta.getMongoFields() ).thenReturn( mongoFields );

    doReturn( "thisStepName" ).when( analyzer ).getStepName();
    when( node.getLogicalId() ).thenReturn( "logical id" );

    IMetaverseNode node = analyzer.createOutputFieldNode(
      context,
      vmi,
      ExternalResourceStepAnalyzer.RESOURCE,
      DictionaryConst.NODE_TYPE_TRANS_FIELD );

    assertNotNull( node );
    assertEquals( "field1", node.getName() );
    assertEquals( mongoField1.m_fieldPath, node.getProperty( MongoDbInputStepAnalyzer.JSON_PATH ) );
    assertEquals( mongoField1.m_arrayIndexInfo, node.getProperty( MongoDbInputStepAnalyzer.MINMAX_RANGE ) );
    assertEquals( mongoField1.m_occurenceFraction, node.getProperty( MongoDbInputStepAnalyzer.OCCUR_RATIO ) );
    assertEquals( mongoField1.m_indexedVals, node.getProperty( MongoDbInputStepAnalyzer.INDEXED_VALS ) );
    assertEquals( mongoField1.m_disparateTypes, node.getProperty( MongoDbInputStepAnalyzer.DISPARATE_TYPES ) );

  }

  @Test
  public void testCreateOutputFieldNode_noFields() throws Exception {

    ValueMetaInterface vmi = new ValueMeta( "field1" );
    IAnalysisContext context = mock( IAnalysisContext.class );
    when( meta.getMongoFields() ).thenReturn( null );

    doReturn( "thisStepName" ).when( analyzer ).getStepName();
    when( node.getLogicalId() ).thenReturn( "logical id" );

    IMetaverseNode node = analyzer.createOutputFieldNode(
      context,
      vmi,
      ExternalResourceStepAnalyzer.RESOURCE,
      DictionaryConst.NODE_TYPE_TRANS_FIELD );

    assertEquals( "field1", node.getName() );
    assertNull( node.getProperty( MongoDbInputStepAnalyzer.JSON_PATH ) );
    assertNull( node.getProperty( MongoDbInputStepAnalyzer.MINMAX_RANGE ) );
    assertNull( node.getProperty( MongoDbInputStepAnalyzer.OCCUR_RATIO ) );
    assertNull( node.getProperty( MongoDbInputStepAnalyzer.INDEXED_VALS ) );
    assertNull( node.getProperty( MongoDbInputStepAnalyzer.DISPARATE_TYPES ) );


  }

  @Test
  public void testMongoDbInputExternalResourceConsumer() throws Exception {
    MongoDbInputExternalResourceConsumer consumer = new MongoDbInputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", this.meta );
    StepMeta spyMeta = spy( meta );

    when( this.meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    assertFalse( consumer.isDataDriven( this.meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( this.meta );
    assertNotNull( resources );
    assertEquals( 1, resources.size() );

    assertEquals( MongoDbInputMeta.class, consumer.getMetaClass() );
  }

  @Test
  public void testGetSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( MongoDbInputMeta.class ) );
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_DATA_COLUMN, analyzer.getResourceInputNodeType() );
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
}

