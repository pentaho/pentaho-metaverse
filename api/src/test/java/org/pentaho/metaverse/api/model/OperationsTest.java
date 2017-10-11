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

package org.pentaho.metaverse.api.model;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metaverse.api.ChangeType;

import java.util.List;

import static org.junit.Assert.*;

public class OperationsTest {
  Operations operations;

  @Before
  public void setUp() throws Exception {
    operations = new Operations();
  }

  @Test
  public void testAddOperation() throws Exception {
    assertTrue( operations.isEmpty() );
    Operation operation = new Operation( "opName", "opDesc" );
    operations.addOperation( ChangeType.METADATA, operation );
    assertEquals( 1, operations.size() );

  }

  @Test
  public void testGetOperationByType() throws Exception {
    Operation operation = new Operation( "opName", "opDesc" );
    operations.addOperation( ChangeType.METADATA, operation );
    Operation operation2 = new Operation( "opName2", "opDesc2" );
    operations.addOperation( ChangeType.METADATA, operation2 );
    Operation operation3 = new Operation( "opName3", "opDesc3" );
    operations.addOperation( ChangeType.DATA, operation3 );

    List<IOperation> metadataOperations = operations.get( ChangeType.METADATA );
    List<IOperation> dataOperations = operations.get( ChangeType.DATA );
    assertNotNull( metadataOperations );
    assertNotNull( dataOperations );
    assertEquals( 2, metadataOperations.size() );
    assertEquals( 1, dataOperations.size() );
  }
}
