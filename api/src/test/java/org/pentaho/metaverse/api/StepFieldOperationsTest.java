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
