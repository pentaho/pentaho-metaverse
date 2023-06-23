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

package org.pentaho.metaverse;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import org.pentaho.di.trans.steps.http.HTTPMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;
import org.pentaho.metaverse.frames.CalculatorStepNode;
import org.pentaho.metaverse.frames.CsvFileInputStepNode;
import org.pentaho.metaverse.frames.DatabaseColumnNode;
import org.pentaho.metaverse.frames.DatabaseTableNode;
import org.pentaho.metaverse.frames.DatasourceNode;
import org.pentaho.metaverse.frames.FieldNode;
import org.pentaho.metaverse.frames.FileInputStepNode;
import org.pentaho.metaverse.frames.FilterRowsStepNode;
import org.pentaho.metaverse.frames.FixedFileInputStepNode;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.GroupByStepNode;
import org.pentaho.metaverse.frames.HttpClientStepNode;
import org.pentaho.metaverse.frames.HttpPostStepNode;
import org.pentaho.metaverse.frames.JobEntryNode;
import org.pentaho.metaverse.frames.JobNode;
import org.pentaho.metaverse.frames.KettleNode;
import org.pentaho.metaverse.frames.LocatorNode;
import org.pentaho.metaverse.frames.MergeJoinStepNode;
import org.pentaho.metaverse.frames.RowsToResultStepNode;
import org.pentaho.metaverse.frames.SelectValuesTransStepNode;
import org.pentaho.metaverse.frames.SplitFieldsStepNode;
import org.pentaho.metaverse.frames.StreamFieldNode;
import org.pentaho.metaverse.frames.StreamLookupStepNode;
import org.pentaho.metaverse.frames.TableInputStepNode;
import org.pentaho.metaverse.frames.TableOutputStepNode;
import org.pentaho.metaverse.frames.TextFileInputNode;
import org.pentaho.metaverse.frames.TextFileOutputStepNode;
import org.pentaho.metaverse.frames.TransExecutorStepNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;
import org.pentaho.metaverse.util.MetaverseUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: RFellows Date: 8/20/14
 */
public abstract class MetaverseValidationIT extends BaseMetaverseValidationIT {

  private static final String ROOT_FOLDER = "src/it/resources/repo/validation";
  private static final String OUTPUT_FILE = "target/outputfiles/validationGraph.graphml";

  /**
   * Call in the child class's BeforeClass method.
   */
  public static void init() throws Exception {
    BaseMetaverseValidationIT.init( ROOT_FOLDER, OUTPUT_FILE );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();
  }

