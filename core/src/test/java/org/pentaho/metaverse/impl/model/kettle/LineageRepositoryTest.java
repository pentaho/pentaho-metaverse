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


package org.pentaho.metaverse.impl.model.kettle;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 12/3/14
 */
public class LineageRepositoryTest {

  LineageRepository repo;
  ObjectId stepId;
  ObjectId saveStepId;
  ObjectId jobId;
  ObjectId jobEntryId;

  @Before
  public void setUp() throws Exception {
    repo = new LineageRepository();
    stepId = new StringObjectId( "testStepId" );
    saveStepId = new StringObjectId( "saveStepId" );
    jobId = new StringObjectId( "testJobId" );
    jobEntryId = new StringObjectId( "saveJobEntryId" );

    Map<String, Object> attrs = new HashMap<String, Object>();

    List<Map<String, Object>> stepFields = new ArrayList<Map<String, Object>>();
    Map<String, Object> boolField = new HashMap<String, Object>();
    boolField.put( "name", "test" );
    boolField.put( "boolField", true );
    attrs.put( "boolField", true );
    stepFields.add( 0, boolField );

    Map<String, Object> intField = new HashMap<String, Object>();
    intField.put( "name", "test" );
    intField.put( "intField", 4 );
    attrs.put( "intField", 4 );
    stepFields.add( 1, intField );

    Map<String, Object> stringField = new HashMap<String, Object>();
    stringField.put( "name", "test" );
    stringField.put( "stringField", "hello world" );
    attrs.put( "stringField", "hello world" );
    stepFields.add( 2, stringField );

    repo.stepFieldCache.put( stepId, stepFields );
    repo.stepAttributeCache.put( stepId, attrs );
  }

  @Test
  public void testGetStepAttributeBoolean_index_default() throws Exception {
    String code = "boolField";
    boolean result = repo.getStepAttributeBoolean( stepId, 0, code, false );
    assertTrue( result );
  }
  @Test
  public void testGetStepAttributeBoolean_index_returnDefault() throws Exception {
    Map<String, Object> boolField = new HashMap<String, Object>();
    boolField.put( "boolField", null );

    Map<String, Object> noValue = new HashMap<String, Object>();
    noValue.put( "name", "test" );

    repo.stepFieldCache.get( stepId ).add( 1, boolField );
    repo.stepFieldCache.get( stepId ).add( 2, noValue );

    String code = "boolField";
    boolean result = repo.getStepAttributeBoolean( stepId, 1, code, false );
    assertFalse( result );

    result = repo.getStepAttributeBoolean( stepId, 2, code, false );
    assertFalse( result );
  }
  @Test
  public void testGetStepAttributeBoolean_index_noDefault() throws Exception {
    String code = "boolField";
    boolean result = repo.getStepAttributeBoolean( stepId, 0, code );
    assertTrue( result );
  }
  @Test
  public void testGetStepAttributeBoolean_index_noDefault_codeNotFound() throws Exception {
    String code = "not there";
    boolean result = repo.getStepAttributeBoolean( stepId, 0, code );
    assertFalse( result );
  }
  @Test
  public void testGetStepAttributeBoolean() throws Exception {
    String code = "boolField";
    boolean result = repo.getStepAttributeBoolean( stepId, code );
    assertTrue( result );
  }
  @Test
  public void testGetStepAttributeBoolean_codeNotFound() throws Exception {
    String code = "not there";
    boolean result = repo.getStepAttributeBoolean( stepId, code );
    assertFalse( result );
  }

  @Test
  public void testGetStepAttributeInteger() throws Exception {
    String code = "intField";
    long result = repo.getStepAttributeInteger( stepId, code );
    assertEquals( 4, result );
  }
  @Test
  public void testGetStepAttributeInteger_codeNotFound() throws Exception {
    String code = "not found";
    long result = repo.getStepAttributeInteger( stepId, code );
    assertEquals( 0L, result );
  }
  @Test
  public void testGetStepAttributeInteger_index() throws Exception {
    String code = "intField";
    long result = repo.getStepAttributeInteger( stepId, 1, code );
    assertEquals( 4, result );
  }

  @Test
  public void testGetStepAttributeString() throws Exception {
    String code = "stringField";
    String result = repo.getStepAttributeString( stepId, code );
    assertEquals( "hello world", result );
  }
  @Test
  public void testGetStepAttributeString_codeNotFound() throws Exception {
    String code = "not found";
    String result = repo.getStepAttributeString( stepId, code );
    assertEquals( null, result );
  }
  @Test
  public void testGetStepAttributeString_index() throws Exception {
    String code = "stringField";
    String result = repo.getStepAttributeString( stepId, 2, code );
    assertEquals( "hello world", result );
  }

  @Test
  public void testSaveAttribute_boolean_idx() throws Exception {
    repo.saveStepAttribute( null, saveStepId, 0, "boolField", true );
    boolean result = repo.getStepAttributeBoolean( saveStepId, 0, "boolField" );
    assertTrue( result );
  }
  @Test
  public void testSaveAttribute_boolean() throws Exception {
    repo.saveStepAttribute( null, saveStepId, "boolField", true );
    boolean result = repo.getStepAttributeBoolean( saveStepId, "boolField" );
    assertTrue( result );
  }

