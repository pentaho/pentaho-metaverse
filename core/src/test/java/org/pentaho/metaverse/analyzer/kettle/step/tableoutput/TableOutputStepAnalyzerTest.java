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


package org.pentaho.metaverse.analyzer.kettle.step.tableoutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author mburgess
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
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

    lenient().when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );

    lenient().when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    lenient().when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    lenient().when( parentStepMeta.getName() ).thenReturn( "test" );
    lenient().when( parentStepMeta.getStepID() ).thenReturn( "TableOutputStep" );

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
    lenient().when( meta.getFieldDatabase() ).thenReturn( tableFields );
    String[] streamFields = new String[] { "field2", "field3", "bogus" };
    lenient().when( meta.getFieldStream() ).thenReturn( streamFields );

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
