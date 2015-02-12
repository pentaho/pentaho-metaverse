package com.pentaho.metaverse.client;

import com.pentaho.metaverse.api.model.Operations;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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

}
