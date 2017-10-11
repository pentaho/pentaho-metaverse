/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.metaverse.api.model.IInfo;
import org.pentaho.metaverse.api.model.JdbcResourceInfo;
import org.pentaho.metaverse.api.model.JndiResourceInfo;
import org.pentaho.metaverse.api.model.kettle.HopInfo;
import org.pentaho.metaverse.impl.model.ParamInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tinkerpop.frames.util.Validate.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: RFellows Date: 12/2/14
 */
@RunWith( MockitoJUnitRunner.class )
public class TransMetaJsonDeserializerTest {

  TransMetaJsonDeserializer deserializer;
  @Mock Repository repo;
  @Mock JsonParser parser;
  @Mock DeserializationContext context;
  @Mock ObjectMapper mapper;
  @Mock JsonNode root;
  @Mock JsonNode root_nameNode;
  @Mock JsonNode root_descNode;
  @Mock JsonNode root_pathNode;
  @Mock JsonNode root_createdByNode;
  @Mock JsonNode root_createdDateNode;
  @Mock JsonNode root_modifiedByNode;
  @Mock JsonNode root_modifiedDateNode;
  @Mock JsonNode root_paramsArray_Node0;
  @Mock JsonNode root_paramsArray_Node1;
  @Mock JsonNode root_varsArray_Node0;
  @Mock JsonNode root_varsArray_Node1;
  @Mock JsonNode root_stepsArray_Node0;
  @Mock JsonNode root_stepsArray_Node1;
  @Mock JsonNode root_stepsArray_Node0_classNode;
  @Mock JsonNode root_stepsArray_Node0_nameNode;
  @Mock JsonNode root_stepsArray_Node0_attributesNode;
  @Mock JsonNode root_stepsArray_Node0_fieldsNode;
  @Mock JsonNode root_stepsArray_Node1_classNode;
  @Mock JsonNode root_stepsArray_Node1_nameNode;
  @Mock JsonNode root_stepsArray_Node1_attributesNode;
  @Mock JsonNode root_stepsArray_Node1_fieldsNode;
  @Mock JsonNode root_hopsArray_hop0;
  @Mock JsonNode root_connectionsArray_Node0;
  @Mock JsonNode root_connectionsArray_Node0_classNode;
  @Mock JsonNode root_connectionsArray_Node1;
  @Mock JsonNode root_connectionsArray_Node1_classNode;
  @Mock HopInfo hop0;
  @Mock StepMeta fromStep;
  @Mock StepMeta toStep;

  TransMeta transMeta;

  // DOH! can't mock/spy final classes like ArrayNode
  ArrayNode root_paramsArray = new ArrayNode( JsonNodeFactory.instance );
  ArrayNode root_varsArray = new ArrayNode( JsonNodeFactory.instance );
  ArrayNode root_stepsArray = new ArrayNode( JsonNodeFactory.instance );
  ArrayNode root_hopsArray = new ArrayNode( JsonNodeFactory.instance );
  ArrayNode root_connectionsArray = new ArrayNode( JsonNodeFactory.instance );

  ParamInfo param0 = new ParamInfo( "param0", null, "Hello", "param description" );
  ParamInfo param1 = new ParamInfo( "param1", null, "World", "param description" );

  ParamInfo var0 = new ParamInfo( "var0", "hello" );
  ParamInfo var1 = new ParamInfo( "var1", "world" );

  Map<String, Object> attrs0 = new HashMap<>();
  Map<String, Object> attrs1 = new HashMap<>();
  List<Map<String, Object>> fields0 = new ArrayList<>();
  List<Map<String, Object>> fields1 = new ArrayList<>();

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    deserializer = new TransMetaJsonDeserializer( TransMeta.class, repo );

    root_paramsArray.add( root_paramsArray_Node0 );
    root_paramsArray.add( root_paramsArray_Node1 );

    root_varsArray.add( root_varsArray_Node0 );
    root_varsArray.add( root_varsArray_Node1 );

    root_stepsArray.add( root_stepsArray_Node0 );
    root_stepsArray.add( root_stepsArray_Node1 );

