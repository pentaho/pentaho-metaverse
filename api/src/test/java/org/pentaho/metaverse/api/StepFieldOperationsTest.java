package org.pentaho.metaverse.api;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StepFieldOperationsTest {

  private StepFieldOperations stepFieldOperations;

  @Before
  public void setUp() throws Exception {
    stepFieldOperations = new StepFieldOperations();
  }

  @Test
  public void testGetSetOperations() throws Exception {
    stepFieldOperations = new StepFieldOperations( "testStep", "testField", null );
    assertNull( stepFieldOperations.getOperations() );
    Operations operations = new Operations();
    stepFieldOperations.setOperations( operations );
    assertEquals( operations, stepFieldOperations.getOperations() );
  }

  @Test
  public void testToString() {
    stepFieldOperations = new StepFieldOperations( "testStep", "testField", null );
    assertEquals( "{ step:testStep, field:testField, operations: { none } }", stepFieldOperations.toString() );
    Operations operations = new Operations();
    operations.addOperation( ChangeType.METADATA, Operation.getRenameOperation() );
    operations.addOperation( ChangeType.DATA,
      new Operation( Operation.CALC_CATEGORY, ChangeType.DATA, "dataOp", "calcStuff" ) );
    stepFieldOperations.setOperations( operations );
    assertEquals( "{ step:testStep, field:testField, operations: "
        + "{metadataOperations=[modified: name], dataOperations=[dataOp: calcStuff]} }",
      stepFieldOperations.toString() );
  }

}
