/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.api.analyzer.kettle.annotations;

import com.google.common.collect.ImmutableMap;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResultMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.trans.steps.writetolog.WriteToLogMeta;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.dictionary.MetaverseTransientNode;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
import org.pentaho.metaverse.api.analyzer.kettle.step.SubtransAnalyzer;
import org.pentaho.metaverse.api.model.BaseMetaverseBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_DATASOURCE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_MESSAGE_QUEUE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_OTHER;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_DEFINES;
import static org.pentaho.dictionary.DictionaryConst.LINK_DEPENDENCYOF;
import static org.pentaho.dictionary.DictionaryConst.LINK_INPUTS;
import static org.pentaho.dictionary.DictionaryConst.LINK_PARENT_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_READBY;
import static org.pentaho.dictionary.DictionaryConst.LINK_WRITESTO;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_EXTERNAL_CONNECTION;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_TRANS_FIELD;
import static org.pentaho.dictionary.DictionaryConst.PROPERTY_NAME;
import static org.pentaho.dictionary.DictionaryConst.PROPERTY_TYPE;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.AnnotationDrivenStepMetaAnalyzerTest.TestStepMeta.CONN_TYPE;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.AnnotationDrivenStepMetaAnalyzerTest.TestStepMeta.TEST_TYPE;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.FALSE;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.SUBTRANS_INPUT;
import static org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer.RESOURCE;


@RunWith ( PowerMockRunner.class )
@PrepareForTest( KettleAnalyzerUtil.class )
public class AnnotationDrivenStepMetaAnalyzerTest {

  private Map<String, String> typeCategoryMap = new HashMap<>();
  @Mock AnnotationDrivenStepMetaAnalyzer.EntityRegister entityRegister;
  private RowMeta inputRowMeta;
  private RowMeta outputRowMeta;

  @Mock SubtransAnalyzer subtransAnalyzer;

  private IMetaverseNode root = new MetaverseTransientNode( "rootNode" );
  private IMetaverseNode outputMessageNode = new MetaverseTransientNode( "messageOutputNode" );
  private IMetaverseNode outputTopicNode = new MetaverseTransientNode( "topicOutputNode" );
  private StepMetaInterface meta;
  private StepMetaInterface mappingMeta;
  private TestableAnnotationDrivenAnalyzer analyzer;
  private static IMetaverseNode subTransRoot = new MetaverseTransientNode( "subTransRootNode" );


