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

package org.pentaho.metaverse;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.exceloutput.ExcelField;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;
import org.pentaho.di.trans.steps.fieldsplitter.FieldSplitterMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import org.pentaho.di.trans.steps.http.HTTPMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.di.trans.steps.mongodb.MongoDbMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.IDocumentController;
import org.pentaho.metaverse.api.IDocumentLocatorProvider;
import org.pentaho.metaverse.api.IMetaverseReader;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;
import org.pentaho.metaverse.frames.CalculatorStepNode;
import org.pentaho.metaverse.frames.CsvFileInputStepNode;
import org.pentaho.metaverse.frames.DatabaseColumnNode;
import org.pentaho.metaverse.frames.DatabaseTableNode;
import org.pentaho.metaverse.frames.DatasourceNode;
import org.pentaho.metaverse.frames.ExcelInputStepNode;
import org.pentaho.metaverse.frames.ExcelOutputStepNode;
import org.pentaho.metaverse.frames.FieldNode;
import org.pentaho.metaverse.frames.FileInputStepNode;
import org.pentaho.metaverse.frames.FilterRowsStepNode;
import org.pentaho.metaverse.frames.FixedFileInputStepNode;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.GetXMLDataStepNode;
import org.pentaho.metaverse.frames.GroupByStepNode;
import org.pentaho.metaverse.frames.HttpClientStepNode;
import org.pentaho.metaverse.frames.HttpPostStepNode;
import org.pentaho.metaverse.frames.JobEntryNode;
import org.pentaho.metaverse.frames.JobNode;
import org.pentaho.metaverse.frames.LocatorNode;
import org.pentaho.metaverse.frames.MergeJoinStepNode;
import org.pentaho.metaverse.frames.MongoConnectionNode;
import org.pentaho.metaverse.frames.MongoDbDatasourceNode;
import org.pentaho.metaverse.frames.MongoDbInputStepNode;
import org.pentaho.metaverse.frames.RestClientStepNode;
import org.pentaho.metaverse.frames.RootNode;
import org.pentaho.metaverse.frames.RowsToResultStepNode;
import org.pentaho.metaverse.frames.SelectValuesTransStepNode;
import org.pentaho.metaverse.frames.SplitFieldsStepNode;
import org.pentaho.metaverse.frames.StreamFieldNode;
import org.pentaho.metaverse.frames.StreamLookupStepNode;
import org.pentaho.metaverse.frames.TableInputStepNode;
import org.pentaho.metaverse.frames.TableOutputStepNode;
import org.pentaho.metaverse.frames.TextFileOutputStepNode;
import org.pentaho.metaverse.frames.TransExecutorStepNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;
import org.pentaho.metaverse.frames.XMLOutputStepNode;
import org.pentaho.metaverse.locator.FileSystemLocator;
import org.pentaho.metaverse.util.MetaverseUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * User: RFellows Date: 8/20/14
 */
public class MetaverseValidationIT {

  private static final String ROOT_FOLDER = "src/it/resources/repo/validation";
  private static IMetaverseReader reader;
  private static Graph graph;
  private static FramedGraphFactory framedGraphFactory;
  private static FramedGraph framedGraph;
  private static RootNode root;

  @BeforeClass
  public static void init() throws Exception {
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution/system/pentahoObjects.spring.xml" );

    // we only care about the demo folder
    FileSystemLocator fileSystemLocator = PentahoSystem.get( FileSystemLocator.class );
    IDocumentLocatorProvider provider = PentahoSystem.get( IDocumentLocatorProvider.class );
    // remove the original locator so we can set the modified one back on it
    provider.removeDocumentLocator( fileSystemLocator );
    fileSystemLocator.setRootFolder( ROOT_FOLDER );
    provider.addDocumentLocator( fileSystemLocator );

    MetaverseUtil.setDocumentController( PentahoSystem.get( IDocumentController.class ) );

    // build the graph using our updated locator/provider
    graph = IntegrationTestUtil.buildMetaverseGraph( provider );
    reader = PentahoSystem.get( IMetaverseReader.class );

    framedGraphFactory = new FramedGraphFactory( new GremlinGroovyModule() );
    framedGraph = framedGraphFactory.create( graph );
    root = (RootNode) framedGraph.getVertex( "entity", RootNode.class );
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    IntegrationTestUtil.shutdownPentahoSystem();

    File exportFile = new File( "target/outputfiles/validationGraph.graphml" );
    FileUtils.writeStringToFile( exportFile, reader.exportToXml(), "UTF-8" );
  }

