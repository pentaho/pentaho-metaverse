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
import com.pentaho.metaverse.api.IMetaverseReader;
import com.pentaho.metaverse.locator.FileSystemLocator;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * User: RFellows Date: 8/20/14
 */
public class MetaverseValidationIT {

  private static IMetaverseReader reader;
  private static Graph graph;
  FramedGraphFactory framedGraphFactory;
  FramedGraph framedGraph;
  RootNode root;

  @BeforeClass
  public static void init() throws Exception {
    IntegrationTestUtil.initializePentahoSystem( "src/it/resources/solution" );

    // we only care about the demo folder
    FileSystemLocator dl = PentahoSystem.get( FileSystemLocator.class );
    dl.setRootFolder( "src/it/resources/repo/demo" );

    graph = IntegrationTestUtil.buildMetaverseGraph();
    reader = PentahoSystem.get( IMetaverseReader.class );
  }

  @Before
  public void setUp() throws Exception {
    framedGraphFactory = new FramedGraphFactory( new GremlinGroovyModule() );
    framedGraph = framedGraphFactory.create( graph );
    root = (RootNode) framedGraph.getVertex( "entity", RootNode.class );
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
    MetNode node = root.getEntity( DictionaryConst.NODE_TYPE_TRANS );
    assertEquals( DictionaryConst.NODE_TYPE_ENTITY, node.getType() );
    assertEquals( DictionaryConst.NODE_TYPE_TRANS, node.getName() );
    assertEquals( "Pentaho Data Integration", node.getDescription() );
  }

  @Test
  public void testEntity_Job() throws Exception {
    MetNode node = root.getEntity( DictionaryConst.NODE_TYPE_JOB );
    assertEquals( DictionaryConst.NODE_TYPE_ENTITY, node.getType() );
    assertEquals( DictionaryConst.NODE_TYPE_JOB, node.getName() );
    assertEquals( "Pentaho Data Integration", node.getDescription() );
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

      }

      // variables?
      String[] variables = tm.listVariables();
      for ( String variable : variables ) {

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

      }

