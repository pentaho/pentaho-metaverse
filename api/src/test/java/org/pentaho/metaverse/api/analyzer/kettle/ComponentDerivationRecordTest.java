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

package org.pentaho.metaverse.api.analyzer.kettle;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;

import java.util.List;

import static org.junit.Assert.*;

public class ComponentDerivationRecordTest {

  ComponentDerivationRecord record;

  @Before
  public void setUp() throws Exception {
    record = new ComponentDerivationRecord( "myRecord" );
  }

  @Test
  public void testNoArgConstructor() {
    record = new ComponentDerivationRecord();
  }

  @Test
  public void testNonDefaultConstructor() {
    record = new ComponentDerivationRecord( "originalRecord", "myRecord" );
    assertEquals( ChangeType.METADATA, record.getChangeType() );

    record.setChangeType( ChangeType.DATA );
    assertEquals( ChangeType.DATA, record.getChangeType() );

    record = new ComponentDerivationRecord( "originalRecord", "myRecord", ChangeType.DATA );
    assertEquals( ChangeType.DATA, record.getChangeType() );
  }

  @Test
  public void testGetEntityName() throws Exception {
    assertEquals( "myRecord", record.getChangedEntityName() );
    assertNull( new ComponentDerivationRecord().getChangedEntityName() );
  }

  @Test
  public void testSetEntityName() throws Exception {
    assertEquals( "myRecord", record.getChangedEntityName() );
    record.setChangedEntityName( "newName" );
    assertEquals( "newName", record.getChangedEntityName() );
  }

  @Test
  public void testGetSetOriginalEntityName() throws Exception {
    assertEquals( "myRecord", record.getChangedEntityName() );
    record.setOriginalEntityName( "originalName" );
    assertEquals( "originalName", record.getOriginalEntityName() );
  }

  @Test
  public void testGetOperations() throws Exception {
    Operations operations = record.getOperations();
    assertNotNull( operations );
    assertTrue( operations.isEmpty() );
    String operands = "testOperand1, testOperand2";
    record.addOperation( new Operation( "testOperation", operands ) );
    assertNull( record.getOperations( ChangeType.DATA ) );
    assertNotNull( record.getOperations( ChangeType.METADATA ) );
  }

  @Test
  public void testAddOperation() throws Exception {
    Operations operations = record.getOperations();
    assertNotNull( operations );
    assertTrue( operations.isEmpty() );
    String operands = "testOperand1, testOperand2";
    record.addOperation( new Operation( "testOperation", operands ) );
    operations = record.getOperations();
    assertNotNull( operations );
    List<IOperation> checkOperations = operations.get( ChangeType.METADATA );
    assertNotNull( checkOperations );
    assertEquals( 1, checkOperations.size() );
    IOperation checkOperation = checkOperations.get( 0 );
    assertTrue( "testOperand1 not in operands!", checkOperation.getDescription().contains( "testOperand1" ) );
    assertTrue( "testOperand2 not in operands!", checkOperation.getDescription().contains( "testOperand2" ) );
  }

  @Test
  public void testAddOperationNull() throws Exception {
    record.operations = null;

    String operands = "testOperand1, testOperand2";
    record.addOperation( new Operation( "testOperation", operands ) );
    Operations operations = record.getOperations();
    assertNotNull( operations );
    List<IOperation> checkOperations = operations.get( ChangeType.METADATA );
    assertNotNull( checkOperations );
    assertEquals( 1, checkOperations.size() );
    IOperation checkOperation = checkOperations.get( 0 );
    assertTrue( "testOperand1 not in operands!", checkOperation.getDescription().contains( "testOperand1" ) );
    assertTrue( "testOperand2 not in operands!", checkOperation.getDescription().contains( "testOperand2" ) );
  }

  @Test
  public void testPutOperationNullOperation() throws Exception {
    Operations operations = record.getOperations();
    assertNotNull( operations );
    assertTrue( operations.isEmpty() );
    record.addOperation( null );
    String operands = "testOperand1, testOperand2";
    record.addOperation( new Operation( null, operands ) );
    operations = record.getOperations();
    assertNotNull( operations );
    List<IOperation> checkOperands = operations.get( "testOperation" );
    assertNull( checkOperands );
  }

  @Test
  public void testPutOperationNullOperands() throws Exception {
    Operations operations = record.getOperations();
    assertNotNull( operations );
    assertTrue( operations.isEmpty() );
    record.addOperation( new Operation( "testOperation", null ) );
    operations = record.getOperations();
    assertNotNull( operations );
    List<IOperation> checkOperands = operations.get( "testOperation" );
    assertNull( checkOperands );
  }

  @Test
  public void testAddOperand() throws Exception {
    record.operations = null;
    record.addOperation( new Operation( "testOperation", "testOperand" ) );
    Operations operations = record.getOperations();
    assertNotNull( operations );
    List<IOperation> checkOperations = operations.get( ChangeType.METADATA );
    assertNotNull( checkOperations );
    assertEquals( 1, checkOperations.size() );
    IOperation checkOperation = checkOperations.get( 0 );
    assertTrue( "testOperand not in operands!", checkOperation.getDescription().contains( "testOperand" ) );
  }

  @Test
  public void testAddOperandNullOperand() throws Exception {
    Operations operations = record.getOperations();
    assertNotNull( operations );
    assertTrue( operations.isEmpty() );
    record.addOperation( new Operation( "testOperation", null ) );
    List<IOperation> checkOperands = record.getOperations().get( "testOperation" );
    assertNull( checkOperands );
  }

  @Test
  public void testHasDelta() throws Exception {
    Operations operations = record.getOperations();
    assertNotNull( operations );
    assertTrue( operations.isEmpty() );
    assertFalse( "This record should not say it has been changed!", record.hasDelta() );
    record.addOperation( new Operation( "testOperation", "testOperand" ) );
    assertTrue( "This record should say it has been changed!", record.hasDelta() );
  }

  @Test
  public void testToString() throws Exception {
    Operations operations = record.getOperations();
    assertNotNull( operations );
    assertTrue( operations.isEmpty() );
    assertEquals( record.toString(), "{}" );
    record.addOperation( new Operation( "testOperation", "testOperand" ) );
    assertEquals(
      record.toString(),
      "{\"metadataOperations\":[{\"category\":\"changeMetadata\",\"class\":"
        + "\"org.pentaho.metaverse.api.model.Operation\",\"description\":"
        + "\"testOperand\",\"name\":\"testOperation\",\"type\":\"METADATA\"}]}" );
  }

  @Test
  public void testGetOperationsWithNullOperations() {
    record.operations = null;
    assertNotNull( record.getOperations() );
    assertTrue( record.getOperations().isEmpty() );
  }

  @Test
  public void testEquals() {
    assertTrue( record.equals( record ) );
    assertFalse( record.equals( new Object() ) );

    ComponentDerivationRecord record2 = new ComponentDerivationRecord( "myRecord" );
    assertTrue( record.equals( record2 ) );

    ComponentDerivationRecord record3 = new ComponentDerivationRecord( "originalRecord", "myRecord" );
    assertFalse( record.equals( record3 ) );
    record2.setChangeType( ChangeType.DATA );
    assertFalse( record.equals( record2 ) );

    record2 = new ComponentDerivationRecord( "myRecord" );
    record2.setChangedEntityName( "Some other name" );
    assertFalse( record.equals( record2 ) );

    record2 = new ComponentDerivationRecord( "myRecord" );
    record.addOperation( new Operation( "testOperation", "testOperand" ) );
    assertFalse( record.equals( record2 ) );
  }

}