  @Before
  public void setUp() throws Exception {

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
      (LocatorNode) framedGraph.getVertex( "{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}", LocatorNode.class );
    assertEquals( DictionaryConst.NODE_TYPE_LOCATOR, node.getType() );
    assertEquals( "FILE_SYSTEM_REPO", node.getName() );
    assertNotNull( node.getDescription() );
    assertNotNull( node.getUrl() );
    assertNotNull( node.getLastScan() );
    int countDocuments = getIterableSize( node.getDocuments() );

    File folder = new File( ROOT_FOLDER );
    int fileCount = FileUtils.listFiles( folder, new String[]{ "ktr", "kjb" }, true ).size();

    assertEquals( fileCount, countDocuments );
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
        assertEquals( "Incorrect entity type", DictionaryConst.NODE_TYPE_JOB_ENTRY, jobEntryNode.getEntity().getName() );
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
      int stepCount = getIterableSize( transNode.getStepNodes() );
      int matchCount = 0;

      for ( StepMeta transMetaStep : transMetaSteps ) {
        // let's see if the steps are in the graph for this transformation
        TransformationStepNode stepNode = transNode.getStepNode( transMetaStep.getName() );
        assertNotNull( stepNode );
        assertEquals( "Incorrect type", DictionaryConst.NODE_TYPE_TRANS_STEP, stepNode.getType() );
        assertEquals( "Incorrect entity type", DictionaryConst.NODE_TYPE_TRANS_STEP, stepNode.getEntity().getName() );
        matchCount++;
      }

      assertEquals( "Not all transformation steps are modeled in the graph for [" + tm.getName() + "]", transMetaSteps
        .size(), matchCount );

      assertEquals( "Incorrect number of Steps in the graph for transformation [" + tm.getName() + "]", transMetaSteps
        .size(), stepCount );

    }
  }

  @Test
  public void testSelectValuesStep() throws Exception {
    // this tests a specific select values step. the one in trans "Populate Table From File"
    SelectValuesTransStepNode selectValues = root.getSelectValuesStepNode();
    assertNotNull( selectValues );
    int countUses = getIterableSize( selectValues.getStreamFieldNodesUses() );
    int countOutputs = getIterableSize( selectValues.getOutputStreamFields() );
    int countInputs = getIterableSize( selectValues.getInputStreamFields() );
    assertEquals( 9, countUses );
    SelectValuesMeta meta = (SelectValuesMeta) getStepMeta( selectValues );
    assertEquals( getExpectedOutputFieldCount( meta ), countOutputs );
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
  public void testExcelInputStep() throws Exception {
    // this is testing a specific TextFileInputStep instance
    ExcelInputStepNode excelInputStepNode = root.getExcelInputStepNode();
    assertNotNull( excelInputStepNode );

    Iterable<FramedMetaverseNode> inputFiles = excelInputStepNode.getInputFiles();
    int countInputFiles = getIterableSize( inputFiles );
    assertEquals( 1, countInputFiles );
    for ( FramedMetaverseNode inputFile : inputFiles ) {
      assertTrue( inputFile.getName().endsWith( "SacramentoCrime.xls" ) );
    }

    assertEquals( "Microsoft Excel Input", excelInputStepNode.getStepType() );

    int countUses = getIterableSize( excelInputStepNode.getFileFieldNodesUses() );
    int countInputs = getIterableSize( excelInputStepNode.getInputStreamFields() );

    assertEquals( 0, countUses );
    int fileFieldCount = 0;
    Iterable<StreamFieldNode> outFields = excelInputStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      FieldNode fieldPopulatesMe = outField.getFieldPopulatesMe();
      assertNotNull( fieldPopulatesMe );
      assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, fieldPopulatesMe.getType() );
      assertEquals( excelInputStepNode, fieldPopulatesMe.getStepThatInputsMe() );
      fileFieldCount++;
    }
    assertEquals( countInputs, fileFieldCount );
    assertEquals( countOutputs, fileFieldCount );

  }

  @Test
  public void testExcelInputStep_filenameFromField() throws Exception {
    // this is testing a specific TextFileInputStep instance
    ExcelInputStepNode excelInputStepNode = root.getExcelInputFileNameFromFieldStepNode();
    assertNotNull( excelInputStepNode );

    int countUses = getIterableSize( excelInputStepNode.getFileFieldNodesUses() );
    int countInputs = getIterableSize( excelInputStepNode.getInputStreamFields() );

    assertEquals( 1, countUses );

    int fileFieldCount = 0;
    Iterable<StreamFieldNode> outFields = excelInputStepNode.getOutputStreamFields();
    int countOutputs = getIterableSize( outFields );
    for ( StreamFieldNode outField : outFields ) {
      assertNotNull( outField.getKettleType() );
      if ( !outField.getName().equals( "filename" ) ) {
        FieldNode fieldPopulatesMe = outField.getFieldPopulatesMe();
        assertNotNull( fieldPopulatesMe );
        assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, fieldPopulatesMe.getType() );
        assertEquals( excelInputStepNode, fieldPopulatesMe.getStepThatInputsMe() );
        fileFieldCount++;
      }
    }
    // we should have one more input than file fields since we are reading it off of the input stream
    assertEquals( countInputs - 1, fileFieldCount );
    assertEquals( countOutputs - 1, fileFieldCount );

    String filenameField = null;
    TransMeta tm =
      new TransMeta( new FileInputStream( excelInputStepNode.getTransNode().getPath() ), null, true, null, null );
    for ( StepMeta stepMeta : tm.getSteps() ) {
      if ( stepMeta.getName().equals( excelInputStepNode.getName() ) ) {
        ExcelInputMeta meta = (ExcelInputMeta) getBaseStepMetaFromStepMeta( stepMeta );
        assertTrue( meta.isAcceptingFilenames() );
        filenameField = meta.getAcceptingField();
        assertNotNull( filenameField );
        assertEquals( filenameField, excelInputStepNode.getFileFieldNodesUses().iterator().next().getName() );
        // this was the one we cared about...
        break;
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

    assertEquals( "Old Text file input", fileInputStepNode.getStepType() );

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
      assertEquals( fileInputStepNode, fieldPopulatesMe.getStepThatInputsMe() );
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
  public void testMongoDbConnections() throws Exception {
    int countMongoConnections = getIterableSize( root.getMongoDbDatasourceNodes() );
    for ( MongoDbDatasourceNode ds : root.getMongoDbDatasourceNodes() ) {
      // make sure at least one step uses the connection
      int countUsedSteps = getIterableSize( ds.getTransformationStepNodes() );
      assertTrue( countUsedSteps > 0 );

      assertEquals( DictionaryConst.NODE_TYPE_MONGODB_CONNECTION, ds.getEntity().getName() );
      assertNotNull( ds.getName() );
      assertNotNull( ds.getDatabaseName() );
      assertNotNull( ds.getPort() );
    }
    assertTrue( countMongoConnections > 0 );
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
  public void testTableOutputStepNode() throws Exception {
    // this tests a specific step in a specific transform
    TableOutputStepNode node = root.getTableOutputStepNode();

    // check the table that it writes to
    TableOutputMeta meta = (TableOutputMeta) getStepMeta( node );
    String tableName = meta.getTableName();
    String schema = meta.getSchemaName();
    boolean truncateTable = meta.truncateTable();
    DatabaseTableNode databaseTableNode = node.getDatabaseTable();
    assertEquals( tableName, databaseTableNode.getName() );
    assertEquals( schema, node.getSchema() );
    assertEquals( truncateTable, node.isTruncateTable() );

    Iterable<StreamFieldNode> inputs = node.getInputStreamFields();
    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();

    assertEquals( getIterableSize( inputs ) + meta.getFieldDatabase().length, getIterableSize( outputs ) );

    for ( StreamFieldNode input : inputs ) {
      assertEquals( input.getName(), input.getFieldPopulatedByMe().getName() );
    }

    DatasourceNode datasource = node.getDatasource( meta.getDatabaseMeta().getName() );
    assertEquals( meta.getDatabaseMeta().getHostname(), datasource.getHost() );
    assertEquals( meta.getDatabaseMeta().getDatabasePortNumberString(), datasource.getPort() );
    assertEquals( meta.getDatabaseMeta().getUsername(), datasource.getUserName() );
    assertEquals( meta.getDatabaseMeta().getDatabaseName(), datasource.getDatabaseName() );
    assertEquals( DictionaryConst.NODE_TYPE_DATASOURCE, datasource.getType() );

    assertEquals( meta.getTableName(), databaseTableNode.getName() );
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
          assertEquals( dataOp.toString(), meta.getSourceValue()[i] + " -> " + meta.getTargetValue()[i] );
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
    // should write to one file
    Iterable<FramedMetaverseNode> outputFiles = textFileOutputStepNode.getOutputFiles();
    assertEquals( fileNames.length, getIterableSize( outputFiles ) );
    int i = 0;
    for ( FramedMetaverseNode node : outputFiles ) {
      assertEquals( fileNames[i++].replace( "file://", "" ), node.getName() );
    }

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
    String[] fieldsThatDerive = new String[2];
    fieldsThatDerive = new String[2];
    int i = 0;
    for ( StreamFieldNode sfn : celsius.getFieldNodesThatDeriveMe() ) {
      fieldsThatDerive[i++] = sfn.getName();
    }

    assertTrue( Arrays.asList( fieldsThatDerive ).contains( "tempCelsius" ) );
    assertTrue( Arrays.asList( fieldsThatDerive ).contains( "tempRatio" ) );

    fieldsThatDerive = new String[2];
    i = 0;
    for ( StreamFieldNode sfn : kelvin.getFieldNodesThatDeriveMe() ) {
      fieldsThatDerive[i++] = sfn.getName();
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
  public void testExcelOutputStepNode() throws Exception {
    ExcelOutputStepNode excelOutputStepNode = root.getExcelOutputStepNode();
    assertNotNull( excelOutputStepNode );

    ExcelOutputMeta meta = (ExcelOutputMeta) getStepMeta( excelOutputStepNode );
    TransMeta tm = meta.getParentStepMeta().getParentTransMeta();
    String[] fileNames = meta.getFiles( tm );

    RowMetaInterface incomingFields = tm.getStepFields( meta.getParentStepMeta() );
    ExcelField[] outputFields = meta.getOutputFields();

    // should write to one file
    Iterable<FramedMetaverseNode> outputFiles = excelOutputStepNode.getOutputFiles();
    assertEquals( fileNames.length, getIterableSize( outputFiles ) );
    int i = 0;
    for ( FramedMetaverseNode node : outputFiles ) {
      assertEquals( fileNames[i++].replace( "file://", "" ), node.getName() );
    }

    Iterable<StreamFieldNode> outFields = excelOutputStepNode.getOutputStreamFields();
    int outFieldCount = getIterableSize( outFields );
    // should have output stream nodes as well as file nodes
    assertEquals( outputFields.length * 2, outFieldCount );

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
      assertEquals( streamFieldNode.getName(), streamFieldNode.getFieldNodesThatDeriveMe().iterator().next().getName() );
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
    assertEquals( "HTTP Client", node.getStepType() );

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
    assertEquals( "HTTP Client", node.getStepType() );

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
  public void testRestClientStepNode() throws Exception {
    RestClientStepNode node = root.getRestClientStepNode( "REST Client" );
    assertNotNull( node );
    Iterable<FramedMetaverseNode> inputUrls = node.getInputUrls();
    int countInputUrls = getIterableSize( inputUrls );
    assertEquals( 1, countInputUrls );
    assertEquals( "REST Client", node.getStepType() );

    RestMeta stepMeta = (RestMeta) getStepMeta( node );
    for ( FramedMetaverseNode inputUrl : inputUrls ) {
      assertEquals( stepMeta.getUrl(), inputUrl.getName() );
    }

    // check the param  field is "used"
    Iterable<StreamFieldNode> streamFieldNodesUses = node.getStreamFieldNodesUses();
    assertEquals( 1, getIterableSize( streamFieldNodesUses ) );
    for ( StreamFieldNode streamFieldNodesUse : streamFieldNodesUses ) {
      assertEquals( stepMeta.getParameterField()[0], streamFieldNodesUse.getName() );
    }

    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();
    assertEquals( 4, getIterableSize( outputs ) );

    Iterable<StreamFieldNode> inputs = node.getInputStreamFields();
    assertEquals( 3, getIterableSize( inputs ) );
    for ( StreamFieldNode in : inputs ) {
      assertNotNull( in.getFieldNodesDerivedFromMe() );
      assertEquals( in.getName(), in.getFieldNodesDerivedFromMe().iterator().next().getName() );
    }
  }

  @Test
  public void testRestClientStepNode_urlFromField() throws Exception {
    RestClientStepNode node = root.getRestClientStepNode( "REST Client - parameterized" );
    assertNotNull( node );
    Iterable<FramedMetaverseNode> inputUrls = node.getInputUrls();
    assertEquals( "REST Client", node.getStepType() );

    RestMeta stepMeta = (RestMeta) getStepMeta( node );
    for ( FramedMetaverseNode inputUrl : inputUrls ) {
      assertEquals( stepMeta.getUrlField(), inputUrl.getName() );
    }

    Set<String> usedFields = new HashSet<>();
    Collections.addAll( usedFields, stepMeta.getHeaderField() );
    Collections.addAll( usedFields, stepMeta.getParameterField() );
    if ( stepMeta.isUrlInField() ) {
      usedFields.add( stepMeta.getUrlField() );
    }
    if ( stepMeta.isDynamicMethod() ) {
      usedFields.add( stepMeta.getMethodFieldName() );
    }
    if ( StringUtils.isNotEmpty( stepMeta.getBodyField() ) ) {
      usedFields.add( stepMeta.getBodyField() );
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

    assertEquals( "HTTP Post", node.getStepType() );

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

    assertEquals( "HTTP Post", node.getStepType() );

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
  public void testMongoDbInput() throws Exception {
    MongoDbInputStepNode node = root.getMongoDbInputStepNode();
    assertNotNull( node );
    MongoDbMeta meta = (MongoDbMeta) getStepMeta( node );
    assertEquals( meta.getCollection(), node.getCollection().getName() );
    assertEquals( meta.getDbName(), node.getDatasource( meta.getDbName() ).getName() );

    Iterable<StreamFieldNode> inputs = node.getInputStreamFields();
    Iterable<StreamFieldNode> outputs = node.getOutputStreamFields();

    assertEquals( getIterableSize( inputs ), getIterableSize( outputs ) );

    for ( StreamFieldNode output : outputs ) {
      assertEquals( output.getName(), output.getFieldPopulatesMe().getName() );
    }

    MongoConnectionNode datasource = node.getDatasource( meta.getDbName() );
    assertEquals( meta.getHostnames(), datasource.getHost() );
    assertEquals( meta.getPort(), datasource.getPort() );
    assertEquals( meta.getAuthenticationUser(), datasource.getUserName() );
    assertEquals( meta.getDbName(), datasource.getDatabaseName() );
    assertEquals( DictionaryConst.NODE_TYPE_MONGODB_CONNECTION, datasource.getType() );

    FramedMetaverseNode collection = node.getCollection();
    assertEquals( meta.getCollection(), collection.getName() );
    assertEquals( DictionaryConst.NODE_TYPE_MONGODB_COLLECTION, collection.getType() );
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

  private int getIterableSize( Iterable<?> iterable ) {
    int count = 0;
    for ( Object o : iterable ) {
      if ( o != null ) {
        count++;
      }
    }
    return count;
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

}