  @Test
  public void testSaveAttribute_string_idx() throws Exception {
    repo.saveStepAttribute( null, saveStepId, 1, "stringField", "value" );
    String result = repo.getStepAttributeString( saveStepId, 1, "stringField" );
    assertEquals( "value", result );
  }
  @Test
  public void testSaveAttribute_string() throws Exception {
    repo.saveStepAttribute( null, saveStepId, "stringField", "value" );
    String result = repo.getStepAttributeString( saveStepId, "stringField" );
    assertEquals( "value", result );
  }

  @Test
  public void testSaveAttribute_long_idx() throws Exception {
    repo.saveStepAttribute( null, saveStepId, 1, "field", 3L );
    long result = repo.getStepAttributeInteger( saveStepId, 1, "field" );
    assertEquals( 3L, result );
  }
  @Test
  public void testSaveAttribute_long() throws Exception {
    repo.saveStepAttribute( null, saveStepId, "field", 3L );
    long result = repo.getStepAttributeInteger( saveStepId, "field" );
    assertEquals( 3L, result );
  }

  @Test
  public void testSaveAttribute_int_idx() throws Exception {
    repo.saveStepAttribute( null, saveStepId, 1, "field", 3 );
    long result = repo.getStepAttributeInteger( saveStepId, 1, "field" );
    assertEquals( 3L, result );
  }
  @Test
  public void testSaveAttribute_int() throws Exception {
    repo.saveStepAttribute( null, saveStepId, "field", 3 );
    long result = repo.getStepAttributeInteger( saveStepId, "field" );
    assertEquals( 3L, result );
  }

  @Test
  public void testSaveAttribute_double_idx() throws Exception {
    repo.saveStepAttribute( null, saveStepId, 1, "field", 3.8D );
    // no getStepAttributeDouble method on the interface, get the string instead
    String result = repo.getStepAttributeString( saveStepId, 1, "field" );
    assertEquals( "3.8", result );
  }
  @Test
  public void testSaveAttribute_double() throws Exception {
    repo.saveStepAttribute( null, saveStepId, "field", 3.8D );
    // no getStepAttributeDouble method on the interface, get the string instead
    String result = repo.getStepAttributeString( saveStepId, "field" );
    assertEquals( "3.8", result );
  }

  @Test
  public void testCountNrStepAttributes() throws Exception {
    int count = repo.countNrStepAttributes( stepId, "intField" );
    assertEquals( 1, count );
  }

  @Test
  public void testSaveDatabaseMetaStepAttribute() throws Exception {
    List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>();
    DatabaseMeta dbmeta = mock( DatabaseMeta.class );
    DatabaseMeta dbmetaA = mock( DatabaseMeta.class );
    DatabaseMeta dbmetaB = mock( DatabaseMeta.class );
    dbs.add( dbmetaA );
    dbs.add( dbmeta );
    dbs.add( dbmetaB );

    when( dbmeta.getName() ).thenReturn( "MyConnection" );
    when( dbmetaA.getName() ).thenReturn( "NotMyConnection" );
    when( dbmetaB.getName() ).thenReturn( "NotMyConnectionEither" );

    repo.saveDatabaseMetaStepAttribute( null, saveStepId, "id_connection", dbmeta );
    DatabaseMeta result = repo.loadDatabaseMetaFromStepAttribute( saveStepId, "id_connection", dbs );

    assertEquals( "MyConnection", result.getName() );
  }

  @Test
  public void testSaveJobEntryAttribute_string() throws Exception {
    repo.saveJobEntryAttribute( jobId, jobEntryId, "name", "test" );
    String result = repo.getJobEntryAttributeString( jobEntryId, "name" );
    assertEquals( "test", result );
  }
  @Test
  public void testSaveJobEntryAttribute_string_idx() throws Exception {
    repo.saveJobEntryAttribute( jobId, jobEntryId, 2, "name", "test" );
    String result = repo.getJobEntryAttributeString( jobEntryId, 2, "name" );
    assertEquals( "test", result );
  }

  @Test
  public void testSaveJobEntryAttribute_boolean() throws Exception {
    repo.saveJobEntryAttribute( jobId, jobEntryId, "hasName", true );
    Boolean result = repo.getJobEntryAttributeBoolean( jobEntryId, "hasName" );
    assertEquals( true, result );
  }
  @Test
  public void testSaveJobEntryAttribute_boolean_idx() throws Exception {
    repo.saveJobEntryAttribute( jobId, jobEntryId, 4, "hasName", true );
    Boolean result = repo.getJobEntryAttributeBoolean( jobEntryId, 4, "hasName" );
    assertEquals( true, result );
  }

  @Test
  public void testSaveJobEntryAttribute_long() throws Exception {
    repo.saveJobEntryAttribute( jobId, jobEntryId, "count", 4L );
    long result = repo.getJobEntryAttributeInteger( jobEntryId, "count" );
    assertEquals( 4L, result );
  }
  @Test
  public void testSaveJobEntryAttribute_long_idx() throws Exception {
    repo.saveJobEntryAttribute( jobId, jobEntryId, 0, "count", 4L );
    long result = repo.getJobEntryAttributeInteger( jobEntryId, 0, "count" );
    assertEquals( 4L, result );
  }
}
