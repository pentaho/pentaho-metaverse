/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metaverse.analyzer.kettle.step.tableinput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.analyzer.kettle.step.ClonableStepAnalyzerTest;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.DatabaseConnectionAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.BaseDatabaseResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 5/29/15.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TableInputStepAnalyzerTest extends ClonableStepAnalyzerTest {

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
  public void testSetBaseStepMeta() throws Exception {
    analyzer.setBaseStepMeta( meta );
    DatabaseConnectionAnalyzer dbAnalyzer = mock( DatabaseConnectionAnalyzer.class );
    doReturn( dbAnalyzer ).when( analyzer ).getConnectionAnalyzer();
    analyzer.getConnectionNode();
    verify( meta, times( 1 ) ).getDatabaseMeta();
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

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new TableInputStepAnalyzer();
  }
}
