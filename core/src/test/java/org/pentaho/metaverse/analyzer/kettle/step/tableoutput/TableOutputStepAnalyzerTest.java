/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.MetaverseTransientNode;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IConnectionAnalyzer;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.model.BaseDatabaseResourceInfo;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * @author mburgess
 */
@RunWith(MockitoJUnitRunner.class)
public class TableOutputStepAnalyzerTest {

  private TableOutputStepAnalyzer analyzer;

  @Mock IMetaverseNode node;

  @Mock
  private IMetaverseBuilder mockBuilder;
  @Mock
  private TableOutputMeta meta;
  @Mock
  private INamespace mockNamespace;
  @Mock
  private StepMeta parentStepMeta;
  @Mock
  private TransMeta parentTransMeta;

  IComponentDescriptor descriptor;

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );
    analyzer = spy( new TableOutputStepAnalyzer() );
    analyzer.setConnectionAnalyzer( mock( IConnectionAnalyzer.class ) );
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.setBaseStepMeta( meta );
    analyzer.setRootNode( node );
    analyzer.setParentTransMeta( parentTransMeta );
    analyzer.setParentStepMeta( parentStepMeta );

    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );

    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    when( parentStepMeta.getName() ).thenReturn( "test" );
    when( parentStepMeta.getStepID() ).thenReturn( "TableOutputStep" );
  }

  @Test
  public void testGetSupportedSteps() {
    TableOutputStepAnalyzer analyzer = new TableOutputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( TableOutputMeta.class ) );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    when( meta.truncateTable() ).thenReturn( true );
    IMetaverseNode node = new MetaverseTransientNode( "new node" );
    analyzer.customAnalyze( meta, node );
    assertNotNull( node );

    assertTrue( (Boolean) node.getProperty( TableOutputStepAnalyzer.TRUNCATE_TABLE ) );
  }

  @Test
  public void testCreateTableNode() throws Exception {
    IConnectionAnalyzer connectionAnalyzer = mock( IConnectionAnalyzer.class );

    doReturn( connectionAnalyzer ).when( analyzer ).getConnectionAnalyzer();
    IMetaverseNode connNode = mock( IMetaverseNode.class );
    when( connectionAnalyzer.analyze( any( IComponentDescriptor.class ), anyObject() ) ).thenReturn( connNode );


    BaseDatabaseResourceInfo resourceInfo = mock( BaseDatabaseResourceInfo.class );
    Map<Object, Object> attributes = new HashMap<>();
    attributes.put( DictionaryConst.PROPERTY_TABLE, "tableName" );
    attributes.put( DictionaryConst.PROPERTY_SCHEMA, "schemaName" );
    when( resourceInfo.getAttributes() ).thenReturn( attributes );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();
    when( connectionNode.getLogicalId() ).thenReturn( "CONNECTION_ID" );

    IMetaverseNode resourceNode = analyzer.createTableNode( resourceInfo );
    assertEquals( "tableName", resourceNode.getProperty( DictionaryConst.PROPERTY_TABLE ) );
    assertEquals( "tableName", resourceNode.getName() );
    assertEquals( "schemaName", resourceNode.getProperty( DictionaryConst.PROPERTY_SCHEMA ) );
    assertEquals( "CONNECTION_ID", resourceNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
  }

  @Test
  public void testCreateTableNode_nullSchema() throws Exception {
    IConnectionAnalyzer connectionAnalyzer = mock( IConnectionAnalyzer.class );

    doReturn( connectionAnalyzer ).when( analyzer ).getConnectionAnalyzer();
    IMetaverseNode connNode = mock( IMetaverseNode.class );
    when( connectionAnalyzer.analyze( any( IComponentDescriptor.class ), anyObject() ) ).thenReturn( connNode );


    BaseDatabaseResourceInfo resourceInfo = mock( BaseDatabaseResourceInfo.class );
    Map<Object, Object> attributes = new HashMap<>();
    attributes.put( DictionaryConst.PROPERTY_TABLE, "tableName" );
    when( resourceInfo.getAttributes() ).thenReturn( attributes );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();
    when( connectionNode.getLogicalId() ).thenReturn( "CONNECTION_ID" );

    IMetaverseNode resourceNode = analyzer.createTableNode( resourceInfo );
    assertEquals( "tableName", resourceNode.getProperty( DictionaryConst.PROPERTY_TABLE ) );
    assertEquals( "tableName", resourceNode.getName() );
    assertNull( resourceNode.getProperty( DictionaryConst.PROPERTY_SCHEMA ) );
    assertEquals( "CONNECTION_ID", resourceNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
  }

  @Test
  public void testCreateTableNode_nullTable() throws Exception {
    IConnectionAnalyzer connectionAnalyzer = mock( IConnectionAnalyzer.class );

    doReturn( connectionAnalyzer ).when( analyzer ).getConnectionAnalyzer();
    IMetaverseNode connNode = mock( IMetaverseNode.class );
    when( connectionAnalyzer.analyze( any( IComponentDescriptor.class ), anyObject() ) ).thenReturn( connNode );


    BaseDatabaseResourceInfo resourceInfo = mock( BaseDatabaseResourceInfo.class );
    Map<Object, Object> attributes = new HashMap<>();
    when( resourceInfo.getAttributes() ).thenReturn( attributes );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();
    when( connectionNode.getLogicalId() ).thenReturn( "CONNECTION_ID" );

    IMetaverseNode resourceNode = analyzer.createTableNode( resourceInfo );
    assertNull( resourceNode.getProperty( DictionaryConst.PROPERTY_TABLE ) );
    assertNull( resourceNode.getProperty( DictionaryConst.PROPERTY_SCHEMA ) );
    assertEquals( "CONNECTION_ID", resourceNode.getProperty( DictionaryConst.PROPERTY_NAMESPACE ) );
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertNull( analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_DATA_COLUMN, analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertTrue( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertFalse( analyzer.isInput() );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( meta ) );
  }

  @Test
  public void testGetOutputResourceFields() throws Exception {
    String[] outputFields = new String[2];
    outputFields[0] = "field1";
    outputFields[1] = "field2";

    when( meta.getFieldDatabase() ).thenReturn( outputFields );

    Set<String> outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( outputFields.length, outputResourceFields.size() );
    for ( String outputField : outputFields ) {
      assertTrue( outputResourceFields.contains( outputField ) );
    }
  }

  /**
   * Test case for http://jira.pentaho.com/browse/PDI-14959 issue.
   *
   * @throws Exception
   */
  @Test
  public void testGetOutputResourceFieldsWithoutSpecifiedFields() throws Exception {
    String[] outputFields = new String[2];
    outputFields[0] = "fieldA";
    outputFields[1] = "fieldB";
    RowMetaInterface rmi = mock( RowMetaInterface.class );

    when( meta.getFieldDatabase() ).thenReturn( new String[0] );
    when( meta.specifyFields() ).thenReturn( Boolean.FALSE );
    when( rmi.getFieldNames() ).thenReturn( outputFields );

    doReturn( rmi ).when( analyzer ).getOutputFields( meta );

    Set<String> outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( outputFields.length, outputResourceFields.size() );
    for ( String outputField : outputFields ) {
      assertTrue( outputResourceFields.contains( outputField ) );
    }

    when( meta.specifyFields() ).thenReturn( Boolean.TRUE );
    outputResourceFields = analyzer.getOutputResourceFields( meta );
    assertEquals( 0, outputResourceFields.size() );
  }

  @Test
  public void testGetChangeRecords() throws Exception {
    String[] tableFields = new String[] { "field1", "field2" };
    when( meta.getFieldDatabase() ).thenReturn( tableFields );

    String[] streamFields = new String[] { "f1", "field2", "field3" };
    when( meta.getFieldStream() ).thenReturn( streamFields );

    StepNodes inputs = new StepNodes();
    inputs.addNode( "prevStep", "f1", node );
    inputs.addNode( "prevStep", "field2", node );
    inputs.addNode( "prevStep", "field3", node );
    inputs.addNode( ExternalResourceStepAnalyzer.RESOURCE, "field1", node );
    inputs.addNode( ExternalResourceStepAnalyzer.RESOURCE, "field2", node );

    doReturn( inputs ).when( analyzer ).getInputs();

    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( meta );
    assertNotNull( changeRecords );
    assertEquals( tableFields.length, changeRecords.size() );

    for ( ComponentDerivationRecord changeRecord : changeRecords ) {
      if ( "f1".equals( changeRecord.getOriginalEntityName() ) ) {
        assertEquals( "field1", changeRecord.getChangedEntityName() );
      } else if ( "field2".equals( changeRecord.getOriginalEntityName() ) ) {
        assertEquals( "field2", changeRecord.getChangedEntityName() );
      } else {
        fail( "We encountered a change record that shouldn't be here - " + changeRecord.toString() );
      }
    }

  }

  @Test
  public void testGetOutputRowMetaInterfaces() throws Exception {
    String[] nextStepNames = new String[] { "nextStep1" };
    when( parentTransMeta.getNextStepNames( parentStepMeta ) ).thenReturn( nextStepNames );

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    doReturn( rmi ).when( analyzer ).getOutputFields( meta );

    String[] tableFields = new String[] { "field1", "field2" };
    when( meta.getFieldDatabase() ).thenReturn( tableFields );

    Map<String, RowMetaInterface> outputs = analyzer.getOutputRowMetaInterfaces( meta );
    assertNotNull( outputs );

    assertEquals( 2, outputs.size() );
    RowMetaInterface resourceRow = outputs.get( ExternalResourceStepAnalyzer.RESOURCE );
    assertNotNull( resourceRow );

    for ( String tableField : tableFields ) {
      assertNotNull( resourceRow.searchValueMeta( tableField ) );
    }
    assertEquals( rmi, outputs.get( "nextStep1" ) );
  }
}
