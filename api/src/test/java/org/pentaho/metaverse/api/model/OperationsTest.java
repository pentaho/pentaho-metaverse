/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
