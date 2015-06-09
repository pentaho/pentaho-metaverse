/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
