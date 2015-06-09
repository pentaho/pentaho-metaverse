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

package org.pentaho.metaverse.analyzer.kettle.step.tableinput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.DatabaseConnectionAnalyzer;
import org.pentaho.metaverse.api.model.BaseDatabaseResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 5/29/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class TableInputStepAnalyzerTest {

  TableInputStepAnalyzer analyzer;

  @Mock
  TableInputMeta meta;
  @Mock
  DatabaseMeta dbMeta;
  @Mock
  INamespace mockNamespace;
  @Mock
  IMetaverseNode connectionNode;
  @Mock
  IMetaverseBuilder builder;


  IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    analyzer = spy( new TableInputStepAnalyzer() );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );
    analyzer.setBaseStepMeta( meta );

    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( builder.getMetaverseObjectFactory() ).thenReturn( factory );

    analyzer.setMetaverseBuilder( builder );
  }

  @Test
  public void testGetSupportedSteps() throws Exception {
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TableInputMeta.class ) );
  }

  @Test
  public void testCreateTableNode() throws Exception {

    BaseDatabaseResourceInfo resourceInfo = mock( BaseDatabaseResourceInfo.class );
    Map<Object, Object> attributes = new HashMap<>();
    attributes.put( DictionaryConst.PROPERTY_QUERY, "select * from mytable" );
    when( resourceInfo.getAttributes() ).thenReturn( attributes );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();
    when( connectionNode.getLogicalId() ).thenReturn( "CONNECTION_ID" );

    IMetaverseNode resourceNode = analyzer.createTableNode( resourceInfo );
    assertEquals( "select * from mytable", resourceNode.getProperty( DictionaryConst.PROPERTY_QUERY ) );
    assertEquals( "SQL", resourceNode.getName() );
    assertEquals( "CONNECTION_ID", resourceNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );

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

  @Test
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( meta ) );
  }

  @Test
  public void testGetConnectionNode() throws Exception {
    DatabaseConnectionAnalyzer dbAnalyzer = mock( DatabaseConnectionAnalyzer.class );
    when( meta.getDatabaseMeta() ).thenReturn( dbMeta );
    when( dbAnalyzer.analyze( descriptor, dbMeta ) ).thenReturn( connectionNode );
    doReturn( dbAnalyzer ).when( analyzer ).getConnectionAnalyzer();

    IMetaverseNode node = analyzer.getConnectionNode();

    verify( analyzer ).getConnectionAnalyzer();
    verify( dbAnalyzer ).analyze( descriptor, dbMeta );
    assertEquals( node, connectionNode );
  }
}