      // variables?
      String[] variables = jm.listVariables();
      for ( String variable : variables ) {

      }
    }
  }

  @Test
  public void testTransformationStepNodes() throws Exception {
    for ( TransformationNode transNode : root.getTransformations() ) {
      TransMeta tm = new TransMeta( new FileInputStream( transNode.getPath() ), null, true, null, null );

      List<StepMeta> transMetaSteps = tm.getSteps();
      int stepCount = 0;
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

      for ( TransformationStepNode tsn : transNode.getStepNodes() ) {
        stepCount++;
      }
      assertEquals( "Incorrect number of Steps in the graph for transformation [" + tm.getName() + "]",
        transMetaSteps.size(), stepCount );

    }
  }

  @Test
  public void testSelectValuesStep() throws Exception {
    SelectValuesTransStepNode selectValues = root.getSelectValuesStepNode();
    assertNotNull( selectValues );
    assertNotNull( selectValues.getStreamFieldNodesUses() );
    assertTrue( selectValues.getStreamFieldNodesCreates() != null || selectValues.getStreamFieldNodesDeletes() != null );
    assertEquals( "SelectValuesMeta", selectValues.getMetaType() );

  }

  @Test
  public void testTextFileInputStep() throws Exception {
    TextFileInputStepNode textFileInputStepNode = root.getTextFileInputStepNode();
    assertNotNull( textFileInputStepNode );

    assertNotNull( textFileInputStepNode.getInputFiles() );
    assertEquals( "TextFileInputMeta", textFileInputStepNode.getMetaType() );

    Iterable<FileFieldNode> fileFieldNodes = textFileInputStepNode.getFileFieldNodesUses();
    assertNotNull( fileFieldNodes );
    int countFileFieldNode = 0;
    for ( FileFieldNode fileFieldNode : fileFieldNodes ) {
      countFileFieldNode++;
      assertNotNull( fileFieldNode.getKettleType() );
    }

    Iterable<StreamFieldNode> streamFieldNodes = textFileInputStepNode.getStreamFieldNodesCreates();
    assertNotNull( streamFieldNodes );
    int countStreamFieldNode = 0;
    for ( StreamFieldNode streamFieldNode : streamFieldNodes ) {
      countStreamFieldNode++;
      assertNotNull( streamFieldNode.getKettleType() );
    }

    // we should create as many fields as we read in
    assertEquals( countFileFieldNode, countStreamFieldNode );

    // TODO - go get the actual StepMeta and compare it
  }

  @Test
  public void testDatasources() throws Exception {
    for ( DatasourceNode ds : root.getDatasourceNodes() ) {
      // make sure at least one step uses the connection
      assertNotNull( ds.getTransformationStepNodes() );
      assertEquals( DictionaryConst.NODE_TYPE_DATASOURCE, ds.getEntity().getName() );
      assertNotNull( ds.getName() );
      assertNotNull( ds.getPort() );
      assertNotNull( ds.getUserName() );
    }
  }

  @Test
  public void testTableOutputStepNode() throws Exception {
    TableOutputStepNode tableOutputStepNode = root.getTableOutputStepNode();
    TransMeta tm = new TransMeta( tableOutputStepNode.getTransNode().getPath(), null, true, null, null);

    // check the table that it writes to
    for ( StepMeta step : tm.getSteps() ) {
      if ( tableOutputStepNode.getName().equals( step.getName() ) ) {
        String tableName = ( (TableOutputMeta)getBaseStepMetaFromStepMeta( step ) ).getTableName();
        assertEquals( tableName, tableOutputStepNode.getDatabaseTable().getName() );
        break;
      }
    }

    // TODO - check the fields that it uses
    // TODO - check the database connection that is used

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
    switch( transactionStatus ) {
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

  public interface MetNode {
    @Property( DictionaryConst.PROPERTY_NAME )
    public String getName();

    @Property( DictionaryConst.PROPERTY_TYPE )
    public String getType();

    @Property( "virtual" )
    public Boolean isVirtual();

    @Property( DictionaryConst.PROPERTY_DESCRIPTION )
    public String getDescription();
  }

  public interface RootNode extends MetNode {
    @Property( "division" )
    public String getDivision();

    @Property( "project" )
    public String getProject();

    @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Transformation'}.dedup" )
    public Iterable<TransformationNode> getTransformations();

    @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Transformation' && it.object.name == name }.dedup" )
    public TransformationNode getTransformation( @GremlinParam( "name" ) String name );

    @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Job'}.dedup" )
    public Iterable<JobNode> getJobs();

    @GremlinGroovy( "it.out.loop(1){it.loops < 10}{it.object.type == 'Job' && it.object.name == name }.dedup" )
    public JobNode getJob( @GremlinParam( "name" ) String name );

    @Adjacency( label = "", direction = Direction.IN )
    public Iterable<MetNode> getEntities();

    @GremlinGroovy( "it.out.filter{ it.name == name }" )
    public MetNode getEntity( @GremlinParam( "name" ) String name );

    @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Select values'}" )
    public SelectValuesTransStepNode getSelectValuesStepNode();

    @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Sacramento crime stats 2006 file '}" )
    public TextFileInputStepNode getTextFileInputStepNode();

    @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Database Connection' }" )
    public Iterable<DatasourceNode> getDatasourceNodes();

    @GremlinGroovy( "it.out.loop(1){it.loops < 20}{it.object.type == 'Transformation Step' && it.object.name == 'Demo table crime stats output'}" )
    public TableOutputStepNode getTableOutputStepNode();
  }

  public interface IsAEntity extends MetNode {
    @GremlinGroovy( "it.in.filter{ it.type == 'Entity' }" )
    public MetNode getEntity();
  }

  public interface KettleNode extends IsAEntity {
    @Property( DictionaryConst.PROPERTY_PATH )
    public String getPath();

    @Property( DictionaryConst.PROPERTY_ARTIFACT_VERSION )
    public String getVersion();

    @Property( "extendedDescription" )
    public String getExtendedDescription();

    @Property( DictionaryConst.PROPERTY_STATUS )
    public String getStatus();

    @Property( DictionaryConst.PROPERTY_LAST_MODIFIED )
    public String getLastModified();

    @Property( DictionaryConst.PROPERTY_LAST_MODIFIED_BY )
    public String getLastModifiedBy();

    @Property( DictionaryConst.PROPERTY_CREATED )
    public String getCreated();

    @Property( DictionaryConst.PROPERTY_CREATED_BY )
    public String getCreatedBy();
  }

  public interface JobNode extends KettleNode {
    @Adjacency( label = "contains", direction = Direction.OUT )
    public Iterable<JobEntryNode> getJobEntryNodes();

    @GremlinGroovy( "it.out('contains').filter{it.name == name}" )
    public JobEntryNode getJobEntryNode( @GremlinParam( "name" ) String name );
  }

  public interface TransformationNode extends KettleNode {
    @Adjacency( label = "contains", direction = Direction.OUT )
    public Iterable<TransformationStepNode> getStepNodes();

    @GremlinGroovy( "it.out('contains').filter{it.name == name}" )
    public TransformationStepNode getStepNode( @GremlinParam( "name" ) String name );
  }

  public interface TransformationStepNode extends IsAEntity {
    @Property( "kettleStepMetaType" )
    public String getMetaType();

    @Adjacency( label = "contains", direction = Direction.IN )
    public TransformationNode getTransNode();

    @Adjacency( label = "deletes", direction = Direction.OUT )
    public Iterable<StreamFieldNode> getStreamFieldNodesDeletes();

    @Adjacency( label = "creates", direction = Direction.OUT )
    public Iterable<StreamFieldNode> getStreamFieldNodesCreates();
  }

  public interface JobEntryNode extends IsAEntity {
    @Adjacency( label = "contains", direction = Direction.IN )
    public TransformationNode getTransNode();
  }

  public interface FieldNode extends IsAEntity {
    @Property( DictionaryConst.PROPERTY_KETTLE_TYPE )
    public String getKettleType();
  }

  public interface StreamFieldNode extends FieldNode {
  }

  public interface FileFieldNode extends FieldNode {
  }

  public interface SelectValuesTransStepNode extends TransformationStepNode {
    @Adjacency( label = "uses", direction = Direction.OUT )
    public Iterable<StreamFieldNode> getStreamFieldNodesUses();
  }

  public interface TextFileInputStepNode extends TransformationStepNode {
    @Adjacency( label = "isreadby", direction = Direction.IN )
    public Iterable<MetNode> getInputFiles();

    @Adjacency( label = "uses", direction = Direction.OUT )
    public Iterable<FileFieldNode> getFileFieldNodesUses();
  }

  public interface TableOutputStepNode extends TransformationStepNode {
    @Adjacency( label = "dependencyof", direction = Direction.IN )
    public Iterable<DatasourceNode> getDatasource();

    @Adjacency( label = "uses", direction = Direction.OUT )
    public Iterable<DatabaseColumnNode> getDatabaseColumns();

    @Adjacency( label = "writesto", direction = Direction.OUT )
    public DatabaseTableNode getDatabaseTable();

  }

  public interface DatasourceNode extends IsAEntity {
    @Property( "port" )
    public String getPort();
    @Property( "host" )
    public String getHost();
    @Property( "userName" )
    public String getUserName();
    @Property( "password" )
    public String getPassword();
    @Adjacency( label = "dependencyof", direction = Direction.OUT )
    public Iterable<TransformationStepNode> getTransformationStepNodes();
  }

  public interface DatabaseTableNode extends MetNode {
    @Adjacency( label = "writesto", direction = Direction.IN )
    public Iterable<TransformationStepNode> getStepNodes();

    @Adjacency( label = "contains", direction = Direction.OUT )
    public Iterable<DatabaseColumnNode> getDatabaseColumns();
  }

  public interface DatabaseColumnNode extends FieldNode {
    @Adjacency( label = "populates", direction = Direction.IN )
    public Iterable<StreamFieldNode> getPopulators();

    @Adjacency( label = "contains", direction = Direction.IN )
    public TransformationStepNode getTable();

  }
}
