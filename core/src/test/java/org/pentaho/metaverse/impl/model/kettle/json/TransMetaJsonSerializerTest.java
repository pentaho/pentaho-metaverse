/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metaverse.impl.model.kettle.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.metaverse.api.model.BaseResourceInfo;
import org.pentaho.metaverse.api.model.IInfo;
import org.pentaho.metaverse.api.model.IParamInfo;
import org.pentaho.metaverse.api.model.kettle.HopInfo;
import org.pentaho.metaverse.impl.model.kettle.LineageRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 12/1/14
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class TransMetaJsonSerializerTest {

  TransMetaJsonSerializer serializer;
  @Mock
  TransMeta transMeta;
  @Mock
  JsonGenerator json;
  @Mock
  SerializerProvider provider;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    serializer = new TransMetaJsonSerializer( TransMeta.class );
    serializer.setLineageRepository( new LineageRepository() );

    when( transMeta.getName() ).thenReturn( "TestTransName" );
    when( transMeta.getDescription() ).thenReturn( "Trans description" );
  }

  @Test
  public void testConstructors() {
    serializer = new TransMetaJsonSerializer( TransMeta.class );
    serializer = new TransMetaJsonSerializer( TransMeta.class, true );
    serializer = new TransMetaJsonSerializer( TypeFactory.defaultInstance().uncheckedSimpleType( TransMeta.class ) );

  }

  @Test
  public void testSerializeBasic() throws Exception {
    serializer.serialize( transMeta, json, provider );

    verify( json ).writeStartObject();
    verify( json ).writeStringField( IInfo.JSON_PROPERTY_CLASS, transMeta.getClass().getName() );
    verify( json ).writeStringField( IInfo.JSON_PROPERTY_NAME, transMeta.getName() );
    verify( json ).writeStringField( IInfo.JSON_PROPERTY_DESCRIPTION, transMeta.getDescription() );

    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_PARAMETERS );
    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_VARIABLES );
    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_STEPS );
    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_CONNECTIONS );
    verify( json ).writeEndObject();
  }

  @Test
  public void testSerializeParams() throws Exception {
    String[] params = new String[]{ "param1", "param2", "invalid" };

    when( transMeta.listParameters() ).thenReturn( params );
    when( transMeta.getParameterDescription( "param1" ) ).thenReturn( "paramDescription" );
    when( transMeta.getParameterDefault( "param1" ) ).thenReturn( "defaultValue" );
    when( transMeta.getParameterDescription( "param2" ) ).thenReturn( "paramDescription" );
    when( transMeta.getParameterDefault( "param2" ) ).thenReturn( "defaultValue" );
    // get some exception handling code coverage
    when( transMeta.getParameterDescription( "invalid" ) ).thenThrow( new UnknownParamException() );

    serializer.serializeParameters( transMeta, json );

    verify( json ).writeArrayFieldStart( "parameters" );
    verify( json, times( params.length - 1 ) ).writeObject( any( IParamInfo.class ) );
  }

  @Test
  public void testSerializeVariables() throws Exception {
    List<String> vars = new ArrayList<String>() {{
      add( "var1" );
      add( "var2" );
      add( "no value" );
    }};

    when( transMeta.getUsedVariables() ).thenReturn( vars );
    when( transMeta.getVariable( "var1" ) ).thenReturn( "value1" );
    when( transMeta.getVariable( "var2" ) ).thenReturn( "value2" );
    // get some exception handling code coverage
    when( transMeta.getVariable( "no value" ) ).thenReturn( null );

    serializer.serializeVariables( transMeta, json );

    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_VARIABLES );
    verify( json, times( vars.size() ) ).writeObject( any( IParamInfo.class ) );
  }

  @Test
  public void testSerializeSteps() throws Exception {
    final StepMeta spyStep1 = spy( new StepMeta() );
    final StepMeta spyStep2 = spy( new StepMeta() );
    final StepMeta spyStep3 = spy( new StepMeta() );

    DummyTransMeta spyDummy1 = spy( new DummyTransMeta() );
    DummyTransMeta spyDummy2 = spy( new DummyTransMeta() );
    DummyTransMeta spyDummy3 = spy( new DummyTransMeta() );

    List<StepMeta> steps = new ArrayList<StepMeta>() {{
      add( spyStep1 );
      add( spyStep2 );
      add( spyStep3 );
    }};

    when( transMeta.getSteps() ).thenReturn( steps );
    when( spyStep1.getStepMetaInterface() ).thenReturn( spyDummy1 );
    when( spyStep2.getStepMetaInterface() ).thenReturn( spyDummy2 );
    when( spyStep3.getStepMetaInterface() ).thenReturn( spyDummy3 );

    doThrow( new KettleException() ).when( spyDummy3 ).saveRep( any( Repository.class ), any( ), any(), any( ObjectId.class ) );

    serializer.serializeSteps( transMeta, json );

    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_STEPS );
    // make sure we call the saveRep method for each step to collect the common attribute stuff
    verify( spyDummy1 ).saveRep( any( Repository.class ), any( ), any(), any( ObjectId.class ) );
    verify( spyDummy2 ).saveRep( any( Repository.class ), any( ), any(), any( ObjectId.class ) );
    verify( spyDummy3 ).saveRep( any( Repository.class ), any( ), any(), any( ObjectId.class ) );

    // make sure we are writing out each step
    verify( json ).writeObject( spyDummy1 );
    verify( json ).writeObject( spyDummy2 );
    verify( json ).writeObject( spyDummy3 );
  }

  @Test
  public void testSerializeConnections() throws Exception {
    final DatabaseMeta db1 = spy( new DatabaseMeta() );
    final DatabaseMeta db2 = spy( new DatabaseMeta() );

    List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>() {{
      add( db1 );
      add( db2 );
    }};
    when( transMeta.getDatabases() ).thenReturn( dbs );

    when( db1.getAccessTypeDesc() ).thenReturn( "Native" );
    when( db1.getName() ).thenReturn( "DB1" );
    when( db1.getDatabasePortNumberString() ).thenReturn( "5432" );
    when( db1.getDescription() ).thenReturn( null );
    when( db1.getUsername() ).thenReturn( "user" );
    when( db1.getPassword() ).thenReturn( "password" );
    when( db1.getDatabaseName() ).thenReturn( "Test" );

    when( db2.getAccessTypeDesc() ).thenReturn( "JNDI" );
    when( db2.getName() ).thenReturn( "DB2" );
    when( db2.getDescription() ).thenReturn( null );

    serializer.serializeConnections( transMeta, json );
    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_CONNECTIONS );

    // make sure we write one of each types
    verify( json, times( dbs.size() ) ).writeObject( any( BaseResourceInfo.class ) );
  }

  @Test
  public void testGetBaseStepMetaFromStepMetaNull() throws Exception {
    // BaseStepMeta should not be null, but its parent should be
    BaseStepMeta baseStepMeta = serializer.getBaseStepMetaFromStepMeta( null );
    assertNotNull( baseStepMeta );
    assertNull( baseStepMeta.getParentStepMeta() );
  }

  @Test
  public void testSerializeHops() throws Exception {
    TransHopMeta hop = mock( TransHopMeta.class );

    when( transMeta.nrTransHops() ).thenReturn( 1 );
    when( transMeta.getTransHop( 0 ) ).thenReturn( hop );

    serializer.serializeHops( transMeta, json );

    verify( json ).writeArrayFieldStart( TransMetaJsonSerializer.JSON_PROPERTY_HOPS );
    verify( json ).writeObject( any( HopInfo.class ) );

  }
}
