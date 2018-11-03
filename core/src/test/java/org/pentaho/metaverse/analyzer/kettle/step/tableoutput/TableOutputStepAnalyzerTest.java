/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.mockito.Mockito;
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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

  private static final String TABLE_NAME = "myTableName";

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

    when( parentTransMeta.environmentSubstitute( TABLE_NAME ) ).thenReturn( TABLE_NAME );
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
    attributes.put( DictionaryConst.PROPERTY_TABLE, TABLE_NAME );
    attributes.put( DictionaryConst.PROPERTY_SCHEMA, "schemaName" );
    when( resourceInfo.getAttributes() ).thenReturn( attributes );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();
    when( connectionNode.getLogicalId() ).thenReturn( "CONNECTION_ID" );

    IMetaverseNode resourceNode = analyzer.createTableNode( resourceInfo );
    assertEquals( TABLE_NAME, resourceNode.getProperty( DictionaryConst.PROPERTY_TABLE ) );
    assertEquals( TABLE_NAME, resourceNode.getName() );
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
    attributes.put( DictionaryConst.PROPERTY_TABLE, TABLE_NAME );
    when( resourceInfo.getAttributes() ).thenReturn( attributes );

    IMetaverseNode connectionNode = mock( IMetaverseNode.class );
    doReturn( connectionNode ).when( analyzer ).getConnectionNode();
    when( connectionNode.getLogicalId() ).thenReturn( "CONNECTION_ID" );

    IMetaverseNode resourceNode = analyzer.createTableNode( resourceInfo );
    assertEquals( TABLE_NAME, resourceNode.getProperty( DictionaryConst.PROPERTY_TABLE ) );
    assertEquals( TABLE_NAME, resourceNode.getName() );
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
  public void testGetOutputResourceFieldsRenamed() throws Exception {
    String[] tableFields = new String[] { "foo", "foe", "fee" };
    when( meta.getFieldDatabase() ).thenReturn( tableFields );
    String[] streamFields = new String[] { "field2", "field3", "bogus" };
    when( meta.getFieldStream() ).thenReturn( streamFields );

    RowMetaInterface rmi = Mockito.mock( RowMetaInterface.class );
    String[] outputFields = new String[2];
    outputFields[0] = "field2";
    outputFields[1] = "field3";
    doReturn( rmi ).when( analyzer ).getOutputFields( meta );
    when( rmi.getFieldNames() ).thenReturn( outputFields );

    // with "specify fields" set to false, the field rename defined within the db fields table should be ignored
    when( meta.specifyFields() ).thenReturn( Boolean.FALSE );

    Set<String> outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( outputFields.length, outputResourceFields.size() );
    for ( String outputField : outputFields ) {
      assertTrue( outputResourceFields.contains( outputField ) );
    }

    // "specify fields" is selected, fields should be renamed
    when( meta.specifyFields() ).thenReturn( Boolean.TRUE );
    outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( tableFields.length, outputResourceFields.size() );
    for ( String tableField : tableFields ) {
      assertTrue( outputResourceFields.contains( tableField ) );
    }

  }

  @Test
  public void testGetOutputResourceFields() throws Exception {
    String[] tableFields = new String[3];
    tableFields[0] = "field1";
    tableFields[1] = "field2";
    tableFields[2] = "field3";
    when( meta.getFieldDatabase() ).thenReturn( tableFields );

    RowMetaInterface rmi = Mockito.mock( RowMetaInterface.class );
    String[] outputFields = new String[2];
    outputFields[0] = "out-field1";
    outputFields[1] = "out-field2";

    // specify fields is false (default)
    doReturn( rmi ).when( analyzer ).getOutputFields( meta );
    when( rmi.getFieldNames() ).thenReturn( outputFields );

    Set<String> outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( outputFields.length, outputResourceFields.size() );
    for ( String outputField : outputFields ) {
      assertTrue( outputResourceFields.contains( outputField ) );
    }

    // specify fields is true
    when( meta.specifyFields() ).thenReturn( Boolean.TRUE );
    outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( tableFields.length, outputResourceFields.size() );
    for ( String tableField : tableFields ) {
      assertTrue( outputResourceFields.contains( tableField ) );
    }
  }

  @Test
  public void testGetChangeRecords() throws Exception {
    String[] tableFields = new String[] { "field2", "field3" };
    when( meta.getFieldDatabase() ).thenReturn( tableFields );
    String[] streamFields = new String[] { "field2", "field3" };
    when( meta.getFieldStream() ).thenReturn( streamFields );

    StepNodes inputs = new StepNodes();
    inputs.addNode( "prevStep", "f1", node );
    inputs.addNode( "prevStep", "field2", node );
    inputs.addNode( "prevStep", "field3", node );
    inputs.addNode( ExternalResourceStepAnalyzer.RESOURCE, "field2", node );
    inputs.addNode( ExternalResourceStepAnalyzer.RESOURCE, "field3", node );

    doReturn( inputs ).when( analyzer ).getInputs();

    // specify db fields box is NOT selected (by default), no changes are expected
    Set<ComponentDerivationRecord> changeRecords = analyzer.getChangeRecords( meta );
    assertNotNull( changeRecords );
    assertEquals( 0, changeRecords.size() );

    // specify db fields box is selected, but no fields are renamed
    when( meta.specifyFields() ).thenReturn( true );
    changeRecords = analyzer.getChangeRecords( meta );
    assertNotNull( changeRecords );
    assertEquals( 0, changeRecords.size() );

    // one of the fields is changing
    tableFields = new String[] { "field2-new", "field3" };
    when( meta.getFieldDatabase() ).thenReturn( tableFields );
    changeRecords = analyzer.getChangeRecords( meta );
    assertNotNull( changeRecords );
    assertEquals( 1, changeRecords.size() );
    ComponentDerivationRecord field2 = changeRecords.stream().filter( record -> record.getOriginalEntityName()
      .equals( "field2" ) ).findFirst().orElse( null );
    assertNotNull( field2 );
    assertEquals( field2.getChangedEntityName(), "field2-new" );

    // both fields are changing
    tableFields = new String[] { "field2-new", "field3-new" };
    when( meta.getFieldDatabase() ).thenReturn( tableFields );
    changeRecords = analyzer.getChangeRecords( meta );
    assertNotNull( changeRecords );
    assertEquals( 2, changeRecords.size() );

    field2 = changeRecords.stream().filter( record -> record.getOriginalEntityName().equals( "field2" ) )
      .findFirst().orElse( null );
    assertNotNull( field2 );
    assertEquals( field2.getChangedEntityName(), "field2-new" );
    ComponentDerivationRecord field3 = changeRecords.stream().filter( record -> record.getOriginalEntityName()
      .equals( "field3" ) ).findFirst().orElse( null );
    assertNotNull( field3 );
    assertEquals( field3.getChangedEntityName(), "field3-new" );

    // field is changing, but the specify db fields checkbox is NOT selected, therefore the change should be ignored
    when( meta.specifyFields() ).thenReturn( false );
    changeRecords = analyzer.getChangeRecords( meta );
    assertNotNull( changeRecords );
    assertEquals( 0, changeRecords.size() );
  }

  @Test
  public void testGetOutputRowMetaInterfaces() throws Exception {
    String[] nextStepNames = new String[] { "nextStep1" };
    when( parentTransMeta.getNextStepNames( parentStepMeta ) ).thenReturn( nextStepNames );

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    doReturn( rmi ).when( analyzer ).getOutputFields( meta );

    String[] tableFields = new String[] { "field1", "field2" };
    when( meta.getFieldDatabase() ).thenReturn( tableFields );

    // fields are specified - use the values returned by getFieldDatabase()
    when( meta.specifyFields() ).thenReturn( true );
    Map<String, RowMetaInterface> outputs = analyzer.getOutputRowMetaInterfaces( meta );
    assertNotNull( outputs );

    assertEquals( 2, outputs.size() );
    RowMetaInterface resourceRow = outputs.get( ExternalResourceStepAnalyzer.RESOURCE );
    assertNotNull( resourceRow );
    assertEquals( 2, resourceRow.size() );

    for ( String tableField : tableFields ) {
      assertNotNull( resourceRow.searchValueMeta( tableField ) );
    }
    assertEquals( rmi, outputs.get( "nextStep1" ) );

    // fields NOT specified - use the step output fields instead of the table fields specified in the DB colum table
    when( meta.specifyFields() ).thenReturn( false );
    RowMetaInterface rmi2 = Mockito.mock( RowMetaInterface.class );
    String[] outputFields = new String[] { "foo" };
    when( rmi2.getFieldNames() ).thenReturn( outputFields );
    doReturn( rmi2 ).when( analyzer ).getOutputFields( meta );

    outputs = analyzer.getOutputRowMetaInterfaces( meta );
    assertNotNull( outputs );

    assertEquals( 2, outputs.size() );
    resourceRow = outputs.get( ExternalResourceStepAnalyzer.RESOURCE );
    assertNotNull( resourceRow );
    assertEquals( 1, resourceRow.size() );

    for ( String outputField : outputFields ) {
      assertNotNull( resourceRow.searchValueMeta( outputField ) );
    }
    assertEquals( rmi2, outputs.get( "nextStep1" ) );
  }
}