    root_hopsArray.add( root_hopsArray_hop0 );

    // build parser expectations
    when( parser.getCodec() ).thenReturn( mapper );

    // build mapper expectations
    when( mapper.readTree( parser ) ).thenReturn( root );
    when( mapper.readValue( "mocked param0", ParamInfo.class ) ).thenReturn( param0 );
    when( mapper.readValue( "mocked param1", ParamInfo.class ) ).thenReturn( param1 );
    when( mapper.readValue( "mocked var0", ParamInfo.class ) ).thenReturn( var0 );
    when( mapper.readValue( "mocked var1", ParamInfo.class ) ).thenReturn( var1 );
    when( mapper.readValue( "attrs0", attrs0.getClass() ) ).then( invocationOnMock -> attrs0 );
    when( mapper.readValue( "attrs1", attrs1.getClass() ) ).then( invocationOnMock -> attrs1 );
    when( mapper.readValue( "fields0", fields0.getClass() ) ).then( invocationOnMock -> fields0 );
    when( mapper.readValue( "fields1", fields1.getClass() ) ).then( invocationOnMock -> fields1 );

    // build root node expectations
    when( root.get( IInfo.JSON_PROPERTY_NAME ) ).thenReturn( root_nameNode );
    when( root.get( IInfo.JSON_PROPERTY_DESCRIPTION ) ).thenReturn( root_descNode );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_PATH ) ).thenReturn( root_pathNode );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_CREATED_BY ) ).thenReturn( root_createdByNode );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_LAST_MODIFIED_BY ) ).thenReturn( root_modifiedByNode );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_CREATED_DATE ) ).thenReturn( root_createdDateNode );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_LAST_MODIFIED_DATE ) ).thenReturn( root_modifiedDateNode );

    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_PARAMETERS ) ).thenReturn( root_paramsArray );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_VARIABLES ) ).thenReturn( root_varsArray );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_STEPS ) ).thenReturn( root_stepsArray );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_HOPS ) ).thenReturn( root_hopsArray );
    when( root.get( TransMetaJsonSerializer.JSON_PROPERTY_CONNECTIONS ) ).thenReturn( root_connectionsArray );

    Date date = new Date();

    // build text property nodes expectations
    when( root_nameNode.textValue() ).thenReturn( "Trans Name" );
    when( root_descNode.textValue() ).thenReturn( "Trans Description" );
    when( root_pathNode.textValue() ).thenReturn( "path/to/file" );
    when( root_createdByNode.textValue() ).thenReturn( "rfellows" );
    when( root_modifiedByNode.textValue() ).thenReturn( "rfellows" );
    when( root_createdDateNode.asLong() ).thenReturn( date.getTime() );
    when( root_modifiedDateNode.asLong() ).thenReturn( date.getTime() );

    when( root_paramsArray_Node0.toString() ).thenReturn( "mocked param0" ); // mocked, values don't matter
    when( root_paramsArray_Node1.toString() ).thenReturn( "mocked param1" ); // mocked, values don't matter

    when( root_varsArray_Node0.toString() ).thenReturn( "mocked var0" ); // mocked, values don't matter
    when( root_varsArray_Node1.toString() ).thenReturn( "mocked var1" ); // mocked, values don't matter

    // build up the step nodes expectations
    when( root_stepsArray_Node0.get( IInfo.JSON_PROPERTY_CLASS ) ).thenReturn( root_stepsArray_Node0_classNode );
    when( root_stepsArray_Node0.get( IInfo.JSON_PROPERTY_NAME ) ).thenReturn( root_stepsArray_Node0_nameNode );
    when( root_stepsArray_Node0.get( AbstractStepMetaJsonSerializer.JSON_PROPERTY_ATTRIBUTES ) )
      .thenReturn( root_stepsArray_Node0_attributesNode );
    when( root_stepsArray_Node0.get( AbstractStepMetaJsonSerializer.JSON_PROPERTY_FIELDS ) )
      .thenReturn( root_stepsArray_Node0_fieldsNode );
    when( root_stepsArray_Node0_classNode.asText() ).thenReturn( DummyTransMeta.class.getName() );
    when( root_stepsArray_Node0_nameNode.asText() ).thenReturn( "Step 0" );

    when( root_stepsArray_Node1.get( IInfo.JSON_PROPERTY_CLASS ) ).thenReturn( root_stepsArray_Node1_classNode );
    when( root_stepsArray_Node1.get( IInfo.JSON_PROPERTY_NAME ) ).thenReturn( root_stepsArray_Node1_nameNode );
    when( root_stepsArray_Node1.get( AbstractStepMetaJsonSerializer.JSON_PROPERTY_ATTRIBUTES ) )
      .thenReturn( root_stepsArray_Node1_attributesNode );
    when( root_stepsArray_Node1.get( AbstractStepMetaJsonSerializer.JSON_PROPERTY_FIELDS ) )
      .thenReturn( root_stepsArray_Node1_fieldsNode );
    when( root_stepsArray_Node1_classNode.asText() ).thenReturn( DummyTransMeta.class.getName() );
    when( root_stepsArray_Node1_nameNode.asText() ).thenReturn( "Step 1" );

    when( root_stepsArray_Node0_attributesNode.toString() ).thenReturn( "attrs0" ); // mocked, values does not matter
    when( root_stepsArray_Node0_fieldsNode.toString() ).thenReturn( "fields0" ); // mocked, values does not matter
    when( root_stepsArray_Node1_attributesNode.toString() ).thenReturn( "attrs1" ); // mocked, values does not matter
    when( root_stepsArray_Node1_fieldsNode.toString() ).thenReturn( "fields1" ); // mocked, values does not matter

  }

  @Test
  public void testDeserialize() throws Exception {
    TransMeta tm = deserializer.deserialize( parser, context );
    assertNotNull( tm );
    assertEquals( "Trans Name", tm.getName() );
    assertEquals( "Trans Description", tm.getDescription() );
    assertEquals( "rfellows", tm.getCreatedUser() );
    assertNotNull( tm.getCreatedDate() );
    assertEquals( "rfellows", tm.getModifiedUser() );
    assertNotNull( tm.getModifiedDate() );
    assertEquals( "path/to/file", tm.getFilename() );

    assertEquals( root_paramsArray.size(), tm.listParameters().length );
    assertEquals( root_stepsArray.size(), tm.getSteps().size() );
  }

  @Test
  public void testDeserializeHops() throws Exception {
    transMeta = spy( new TransMeta() );
    when( transMeta.findStep( "from" ) ).thenReturn( fromStep );
    when( transMeta.findStep( "to" ) ).thenReturn( toStep );
    when( mapper.readValue( "to be mocked", HopInfo.class ) ).thenReturn( hop0 );
    when( root_hopsArray_hop0.toString() ).thenReturn( "to be mocked" ); // mocked, value does not matter
    when( hop0.isEnabled() ).thenReturn( true );
    when( hop0.getFromStepName() ).thenReturn( "from" );
    when( hop0.getToStepName() ).thenReturn( "to" );

    deserializer.deserializeHops( transMeta, root, mapper );

    assertEquals( root_hopsArray.size(), transMeta.nrTransHops() );

  }

  @Test
  public void testDeserializeVariables() throws Exception {
    transMeta = spy( new TransMeta() );

    deserializer.deserializeVariables( transMeta, root, mapper );

    verify( transMeta, times( root_varsArray.size() ) ).setVariable( anyString(), anyString() );

  }

  @Test
  public void testConstructor() throws Exception {
    // for code coverage
    deserializer = new TransMetaJsonDeserializer( TransMeta.class );
  }

  @Test
  public void testWriteJsonAttributes() throws Exception {
    JsonNode attributes = mock( JsonNode.class );
    ObjectId stepId = new StringObjectId( "id" );

    Map<String, Object> attrMap = new HashMap<String, Object>(){{
      put( "name", "Test" );
      put( "int", 2 );
      put( "long", 3L );
      put( "double", 3.0D );
      put( "bool", true );
      put( "null", null );
    }};

    when( attributes.toString() ).thenReturn( "mockedAttributes" );
    when( mapper.readValue( "mockedAttributes", attrs0.getClass() ) ).then( invocationOnMock -> attrMap );

    deserializer.writeJsonAttributes( attributes, mapper, stepId );

    verify( repo ).saveStepAttribute( null, stepId, "name", "Test" );
    verify( repo ).saveStepAttribute( null, stepId, "int", 2 );
    verify( repo ).saveStepAttribute( null, stepId, "long", 3L );
    verify( repo ).saveStepAttribute( null, stepId, "double", 3.0D );
    verify( repo ).saveStepAttribute( null, stepId, "bool", true );
    verify( repo ).saveStepAttribute( null, stepId, "null", null );
  }

  @Test
  public void testWriteJsonFields() throws Exception {
    JsonNode fields = mock( JsonNode.class );
    ObjectId stepId = new StringObjectId( "id" );
    List<Map<String, Object>> fieldLists = new ArrayList<Map<String, Object>>();
    Map<String, Object> fieldMap0 = new HashMap<String, Object>(){{
      put( "name", "Test 1" );
      put( "int", 2 );
      put( "long", 3L );
      put( "double", 3.0D );
      put( "bool", true );
      put( "null", null );
    }};
    Map<String, Object> fieldMap1 = new HashMap<String, Object>(){{
      put( "name", "Test 2" );
      put( "int", 2 );
      put( "long", 3L );
      put( "double", 3.0D );
      put( "bool", true );
      put( "null", null );
    }};
    fieldLists.add( fieldMap0 );
    fieldLists.add( fieldMap1 );

    when( fields.toString() ).thenReturn( "mockedFields" );
    when( mapper.readValue( "mockedFields", fieldLists.getClass() ) ).then( invocationOnMock -> fieldLists );

    deserializer.writeJsonFields( fields, mapper, stepId );

    verify( repo ).saveStepAttribute( null, stepId, 0, "name", "Test 1" );
    verify( repo ).saveStepAttribute( null, stepId, 1, "name", "Test 2" );

    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "int" ), anyInt() );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "long" ), eq( 3L ) );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "double" ), eq( 3.0D ) );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "bool" ), eq( true ) );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "null" ), anyString() ) ;
  }

  @Test
  public void testDeserializeConnections() throws Exception {
    JndiResourceInfo jndi = new JndiResourceInfo( "jndi" );
    jndi.setPluginId( "ORACLE" );
    JdbcResourceInfo jdbc = new JdbcResourceInfo( "localhost", "test", 5432, "sa", "password" );
    jdbc.setPluginId( "POSTGRESQL" );

    transMeta = spy( new TransMeta() );

    root_connectionsArray.add( root_connectionsArray_Node0 );
    root_connectionsArray.add( root_connectionsArray_Node1 );

    when( root_connectionsArray_Node0.toString() ).thenReturn( "mocked jdbc" );
    when( root_connectionsArray_Node0.get( IInfo.JSON_PROPERTY_CLASS ) )
      .thenReturn( root_connectionsArray_Node0_classNode );
    when( root_connectionsArray_Node0_classNode.asText() ).thenReturn( JdbcResourceInfo.class.getName() );

    when( root_connectionsArray_Node1.toString() ).thenReturn( "mocked jndi" );
    when( root_connectionsArray_Node1.get( IInfo.JSON_PROPERTY_CLASS ) )
      .thenReturn( root_connectionsArray_Node1_classNode );
    when( root_connectionsArray_Node1_classNode.asText() ).thenReturn( JndiResourceInfo.class.getName() );

    when( mapper.readValue( "mocked jdbc", JdbcResourceInfo.class ) ).thenReturn( jdbc );
    when( mapper.readValue( "mocked jndi", JndiResourceInfo.class ) ).thenReturn( jndi );

    deserializer.deserializeConnections( transMeta, root, mapper );
    assertEquals( root_connectionsArray.size(), transMeta.getDatabases().size() );
    for ( DatabaseMeta databaseMeta : transMeta.getDatabases() ) {
      assertNotNull( databaseMeta.getDatabaseInterface() );
    }

  }
}