  @BeforeClass
  public static void setupClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init();
  }

  @Before
  public void before() throws KettleException {
    inputRowMeta = new RowMeta();
    outputRowMeta = new RowMeta();

    this.meta = getTestStepMeta( inputRowMeta );
    outputMessageNode.setName( "messageOutputNode" );
    outputMessageNode.setType( NODE_TYPE_TRANS_FIELD );
    outputTopicNode.setName( "topicOutputNode" );
    outputTopicNode.setType( NODE_TYPE_TRANS_FIELD );

    analyzer = new TestableAnnotationDrivenAnalyzer( new TestStepMeta(), typeCategoryMap, entityRegister );
    //doNothing().when( subtransAnalyzer ).linkResultFieldToSubTrans( any(), any(), any(), any() );
  }


  @After
  public void after() {
    // the act of creating a metaverse builder registers entities.  Clear them out so
    // no state is carried forward.
    DictionaryHelper.clearEntityRegistry();
  }

  @Test
  public void testGetUsedFields() {
    Set<StepField> usedFields = analyzer.getUsedFields( (BaseStepMeta) meta );
    assertThat( usedFields.size(), equalTo( 5 ) );
    List<String> usedList = usedFields.stream()
      .map( StepField::getFieldName )
      .sorted()
      .collect( Collectors.toList() );
    List<String> inputSteps = usedFields.stream()
      .map( StepField::getStepName )
      .sorted()
      .collect( Collectors.toList() );
    assertThat( usedList, equalTo( Arrays.asList( "messageField", "substitutedField",
      "usedField", "usedField1", "usedField2" ) ) );
    assertThat( inputSteps, equalTo( Arrays.asList( "Step1", "Step1", "Step1", "Step1", "Step1" ) ) );
  }

  @Test
  public void testCustomAnalyze() throws MetaverseAnalyzerException {
    IMetaverseBuilder builder = getBuilder( root );
    ///analyzer.processInputs();
    analyzer.customAnalyze( (BaseStepMeta) meta, root );
    Graph graph = builder.getGraph();

    List<Edge> edges = getEdgesWithLabel( graph, LINK_READBY );

    assertThat( edges.size(), equalTo( 1 ) );
    Edge e = edges.get( 0 );

    Vertex externalResourceV = e.getVertex( Direction.OUT );
    assertThat( externalResourceV.getProperty( "server" ), equalTo( "ServernameOrWhatever" ) );
    assertThat( externalResourceV.getProperty( "port" ), equalTo( "123" ) );
    assertThat( externalResourceV.getProperty( "isSsl" ), equalTo( "true" ) );
    assertThat( externalResourceV.getProperty( "name" ), equalTo( "ServernameOrWhatever" ) );
    assertThat( externalResourceV.getProperty( "type" ), equalTo( TEST_TYPE ) );

    // nested properties in referenced class
    assertThat( externalResourceV.getProperty( "SUBPROP" ), equalTo( "subProp1" ) );
    assertThat( externalResourceV.getProperty( "subProp2" ), equalTo( "123" ) );
    assertThat( externalResourceV.getProperty( "method-property" ), equalTo( "blah" ) );

    edges = getEdgesWithLabel( graph, LINK_DEPENDENCYOF );

    assertThat( edges.size(), equalTo( 1 ) );
    e = edges.get( 0 );

    externalResourceV = e.getVertex( Direction.OUT );
    assertThat( externalResourceV.getProperty( "dbaddress" ), equalTo( "127.0.0.1:3363" ) );
  }

  @Test
  public void testCustomAnalyzeSubtrans() throws MetaverseAnalyzerException, KettleException {
    IMetaverseBuilder builder = getBuilder( root );

    this.mappingMeta = getTestStepMappingMeta( inputRowMeta, outputRowMeta );

    analyzer.customAnalyze( (BaseStepMeta) mappingMeta, root );
    Graph graph = builder.getGraph();

    List<Edge> edges = getEdgesWithLabel( graph, LINK_READBY );

    assertThat( edges.size(), equalTo( 1 ) );
    Edge e = edges.get( 0 );

    Vertex externalResourceV = e.getVertex( Direction.OUT );
    assertThat( externalResourceV.getProperty( "server" ), equalTo( "ServernameOrWhatever" ) );
    assertThat( externalResourceV.getProperty( "port" ), equalTo( "123" ) );
    assertThat( externalResourceV.getProperty( "isSsl" ), equalTo( "true" ) );
    assertThat( externalResourceV.getProperty( "name" ), equalTo( "ServernameOrWhatever" ) );
    assertThat( externalResourceV.getProperty( "type" ), equalTo( TEST_TYPE ) );

    //verify server node "contains" the two input resources
    List<Vertex> resourceNodes = new ArrayList<>();
    externalResourceV.getVertices( Direction.OUT ).forEach( v -> resourceNodes.add( v ) );
    assertThat( resourceNodes.size(), equalTo( 3 ) );
    Vertex messageVertex = resourceNodes.stream().filter( v -> v.getProperty( PROPERTY_NAME ).equals( "message" ) ).findFirst().get();
    Vertex topicVertex = resourceNodes.stream().filter( v -> v.getProperty( PROPERTY_NAME ).equals( "topic" ) ).findFirst().get();

    assertThat( messageVertex.getProperty( PROPERTY_TYPE ), equalTo( RESOURCE ) );
    assertThat( topicVertex.getProperty( PROPERTY_TYPE ), equalTo( RESOURCE ) );

    // nested properties in referenced class
    assertThat( externalResourceV.getProperty( "SUBPROP" ), equalTo( "subProp1" ) );
    assertThat( externalResourceV.getProperty( "subProp2" ), equalTo( "123" ) );

    edges = getEdgesWithLabel( graph, LINK_DEPENDENCYOF );

    assertThat( edges.size(), equalTo( 1 ) );
    e = edges.get( 0 );

    externalResourceV = e.getVertex( Direction.OUT );
    assertThat( externalResourceV.getProperty( "dbaddress" ), equalTo( "127.0.0.1:3363" ) );

    doNothing().when( subtransAnalyzer ).linkUsedFieldToSubTrans( any(), any(), any(), any(), any() );
    doNothing().when( subtransAnalyzer ).linkResultFieldToSubTrans(
      any( IMetaverseNode.class ), any( TransMeta.class ), any( IMetaverseNode.class ),
      any( IComponentDescriptor.class ), any( String.class ) );

    // verify subtrans analyzer was called on the expected nodes for input to subtrans
    ArgumentCaptor<IMetaverseNode> usedFieldsNodeCaptor = ArgumentCaptor.forClass( IMetaverseNode.class );
    ArgumentCaptor<IMetaverseNode> outputFieldsNodeCaptor = ArgumentCaptor.forClass( IMetaverseNode.class );

    verify( subtransAnalyzer, times( 2 ) )
      .linkResultFieldToSubTrans( outputFieldsNodeCaptor.capture(), any(), any(), any(), any() );
    verify( subtransAnalyzer, times( 2 ) )
      .linkUsedFieldToSubTrans( usedFieldsNodeCaptor.capture(), any(), any(), any(), any() );

    IMetaverseNode messageNode = usedFieldsNodeCaptor.getAllValues().stream()
      .filter( node -> node.getName().equals( "message" ) ).findFirst().get();
    IMetaverseNode topicNode = usedFieldsNodeCaptor.getAllValues().stream()
      .filter( node -> node.getName().equals( "topic" ) ).findFirst().get();

    assertThat( usedFieldsNodeCaptor.getAllValues().size(), equalTo( 2 ) );
    assertThat( topicNode.getType(), equalTo( RESOURCE ) );
    assertThat( messageNode.getType(), equalTo( RESOURCE ) );

    // verify subtrans analyzer was called on the expected nodes for output from subtrans
    IMetaverseNode messageOutputNode = outputFieldsNodeCaptor.getAllValues().stream()
      .filter( node -> node.getName().equals( "messageOutputNode" ) ).findFirst().get();
    IMetaverseNode topicOutputNode = outputFieldsNodeCaptor.getAllValues().stream()
      .filter( node -> node.getName().equals( "topicOutputNode" ) ).findFirst().get();

    assertThat( outputFieldsNodeCaptor.getAllValues().size(), equalTo( 2 ) );
    assertThat( topicOutputNode.getType(), equalTo( NODE_TYPE_TRANS_FIELD ) );
    assertThat( messageOutputNode.getType(), equalTo( NODE_TYPE_TRANS_FIELD ) );
  }

  @Test
  public void testResourceLinkage() throws MetaverseAnalyzerException {
    IMetaverseBuilder builder = getBuilder( root );

    MetaverseComponentDescriptor descriptor = new MetaverseComponentDescriptor(
      "name", "type",
      root, analyzer.getDescriptor().getContext() );

    analyzer.analyze( descriptor, (BaseStepMeta) meta );
    Graph graph = builder.getGraph();

    List<Edge> edges = getEdgesWithLabel( graph, LINK_WRITESTO );

    assertThat( edges.size(), equalTo( 2 ) );
    edges.sort( Comparator.comparing( e -> e.getVertex( Direction.IN ).getProperty( "name" ) ) );
    assertEdge( edges.get( 0 ), "Bora Bora", "name" );
    assertEdge( edges.get( 1 ), "message", "ServernameOrWhatever" );

    edges = getEdgesWithLabel( graph, LINK_CONTAINS );
    assertThat( edges.size(), equalTo( 1 ) );
    assertEdge( edges.get( 0 ), "name", "private-field-value" );

  }

  private void assertEdge( Edge edge, String from, String to ) {
    assertThat( edge.getVertex( Direction.IN ).getProperty( "name" ), equalTo( from ) );
    assertThat( edge.getVertex( Direction.OUT ).getProperty( "name" ), equalTo( to ) );
  }

  @Test
  public void testCategoryLinks() {
    assertThat( typeCategoryMap.get( CONN_TYPE ),
      equalTo( CATEGORY_DATASOURCE ) );
    assertThat( typeCategoryMap.get( TEST_TYPE ),
      equalTo( CATEGORY_OTHER ) );
  }

  @Test
  public void testEntityLinks() {

    verify( entityRegister ).registerEntityTypes( LINK_PARENT_CONCEPT, CONN_TYPE, NODE_TYPE_EXTERNAL_CONNECTION );
    verify( entityRegister ).registerEntityTypes( LINK_CONTAINS_CONCEPT, TEST_TYPE, CONN_TYPE );
    verify( entityRegister ).registerEntityTypes( LINK_PARENT_CONCEPT, TEST_TYPE, null );
  }

  @Test
  public void testGetSupportedSteps() {
    assertThat( analyzer.getSupportedSteps(), equalTo( singleton( meta.getClass() ) ) );
  }


  private IMetaverseBuilder getBuilder( IMetaverseNode rootNode ) {
    IComponentDescriptor descriptor = new MetaverseComponentDescriptor( "descriptor", "someType", rootNode );
    analyzer.setDescriptor( descriptor );
    IMetaverseBuilder builder = new BaseMetaverseBuilder( new TinkerGraph() );
    analyzer.setMetaverseBuilder( builder );
    return builder;
  }

  private List<Edge> getEdgesWithLabel( Graph graph, String edgeName ) {
    return StreamSupport.stream( graph.getEdges().spliterator(), false )
      .filter( edge -> edge.getLabel().equals( edgeName ) )
      .collect( Collectors.toList() );
  }

  /**
   * Used to stub out some values that would normally be set during analyze() of a full transformation.
   */
  class TestableAnnotationDrivenAnalyzer extends AnnotationDrivenStepMetaAnalyzer {

    TestableAnnotationDrivenAnalyzer( BaseStepMeta meta, Map<String, String> typeCategoryMap,
                                      EntityRegister register ) {
      super( meta, typeCategoryMap, register );
    }

    {
      setMetaverseObjectFactory( new MetaverseObjectFactory() );
      rootNode = root;
    }

    protected SubtransAnalyzer<BaseStepMeta> getSubtransAnalyzer() {
      return subtransAnalyzer;
    }

    public Map<String, RowMetaInterface> getInputFields( final TransMeta parentTransMeta,
                                                         final StepMeta parentStepMeta ) {

      return ImmutableMap.of( "Step1", inputRowMeta,
        "Step2", new RowMeta() );
    }

    public StepNodes getOutputs() {
      StepNodes outputs = new StepNodes();
      outputs.addNode( "SomeNextStep", "messageFieldOut", outputMessageNode );
      outputs.addNode( "SomeNextStep", "topicFieldOut", outputTopicNode );
      return outputs;
    }
  }

  static TestStepMeta getTestStepMeta( RowMeta inputRowMeta ) {
    TestStepMeta meta = new TestStepMeta(); // step meta with metaverse annotations

    inputRowMeta.addValueMeta( new ValueMetaString( "usedField" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "usedField1" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "usedField2" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "messageField" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "substitutedField" ) );

    StepMeta parentStepMeta = new StepMeta();
    final Variables variables = new Variables();
    variables.setVariable( "substitute", "substitutedField" );

    TransMeta transMeta = spy( new TransMeta( variables ) );
    // stub out previous step input
    try {
      when( transMeta.getPrevStepFields( any(), any(), any() ) ).thenReturn( inputRowMeta );
    } catch ( KettleStepException e ) {
      throw new IllegalStateException( e );
    }

    meta.setParentStepMeta( parentStepMeta );
    parentStepMeta.setStepMetaInterface( meta );
    StepIOMeta stepIOMeta = new StepIOMeta( true, true, true,
      false, false, false );

    meta.setStepIOMeta( stepIOMeta );
    transMeta.addStep( parentStepMeta );
    return meta;
  }

  static TestStepMappingMeta getTestStepMappingMeta( RowMeta inputRowMeta, RowMeta outputRowMeta ) throws KettleException {
    PowerMockito.mockStatic( KettleAnalyzerUtil.class );
    TestStepMappingMeta meta = new TestStepMappingMeta(); // step meta with metaverse annotations

    inputRowMeta.addValueMeta( new ValueMetaString( "usedField" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "usedField1" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "usedField2" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "messageField" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "substitutedField" ) );

    outputRowMeta.addValueMeta( new ValueMetaString( "messageFieldOut" ) );
    outputRowMeta.addValueMeta( new ValueMetaString( "topicFieldOut" ) );

    StepMeta parentStepMeta = new StepMeta();
    final Variables variables = new Variables();
    variables.setVariable( "substitute", "substitutedField" );

    TransMeta transMeta = spy( new TransMeta( variables ) );
    TransMeta subTransMeta = spy( new TransMeta( variables ) );

    Repository repository = mock( Repository.class );
    RepositoryDirectory repositoryDirectory = mock( RepositoryDirectory.class );

    transMeta.setRepository( repository );

    when( repository.findDirectory( any( String.class ) ) ).thenReturn( repositoryDirectory );
    when( repository.loadTransformation( any( String.class ), eq( repositoryDirectory ), any( ProgressMonitorListener.class ), any( Boolean.class ), any( String.class ) ) ).thenReturn( subTransMeta );

    // stub out previous step input
    try {
      when( transMeta.getPrevStepFields( any(), any(), any() ) ).thenReturn( inputRowMeta );
    } catch ( KettleStepException e ) {
      throw new IllegalStateException( e );
    }

    // stub out output
    try {
      when( transMeta.getStepFields( any(), any() ) ).thenReturn( outputRowMeta );
    } catch ( KettleStepException e ) {
      throw new IllegalStateException( e );
    }

    meta.setParentStepMeta( parentStepMeta );
    parentStepMeta.setStepMetaInterface( meta );
    StepIOMeta stepIOMeta = new StepIOMeta( true, true, true,
      false, false, false );

    meta.setStepIOMeta( stepIOMeta );
    transMeta.addStep( parentStepMeta );

    StepMeta firstSubStepMeta = new StepMeta();
    StepMeta secondSubStepMeta = new StepMeta();
    RowsFromResultMeta rowsFromResultMeta = new RowsFromResultMeta();
    WriteToLogMeta writeToLogMeta = new WriteToLogMeta();

    String[] fieldNameArray = new String[] { "messageFieldOut", "topicFieldOut" };
    int[] typeArray = new int[] { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING  };
    int[] lengthArray = new int[] { 0, 0 };
    int[] precisionArray = new int[] { 0, 0 };

    rowsFromResultMeta.setFieldname( fieldNameArray );
    rowsFromResultMeta.setType( typeArray );
    rowsFromResultMeta.setPrecision( precisionArray );
    rowsFromResultMeta.setLength( lengthArray );
    writeToLogMeta.setFieldName( fieldNameArray );

    rowsFromResultMeta.setParentStepMeta( firstSubStepMeta );
    writeToLogMeta.setParentStepMeta( secondSubStepMeta );
    firstSubStepMeta.setStepMetaInterface( rowsFromResultMeta );
    firstSubStepMeta.setName( "Rows from result" );
    secondSubStepMeta.setStepMetaInterface( writeToLogMeta );
    secondSubStepMeta.setName( "Write to log" );
    rowsFromResultMeta.setStepIOMeta( stepIOMeta );
    writeToLogMeta.setStepIOMeta( stepIOMeta );
    subTransMeta.addStep( firstSubStepMeta );
    subTransMeta.addStep( secondSubStepMeta );
    subTransMeta.addTransHop( new TransHopMeta( firstSubStepMeta, secondSubStepMeta, true ) );
    subTransMeta.setName( "SubTransMetaName" );

    try {
      when( KettleAnalyzerUtil.analyze( any(), any(), any(), any() ) ).thenReturn( subTransRoot );
    } catch ( MetaverseAnalyzerException e ) {
      fail( e.getMessage() );
    }

    return meta;
  }


  @Metaverse.CategoryMap ( entity = CONN_TYPE, category = CATEGORY_DATASOURCE )
  @Metaverse.CategoryMap ( entity = TEST_TYPE, category = CATEGORY_OTHER )
  @Metaverse.EntityLink ( entity = CONN_TYPE, link = LINK_PARENT_CONCEPT, parentEntity =
    NODE_TYPE_EXTERNAL_CONNECTION )
  @Metaverse.EntityLink ( entity = TEST_TYPE, link = LINK_CONTAINS_CONCEPT, parentEntity = CONN_TYPE )
  @Metaverse.EntityLink ( entity = TEST_TYPE, link = LINK_PARENT_CONCEPT )
  @SuppressWarnings ( "unused" )
  static class TestStepMeta extends BaseStepMeta implements StepMetaInterface {

    static final String CONN_TYPE = "test_conn_type";
    static final String TEST_TYPE = "test_type";

    @Metaverse.Property public String field1 = "usedField";
    @Metaverse.Property public String field2 = "usedField1";
    @Metaverse.Property public String field3 = "usedField2";
    @Metaverse.Property public String envSubstituteAttr = "${substitute}";
    public String otherMeta;
    public String otherMeta2;

    @InjectionDeep
    public SubMeta subMeta = new SubMeta();

    @Metaverse.Node ( link = LINK_READBY, type = TEST_TYPE, name = "test_name" )
    @Metaverse.Property ( name = "server", parentNodeName = "test_name" ) public String serverER =
      "ServernameOrWhatever";
    @Metaverse.Property ( name = "port", parentNodeName = "test_name" ) public int portER = 123;
    @Metaverse.Property ( name = "isSsl", parentNodeName = "test_name" ) public boolean isSslER = true;

    @Metaverse.Node ( name = "message", type = RESOURCE, link = LINK_DEFINES, nameFromValue = "FALSE" )
    @Metaverse.Property ( name = "message", parentNodeName = "test_name", category = CATEGORY_MESSAGE_QUEUE )
    @Metaverse.NodeLink ( nodeName = "message", parentNodeName = "test_name", parentNodelink = LINK_WRITESTO,
      linkDirection = "OUT" )
    public String message = "messageField";

    @Metaverse.Node ( name = "vacataion_spot", type = TEST_TYPE, link = LINK_WRITESTO, linkDirection = "IN" )
    public String destination = "Bora Bora";

    @Metaverse.Node ( type = CONN_TYPE, name = "test_conn_name" )
    @Metaverse.Property ( name = "dbaddress", parentNodeName = "test_conn_name" ) public String dbaddress =
      "127.0.0.1:3363";

    @Metaverse.Property ( name = "method-property", parentNodeName = "test_name" )
    public String methodProperty() {
      return "blah";
    }

    @Metaverse.Node ( name = "method-node", type = "otherType", link = LINK_CONTAINS )
    public String methodNode() {
      return "private-field-value";
    }

    @Override public void setDefault() {

    }

    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int i, TransMeta transMeta,
                                  Trans trans ) {
      return null;
    }

    @Override public StepDataInterface getStepData() {
      return null;
    }
  }

  @Metaverse.CategoryMap ( entity = CONN_TYPE, category = CATEGORY_DATASOURCE )
  @Metaverse.CategoryMap ( entity = TEST_TYPE, category = CATEGORY_OTHER )
  @Metaverse.EntityLink ( entity = CONN_TYPE, link = LINK_PARENT_CONCEPT, parentEntity =
    NODE_TYPE_EXTERNAL_CONNECTION )
  @Metaverse.EntityLink ( entity = TEST_TYPE, link = LINK_CONTAINS_CONCEPT, parentEntity = CONN_TYPE )
  @Metaverse.EntityLink ( entity = TEST_TYPE, link = LINK_PARENT_CONCEPT )
  @SuppressWarnings ( "unused" )
  static class TestStepMappingMeta extends BaseStreamStepMeta implements StepMetaInterface, ISubTransAwareMeta {

    static final String CONN_TYPE = "test_conn_type";
    static final String TEST_TYPE = "test_type";

    @Metaverse.Property public String field1 = "usedField";
    @Metaverse.Property public String field2 = "usedField1";
    @Metaverse.Property public String field3 = "usedField2";
    @Metaverse.Property public String envSubstituteAttr = "${substitute}";
    public String otherMeta;
    public String otherMeta2;

    @InjectionDeep
    public SubMeta subMeta = new SubMeta();

    @Metaverse.Node ( link = LINK_READBY, type = TEST_TYPE, name = "test_name" )
    @Metaverse.Property ( name = "server", parentNodeName = "test_name" ) public String serverER =
      "ServernameOrWhatever";
    @Metaverse.Property ( name = "port", parentNodeName = "test_name" ) public int portER = 123;
    @Metaverse.Property ( name = "isSsl", parentNodeName = "test_name" ) public boolean isSslER = true;

    @Metaverse.Node ( name = "message", type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
    @Metaverse.Property ( name = "message", parentNodeName = "test_name", category = CATEGORY_MESSAGE_QUEUE )
    @Metaverse.NodeLink ( nodeName = "message", parentNodeName = "test_name", parentNodelink = LINK_CONTAINS,
      linkDirection = "OUT" )
    public String message = "messageField";


    @Metaverse.Node ( name = "topic", type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
    @Metaverse.Property ( name = "topic", parentNodeName = "test_name", category = CATEGORY_MESSAGE_QUEUE )
    @Metaverse.NodeLink ( nodeName = "topic", parentNodeName = "test_name", parentNodelink = LINK_CONTAINS,
      linkDirection = "OUT" )
    public String topic = "topicField";

    @Metaverse.Node ( name = "vacataion_spot", type = TEST_TYPE, link = LINK_WRITESTO, linkDirection = "IN" )
    public String destination = "Bora Bora";

    @Metaverse.Node ( type = CONN_TYPE, name = "test_conn_name" )
    @Metaverse.Property ( name = "dbaddress", parentNodeName = "test_conn_name" ) public String dbaddress =
      "127.0.0.1:3363";

    @Override public void setDefault() {

    }

    @Override
    public RowMeta getRowMeta( String s, VariableSpace variableSpace ) throws KettleStepException {
      return null;
    }

    @Override
    public String getSubStep() {
      return "Write to log";
    }

    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int i, TransMeta transMeta,
                                  Trans trans ) {
      return null;
    }

    @Override public StepDataInterface getStepData() {
      return null;
    }

    @Override public String getTransName() {
      return "TestTransName";
    }

    @Override public String getDirectoryPath() {
      return "/";
    }

    public TestStepMappingMeta() {
      super();
      setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    }
  }

  /**
   * Used to test meta elements in referenced objects.
   * (similar to @InjectionDeep with Metadata injection
   */
  @Metaverse
  @SuppressWarnings ( "unused" )
  static class SubMeta {

    @Metaverse.Property ( name = "SUBPROP", parentNodeName = "test_name" )
    public String someSubProp = "subProp1";

    @Metaverse.Property ( name = "subProp2", parentNodeName = "test_name" )
    public int subPropNum2 = 123;

  }
}
