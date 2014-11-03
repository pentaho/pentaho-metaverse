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

package com.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.pentaho.metaverse.impl.model.ParamInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tinkerpop.frames.util.Validate.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
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
  @Mock JsonNode root_paramsArray_Node0;
  @Mock JsonNode root_paramsArray_Node1;
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

  // DOH! can't mock/spy final classes like ArrayNode
  ArrayNode root_paramsArray = new ArrayNode( JsonNodeFactory.instance );
  ArrayNode root_stepsArray = new ArrayNode( JsonNodeFactory.instance );

  ParamInfo param0 = new ParamInfo( "param0", null, "Hello", "param description" );
  ParamInfo param1 = new ParamInfo( "param1", null, "World", "param description" );

  Map<String, Object> attrs0 = new HashMap<String, Object>();
  Map<String, Object> attrs1 = new HashMap<String, Object>();
  List<Map<String, Object>> fields0 = new ArrayList<Map<String, Object>>();
  List<Map<String, Object>> fields1 = new ArrayList<Map<String, Object>>();

  @Before
  public void setUp() throws Exception {
    deserializer = new TransMetaJsonDeserializer( TransMeta.class, repo );

    root_paramsArray.add( root_paramsArray_Node0 );
    root_paramsArray.add( root_paramsArray_Node1 );

    root_stepsArray.add( root_stepsArray_Node0 );
    root_stepsArray.add( root_stepsArray_Node1 );

    // build parser expectations
    when( parser.getCodec() ).thenReturn( mapper );

    // build mapper expectations
    when( mapper.readTree( parser ) ).thenReturn( root );
    when( mapper.readValue( "mocked param0", ParamInfo.class ) ).thenReturn( param0 );
    when( mapper.readValue( "mocked param1", ParamInfo.class ) ).thenReturn( param1 );
    when( mapper.readValue( "attrs0", attrs0.getClass() ) ).thenReturn( attrs0 );
    when( mapper.readValue( "attrs1", attrs1.getClass() ) ).thenReturn( attrs1 );
    when( mapper.readValue( "fields0", fields0.getClass() ) ).thenReturn( fields0 );
    when( mapper.readValue( "fields1", fields1.getClass() ) ).thenReturn( fields1 );

    // build root node expectations
    when( root.get( "name" ) ).thenReturn( root_nameNode );
    when( root.get( "description" ) ).thenReturn( root_descNode );
    when( root.get( "parameters" ) ).thenReturn( root_paramsArray );
    when( root.get( "steps" ) ).thenReturn( root_stepsArray );

    // build text property nodes expectations
    when( root_nameNode.textValue() ).thenReturn( "Trans Name" );
    when( root_descNode.textValue() ).thenReturn( "Trans Description" );

    when( root_paramsArray_Node0.toString() ).thenReturn( "mocked param0" ); // mocked, values don't matter
    when( root_paramsArray_Node1.toString() ).thenReturn( "mocked param1" ); // mocked, values don't matter

    // build up the step nodes expectations
    when( root_stepsArray_Node0.get( "@class" ) ).thenReturn( root_stepsArray_Node0_classNode );
    when( root_stepsArray_Node0.get( "name" ) ).thenReturn( root_stepsArray_Node0_nameNode );
    when( root_stepsArray_Node0.get( "attributes" ) ).thenReturn( root_stepsArray_Node0_attributesNode );
    when( root_stepsArray_Node0.get( "fields" ) ).thenReturn( root_stepsArray_Node0_fieldsNode );
    when( root_stepsArray_Node0_classNode.asText() ).thenReturn( DummyTransMeta.class.getName() );
    when( root_stepsArray_Node0_nameNode.asText() ).thenReturn( "Step 0" );

    when( root_stepsArray_Node1.get( "@class" ) ).thenReturn( root_stepsArray_Node1_classNode );
    when( root_stepsArray_Node1.get( "name" ) ).thenReturn( root_stepsArray_Node1_nameNode );
    when( root_stepsArray_Node1.get( "attributes" ) ).thenReturn( root_stepsArray_Node1_attributesNode );
    when( root_stepsArray_Node1.get( "fields" ) ).thenReturn( root_stepsArray_Node1_fieldsNode );
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
    assertEquals( root_paramsArray.size(), tm.listParameters().length );
    assertEquals( root_stepsArray.size(), tm.getSteps().size() );
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
    when( mapper.readValue( "mockedAttributes", attrs0.getClass() ) ).thenReturn( attrMap );

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
    when( mapper.readValue( "mockedFields", fieldLists.getClass() ) ).thenReturn( fieldLists );

    deserializer.writeJsonFields( fields, mapper, stepId );

    verify( repo ).saveStepAttribute( null, stepId, 0, "name", "Test 1" );
    verify( repo ).saveStepAttribute( null, stepId, 1, "name", "Test 2" );

    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "int" ), anyInt() );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "long" ), eq( 3L ) );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "double" ), eq( 3.0D ) );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "bool" ), eq( true ) );
    verify( repo, times( 2 ) ).saveStepAttribute( any( ObjectId.class ), eq( stepId ), anyInt(), eq( "null" ), anyString() ) ;
  }
}
