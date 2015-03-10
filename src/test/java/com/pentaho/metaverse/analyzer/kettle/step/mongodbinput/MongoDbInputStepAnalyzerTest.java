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
import com.pentaho.metaverse.analyzer.kettle.IConnectionAnalyzer;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.MetaverseComponentDescriptor;
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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mongodbinput.MongoDbInputMeta;
import org.pentaho.mongo.wrapper.field.MongoField;
import org.pentaho.platform.api.metaverse.IComponentDescriptor;
import org.pentaho.platform.api.metaverse.IMetaverseBuilder;
import org.pentaho.platform.api.metaverse.IMetaverseNode;
import org.pentaho.platform.api.metaverse.IMetaverseObjectFactory;
import org.pentaho.platform.api.metaverse.INamespace;
import org.pentaho.platform.api.metaverse.MetaverseAnalyzerException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 3/6/15
 */
@RunWith( MockitoJUnitRunner.class )
public class MongoDbInputStepAnalyzerTest {
  MongoDbInputStepAnalyzer analyzer;

  @Mock
  private IMetaverseBuilder mockBuilder;
  @Mock
  private MongoDbInputMeta mongoDbInputMeta;
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
    analyzer = new MongoDbInputStepAnalyzer();
    analyzer.setConnectionAnalyzer( mock( IConnectionAnalyzer.class ) );
    analyzer.setMetaverseBuilder( mockBuilder );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS, mockNamespace );

    when( mongoDbInputMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( parentStepMeta.getName() ).thenReturn( "test" );
    when( parentStepMeta.getStepID() ).thenReturn( "MongoDbInput" );

  }

  @Test( expected = MetaverseAnalyzerException.class )
  public void testNullAnalyze() throws MetaverseAnalyzerException {
    analyzer.analyze( descriptor, null );
  }

  @Test
  public void testAnalyze() throws Exception {
    when( mongoDbInputMeta.getJsonQuery() ).thenReturn( "{test:test}" );
    when( mongoDbInputMeta.getCollection() ).thenReturn( "myCollection" );
    // Call analyze now just for branch coverage, we'll do a full assertion test afterwards
    IMetaverseNode node = analyzer.analyze( descriptor, mongoDbInputMeta );
    assertNotNull( node );

    MongoField mongoField1 = new MongoField();
    mongoField1.m_fieldName = "field1";
    mongoField1.m_fieldPath = "$.field1";
    mongoField1.m_kettleType = "ValueMetaString";
    mongoField1.m_outputIndex = 0;

    List<MongoField> mongoFields = Arrays.asList( mongoField1 );
    when( mongoDbInputMeta.getMongoFields() ).thenReturn( mongoFields );

    node = analyzer.analyze( descriptor, mongoDbInputMeta );
    assertNotNull( node );

    assertEquals( "{test:test}", node.getProperty( DictionaryConst.PROPERTY_QUERY ) );
    assertEquals( "myCollection", node.getProperty( MongoDbInputStepAnalyzer.COLLECTION ) );

    // Test the "Output JSON" option
    doReturn( true ).when( mongoDbInputMeta ).getOutputJson();
    node = analyzer.analyze( descriptor, mongoDbInputMeta );
    assertNotNull( node );
    assertTrue( (Boolean) node.getProperty( "fullJSON" ) );
  }

  @Test
  public void testMongoDbInputExternalResourceConsumer() throws Exception {
    MongoDbInputExternalResourceConsumer consumer = new MongoDbInputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", mongoDbInputMeta );
    StepMeta spyMeta = spy( meta );

    when( mongoDbInputMeta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );

    assertFalse( consumer.isDataDriven( mongoDbInputMeta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( mongoDbInputMeta );
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

}