  @Test
  public void testRootEntity() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_ROOT_ENTITY, root.getType() );
    assertEquals( "METAVERSE", root.getName() );
    assertEquals( "Engineering", root.getDivision() );
    assertEquals( "Pentaho Data Lineage", root.getProject() );
    assertNotNull( root.getDescription() );
  }

  @Test
  public void testEntity_Transformation() throws Exception {
    FramedMetaverseNode node = root.getEntity( DictionaryConst.NODE_TYPE_TRANS );
    assertEquals( DictionaryConst.NODE_TYPE_ENTITY, node.getType() );
    assertEquals( DictionaryConst.NODE_TYPE_TRANS, node.getName() );
    assertEquals( "Pentaho Data Integration", node.getDescription() );
  }

  @Test
  public void testEntity_Job() throws Exception {
    FramedMetaverseNode node = root.getEntity( DictionaryConst.NODE_TYPE_JOB );
    assertEquals( DictionaryConst.NODE_TYPE_ENTITY, node.getType() );
    assertEquals( DictionaryConst.NODE_TYPE_JOB, node.getName() );
    assertEquals( "Pentaho Data Integration", node.getDescription() );
  }

  @Test
  public void testEntity_FileSystemLocator() throws Exception {
    LocatorNode node =
      (LocatorNode) framedGraph.getVertex( "{\"name\":\"" + REPO_ID + "\",\"type\":\"Locator\"}", LocatorNode.class );
    assertEquals( DictionaryConst.NODE_TYPE_LOCATOR, node.getType() );
    assertEquals( REPO_ID, node.getName() );
    assertNotNull( node.getDescription() );
    assertNotNull( node.getUrl() );
    assertNotNull( node.getLastScan() );

    File folder = new File( ROOT_FOLDER );

    assertFileNameEquals(
            FileUtils.listFiles( folder, new String[] { "ktr", "kjb" }, true ),
            node.getDocuments());
  }

  @Test
  public void testTransformations() throws Exception {
    for ( TransformationNode node : root.getTransformations() ) {
      assertEquals( "Incorrect entity type", DictionaryConst.NODE_TYPE_TRANS, node.getEntity().getName() );

      // get the TransMeta for this node
      FileInputStream fis = new FileInputStream( node.getPath() );
      TransMeta tm = new TransMeta( fis, null, true, null, null );

      assertNotNull( tm );
      assertEquals( tm.getName(), node.getName() );
      assertEquals( tm.getTransversion(), node.getVersion() );
      assertEquals( convertNumericStatusToString( tm.getTransstatus() ), node.getStatus() );
      assertEquals( tm.getDescription(), node.getDescription() );
      assertEquals( tm.getExtendedDescription(), node.getExtendedDescription() );
      assertNotNull( node.getLastModified() );
      assertEquals( tm.getModifiedUser(), node.getLastModifiedBy() );
      assertNotNull( node.getLastModified() );
      assertEquals( tm.getCreatedUser(), node.getCreatedBy() );

      // params?
      String[] params = tm.listParameters();
      for ( String param : params ) {
        assertNotNull( "Parameter is missing [" + param + "]", node.getParameter( "parameter_" + param ) );
      }

    }
  }

  @Test
  public void testJobs() throws Exception {
    for ( JobNode node : root.getJobs() ) {
      assertEquals( "Incorrect entity type", DictionaryConst.NODE_TYPE_JOB, node.getEntity().getName() );

      // get the TransMeta for this node
      FileInputStream fis = new FileInputStream( node.getPath() );
      JobMeta jm = new JobMeta( fis, null, null );

      assertNotNull( jm );
      assertEquals( jm.getName(), node.getName() );
      assertEquals( jm.getJobversion(), node.getVersion() );
      assertEquals( convertNumericStatusToString( jm.getJobstatus() ), node.getStatus() );
      assertEquals( jm.getDescription(), node.getDescription() );
      assertEquals( jm.getExtendedDescription(), node.getExtendedDescription() );
      assertEquals( String.valueOf( jm.getModifiedDate().getTime() ), node.getLastModified() );
      assertEquals( jm.getModifiedUser(), node.getLastModifiedBy() );
      assertEquals( String.valueOf( jm.getCreatedDate().getTime() ), node.getCreated() );
      assertEquals( jm.getCreatedUser(), node.getCreatedBy() );

      // params?
      String[] params = jm.listParameters();
      for ( String param : params ) {
        assertNotNull( "Parameter is missing [" + param + "]", node.getParameter( "parameter_" + param ) );
      }

    }
  }

  @Test
  public void testJobEntryNodes() throws Exception {
    for ( JobNode jobNode : root.getJobs() ) {
      JobMeta jobMeta = new JobMeta( new FileInputStream( jobNode.getPath() ), null, null );

      int numJobEntries = jobMeta.nrJobEntries();
      int matchCount = 0;
      for ( int i = 0; i < numJobEntries; i++ ) {
        JobEntryCopy jobEntry = jobMeta.getJobEntry( i );
        assertNotNull( jobEntry );
        JobEntryNode jobEntryNode = jobNode.getJobEntryNode( jobEntry.getName() );
        assertNotNull( "Job Entry " + jobEntry.getName() + " should be in the graph!", jobEntryNode );
        assertEquals( jobEntry.getName(), jobEntryNode.getName() );
        assertEquals( jobEntry.getDescription(), jobEntryNode.getDescription() );
        assertEquals( "Incorrect type", DictionaryConst.NODE_TYPE_JOB_ENTRY, jobEntryNode.getType() );
        assertEquals( "Incorrect entity type", DictionaryConst.NODE_TYPE_JOB_ENTRY,
          jobEntryNode.getEntity().getName() );
        matchCount++;
      }

      assertEquals( "Not all job entries are accounted for in the graph for Job [" + jobMeta.getName() + "]",
        numJobEntries, matchCount );

      assertEquals( "Incorrect number of job entries for in the graph for Job [" + jobMeta.getName() + "]",
        numJobEntries, getIterableSize( jobNode.getJobEntryNodes() ) );

      // it should be contained in a "Locator" node
      jobNode.getLocator();

    }
  }

  @Test
  public void testTransformationStepNodes() throws Exception {
    for ( TransformationNode transNode : root.getTransformations() ) {
      TransMeta tm = new TransMeta( new FileInputStream( transNode.getPath() ), null, true, null, null );

      List<StepMeta> transMetaSteps = tm.getSteps();

      int matchCount = 0;

      for ( StepMeta transMetaStep : transMetaSteps ) {
        // let's see if the steps are in the graph for this transformation
        TransformationStepNode stepNode = transNode.getStepNode( transMetaStep.getName() );
        assertNotNull( stepNode );
        assertEquals( "Incorrect type", DictionaryConst.NODE_TYPE_TRANS_STEP, stepNode.getType() );
        assertEquals( "Incorrect entity type", DictionaryConst.NODE_TYPE_TRANS_STEP, stepNode.getEntity().getName() );
        ++matchCount;
      }

      assertEquals( "Not all transformation steps are modeled in the graph for [" + tm.getName() + "]",
          transMetaSteps.size(), matchCount );

      Collection<String> expectedStepNames =  new TreeSet<>(
          transMetaSteps.stream().map( sm -> sm.getName() )
            .collect( Collectors.toList() )
      );
      Collection<String> actualStepNames =  new TreeSet<>(
          StreamSupport.stream( transNode.getStepNodes().spliterator(), false )
            .map( tsn -> tsn.asVertex().getProperty( "name" )
            .toString() ).collect( Collectors.toList() )
      );

      assertEquals( "Incorrect number of Steps in the graph for transformation ["
        + tm.getName() + "]", expectedStepNames, actualStepNames );
    }
  }

  protected void testSelectValuesStep( final int outputEdgeCount ) throws Exception {
    // this tests a specific select values step. the one in trans "Populate Table From File"
    SelectValuesTransStepNode selectValues = root.getSelectValuesStepNode();
    assertNotNull( selectValues );
    int countUses = getIterableSize( selectValues.getStreamFieldNodesUses() );
    int countOutputs = getIterableSize( selectValues.getOutputStreamFields() );
    int countInputs = getIterableSize( selectValues.getInputStreamFields() );
    assertEquals( 8, countUses );
    SelectValuesMeta meta = (SelectValuesMeta) getStepMeta( selectValues );
    assertEquals( outputEdgeCount, countOutputs );
    assertEquals( 9, countInputs );
    assertEquals( "Select values", selectValues.getStepType() );

    for ( StreamFieldNode node : selectValues.getOutputStreamFields() ) {
      // check for operations
      if ( node.getOperations() != null ) {
        Operations ops = MetaverseUtil.convertOperationsStringToMap( node.getOperations() );
        assertNotNull( ops );
        List<IOperation> metadataOps = ops.get( ChangeType.METADATA );
        assertNotNull( metadataOps );
        assertTrue( metadataOps.size() > 0 );

        // there should not be any data operations on nodes touched by this step
        assertNull( ops.get( ChangeType.DATA ) );
      }

      // check the created node is derived from something
      Iterable<StreamFieldNode> deriveNodes = node.getFieldNodesThatDeriveMe();
      for ( StreamFieldNode deriveNode : deriveNodes ) {
        assertNotNull( deriveNode );
      }

    }
  }

  @Test
  public void testTextFileInputStep() throws Exception {
    // this is testing a specific TextFileInputStep instance
    FileInputStepNode fileInputStepNode = (FileInputStepNode)
      root.getFileInputStepNode( "Populate Table From File", "Sacramento crime stats 2006 file" );
    assertNotNull( fileInputStepNode );

    Iterable<FramedMetaverseNode> inputFiles = fileInputStepNode.getInputFiles();
    int countInputFiles = getIterableSize( inputFiles );
    assertEquals( 1, countInputFiles );
    for ( FramedMetaverseNode inputFile : inputFiles ) {
      assertTrue( inputFile.getName().endsWith( "SacramentocrimeJanuary2006.csv" ) );
    }

    assertEquals( "Text file input", fileInputStepNode.getStepType() );

    int countUses = getIterableSize( fileInputStepNode.getFileFieldNodesUses() );
    int countInputs = getIterableSize( fileInputStepNode.getInputStreamFields() );

    assertEquals( 0, countUses );
    int fileFieldCount = 0;
    Iterable<StreamFieldNode> outFields = fileInputStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      FieldNode fieldPopulatesMe = outField.getFieldPopulatesMe();
      assertNotNull( fieldPopulatesMe );
      assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, fieldPopulatesMe.getType() );
      assertEquals( fileInputStepNode.getProperty( "name" ), fieldPopulatesMe.getStepThatInputsMe().getProperty( "name" ) );
      fileFieldCount++;
    }
    assertEquals( countInputs, fileFieldCount );
    assertEquals( countOutputs, fileFieldCount );

  }

  @Test
  public void testOldTextFileInputStep_filenameFromField() throws Exception {
    // this is testing a specific TextFileInputStep instance
    FileInputStepNode fileInputStepNode = root.getOldTextFileInputStepNode_filenameFromField();
    assertNotNull( fileInputStepNode );

    int countUses = getIterableSize( fileInputStepNode.getFileFieldNodesUses() );
    int countInputs = getIterableSize( fileInputStepNode.getInputStreamFields() );

    assertEquals( 1, countUses );

    int fileFieldCount = 0;
    Iterable<StreamFieldNode> outFields = fileInputStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      if ( !outField.getName().equals( "filename" ) ) {
        FieldNode fieldPopulatesMe = outField.getFieldPopulatesMe();
        assertNotNull( fieldPopulatesMe );
        assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, fieldPopulatesMe.getType() );
        assertEquals( fileInputStepNode, fieldPopulatesMe.getStepThatInputsMe() );
        fileFieldCount++;
      }
    }
    // we should have one more input than file fields since we are reading it off of the input stream
    assertEquals( countInputs - 1, fileFieldCount );
    assertEquals( countOutputs, fileFieldCount );

    String filenameField = null;
    TransMeta tm =
      new TransMeta( new FileInputStream( fileInputStepNode.getTransNode().getPath() ), null, true, null, null );
    for ( StepMeta stepMeta : tm.getSteps() ) {
      if ( stepMeta.getName().equals( fileInputStepNode.getName() ) ) {
        org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta meta =
          (org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta) getBaseStepMetaFromStepMeta( stepMeta );
        assertTrue( meta.isAcceptingFilenames() );
        filenameField = meta.getAcceptingField();
        assertNotNull( filenameField );
        assertEquals( filenameField, fileInputStepNode.getFileFieldNodesUses().iterator().next().getName() );
        // this was the one we cared about...
        break;
      }
    }
  }

  @Test
  public void testTextFileInputStep_filenameFromField() throws Exception {
    // this is testing a specific TextFileInputStep instance
    FileInputStepNode fileInputStepNode = root.getTextFileInputStepNode_filenameFromField();
    assertNotNull( fileInputStepNode );

    int countUses = getIterableSize( fileInputStepNode.getFileFieldNodesUses() );
    int countInputs = getIterableSize( fileInputStepNode.getInputStreamFields() );

    assertEquals( 1, countUses );

    int fileFieldCount = 0;
    Iterable<StreamFieldNode> outFields = fileInputStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      if ( !outField.getName().equals( "filename" ) ) {
        FieldNode fieldPopulatesMe = outField.getFieldPopulatesMe();
        assertNotNull( fieldPopulatesMe );
        assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, fieldPopulatesMe.getType() );
        assertEquals( fileInputStepNode, fieldPopulatesMe.getStepThatInputsMe() );
        fileFieldCount++;
      }
    }
    // we should have one more input than file fields since we are reading it off of the input stream
    assertEquals( countInputs - 1, fileFieldCount );
    assertEquals( countOutputs, fileFieldCount );

    String filenameField = null;
    TransMeta tm =
      new TransMeta( new FileInputStream( fileInputStepNode.getTransNode().getPath() ), null, true, null, null );
    for ( StepMeta stepMeta : tm.getSteps() ) {
      if ( stepMeta.getName().equals( fileInputStepNode.getName() ) ) {
        org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta meta =
          (org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta) getBaseStepMetaFromStepMeta( stepMeta );
        assertTrue( meta.isAcceptingFilenames() );
        filenameField = meta.getAcceptingField();
        assertNotNull( filenameField );
        assertEquals( filenameField, fileInputStepNode.getFileFieldNodesUses().iterator().next().getName() );
        // this was the one we cared about...
        break;
      }
    }
  }

  @Test
  public void testDatasources() throws Exception {
    int countDatasources = getIterableSize( root.getDatasourceNodes() );
    for ( DatasourceNode ds : root.getDatasourceNodes() ) {
      // make sure at least one step uses the connection
      int countUsedSteps = getIterableSize( ds.getTransformationStepNodes() );
      assertTrue( countUsedSteps > 0 );

      assertEquals( DictionaryConst.NODE_TYPE_DATASOURCE, ds.getEntity().getName() );
      assertNotNull( ds.getName() );
      assertNotNull( ds.getDatabaseName() );
      assertNotNull( ds.getPort() );
      assertNotNull( ds.getAccessType() );
      assertNotNull( ds.getAccessTypeDesc() );
    }
    assertTrue( countDatasources > 0 );
  }

  @Test
  public void testSampleDataConnection() throws Exception {
    DatasourceNode sampleData = root.getDatasourceNode( "Sampledata" );
    assertEquals( "Sampledata", sampleData.getName() );
    assertEquals( "-1", sampleData.getPort() );
    assertEquals( "Native", sampleData.getAccessTypeDesc() );
    assertEquals( "sampledata", sampleData.getDatabaseName() );
    assertEquals( "sa", sampleData.getUserName() );
  }

  @Test
  public void testTableOutputStepNode1() throws Exception {
    testTableOutputStepNode( root.getTableOutputStepNode1(), "myTestParam1" );
  }

  @Test
  public void testTableOutputStepNode2() throws Exception {
    testTableOutputStepNode( root.getTableOutputStepNode2(), "myTestParam2" );
  }

  public void testTableOutputStepNode(
          final TableOutputStepNode node, final String expectedTableName ) throws Exception {

    // check the table that it writes to
    TableOutputMeta meta = (TableOutputMeta) getStepMeta( node );
    String schema = meta.getSchemaName();
    boolean truncateTable = meta.truncateTable();
    DatabaseTableNode databaseTableNode = node.getDatabaseTable();
    assertEquals( expectedTableName, databaseTableNode.getName() );
    assertEquals( schema, node.getSchema() );
    assertEquals( truncateTable, node.isTruncateTable() );

    Iterable<StreamFieldNode> inputs = node.getInputStreamFields();
    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();

    Collection<String> expectedNames = new TreeSet<>(
        StreamSupport.stream( inputs.spliterator(), false )
          .map( sfn -> sfn.getProperty( "name" ).toString() )
          .collect( Collectors.toList() )
    );

    expectedNames.addAll( Arrays.asList( meta.getFieldDatabase() ) );

    Collection<String> actualNames = new TreeSet<>(
        StreamSupport.stream( outputs.spliterator(), false )
          .map( sfn -> sfn.getProperty( "name" ).toString() )
          .collect( Collectors.toList() )
    );

    actualNames = new TreeSet<>( actualNames );

    assertEquals( "Stream field names do not match, meta field database fields: " + meta.getFieldDatabase() , expectedNames, actualNames );

    for ( StreamFieldNode input : inputs ) {
      assertEquals( input.getName(), ( (FramedMetaverseNode) IteratorUtils.toList(
        input.getNodesPopulatedByMe().iterator() ).get( 0 ) ).getName() );
    }

    DatasourceNode datasource = node.getDatasource( meta.getDatabaseMeta().getName() );
    assertEquals( meta.getDatabaseMeta().getHostname(), datasource.getHost() );
    assertEquals( meta.getDatabaseMeta().getDatabasePortNumberString(), datasource.getPort() );
    assertEquals( meta.getDatabaseMeta().getUsername(), datasource.getUserName() );
    assertEquals( meta.getDatabaseMeta().getDatabaseName(), datasource.getDatabaseName() );
    assertEquals( DictionaryConst.NODE_TYPE_DATASOURCE, datasource.getType() );

    assertEquals( DictionaryConst.NODE_TYPE_DATA_TABLE, databaseTableNode.getType() );

  }

  @Test
  public void testValueMapperStepNode_overwrite() throws Exception {
    TransformationStepNode valueMapperStepNode = root.getStepNode( "value_mapper", "Value Mapper - overwrite" );

    assertEquals( 1, getIterableSize( valueMapperStepNode.getStreamFieldNodesUses() ) );
    StreamFieldNode usesNode = valueMapperStepNode.getStreamFieldNodesUses().iterator().next();

    ValueMapperMeta meta = (ValueMapperMeta) getStepMeta( valueMapperStepNode );
    assertEquals( meta.getFieldToUse(), usesNode.getName() );

    Iterable<StreamFieldNode> inFields = valueMapperStepNode.getInputStreamFields();
    int countInputs = getIterableSize( inFields );

    Iterable<StreamFieldNode> outFields = valueMapperStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    assertEquals( countInputs, countOutputs );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      if ( outField.getName().equals( meta.getTargetField() ) ) {
        Operations ops = MetaverseUtil.convertOperationsStringToMap( usesNode.getOperations() );
        List<IOperation> dataOps = ops.get( ChangeType.DATA );
        assertNotNull( dataOps );
        assertEquals( meta.getSourceValue().length, dataOps.size() );
        for ( int i = 0; i < dataOps.size(); i++ ) {
          IOperation dataOp = dataOps.get( i );
          assertEquals( DictionaryConst.PROPERTY_TRANSFORMS, dataOp.getName() );
          assertEquals( dataOp.toString(), meta.getSourceValue()[ i ] + " -> " + meta.getTargetValue()[ i ] );
        }
        // there should not be any metadata operations
        assertNull( ops.get( ChangeType.METADATA ) );
      }
    }
    int derivedCount = getIterableSize( usesNode.getFieldNodesDerivedFromMe() );
    assertEquals( 1, derivedCount );
  }

  @Test
  public void testValueMapperStepNode_newField() throws Exception {
    TransformationStepNode valueMapperStepNode = root.getStepNode( "value_mapper", "Value Mapper - new field" );

    assertEquals( 1, getIterableSize( valueMapperStepNode.getStreamFieldNodesUses() ) );
    StreamFieldNode usesNode = valueMapperStepNode.getStreamFieldNodesUses().iterator().next();

    ValueMapperMeta meta = (ValueMapperMeta) getStepMeta( valueMapperStepNode );
    assertEquals( meta.getFieldToUse(), usesNode.getName() );
    assertNull( usesNode.getOperations() );

    Iterable<StreamFieldNode> inFields = valueMapperStepNode.getInputStreamFields();
    int countInputs = getIterableSize( inFields );

    Iterable<StreamFieldNode> outFields = valueMapperStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    assertEquals( countInputs + 1, countOutputs );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      if ( outField.getName().equals( meta.getTargetField() ) ) {
        Iterable<StreamFieldNode> fieldNodesThatDeriveMe = outField.getFieldNodesThatDeriveMe();
        assertNotNull( fieldNodesThatDeriveMe );
        int derivesCount = getIterableSize( outField.getFieldNodesThatDeriveMe() );
        assertEquals( 1, derivesCount );
        for ( FieldNode derives : outField.getFieldNodesThatDeriveMe() ) {
          assertEquals( usesNode.getName(), derives.getName() );
          assertEquals( usesNode.getType(), derives.getType() );
        }

        Operations ops = MetaverseUtil.convertOperationsStringToMap( outField.getOperations() );
        List<IOperation> dataOps = ops.get( ChangeType.DATA );
        assertNotNull( dataOps );
        assertEquals( 1, dataOps.size() );
        for ( IOperation dataOp : dataOps ) {
          assertEquals( Operation.MAPPING_CATEGORY, dataOp.getCategory() );
          assertEquals( DictionaryConst.PROPERTY_TRANSFORMS, dataOp.getName() );
        }

        // there should not be any metadata operations
        assertNull( ops.get( ChangeType.METADATA ) );
      }
    }
  }

  @Test
  public void testTextFileOutputStepNode() throws Exception {
    TextFileOutputStepNode textFileOutputStepNode =
      root.getTextFileOutputStepNode( "textFileOutput", "Text file output" );
    TextFileOutputMeta meta = (TextFileOutputMeta) getStepMeta( textFileOutputStepNode );
    TransMeta tm = meta.getParentStepMeta().getParentTransMeta();
    String[] fileNames = meta.getFiles( tm );

    RowMetaInterface incomingFields = tm.getPrevStepFields( meta.getParentStepMeta() );
    int outputFields = getExpectedOutputFieldCount( meta );

    assertNotNull( textFileOutputStepNode );

    Iterable<StreamFieldNode> outFields = textFileOutputStepNode.getOutputStreamFields();
    int outFieldCount = getIterableSize( outFields );
    // should have output stream nodes as well as file nodes
    assertEquals( outputFields + meta.getOutputFields().length, outFieldCount );

    int fileFieldCount = 0;
    for ( StreamFieldNode outField : outFields ) {
      if ( DictionaryConst.NODE_TYPE_FILE_FIELD.equals( outField.getType() ) ) {
        ValueMetaInterface vmi = incomingFields.searchValueMeta( outField.getName() );
        assertEquals( vmi.getName(), outField.getFieldPopulatesMe().getName() );
        fileFieldCount++;
      }
    }
    assertEquals( fileFieldCount, outFieldCount / 2 );
  }

  @Test
  public void testTextFileOutputStepNode_FileFromStreamField() throws Exception {
    TextFileOutputStepNode textFileOutputStepNode =
      root.getTextFileOutputStepNode( "textFileOutput", "Text file output - file from field" );

    TextFileOutputMeta meta = (TextFileOutputMeta) getStepMeta( textFileOutputStepNode );
    TransMeta tm = meta.getParentStepMeta().getParentTransMeta();

    RowMetaInterface incomingFields = tm.getPrevStepFields( meta.getParentStepMeta() );
    int outputFields = getExpectedOutputFieldCount( meta );

    assertNotNull( textFileOutputStepNode );
    // should write to one file
    Iterable<FramedMetaverseNode> outputFiles = textFileOutputStepNode.getOutputFiles();
    assertEquals( 0, getIterableSize( outputFiles ) );

    Iterable<StreamFieldNode> usedFields = textFileOutputStepNode.getStreamFieldNodesUses();
    int usedFieldCount = getIterableSize( usedFields );
    assertEquals( 1, usedFieldCount );

    Iterable<StreamFieldNode> outFields = textFileOutputStepNode.getOutputStreamFields();
    int outFieldCount = getIterableSize( outFields );
    // should have output stream nodes as well as file nodes
    assertEquals( outputFields + meta.getOutputFields().length, outFieldCount );

    int fileFieldCount = 0;
    for ( StreamFieldNode outField : outFields ) {
      if ( DictionaryConst.NODE_TYPE_FILE_FIELD.equals( outField.getType() ) ) {
        ValueMetaInterface vmi = incomingFields.searchValueMeta( outField.getName() );
        assertEquals( vmi.getName(), outField.getFieldPopulatesMe().getName() );
        fileFieldCount++;
      }
    }
    assertEquals( fileFieldCount, outFieldCount / 2 );

  }

  @Test
  public void testMergeJoinStepNode_duplicateFieldNames() throws Exception {
    MergeJoinStepNode node = root.getMergeJoinStepNode();
    MergeJoinMeta meta = (MergeJoinMeta) getStepMeta( node );

    assertEquals( meta.getJoinType(), node.getJoinType() );
    assertEquals( meta.getKeyFields1().length, node.getJoinFieldsLeft().size() );
    assertEquals( meta.getKeyFields2().length, node.getJoinFieldsRight().size() );

    Iterable<StreamFieldNode> usedFields = node.getStreamFieldNodesUses();
    for ( StreamFieldNode usedField : usedFields ) {
      boolean isOnLeft = node.getJoinFieldsLeft().contains( usedField.getName() );
      boolean isOnRight = node.getJoinFieldsRight().contains( usedField.getName() );
      assertTrue( isOnLeft || isOnRight );
      assertTrue( usedField.getFieldNodesThatIJoinTo() != null );
      assertTrue( usedField.getFieldNodesThatJoinToMe() != null );
    }

    Iterable<StreamFieldNode> outputFields = node.getOutputStreamFields();
    assertEquals( getExpectedOutputFieldCount( meta ), getIterableSize( outputFields ) );

    for ( StreamFieldNode outputField : outputFields ) {
      // these should have derives links
      assertTrue( outputField.getFieldNodesThatDeriveMe() != null );
    }

  }

  @Test
  public void testStreamLookupStepNode() throws Exception {
    StreamLookupStepNode node = root.getStreamLookupStepNode();

    assertEquals( 6, getIterableSize( node.getInputStreamFields() ) );
    assertEquals( 3, getIterableSize( node.getStreamFieldNodesUses() ) );
    assertEquals( 5, getIterableSize( node.getOutputStreamFields() ) );

    List<String> expectations = new ArrayList<>();
    expectations.add( "territory" );
    expectations.add( "country_ref" );

    Iterator<StreamFieldNode> iter1 = node.getOutputStreamFields().iterator();
    while ( iter1.hasNext() ) {
      StreamFieldNode outField = iter1.next();
      if ( expectations.contains( outField.getName() ) ) {
        assertEquals( 1, getIterableSize( outField.getFieldNodesThatDeriveMe() ) );
        Iterator<StreamFieldNode> iter2 = outField.getFieldNodesThatDeriveMe().iterator();
        while ( iter2.hasNext() ) {
          StreamFieldNode derivedFromNode = iter2.next();
          assertTrue( expectations.contains( derivedFromNode.getName() ) );
        }
      }
    }

    iter1 = node.getStreamFieldNodesUses().iterator();
    while ( iter1.hasNext() ) {
      StreamFieldNode usesNode = iter1.next();
      if ( usesNode.getName().equals( "country_code" ) || usesNode.getName().equals( "country_code" ) ) {
        assertEquals( 1, getIterableSize( usesNode.getFieldNodesThatJoinToMe() ) );
        StreamFieldNode joinField = usesNode.getFieldNodesThatJoinToMe().iterator().next();
        if ( usesNode.getName().equals( "country_code" ) ) {
          assertEquals( "code", joinField.getName() );
        } else {
          assertEquals( "country_code", joinField.getName() );
        }
      }
    }

  }

  @Test
  public void testCalculatorStepNode() throws Exception {
    CalculatorStepNode node = root.getCalculatorStepNode();

    Set<String> usedFields = new HashSet<String>();
    CalculatorMeta calculatorMeta = (CalculatorMeta) getStepMeta( node );
    for ( CalculatorMetaFunction calculatorMetaFunction : calculatorMeta.getCalculation() ) {
      String fieldName = calculatorMetaFunction.getFieldA();
      if ( !StringUtils.isEmpty( fieldName ) ) {
        usedFields.add( fieldName );
      }
      fieldName = calculatorMetaFunction.getFieldB();
      if ( !StringUtils.isEmpty( fieldName ) ) {
        usedFields.add( fieldName );
      }
      fieldName = calculatorMetaFunction.getFieldC();
      if ( !StringUtils.isEmpty( fieldName ) ) {
        usedFields.add( fieldName );
      }
    }
    int expectedUsedFieldCount = usedFields.size();

    // Make sure we have the right number of links used, created and deleted.
    List<String> nodeUses = new ArrayList<String>();
    for ( StreamFieldNode sfn : node.getStreamFieldNodesUses() ) {
      nodeUses.add( sfn.getName() );
    }

    StreamFieldNode area = null;
    StreamFieldNode kelvin = null;
    StreamFieldNode celsius = null;
    List<String> nodeOutputs = new ArrayList<String>();
    for ( StreamFieldNode sfn : node.getOutputStreamFields() ) {
      nodeOutputs.add( sfn.getName() );
      Operations ops = MetaverseUtil.convertOperationsStringToMap( sfn.getOperations() );
      if ( sfn.getName().equals( "area" ) ) {
        area = sfn;
        assertNotNull( ops.get( ChangeType.DATA ) );
        assertEquals( Operation.CALC_CATEGORY, ops.get( ChangeType.DATA ).get( 0 ).getCategory() );
      } else if ( sfn.getName().equals( "celsius" ) ) {
        celsius = sfn;
        assertNotNull( ops );
        assertNotNull( ops.get( ChangeType.DATA ) );
        assertEquals( Operation.CALC_CATEGORY, ops.get( ChangeType.DATA ).get( 0 ).getCategory() );
      } else if ( sfn.getName().equals( "kelvin" ) ) {
        kelvin = sfn;
        assertNotNull( ops );
        assertNotNull( ops.get( ChangeType.DATA ) );
        assertEquals( Operation.CALC_CATEGORY, ops.get( ChangeType.DATA ).get( 0 ).getCategory() );
      }
    }

    assertEquals( 2, getIterableSize( area.getFieldNodesThatDeriveMe() ) );
    String[] fieldsThatDerive = new String[ 2 ];
    fieldsThatDerive = new String[ 2 ];
    int i = 0;
    for ( StreamFieldNode sfn : celsius.getFieldNodesThatDeriveMe() ) {
      fieldsThatDerive[ i++ ] = sfn.getName();
    }

    assertTrue( Arrays.asList( fieldsThatDerive ).contains( "tempCelsius" ) );
    assertTrue( Arrays.asList( fieldsThatDerive ).contains( "tempRatio" ) );

    fieldsThatDerive = new String[ 2 ];
    i = 0;
    for ( StreamFieldNode sfn : kelvin.getFieldNodesThatDeriveMe() ) {
      fieldsThatDerive[ i++ ] = sfn.getName();
    }

    assertTrue( Arrays.asList( fieldsThatDerive ).contains( "tempKelvin" ) );
    assertTrue( Arrays.asList( fieldsThatDerive ).contains( "tempRatio" ) );

    assertEquals( expectedUsedFieldCount, nodeUses.size() );

    assertEquals( getExpectedOutputFieldCount( calculatorMeta ), nodeOutputs.size() );
  }

  @Test
  public void testCsvFileInputStep() throws Exception {
    // this is testing a specific CsvFileInputStep instance
    CsvFileInputStepNode csvFileInputStepNode = root.getCsvFileInputStepNode();
    assertNotNull( csvFileInputStepNode );

    Iterable<FramedMetaverseNode> inputFiles = csvFileInputStepNode.getInputFiles();
    int countInputFiles = getIterableSize( inputFiles );
    int countOutputs = getIterableSize( csvFileInputStepNode.getOutputStreamFields() );
    assertEquals( 1, countInputFiles );
    assertEquals( 10, countOutputs );

    for ( FramedMetaverseNode inputFile : inputFiles ) {
      assertTrue( inputFile.getName().endsWith( "customers-100.txt" ) );
    }

    assertEquals( "CSV file input", csvFileInputStepNode.getStepType() );

    int fileFieldCount = 0;
    Iterable<StreamFieldNode> outFields = csvFileInputStepNode.getOutputStreamFields();
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      FieldNode fieldPopulatesMe = outField.getFieldPopulatesMe();
      assertNotNull( fieldPopulatesMe );
      assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, fieldPopulatesMe.getType() );
      assertEquals( csvFileInputStepNode, fieldPopulatesMe.getStepThatInputsMe() );
      fileFieldCount++;
    }
    assertEquals( countOutputs, fileFieldCount );
  }

  @Test
  public void testGroupByStep() throws Exception {
    GroupByStepNode groupByStepNode = root.getGroupByStepNode();
    assertNotNull( groupByStepNode );

    GroupByMeta meta = (GroupByMeta) getStepMeta( groupByStepNode );

    int countUses = getIterableSize( groupByStepNode.getStreamFieldNodesUses() );

    int expectedUsesLinksCount = meta.getSubjectField().length + meta.getGroupField().length;
    assertEquals( expectedUsesLinksCount, countUses );
  }

  @Test
  public void testSplitFieldsStepNode_normal() throws Exception {
    SplitFieldsStepNode node = root.getSplitFieldsStepNodeByName( "Split Fields - name" );
    assertNotNull( node );
    FieldSplitterMeta meta = (FieldSplitterMeta) getStepMeta( node );
    assertEquals( meta.getDelimiter(), node.getDelimiter() );
    assertEquals( meta.getEnclosure(), node.getEnclosure() );

    String[] outputFields = meta.getFieldName();
    Iterable<StreamFieldNode> usedFields = node.getStreamFieldNodesUses();
    int usedFieldCount = getIterableSize( usedFields );
    assertEquals( 1, usedFieldCount );
    assertEquals( meta.getSplitField(), usedFields.iterator().next().getName() );

    // make sure the split field derives all of the output fields
    Iterable<StreamFieldNode> outFields = node.getOutputStreamFields();
    for ( StreamFieldNode outField : outFields ) {
      Iterable<StreamFieldNode> derivingNodes = outField.getFieldNodesThatDeriveMe();
      int derivesFieldCount = getIterableSize( derivingNodes );
      assertEquals( 1, derivesFieldCount );
      StreamFieldNode derivingField = derivingNodes.iterator().next();
      if ( !derivingField.getName().equals( "position" ) ) {
        assertEquals( derivingField.getName(), meta.getSplitField() );
      }
    }

    // make sure the node that was used is the split field
    assertEquals( meta.getSplitField(), usedFields.iterator().next().getName() );

  }

  @Test
  public void testSplitFieldsStepNode_reuseTheSplitFieldNameInOutputField() throws Exception {
    SplitFieldsStepNode node = root.getSplitFieldsStepNodeByName( "Split Fields - team" );
    assertNotNull( node );
    FieldSplitterMeta meta = (FieldSplitterMeta) getStepMeta( node );
    assertEquals( meta.getDelimiter(), node.getDelimiter() );
    assertEquals( meta.getEnclosure(), node.getEnclosure() );

    Iterable<StreamFieldNode> usedFields = node.getStreamFieldNodesUses();
    int usedFieldCount = getIterableSize( usedFields );
    // make sure the node that was used is the split field
    assertEquals( meta.getSplitField(), usedFields.iterator().next().getName() );

    assertEquals( 1, usedFieldCount );

    // make sure the split field derives all of the output fields
    // make sure the split field derives all of the output fields
    Iterable<StreamFieldNode> outFields = node.getOutputStreamFields();
    for ( StreamFieldNode outField : outFields ) {
      Iterable<StreamFieldNode> derivingNodes = outField.getFieldNodesThatDeriveMe();
      int derivesFieldCount = getIterableSize( derivingNodes );
      assertEquals( 1, derivesFieldCount );
      StreamFieldNode derivingField = derivingNodes.iterator().next();
      if ( !derivingField.getName().equals( "position" ) ) {
        assertEquals( derivingField.getName(), meta.getSplitField() );
      }
    }
  }

  @Test
  public void testStringOperationsStepNode() throws Exception {
    TransformationStepNode node = root.getStepNode( "string_operations", "String operations" );

    // Make sure we have the right number of links used
    assertEquals( 4, getIterableSize( node.getInputStreamFields() ) );
    assertEquals( 4, getIterableSize( node.getStreamFieldNodesUses() ) );
    assertEquals( 5, getIterableSize( node.getOutputStreamFields() ) );

    for ( StreamFieldNode sfn : node.getOutputStreamFields() ) {
      // "Last Name" is a special case for this test, it is passthrough
      if ( sfn.getName().equals( "Last Name" ) ) {
        assertTrue( Const.isEmpty( sfn.getOperations() ) );
      } else {
        assertFalse( Const.isEmpty( sfn.getOperations() ) );
      }
    }
  }

  @Test
  public void testStringsCutStepNode() throws Exception {
    TransformationStepNode node = root.getStepNode( "strings_cut", "Strings cut" );

    // Make sure we have the right number of links used, created and derived. Also,
    // Ensure there is an entry in the operations for those fields that are derived.
    assertEquals( 3, getIterableSize( node.getInputStreamFields() ) );
    assertEquals( 3, getIterableSize( node.getStreamFieldNodesUses() ) );
    assertEquals( 4, getIterableSize( node.getOutputStreamFields() ) );

    for ( StreamFieldNode sfn : node.getOutputStreamFields() ) {
      // "Last Name" is a special case for this test, it is passthrough
      if ( sfn.getName().equals( "Middle Name" ) ) {
        assertTrue( Const.isEmpty( sfn.getOperations() ) );
      } else {
        assertFalse( Const.isEmpty( sfn.getOperations() ) );
      }
    }
  }

  @Test
  public void testStringsReplaceStepNode() throws Exception {

    TransformationStepNode node = root.getStepNode( "strings_replace", "Replace in string" );

    // Make sure we have the right number of links used, created and derived. Also,
    // Ensure there is an entry in the operations for those fields that are derived.
    assertEquals( 5, getIterableSize( node.getInputStreamFields() ) );
    assertEquals( 5, getIterableSize( node.getStreamFieldNodesUses() ) );
    assertEquals( 10, getIterableSize( node.getOutputStreamFields() ) );

    for ( StreamFieldNode sfn : node.getOutputStreamFields() ) {
      // The following are special cases for this test, they are passthrough fields
      if ( sfn.getName().equals( "LastName" )
        || sfn.getName().equals( "FirstName" )
        || sfn.getName().equals( "NickName" )
        || sfn.getName().equals( "Template" ) ) {
        assertTrue( Const.isEmpty( sfn.getOperations() ) );
      } else {
        assertFalse( Const.isEmpty( sfn.getOperations() ) );
      }
    }
  }


  @Test
  public void testTransExecutorStepNode() throws Exception {
    TransExecutorStepNode transExecutorStepNode = root.getTransExecutorStepNode();
    assertNotNull( transExecutorStepNode );

    TransExecutorMeta meta = (TransExecutorMeta) getStepMeta( transExecutorStepNode );

    assertEquals( meta.getParentStepMeta().getName(), transExecutorStepNode.getName() );
    assertEquals( meta.getOutputRowsSourceStep(), transExecutorStepNode.getOutputRowsTargetStepName() );
    assertEquals( meta.getResultFilesTargetStep(), transExecutorStepNode.getResultFilesTargetStepName() );
    assertEquals( meta.getExecutionResultTargetStep(), transExecutorStepNode.getExecutionResultsTargetStepName() );

    assertNotNull( transExecutorStepNode.getOutputStepByName( meta.getOutputRowsSourceStep() ) );
    assertNotNull( transExecutorStepNode.getOutputStepByName( meta.getResultFilesTargetStep() ) );
    assertNotNull( transExecutorStepNode.getOutputStepByName( meta.getExecutionResultTargetStep() ) );

    RowMetaInterface incomingRow = meta.getParentStepMeta().getParentTransMeta().getPrevStepFields(
      meta.getParentStepMeta() );
    int incomingFieldCount = incomingRow.size();

    Iterable<StreamFieldNode> streamFieldNodes = transExecutorStepNode.getOutputStreamFields();
    int countOutputStreamFieldNode = getIterableSize( streamFieldNodes );
    List<String> outputFields = Arrays.asList( meta.getOutputRowsField() );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertNotNull( streamFieldNode.getKettleType() );
      if ( outputFields.contains( streamFieldNode.getName() ) ) {
        // this field came back to us from the sub trans via a result, lets make sure they link up
        assertEquals( transExecutorStepNode.getTransToExecute(),
          streamFieldNode.getFieldNodesThatDeriveMe().iterator().next().getStepThatOutputsMe().getTransNode() );
      }
    }

    streamFieldNodes = transExecutorStepNode.getStreamFieldNodesDeletes();
    int countDeletedStreamFieldNode = getIterableSize( streamFieldNodes );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertNotNull( streamFieldNode.getKettleType() );
    }

    assertNotNull( transExecutorStepNode.getTransToExecute() );

    streamFieldNodes = transExecutorStepNode.getStreamFieldNodesUses();
    int countUsedStreamFieldNode = getIterableSize( streamFieldNodes );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertNotNull( streamFieldNode.getKettleType() );

      // these should link up to stream fields in the sub trans via the "get rows from result" step
      for ( StreamFieldNode derived : streamFieldNode.getFieldNodesDerivedFromMe() ) {
        // the trans that is to be executed should contain the step that creates a field derived from the incoming field
        assertEquals( transExecutorStepNode.getTransToExecute(), derived.getStepThatInputsMe().getTransNode() );
      }
    }

    assertEquals( incomingFieldCount, countUsedStreamFieldNode );
  }

  @Test
  public void testRowsToResultStepNode() throws Exception {
    RowsToResultStepNode rowsToResultStepNode = root.getRowsToResultStepNode();
    assertNotNull( rowsToResultStepNode );

    assertEquals( "Copy rows to result", rowsToResultStepNode.getStepType() );

    Iterable<StreamFieldNode> streamFieldNodes = rowsToResultStepNode.getStreamFieldNodesCreates();
    int countCreatedStreamFieldNode = getIterableSize( streamFieldNodes );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertNotNull( streamFieldNode.getKettleType() );
      // each field created is derived from an incoming field named the same thing
      assertEquals( streamFieldNode.getName(),
        streamFieldNode.getFieldNodesThatDeriveMe().iterator().next().getName() );
    }

    streamFieldNodes = rowsToResultStepNode.getStreamFieldNodesUses();
    int countUsedStreamFieldNode = getIterableSize( streamFieldNodes );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertNotNull( streamFieldNode.getKettleType() );
    }

    // should create a field for each one it uses
    assertEquals( countCreatedStreamFieldNode, countUsedStreamFieldNode );
  }

  @Test
  public void testFixedFileInputStep() throws Exception {
    // this is testing a specific FixedFileInputStep instance
    FixedFileInputStepNode fixedFileInputStepNode = root.getFixedFileInputStepNode();
    assertNotNull( fixedFileInputStepNode );

    Iterable<FramedMetaverseNode> inputFiles = fixedFileInputStepNode.getInputFiles();
    int countInputFiles = getIterableSize( inputFiles );
    assertEquals( 1, countInputFiles );
    for ( FramedMetaverseNode inputFile : inputFiles ) {
      assertTrue( inputFile.getName().endsWith( "Textfile input - fixed length sample data.txt" ) );
    }

    assertEquals( "Fixed file input", fixedFileInputStepNode.getStepType() );

    int countFileFieldNode = getIterableSize( fixedFileInputStepNode.getFileFieldNodesUses() );
    assertEquals( 0, countFileFieldNode );

    int countOutputs = getIterableSize( fixedFileInputStepNode.getOutputStreamFields() );
    int fileFieldCount = 0;
    Iterable<StreamFieldNode> outFields = fixedFileInputStepNode.getOutputStreamFields();
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      FieldNode fieldPopulatesMe = outField.getFieldPopulatesMe();
      assertNotNull( fieldPopulatesMe );
      assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, fieldPopulatesMe.getType() );
      assertEquals( fixedFileInputStepNode, fieldPopulatesMe.getStepThatInputsMe() );
      fileFieldCount++;
    }
    assertEquals( countOutputs, fileFieldCount );
  }

  @Test
  public void testFilterRowsStepNode() throws Exception {
    FilterRowsStepNode node = root.getFilterRowsStepNode( "Filter rows" );
    assertNotNull( node );
    assertEquals( "Filter rows", node.getStepType() );

    FilterRowsMeta meta = (FilterRowsMeta) getStepMeta( node );
    Operations ops = MetaverseUtil.convertOperationsStringToMap( node.getOperations() );
    assertEquals( 1, ops.get( ChangeType.DATA_FLOW ).size() );
    assertEquals( meta.getCondition().toString(), ops.get( ChangeType.DATA_FLOW ).get( 0 ).getDescription() );

    // should not be any created nodes
    Iterable<StreamFieldNode> streamFieldNodes = node.getStreamFieldNodesCreates();
    int countCreatedStreamFieldNode = getIterableSize( streamFieldNodes );
    assertEquals( 0, countCreatedStreamFieldNode );

    // should not be any deleted nodes
    streamFieldNodes = node.getStreamFieldNodesCreates();
    int countDeletedStreamFieldNode = getIterableSize( streamFieldNodes );
    assertEquals( 0, countDeletedStreamFieldNode );

    // should use all of the fields that are used in the condition of the step
    List<String> expectedUses = Arrays.asList( meta.getCondition().getUsedFields() );
    streamFieldNodes = node.getStreamFieldNodesUses();
    int countUsedStreamFieldNode = getIterableSize( streamFieldNodes );
    assertEquals( expectedUses.size(), countUsedStreamFieldNode );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertTrue( expectedUses.contains( streamFieldNode.getName() ) );
    }

  }

  @Test
  public void testHttpClientStep() throws Exception {
    HttpClientStepNode node = root.getHttpClientStepNode();
    assertNotNull( node );
    Iterable<FramedMetaverseNode> inputUrls = node.getInputUrls();
    int countInputUrls = getIterableSize( inputUrls );
    assertEquals( 1, countInputUrls );
    assertEquals( "HTTP client", node.getStepType() );

    HTTPMeta stepMeta = (HTTPMeta) getStepMeta( node );
    for ( FramedMetaverseNode inputUrl : inputUrls ) {
      assertEquals( stepMeta.getUrl(), inputUrl.getName() );
    }

    // check the param  field is "used"
    Iterable<StreamFieldNode> streamFieldNodesUses = node.getStreamFieldNodesUses();
    assertEquals( 2, getIterableSize( streamFieldNodesUses ) );

    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();
    assertEquals( 5, getIterableSize( outputs ) );

    Iterable<StreamFieldNode> inputs = node.getInputStreamFields();
    assertEquals( 3, getIterableSize( inputs ) );
    for ( StreamFieldNode in : inputs ) {
      assertNotNull( in.getFieldNodesDerivedFromMe() );
      assertEquals( in.getName(), in.getFieldNodesDerivedFromMe().iterator().next().getName() );
    }
  }

  @Test
  public void testHTTPClientStep_UrlFromField() throws Exception {
    // this is testing a specific TextFileInputStep instance
    HttpClientStepNode node = root.getHttpClientStepNode_urlFromField();
    assertNotNull( node );

    // this HTTP Client gets it's files from an incoming stream field, there should be no files modeled statically
    Iterable<FramedMetaverseNode> inputUrls = node.getInputUrls();
    int countInputUrls = getIterableSize( inputUrls );
    // it is coming from a field
    assertEquals( 0, countInputUrls );

    HTTPMeta stepMeta = (HTTPMeta) getStepMeta( node );
    assertEquals( "HTTP client", node.getStepType() );

    Set<String> usedFields = new HashSet<>();
    Collections.addAll( usedFields, stepMeta.getHeaderField() );
    Collections.addAll( usedFields, stepMeta.getArgumentField() );
    if ( stepMeta.isUrlInField() ) {
      usedFields.add( stepMeta.getUrlField() );
    }

    // check the param  field is "used"
    Iterable<StreamFieldNode> streamFieldNodesUses = node.getStreamFieldNodesUses();
    assertEquals( usedFields.size(), getIterableSize( streamFieldNodesUses ) );
    for ( StreamFieldNode streamFieldNodesUse : streamFieldNodesUses ) {
      assertTrue( usedFields.contains( streamFieldNodesUse.getName() ) );
    }

    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();
    assertEquals( getExpectedOutputFieldCount( stepMeta ), getIterableSize( outputs ) );
  }

  @Test
  public void testHttpPostStep() throws Exception {
    // this is testing a specific TextFileInputStep instance
    HttpPostStepNode node = root.getHttpPostStepNode();
    assertNotNull( node );

    Iterable<FramedMetaverseNode> inputUrls = node.getInputUrls();
    int countUrls = getIterableSize( inputUrls );
    assertEquals( 1, countUrls );
    for ( FramedMetaverseNode inputUrl : inputUrls ) {
      assertTrue( inputUrl.getName().endsWith( "/posts" ) );
    }

    assertEquals( "HTTP post", node.getStepType() );

    // check the param  field is "used"
    Iterable<StreamFieldNode> streamFieldNodesUses = node.getStreamFieldNodesUses();
    assertEquals( 1, getIterableSize( streamFieldNodesUses ) );

    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();
    assertEquals( 3, getIterableSize( outputs ) );

    Iterable<StreamFieldNode> inputs = node.getInputStreamFields();
    assertEquals( 1, getIterableSize( inputs ) );
    for ( StreamFieldNode in : inputs ) {
      assertNotNull( in.getFieldNodesDerivedFromMe() );
      assertEquals( in.getName(), in.getFieldNodesDerivedFromMe().iterator().next().getName() );
    }
  }

  @Test
  public void testHTTPPostStep_UrlFromField() throws Exception {
    // this is testing a specific TextFileInputStep instance
    HttpPostStepNode node = root.getHttpPostStepNode_urlFromField();
    assertNotNull( node );

    // this HTTP Client gets it's files from an incoming stream field, there should be no files modeled statically
    Iterable<FramedMetaverseNode> inputUrls = node.getInputUrls();
    int countInputUrls = getIterableSize( inputUrls );
    assertEquals( 0, countInputUrls );

    assertEquals( "HTTP post", node.getStepType() );

    HTTPPOSTMeta stepMeta = (HTTPPOSTMeta) getStepMeta( node );
    Set<String> usedFields = new HashSet<>();
    Collections.addAll( usedFields, stepMeta.getQueryField() );
    Collections.addAll( usedFields, stepMeta.getArgumentField() );
    if ( stepMeta.isUrlInField() ) {
      usedFields.add( stepMeta.getUrlField() );
    }

    // check the param  field is "used"
    Iterable<StreamFieldNode> streamFieldNodesUses = node.getStreamFieldNodesUses();
    assertEquals( usedFields.size(), getIterableSize( streamFieldNodesUses ) );
    for ( StreamFieldNode streamFieldNodesUse : streamFieldNodesUses ) {
      assertTrue( usedFields.contains( streamFieldNodesUse.getName() ) );
    }

    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();
    assertEquals( getExpectedOutputFieldCount( stepMeta ), getIterableSize( outputs ) );
  }

  @Test
  public void testNumberRangeStepNode_newField() throws Exception {
    TransformationStepNode numberRangeStepNode = root.getStepNode( "number_range", "Number range" );

    assertEquals( 1, getIterableSize( numberRangeStepNode.getStreamFieldNodesUses() ) );

    StreamFieldNode usesNode = null;
    for ( StreamFieldNode node : numberRangeStepNode.getStreamFieldNodesUses() ) {
      usesNode = node;
      break;
    }

    NumberRangeMeta meta = (NumberRangeMeta) getStepMeta( numberRangeStepNode );
    assertEquals( meta.getInputField(), usesNode.getName() );
    assertNull( usesNode.getOperations() );

    Iterable<StreamFieldNode> outFields = numberRangeStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    assertEquals( 2, countOutputs );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      if ( outField.getName().equals( meta.getOutputField() ) ) {
        Iterable<StreamFieldNode> fieldNodesThatDeriveMe = outField.getFieldNodesThatDeriveMe();
        assertNotNull( fieldNodesThatDeriveMe );
        int derivesCount = getIterableSize( outField.getFieldNodesThatDeriveMe() );
        assertEquals( 1, derivesCount );
        for ( FieldNode derives : outField.getFieldNodesThatDeriveMe() ) {
          assertEquals( usesNode.getName(), derives.getName() );
          assertEquals( usesNode.getType(), derives.getType() );
        }

        Operations ops = MetaverseUtil.convertOperationsStringToMap( outField.getOperations() );
        List<IOperation> dataOps = ops.get( ChangeType.DATA );
        assertNotNull( dataOps );
        assertEquals( 3, dataOps.size() );
        for ( IOperation dataOp : dataOps ) {
          assertEquals( Operation.MAPPING_CATEGORY, dataOp.getCategory() );
          assertEquals( DictionaryConst.PROPERTY_TRANSFORMS, dataOp.getName() );
          assertTrue( dataOp.toString().contains( meta.getInputField() ) );
        }

        // there should not be any metadata operations
        assertNull( ops.get( ChangeType.METADATA ) );
      }
    }
  }

  @Test
  public void testTableInput() throws Exception {
    TableInputStepNode tableNode = root.getTableInputStepNode();
    TableInputMeta meta = (TableInputMeta) getStepMeta( tableNode );
    assertNotNull( tableNode );
    assertNotNull( tableNode.getDatasource( meta.getDatabaseMeta().getName() ) );
    assertNotNull( tableNode.getDatabaseQueryNode() );
    assertEquals( meta.getSQL(), tableNode.getDatabaseQueryNode().getQuery() );

    Iterable<StreamFieldNode> outputStreamFields = tableNode.getOutputStreamFields();
    Iterable<DatabaseColumnNode> databaseColumns = tableNode.getDatabaseQueryNode().getDatabaseColumns();
    assertEquals( getIterableSize( outputStreamFields ), getIterableSize( databaseColumns ) );

  }

  public List<FramedMetaverseNode> testTextFileInputNodeImpl( final int expectedContainedFieldCount ) throws Exception {
    TextFileInputNode textFileInputNode = root.getTextFileInputNode();
    List<FramedMetaverseNode> inputFileNodes = IteratorUtils.toList( textFileInputNode.getInputFiles().iterator() );
    assertEquals( 1, inputFileNodes.size() );
    List<FramedMetaverseNode> containedNodes = IteratorUtils.toList( inputFileNodes.get( 0 ).getContainedNodes()
      .iterator() );
    return containedNodes;
  }

  protected BaseStepMeta getStepMeta( TransformationStepNode transformationStepNode ) throws Exception {
    TransMeta tm = new TransMeta( transformationStepNode.getTransNode().getPath(), null, true, null, null );
    BaseStepMeta meta = null;
    for ( StepMeta stepMeta : tm.getSteps() ) {
      if ( transformationStepNode.getName().equals( stepMeta.getName() ) ) {
        meta = getBaseStepMetaFromStepMeta( stepMeta );
        break;
      }
    }
    return meta;
  }

  protected BaseStepMeta getBaseStepMetaFromStepMeta( StepMeta stepMeta ) {

    // Attempt to discover a BaseStepMeta from the given StepMeta
    BaseStepMeta baseStepMeta = new BaseStepMeta();
    baseStepMeta.setParentStepMeta( stepMeta );
    if ( stepMeta != null ) {
      StepMetaInterface smi = stepMeta.getStepMetaInterface();
      if ( smi instanceof BaseStepMeta ) {
        baseStepMeta = (BaseStepMeta) smi;
      }
    }
    return baseStepMeta;
  }

  private String convertNumericStatusToString( int transactionStatus ) {
    String status = null;
    switch ( transactionStatus ) {
      case 1:
        status = "DRAFT";
        break;
      case 2:
        status = "PRODUCTION";
        break;
      default:
        status = null;
        break;
    }
    return status;
  }

  private int getExpectedOutputFieldCount( BaseStepMeta meta ) throws KettleStepException {
    return meta.getParentStepMeta().getParentTransMeta().getStepFields( meta.getParentStepMeta() ).size();
  }

  @Override
  protected boolean shouldCleanupInstance() {
    return false;
  }

  private void assertFileNameEquals( Collection<File> expectedFiles, Iterable<KettleNode> actualKettleNodes ) {
    Set<String> expectedSet = getSortedSet(getFileNamesFromFile(expectedFiles));
    Set<String> actualSet = getSortedSet(getFileNamesFromKettleNode(getCollection(actualKettleNodes)));

    Iterator actualSetIterator = actualSet.iterator();
    while ( actualSetIterator.hasNext() ) {
      expectedSet.remove( actualSetIterator.next());
      actualSetIterator.remove();
    }

    assertTrue( "Missing expected files : " + expectedSet + " and/or Extra actual files: " + actualSet,
            expectedSet.isEmpty() && actualSet.isEmpty() );

  }

  private Collection<String> getFileNamesFromFile( Collection<File> files ) {
    return files.stream().map( File::getName ).collect( Collectors.toList() );
  }

  private Collection<String> getFileNamesFromKettleNode( Collection<KettleNode> kettleNodes ) {
    return kettleNodes.stream().map( KettleNode::getPath )
            .map( s -> s.substring( s.lastIndexOf( '/' ) + 1 ) )
            .collect( Collectors.toList() );
  }

  private Set<String> getSortedSet( Collection<String> collection ) {
    TreeSet<String> set = new TreeSet<>();
    set.addAll( collection );
    return set;
  }

}
