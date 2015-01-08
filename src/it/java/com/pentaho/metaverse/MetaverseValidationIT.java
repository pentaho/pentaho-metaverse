/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
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

package com.pentaho.metaverse;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IDocumentLocatorProvider;
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.frames.DatasourceNode;
import com.pentaho.metaverse.frames.FieldNode;
import com.pentaho.metaverse.frames.FramedMetaverseNode;
import com.pentaho.metaverse.frames.JobEntryNode;
import com.pentaho.metaverse.frames.JobNode;
import com.pentaho.metaverse.frames.LocatorNode;
import com.pentaho.metaverse.frames.MergeJoinStepNode;
import com.pentaho.metaverse.frames.RootNode;
import com.pentaho.metaverse.frames.SelectValuesTransStepNode;
import com.pentaho.metaverse.frames.StreamFieldNode;
import com.pentaho.metaverse.frames.TableOutputStepNode;
import com.pentaho.metaverse.frames.TextFileInputStepNode;
import com.pentaho.metaverse.frames.TextFileOutputStepNode;
import com.pentaho.metaverse.frames.TransformationNode;
import com.pentaho.metaverse.frames.TransformationStepNode;
import com.pentaho.metaverse.frames.ValueMapperStepNode;
import com.pentaho.metaverse.locator.FileSystemLocator;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import flexjson.JSONDeserializer;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;

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
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution" );

    // we only care about the demo folder
    FileSystemLocator fileSystemLocator = PentahoSystem.get( FileSystemLocator.class );
    IDocumentLocatorProvider provider = PentahoSystem.get( IDocumentLocatorProvider.class );
    // remove the original locator so we can set the modified one back on it
    provider.removeDocumentLocator( fileSystemLocator );
    fileSystemLocator.setRootFolder( ROOT_FOLDER );
    provider.addDocumentLocator( fileSystemLocator );

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

    File exportFile = new File( "src/it/resources/validationGraph.graphml" );
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
    LocatorNode node = (LocatorNode) framedGraph.getVertex( "{\"name\":\"FILE_SYSTEM_REPO\",\"type\":\"Locator\"}", LocatorNode.class );
    assertEquals( DictionaryConst.NODE_TYPE_LOCATOR, node.getType() );
    assertEquals( "FILE_SYSTEM_REPO", node.getName() );
    assertNotNull( node.getDescription() );
    assertNotNull( node.getUrl() );
    assertNotNull( node.getLastScan() );
    int countDocuments = getIterableSize( node.getDocuments() );

    File folder = new File( ROOT_FOLDER );
    int fileCount = folder.listFiles( new FilenameFilter() {
      @Override public boolean accept( File dir, String name ) {
        return ( name.endsWith( ".ktr" ) || name.endsWith( ".kjb" ) );
      }
    } ).length;

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
        JobEntryNode jobEntryNode = jobNode.getJobEntryNode( jobEntry.getName() );
        assertNotNull( jobEntryNode );
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

      assertEquals( "Not all transformation steps are modeled in the graph for [" + tm.getName() + "]",
          transMetaSteps.size(), matchCount );

      assertEquals( "Incorrect number of Steps in the graph for transformation [" + tm.getName() + "]",
          transMetaSteps.size(), stepCount );

    }
  }

  @Test
  public void testSelectValuesStep() throws Exception {
    // this tests a specific select values step. the one in trans "Populate Table From File"
    SelectValuesTransStepNode selectValues = root.getSelectValuesStepNode();
    assertNotNull( selectValues );
    int countUses = getIterableSize( selectValues.getStreamFieldNodesUses() );
    int countCreates = getIterableSize( selectValues.getStreamFieldNodesCreates() );
    int countDeletes = getIterableSize( selectValues.getStreamFieldNodesDeletes() );
    assertEquals( 8, countUses );
    assertEquals( 4, countCreates );
    assertEquals( 3, countDeletes );
    assertEquals( "Select values", selectValues.getStepType() );

    // verify the nodes created by the step
    for ( StreamFieldNode node : selectValues.getStreamFieldNodesCreates() ) {
      // check for operations
      if ( node.getMetadataOperations() != null ) {
        Map<String, List<String>> ops = convertOperationsStringToMap( node.getMetadataOperations() );
        assertNotNull( ops );
        assertTrue( ops.size() > 0 );
      }

      // check the created node is derived from something
      Iterable<StreamFieldNode> deriveNodes = node.getFieldNodesThatDeriveMe();
      for ( StreamFieldNode deriveNode : deriveNodes ) {
        assertNotNull( deriveNode );
      }

      // there should not be any data operations on nodes touched by this step
      assertNull( node.getDataOperations() );
    }

    // verify fields deleted
    for ( StreamFieldNode node : selectValues.getStreamFieldNodesDeletes() ) {
      // check the created node is never used to "populate" anything
      FieldNode populatedNode = node.getFieldPopulatedByMe();
      assertNull( populatedNode );
    }
  }

  @Test
  public void testTextFileInputStep() throws Exception {
    // this is testing a specific TextFileInputStep instance
    TextFileInputStepNode textFileInputStepNode = root.getTextFileInputStepNode();
    assertNotNull( textFileInputStepNode );

    Iterable<FramedMetaverseNode> inputFiles = textFileInputStepNode.getInputFiles();
    int countInputFiles = getIterableSize( inputFiles );
    assertEquals( 1, countInputFiles );
    for ( FramedMetaverseNode inputFile : inputFiles ) {
      assertTrue( inputFile.getName().endsWith( "SacramentocrimeJanuary2006.csv" ) );
    }

    assertEquals( "Text file input", textFileInputStepNode.getStepType() );

    int countFileFieldNode = getIterableSize( textFileInputStepNode.getFileFieldNodesUses() );

    Iterable<StreamFieldNode> streamFieldNodes = textFileInputStepNode.getStreamFieldNodesCreates();
    int countStreamFieldNode = getIterableSize( streamFieldNodes );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertNotNull( streamFieldNode.getKettleType() );
    }

    // we should create as many fields as we read in
    assertEquals( countFileFieldNode, countStreamFieldNode );

  }

  @Test
  public void testTextFileInputStep_filenameFromField() throws Exception {
    // this is testing a specific TextFileInputStep instance
    TextFileInputStepNode textFileInputStepNode = root.getTextFileInputStepNode_filenameFromField();
    assertNotNull( textFileInputStepNode );

    // this TFI gets it's files from an incoming stream field, there should be no files modeled statically
    Iterable<FramedMetaverseNode> inputFiles = textFileInputStepNode.getInputFiles();
    int countInputFiles = getIterableSize( inputFiles );
    assertEquals( 0, countInputFiles );

    assertEquals( "Text file input", textFileInputStepNode.getStepType() );

    int countUsesFieldNodes = getIterableSize( textFileInputStepNode.getFileFieldNodesUses() );

    Iterable<StreamFieldNode> streamFieldNodes = textFileInputStepNode.getStreamFieldNodesCreates();
    int countCreatesFieldNodes = getIterableSize( streamFieldNodes );
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      assertNotNull( streamFieldNode.getKettleType() );
    }

    // we should create as many fields as we read in PLUS 1 for the incoming field that defines the files
    assertEquals( countUsesFieldNodes - 1, countCreatesFieldNodes );

    String filenameField = null;
    TransMeta tm =
        new TransMeta( new FileInputStream( textFileInputStepNode.getTransNode().getPath() ), null, true, null, null );
    for ( StepMeta stepMeta : tm.getSteps() ) {
      if ( stepMeta.getName().equals( textFileInputStepNode.getName() ) ) {
        TextFileInputMeta meta = (TextFileInputMeta) getBaseStepMetaFromStepMeta( stepMeta );
        assertTrue( meta.isAcceptingFilenames() );
        filenameField = meta.getAcceptingField();
        assertNotNull( filenameField );

        // this was the one we cared about...
        break;
      }
    }

    // the field that defines the files should be deleted from the outgoing fields
    Iterable<StreamFieldNode> deletedFieldNodes = textFileInputStepNode.getStreamFieldNodesDeletes();
    int countDeletedFieldNodes = getIterableSize( deletedFieldNodes );
    assertEquals( 1, countDeletedFieldNodes );

    for ( StreamFieldNode deletedFieldNode : deletedFieldNodes ) {
      assertEquals( "Should delete the stream field that defines the source files",
          filenameField, deletedFieldNode.getName() );
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
  public void testTableOutputStepNode() throws Exception {
    // this tests a specific step in a specific transform
    TableOutputStepNode tableOutputStepNode = root.getTableOutputStepNode();

    // check the table that it writes to
    TableOutputMeta meta = (TableOutputMeta) getStepMeta( tableOutputStepNode );
    String tableName = meta.getTableName();
    assertEquals( tableName, tableOutputStepNode.getDatabaseTable().getName() );

    // check the fields used
    Iterable<StreamFieldNode> uses = tableOutputStepNode.getStreamFieldNodesUses();
    int fieldsUsedCount = getIterableSize( uses );
    assertEquals( meta.getFieldStream().length, fieldsUsedCount );
    // they should all populate a db column
    for ( StreamFieldNode fieldNode : uses ) {
      assertNotNull( "Used field does not populate anything [" + fieldNode.getName() + "]",
          fieldNode.getFieldPopulatedByMe() );
      assertEquals( "Stream Field [" + fieldNode.getName() + "] populates the wrong kind of node",
          DictionaryConst.NODE_TYPE_DATA_COLUMN, fieldNode.getFieldPopulatedByMe().getType() );
    }

    int countDbConnections = getIterableSize( tableOutputStepNode.getDatasources() );
    for ( DatabaseMeta dbMeta : meta.getUsedDatabaseConnections() ) {
      assertNotNull( "Datasource is not used but should be [" + dbMeta.getName() + "]",
          tableOutputStepNode.getDatasource( dbMeta.getName() ) );
    }
    assertEquals( meta.getUsedDatabaseConnections().length, countDbConnections );


  }

  @Test
  public void testValueMapperStepNode_overwrite() throws Exception {
    ValueMapperStepNode valueMapperStepNode = root.getValueMapperStepNode( "Value Mapper - overwrite" );

    assertEquals( 1, getIterableSize( valueMapperStepNode.getStreamFieldNodesUses() ) );
    assertEquals( 0, getIterableSize( valueMapperStepNode.getStreamFieldNodesCreates() ) );
    assertEquals( 0, getIterableSize( valueMapperStepNode.getStreamFieldNodesDeletes() ) );
    StreamFieldNode usesNode = null;
    for ( StreamFieldNode node : valueMapperStepNode.getStreamFieldNodesUses() ) {
      usesNode = node;
      break;
    }

    ValueMapperMeta meta = (ValueMapperMeta) getStepMeta( valueMapperStepNode );
    assertEquals( meta.getFieldToUse(), usesNode.getName() );
    Map<String, List<String>> ops = convertOperationsStringToMap( usesNode.getDataOperations() );
    assertEquals( meta.getSourceValue().length, ops.get( DictionaryConst.PROPERTY_TRANSFORMS ).size() );
    assertEquals( meta.getTargetValue().length, ops.get( DictionaryConst.PROPERTY_TRANSFORMS ).size() );

    int derivedCount = getIterableSize( usesNode.getFieldNodesDerivedFromMe() );
    assertEquals( 0, derivedCount );

    // there should not be any metadata operations
    assertNull( usesNode.getMetadataOperations() );
  }

  @Test
  public void testValueMapperStepNode_newField() throws Exception {
    ValueMapperStepNode valueMapperStepNode = root.getValueMapperStepNode( "Value Mapper - new field" );

    assertEquals( 1, getIterableSize( valueMapperStepNode.getStreamFieldNodesUses() ) );
    assertEquals( 1, getIterableSize( valueMapperStepNode.getStreamFieldNodesCreates() ) );
    assertEquals( 0, getIterableSize( valueMapperStepNode.getStreamFieldNodesDeletes() ) );

    StreamFieldNode usesNode = null;
    for ( StreamFieldNode node : valueMapperStepNode.getStreamFieldNodesUses() ) {
      usesNode = node;
      break;
    }

    StreamFieldNode createsNode = null;
    for ( StreamFieldNode node : valueMapperStepNode.getStreamFieldNodesCreates() ) {
      createsNode = node;
      break;
    }

    ValueMapperMeta meta = (ValueMapperMeta) getStepMeta( valueMapperStepNode );
    assertEquals( meta.getFieldToUse(), usesNode.getName() );
    assertEquals( meta.getTargetField(), createsNode.getName() );
    assertNull( usesNode.getMetadataOperations() );

    int derivesCount = getIterableSize( createsNode.getFieldNodesThatDeriveMe() );
    assertEquals( 1, derivesCount );

    for ( FieldNode derives : createsNode.getFieldNodesThatDeriveMe() ) {
      assertEquals( usesNode.getName(), derives.getName() );
      assertEquals( usesNode.getType(), derives.getType() );
    }

    Map<String, List<String>> ops = convertOperationsStringToMap( createsNode.getDataOperations() );

    assertEquals( meta.getSourceValue().length, ops.get( DictionaryConst.PROPERTY_TRANSFORMS ).size() );
    assertEquals( meta.getTargetValue().length, ops.get( DictionaryConst.PROPERTY_TRANSFORMS ).size() );

    // there should not be any metadata operations
    assertNull( createsNode.getMetadataOperations() );
  }

  @Test
  public void testTextFileOutputStepNode() throws Exception {
    TextFileOutputStepNode textFileOutputStepNode = root.getTextFileOutputStepNode( "textFileOutput", "Text file output" );
    TextFileOutputMeta meta = (TextFileOutputMeta) getStepMeta( textFileOutputStepNode );
    TransMeta tm = meta.getParentStepMeta().getParentTransMeta();
    String[] fileNames = meta.getFiles( tm );

    RowMetaInterface incomingFields = tm.getStepFields( meta.getParentStepMeta() );
    TextFileField[] outputFields = meta.getOutputFields();

    assertNotNull( textFileOutputStepNode );
    // should write to one file
    Iterable<FramedMetaverseNode> outputFiles = textFileOutputStepNode.getOutputFiles();
    assertEquals( fileNames.length, getIterableSize( outputFiles ) );
    int i = 0;
    for ( FramedMetaverseNode node : outputFiles ) {
      assertEquals( fileNames[i++].replace( "file://", "" ), node.getName() );
    }

    Iterable<StreamFieldNode> usedFields = textFileOutputStepNode.getStreamFieldNodesUses();
    int usedFieldCount = getIterableSize( usedFields );
    assertEquals( outputFields.length, usedFieldCount );
    assertEquals( incomingFields.size(), usedFieldCount );

    for ( StreamFieldNode usedField : usedFields ) {
      ValueMetaInterface vmi = incomingFields.searchValueMeta( usedField.getName() );
      assertEquals( vmi.getName(), usedField.getFieldPopulatedByMe().getName() );
    }

  }

  @Test
  public void testTextFileOutputStepNode_FileFromSteamField() throws Exception {
    TextFileOutputStepNode textFileOutputStepNode = root.getTextFileOutputStepNode(
      "textFileOutput", "Text file output - file from field" );

    TextFileOutputMeta meta = (TextFileOutputMeta) getStepMeta( textFileOutputStepNode );
    TransMeta tm = meta.getParentStepMeta().getParentTransMeta();

    RowMetaInterface incomingFields = tm.getStepFields( meta.getParentStepMeta() );
    TextFileField[] outputFields = meta.getOutputFields();

    assertNotNull( textFileOutputStepNode );
    // should write to one file
    Iterable<FramedMetaverseNode> outputFiles = textFileOutputStepNode.getOutputFiles();
    assertEquals( 0, getIterableSize( outputFiles ) );

    Iterable<StreamFieldNode> usedFields = textFileOutputStepNode.getStreamFieldNodesUses();
    int usedFieldCount = getIterableSize( usedFields );
    assertEquals( outputFields.length, usedFieldCount - 1 );
    assertEquals( incomingFields.size(), usedFieldCount );

    for ( StreamFieldNode usedField : usedFields ) {
      ValueMetaInterface vmi = incomingFields.searchValueMeta( usedField.getName() );
      if ( usedField.getName().equals( meta.getFileNameField() ) ) {
        // make sure he doesn't populate anything since we aren't writing it to the file
        assertNull( usedField.getFieldPopulatedByMe() );
      } else {
        assertEquals( vmi.getName(), usedField.getFieldPopulatedByMe().getName() );
      }
    }

  }

  @Test
  public void testMergeJoinStepNode_duplicateFieldNames() throws Exception {
    MergeJoinStepNode node = root.getMergeJoinStepNode();
    MergeJoinMeta meta = (MergeJoinMeta) getStepMeta( node );
    TransMeta tm = meta.getParentStepMeta().getParentTransMeta();

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

    Iterable<StreamFieldNode> createdFields = node.getStreamFieldNodesCreates();
    assertEquals( 2, getIterableSize( createdFields ) );

    for ( StreamFieldNode createdField : createdFields ) {
      // these should have derives links
      assertTrue( createdField.getFieldNodesThatDeriveMe() != null );
    }

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

  private Map<String, List<String>> convertOperationsStringToMap( String operations ) {
    return (Map<String, List<String>>) new JSONDeserializer().deserialize( operations );
  }

}
