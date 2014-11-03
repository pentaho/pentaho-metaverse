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

package com.pentaho.metaverse.impl.model.kettle;

import com.pentaho.metaverse.analyzer.kettle.ComponentDerivationRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * User: RFellows Date: 12/3/14
 */
@RunWith( MockitoJUnitRunner.class )
public class FieldTransformInfoTest {

  FieldTransformInfo fti;

  @Before
  public void setUp() throws Exception {
    fti = new FieldTransformInfo();
  }

  @Test
  public void testConstructor_DerivationRecord() throws Exception {
    ComponentDerivationRecord cdr = new ComponentDerivationRecord( "target" );
    cdr.addOperand( "modifies", "rename" );
    cdr.addOperand( "modifies", "length" );
    fti = new FieldTransformInfo( "source", cdr );

    assertEquals( "source", fti.getSourceField() );
    assertEquals( "target", fti.getTargetField() );
    assertEquals( cdr.getOperations().size(), fti.getOperations().size() );
  }

  @Test
  public void testGettersSetters() throws Exception {
    Map<String, List<String>> ops = new HashMap<String, List<String>>();
    fti.setSourceField( "source" );
    fti.setTargetField( "target" );
    fti.setOperations( ops );
    assertEquals( "source", fti.getSourceField() );
    assertEquals( "target", fti.getTargetField() );
    assertEquals( ops, fti.getOperations() );
  }
}
